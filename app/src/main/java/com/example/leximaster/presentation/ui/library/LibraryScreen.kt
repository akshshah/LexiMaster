package com.example.leximaster.presentation.ui.library

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.leximaster.data.local.entity.WordEntity
import com.example.leximaster.data.repository.MasteryStage
import com.example.leximaster.util.Utils.calculateSuccessRate

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun LibraryScreen(
    state: LibraryState,
    onAction: (LibraryAction) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    var showSortSheet by remember { mutableStateOf(false) }

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
    ) { _ ->
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
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = state.searchQuery,
                            onValueChange = { onAction(LibraryAction.Search(it)) },
                            placeholder = { Text("Search library...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            trailingIcon = {
                                if (state.searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { onAction(LibraryAction.Search("")) }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear search")
                                    }
                                }
                            }
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        OutlinedButton(
                            onClick = { showSortSheet = true },
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp),
                            modifier = Modifier.height(56.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Sort")
                        }
                    }
                }
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
                                wordEntity = word
                            ) { onAction(LibraryAction.NavigateToWordDetail(word.id)) }
                        }
                    }
                }
            }
        }

        if (showSortSheet) {
            SortBottomSheet(
                currentSortType = state.sortType,
                onSortSelected = {
                    onAction(LibraryAction.Sort(it))
                    showSortSheet = false
                },
                onDismiss = { showSortSheet = false },
                sheetState = sheetState
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortBottomSheet(
    currentSortType: SortType,
    onSortSelected: (SortType) -> Unit,
    onDismiss: () -> Unit,
    sheetState: SheetState
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .width(32.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                )
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Sort Library",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            SortOptionItem(
                label = "Alphabetical (A-Z)",
                icon = Icons.Default.SortByAlpha,
                isSelected = currentSortType == SortType.ALPHABETICAL_ASC,
                onClick = { onSortSelected(SortType.ALPHABETICAL_ASC) }
            )
            SortOptionItem(
                label = "Alphabetical (Z-A)",
                icon = Icons.Default.SortByAlpha,
                isSelected = currentSortType == SortType.ALPHABETICAL_DESC,
                onClick = { onSortSelected(SortType.ALPHABETICAL_DESC) }
            )
            
            SortOptionItem(
                label = "Date Added (Newest)",
                icon = Icons.Default.History,
                isSelected = currentSortType == SortType.CREATED_AT_DESC,
                onClick = { onSortSelected(SortType.CREATED_AT_DESC) }
            )
            SortOptionItem(
                label = "Date Added (Oldest)",
                icon = Icons.Default.History,
                isSelected = currentSortType == SortType.CREATED_AT_ASC,
                onClick = { onSortSelected(SortType.CREATED_AT_ASC) }
            )

            SortOptionItem(
                label = "Mastery (Highest)",
                icon = Icons.AutoMirrored.Filled.TrendingUp,
                isSelected = currentSortType == SortType.MASTERY_DESC,
                onClick = { onSortSelected(SortType.MASTERY_DESC) }
            )
            SortOptionItem(
                label = "Mastery (Lowest)",
                icon = Icons.AutoMirrored.Filled.TrendingDown,
                isSelected = currentSortType == SortType.MASTERY_ASC,
                onClick = { onSortSelected(SortType.MASTERY_ASC) }
            )
        }
    }
}

@Composable
fun SortOptionItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    ListItem(
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        headlineContent = {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingContent = {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        colors = androidx.compose.material3.ListItemDefaults.colors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else Color.Transparent
        )
    )
}

@Composable
fun WordItem(
    wordEntity: WordEntity,
    onClick: () -> Unit,
) {
    val masteryStage = MasteryStage.fromScore(wordEntity.masteryScore)
    val successRate = wordEntity.calculateSuccessRate()
    val successRatePercent = (successRate * 100).toInt()

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
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Left accent bar
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(58.dp)
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
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = wordEntity.phonetic,
                        style = MaterialTheme.typography.bodySmall,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.End,
            ) {
                SuccessRateBadge(successRatePercent)
                MasteryBadge(score = wordEntity.masteryScore)
            }
        }
    }
}

@Composable
fun SuccessRateBadge(successRatePercent: Int) {
    val color = when {
        successRatePercent >= 80 -> Color(0xFF2E7D32) // Green
        successRatePercent >= 50 -> Color(0xFFF57C00) // Orange
        else -> Color(0xFFD32F2F) // Red
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(
                MaterialTheme.colorScheme.background.copy(alpha = 0.8f),
                RoundedCornerShape(24.dp)
            )
            .border(1.dp, color.copy(alpha = 0.8f), RoundedCornerShape(24.dp))
            .padding(start = 8.dp, end = 4.dp, top = 4.dp, bottom = 4.dp)
    ) {
        Column {
            Text(
                text = "SUCCESS RATE",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 8.sp,
                    letterSpacing = 0.5.sp,
                    platformStyle = PlatformTextStyle(includeFontPadding = false),
                ),
                color = color
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(28.dp)
        ) {
            Canvas(modifier = Modifier
                .fillMaxSize()
                .padding(2.dp)) {
                // Track
                drawArc(
                    color = color.copy(alpha = 0.15f),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )
                // Progress
                drawArc(
                    color = color,
                    startAngle = -90f,
                    sweepAngle = (successRatePercent / 100f) * 360f,
                    useCenter = false,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )
            }
            Text(
                text = "$successRatePercent",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 8.sp
                ),
                color = color
            )
        }
    }
}

@Composable
fun MasteryBadge(score: Int) {
    val masteryStage = MasteryStage.fromScore(score)
    Text(
        text = "${masteryStage.displayName} • $score",
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
    )
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

@Preview(showBackground = true)
@Composable
private fun LibraryScreenPreview() {
    MaterialTheme {
        LibraryScreen(
            state = LibraryState(
                isLoading = false,
                fullWordList = listOf(
                    WordEntity(id = 1, word = "Apple", phonetic = "/ˈap(ə)l/", masteryScore = 10, createdAt = 0L, notes = null),
                    WordEntity(id = 2, word = "Banana", phonetic = "/bəˈnɑːnə/", masteryScore = 50, createdAt = 0L, notes = null)
                ),
                filteredWords = listOf(
                    WordEntity(id = 1, word = "Apple", phonetic = "/ˈap(ə)l/", masteryScore = 10, createdAt = 0L, notes = null),
                    WordEntity(id = 2, word = "Banana", phonetic = "/bəˈnɑːnə/", masteryScore = 50, createdAt = 0L, notes = null)
                ),
                sortType = SortType.ALPHABETICAL_ASC
            ),
            onAction = {}
        )
    }
}