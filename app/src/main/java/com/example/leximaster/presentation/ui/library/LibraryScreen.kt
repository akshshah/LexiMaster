package com.example.leximaster.presentation.ui.library

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.leximaster.data.local.entity.WordEntity
import com.example.leximaster.ui.theme.competentColor
import com.example.leximaster.ui.theme.expertColor
import com.example.leximaster.ui.theme.masteredColor
import com.example.leximaster.ui.theme.noviceColor

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

            if(state.fullWordList.isNotEmpty()){
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = { onAction(LibraryAction.Search(it)) },
                    placeholder = { Text("Search your library...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
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
                            WordItem(wordEntity = word)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WordItem(wordEntity: WordEntity) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = wordEntity.word,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            // Mastery Score Badge
            MasteryBadge(score = wordEntity.masteryScore)
        }
    }
}

@Composable
fun MasteryBadge(score: Int) {
    val (color, label) = when (score) {
        in 0..33 -> Pair(noviceColor, "Novice")
        in 34..66 -> Pair(competentColor, "Competent")
        in 67..99 -> Pair(expertColor, "Expert")
        else -> Pair(masteredColor, "Mastered")
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$score - $label",
            style = MaterialTheme.typography.labelMedium,
            color = Color.Black,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun EmptyOrErrorState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}