package com.example.leximaster.presentation.word

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel

fun getMasteryLabel(cycleOrder: Int): String {
    return when (cycleOrder) {
        1 -> "Introductory"
        2 -> "Nuanced"
        else -> "Technical/Expert"
    }
}

@Composable
fun WordDiscoveryScreen(
    viewModel: WordDiscoveryViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                is WordDiscoveryEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                else -> Unit
            }
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        floatingActionButton = {
            if (state.previewData != null && !state.isSaving) {
                FloatingActionButton(
                    onClick = { viewModel.onAction(WordDiscoveryAction.ConfirmAndSave) },
                ) {
                    Text("Add")
                }
            }
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
        ) {
            SearchSection(
                query = state.searchQuery,
                onQueryChange = { viewModel.onAction(WordDiscoveryAction.UpdateSearchQuery(it)) },
                onSearchClicked = { viewModel.onAction(WordDiscoveryAction.SubmitSearch) },
                isSearching = state.isSearching,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (state.isSearching && state.previewData == null) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Discovering word context...",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            if (state.previewData != null) {
                PreviewSection(
                    word = state.previewData?.word ?: state.searchQuery,
                    phonetic = state.previewData?.phonetic,
                    contexts = state.previewData?.contexts ?: emptyList(),
                    synonyms = state.editedSynonyms ?: emptyList(),
                    onRemoveSynonym = { viewModel.onAction(WordDiscoveryAction.RemoveSynonym(it)) },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@Composable
fun SearchSection(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearchClicked: () -> Unit,
    isSearching: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = "Discover a Word",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text("Enter a word to discover") },
            enabled = !isSearching,
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearchClicked() }),
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onSearchClicked,
            enabled = !isSearching,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Search")
        }
    }
}


@Composable
fun PreviewSection(
    word: String,
    phonetic: String?,
    contexts: List<com.example.leximaster.data.remote.dto.ContextWithOrder>,
    synonyms: List<String>,
    onRemoveSynonym: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (word.isEmpty()) {
        return
    }

    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Column {
                Text(
                    text = word,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                phonetic?.let {
                    Text(
                        text = "[$it]",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        if (synonyms.isNotEmpty()) {
            item {
                Column {
                    Text(
                        text = "Synonyms",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    SynonymRows(synonyms, onRemoveSynonym)
                }
            }
        }

        items(contexts, key = { it.cycleOrder }) { context ->
            ContextCard(
                cycleOrder = context.cycleOrder,
                meaning = context.meaning,
                exampleUsage = context.exampleUsage,
            )
        }
    }
}

@Composable
fun SynonymRows(
    synonyms: List<String>,
    onRemoveSynonym: (String) -> Unit,
) {
    val chipsPerRow = 3
    val chunked = synonyms.chunked(chipsPerRow)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        chunked.forEach { rowSynonyms ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                rowSynonyms.forEach { synonym ->
                    SynonymChip(synonym) { onRemoveSynonym(synonym) }
                }
            }
        }
    }
}

@Composable
fun SynonymChip(
    synonym: String,
    onRemove: () -> Unit,
) {
    InputChip(
        selected = false,
        label = { Text(synonym) },
        trailingIcon = {
            Icon(
                modifier = Modifier.clickable {
                    onRemove()
                },
                imageVector = Icons.Filled.Close,
                contentDescription = "Remove $synonym",
            )
        },
        onClick = {}
    )
}

@Composable
fun ContextCard(
    cycleOrder: Int,
    meaning: String,
    exampleUsage: String,
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = getMasteryLabel(cycleOrder),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = meaning,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = exampleUsage,
                style = MaterialTheme.typography.bodyMedium,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}