package com.example.leximaster.presentation.ui.wordDiscovery

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.androidx.compose.koinViewModel

fun getMasteryLabel(cycleOrder: Int): String {
    return when (cycleOrder) {
        1 -> "Introductory"
        2 -> "Nuanced"
        else -> "Technical/Expert"
    }
}

fun getMasteryColor(cycleOrder: Int): Color {
    return when (cycleOrder) {
        1 -> Color(0xFF4CAF50)
        2 -> Color(0xFF2196F3)
        else -> Color(0xFF9C27B0)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordDiscoveryScreen(
    viewModel: WordDiscoveryViewModel = koinViewModel(),
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                is WordDiscoveryEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                else -> Unit
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Discover Word",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                windowInsets = WindowInsets(0, 0, 0, 0),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            AnimatedVisibility(
                visible = state.previewData != null && !state.isSaving,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                exit = fadeOut(),
            ) {
                ExtendedFloatingActionButton(
                    text = { Text("Add to Library", fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.Default.Check, null) },
                    onClick = { viewModel.onAction(WordDiscoveryAction.ConfirmAndSave) },
                    containerColor = MaterialTheme.colorScheme.primary,
                )
            }
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            // ── Search Section ────────────────────────────────────────────────
            item {
                SearchSection(
                    query = state.searchQuery,
                    onQueryChange = { viewModel.onAction(WordDiscoveryAction.UpdateSearchQuery(it)) },
                    onSearchClicked = { viewModel.onAction(WordDiscoveryAction.SubmitSearch) },
                    isSearching = state.isSearching,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 16.dp),
                )
            }

            // ── Loading State ─────────────────────────────────────────────────
            if (state.isSearching && state.previewData == null) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 3.dp,
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Discovering word context...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            // ── Preview Hero Banner ───────────────────────────────────────────
            if (state.previewData != null) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    DiscoveryHeroBanner(
                        word = state.previewData?.word ?: state.searchQuery,
                        phonetic = state.previewData?.phonetic,
                    )
                }

                // ── Synonyms ──────────────────────────────────────────────────
                val synonyms = state.editedSynonyms ?: emptyList()
                if (synonyms.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        SynonymSection(
                            synonyms = synonyms,
                            onRemoveSynonym = { viewModel.onAction(WordDiscoveryAction.RemoveSynonym(it)) },
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )
                    }
                }

                // ── Context Cards ─────────────────────────────────────────────
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "CONTEXT JOURNEY",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                items(
                    state.previewData?.contexts ?: emptyList(),
                    key = { it.cycleOrder },
                ) { context ->
                    ContextCard(
                        cycleOrder = context.cycleOrder,
                        meaning = context.meaning,
                        exampleUsage = context.exampleUsage,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                item { Spacer(modifier = Modifier.height(80.dp)) } // FAB clearance
            }
        }
    }
}

// ─── Search Section ───────────────────────────────────────────────────────────

@Composable
fun SearchSection(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearchClicked: () -> Unit,
    isSearching: Boolean,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "WORD SEARCH",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = {
                    Text(
                        text = "Enter a word to discover...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                },
                enabled = !isSearching,
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearchClicked() }),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onSearchClicked,
                enabled = !isSearching && query.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                ),
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Discover",
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

// ─── Discovery Hero Banner ────────────────────────────────────────────────────

@Composable
fun DiscoveryHeroBanner(
    word: String,
    phonetic: String?,
    modifier: Modifier = Modifier,
) {
    val accentColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clipToBounds()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        accentColor.copy(alpha = 0.6f),
                        accentColor.copy(alpha = 0.01f),
                    )
                )
            ),
    ) {
        // Decorative accent circle — top-right (mirrors WordDetailsScreen)
        Box(
            modifier = Modifier
                .size(140.dp)
                .align(Alignment.TopEnd)
                .offset(x = 40.dp, y = (-40).dp)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = 0.15f))
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 28.dp),
        ) {
            Text(
                text = word,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                lineHeight = 44.sp,
            )

            if (!phonetic.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .width(24.dp)
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = phonetic,
                        style = MaterialTheme.typography.bodyLarge,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .width(24.dp)
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                shape = RoundedCornerShape(50),
                color = accentColor.copy(alpha = 0.12f),
                modifier = Modifier.border(
                    width = 1.dp,
                    color = accentColor.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(50),
                ),
            ) {
                Text(
                    text = "✦ New Discovery",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = accentColor,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                )
            }
        }
    }
}

// ─── Synonym Section ──────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SynonymSection(
    synonyms: List<String>,
    onRemoveSynonym: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "SYNONYMS",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
            )

            Spacer(modifier = Modifier.height(12.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                synonyms.forEach { synonym ->
                    RemovableSynonymChip(
                        text = synonym,
                        onRemove = { onRemoveSynonym(synonym) },
                    )
                }
            }
        }
    }
}

@Composable
private fun RemovableSynonymChip(
    text: String,
    onRemove: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
        modifier = Modifier.border(
            width = 0.5.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
            shape = RoundedCornerShape(50),
        ),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 12.dp, end = 8.dp, top = 6.dp, bottom = 6.dp),
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium,
            )
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove $text",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .size(14.dp)
                    .clickable { onRemove() },
            )
        }
    }
}

// ─── Context Card ─────────────────────────────────────────────────────────────

@Composable
fun ContextCard(
    cycleOrder: Int,
    meaning: String,
    exampleUsage: String,
    modifier: Modifier = Modifier,
) {
    val accentColor = getMasteryColor(cycleOrder)

    // Represents how far along the mastery journey this context is (1→33%, 2→66%, 3→100%)
    val targetProgress = cycleOrder / 3f

    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(
            durationMillis = 700,
            easing = FastOutSlowInEasing,
        ),
        label = "contextProgress_$cycleOrder",
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // ── Header row ────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = accentColor.copy(alpha = 0.12f),
                    modifier = Modifier.border(
                        width = 1.dp,
                        color = accentColor.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(50),
                    ),
                ) {
                    Text(
                        text = getMasteryLabel(cycleOrder),
                        style = MaterialTheme.typography.labelMedium,
                        color = accentColor,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                    )
                }

                Text(
                    text = "Stage $cycleOrder / 3",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── Animated progress bar ─────────────────────────────────────────
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(50)),
                color = accentColor,
                trackColor = accentColor.copy(alpha = 0.15f),
                strokeCap = StrokeCap.Round,
            )

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ── Meaning ───────────────────────────────────────────────────────
            Text(
                text = meaning,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ── Example usage ─────────────────────────────────────────────────
            Text(
                text = "\"$exampleUsage\"",
                style = MaterialTheme.typography.bodyMedium,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}