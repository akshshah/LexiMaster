package com.example.leximaster.presentation.ui.library

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.leximaster.data.local.entity.WordEntity
import com.example.leximaster.data.repository.MasteryStage

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun LibraryScreen(
    state: LibraryState,
    onAction: (LibraryAction) -> Unit,
) {
    // Scaffold here ONLY handles the Floating Action Button.
    // The BottomBar is managed securely by MainScreen.
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onAction(LibraryAction.NavigateToWordDiscovery) },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Word")
            }
        }
    ) {  paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(0.dp)
        ) {

            AnimatedVisibility(
                visible = state.fullWordList.isNotEmpty(),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = { onAction(LibraryAction.Search(it)) },
                    placeholder = { Text("Search your library...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                )
            }


            when {
                state.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                state.error != null -> {
                    EmptyOrErrorState("Error: ${state.error}")
                }
                state.fullWordList.isEmpty() -> {
                    EmptyOrErrorState("No words in your library yet.\nStart by discovering a new word!")
                }
                state.filteredWords.isEmpty() -> {
                    EmptyOrErrorState("No word found matching '${state.searchQuery}'.")
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 80.dp) // padding for FAB
                    ) {
                        items(state.filteredWords, key = { it.id }) { word ->
                            WordItem(
                                wordEntity = word,
                                onClick = { onAction(LibraryAction.NavigateToWordDetail(word.id)) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WordItem(
    wordEntity: WordEntity,
    onClick: () -> Unit,
) {
    val masteryStage = MasteryStage.fromScore(wordEntity.masteryScore)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                            masteryStage.color.copy(alpha = 0.3f)
                        )
                    )
                )
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Left accent bar
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(48.dp)
                    .clip(RoundedCornerShape(50))
                    .background(masteryStage.color)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = wordEntity.word,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                if (!wordEntity.phonetic.isNullOrBlank()) {
                    Text(
                        text = wordEntity.phonetic,
                        style = MaterialTheme.typography.bodySmall,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))
            MasteryBadge(score = wordEntity.masteryScore)
        }
    }
}

@Composable
fun MasteryBadge(score: Int) {
    val masteryStage = MasteryStage.fromScore(score)
    Surface(
        shape = RoundedCornerShape(50),
        color = Color.White,
        modifier = Modifier.border(
            width = 1.dp,
            color = masteryStage.color.copy(alpha = 0.4f),
            shape = RoundedCornerShape(50),
        ),
    ) {
        Text(
            text = "${masteryStage.displayName} • $score",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.ExtraBold,
            color = masteryStage.color,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
        )
    }
}

@Composable
fun EmptyOrErrorState(message: String, isError: Boolean = false) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = if (isError) Icons.Default.Warning else  Icons.AutoMirrored.Filled.MenuBook,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}
