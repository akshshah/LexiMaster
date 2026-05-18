package com.example.leximaster.presentation.ui.wordDetail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.leximaster.data.local.entity.ContextEntity
import com.example.leximaster.data.local.entity.SynonymEntity
import com.example.leximaster.data.local.entity.WordEntity
import com.example.leximaster.data.repository.MasteryStage
import com.example.leximaster.ui.theme.LexiMasterTheme
import kotlinx.coroutines.flow.Flow


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordDetailsScreen(
    state: WordDetailsState,
    onAction: (WordDetailsAction) -> Unit,
    onNavigateBack: () -> Unit,
    events: Flow<WordDetailsEvent>,
) {
    LaunchedEffect(Unit) {
        events.collect { event ->
            when (event) {
                WordDetailsEvent.NavigateBack -> onNavigateBack()
            }
        }
    }

    if (state.showDeleteDialog) {
        DeleteConfirmationDialog(
            wordText = state.word?.word.orEmpty(),
            onConfirm = { onAction(WordDetailsAction.OnConfirmDelete) },
            onDismiss = { onAction(WordDetailsAction.OnDismissDeleteDialog) },
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Word Details") },
                navigationIcon = {
                    IconButton(onClick = { onAction(WordDetailsAction.OnBackClick) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { onAction(WordDetailsAction.OnDeleteClick) }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Word",
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
                windowInsets = WindowInsets(0, 0, 0, 0),
            )
        },
    ) { innerPadding ->
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            state.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = state.error,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            state.word != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 32.dp),
                ) {
                    WordHeroBanner(
                        word = state.word.word,
                        phonetic = state.word.phonetic,
                        masteryScore = state.word.masteryScore,
                        masteryStage = state.masteryStage,
                        synonyms = state.synonyms,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    ContextCyclingTimeline(
                        contexts = state.contexts,
                        activeContextOrder = state.activeContextOrder,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    PerformanceAnalyticsCard(
                        correctAnswers = state.word.correctAnswers,
                        wrongAnswers = state.word.wrongAnswers,
                        successRate = state.successRate,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    MemoryJoggerSection(
                        notes = state.word.notes,
                        isEditing = state.isEditingNotes,
                        notesInput = state.notesInput,
                        onEditClick = { onAction(WordDetailsAction.OnEditNotesClick) },
                        onNotesChanged = { onAction(WordDetailsAction.OnNotesChanged(it)) },
                        onSave = { onAction(WordDetailsAction.OnSaveNotes) },
                        onCancel = { onAction(WordDetailsAction.OnCancelEditNotes) },
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
            }
        }
    }
}

// ─── Word Hero Banner ─────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WordHeroBanner(
    word: String,
    phonetic: String?,
    masteryScore: Int,
    masteryStage: MasteryStage,
    synonyms: List<SynonymEntity>,
    modifier: Modifier = Modifier,
) {
    val stageColor = masteryStage.color

    val stageProgress = when (masteryStage) {
        MasteryStage.NOVICE    -> masteryScore / 30f
        MasteryStage.COMPETENT -> (masteryScore - 31) / 39f
        MasteryStage.EXPERT    -> (masteryScore - 71) / 28f
        MasteryStage.MASTERED  -> 1f
    }.coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        stageColor.copy(alpha = 0.5f),
                        stageColor.copy(alpha = 0.01f),
                    )
                )
            ),
    ) {
        // Decorative accent circle — top-right
        Box(
            modifier = Modifier
                .size(160.dp)
                .align(Alignment.TopEnd)
                .offset(x = 50.dp, y = (-50).dp)
                .clip(CircleShape)
                .background(stageColor.copy(alpha = 0.2f))
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp),
        ) {

            // ── 1. Word ──────────────────────────────────────────────────────
            Text(
                text = word,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                lineHeight = 44.sp,
            )

            // ── 2. Phonetic ──────────────────────────────────────────────────
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

            Spacer(modifier = Modifier.height(28.dp))

            // ── 3. Mastery Block ─────────────────────────────────────────────
            MasteryScoreBlock(
                masteryScore = masteryScore,
                masteryStage = masteryStage,
                stageColor = stageColor,
                stageProgress = stageProgress
            )

            // ── 4. Synonyms ──────────────────────────────────────────────────
            if (synonyms.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    modifier = Modifier.padding(horizontal = 8.dp),
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "SYNONYMS",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.height(10.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.Center,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    synonyms.forEach { synonym ->
                        SynonymChip(text = synonym.synonymText, accentColor = stageColor)
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                }
            }
        }
    }
}

// ─── Mastery Score Block ──────────────────────────────────────────────────────

@Composable
private fun MasteryScoreBlock(
    masteryScore: Int,
    masteryStage: MasteryStage,
    stageColor: Color,
    stageProgress: Float,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        // Score + Stage label on same row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Left: stage name
            Text(
                text = masteryStage.displayName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = stageColor,
            )

            // Right: score pill
            Surface(
                shape = RoundedCornerShape(50),
                color = stageColor.copy(alpha = 0.12f),
                modifier = Modifier.border(
                    width = 1.dp,
                    color = stageColor.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(50),
                ),
            ) {
                Text(
                    text = "$masteryScore / 100",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = stageColor,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Segmented stage track
        MasterySegmentedBar(
            masteryStage = masteryStage,
            stageProgress = stageProgress,
            stageColor = stageColor,
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Stage range labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = masteryStage.minScore.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "Progress within ${masteryStage.displayName}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = masteryStage.maxScore.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// ─── Segmented Bar ────────────────────────────────────────────────────────────

@Composable
private fun MasterySegmentedBar(
    masteryStage: MasteryStage,
    stageProgress: Float,
    stageColor: Color,
) {
    val stages = MasteryStage.entries
    val currentStageIndex = stages.indexOf(masteryStage)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        stages.forEachIndexed { index, _ ->
            val targetProgress = when {
                index < currentStageIndex -> 1f
                index == currentStageIndex -> stageProgress
                else -> 0f
            }

            var animatedTarget by remember { mutableFloatStateOf(0f) }
            val segmentProgress by animateFloatAsState(
                targetValue = animatedTarget,
                animationSpec = tween(
                    durationMillis = 700,
                    easing = FastOutSlowInEasing,
                ),
                label = "segment_$index",
            )
            LaunchedEffect(targetProgress) {
                animatedTarget = targetProgress
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(50))
                    .background(stageColor.copy(alpha = 0.15f)),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(segmentProgress)
                        .clip(RoundedCornerShape(50))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    stageColor.copy(alpha = 0.7f),
                                    stageColor,
                                )
                            )
                        ),
                )
            }
        }
    }
}


// Updated SynonymChip — picks up the stage accent color
@Composable
private fun SynonymChip(
    text: String,
    accentColor: Color = MaterialTheme.colorScheme.primary,
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = accentColor.copy(alpha = 0.10f),
        modifier = Modifier.border(
            width = 0.5.dp,
            color = accentColor.copy(alpha = 0.3f),
            shape = RoundedCornerShape(50),
        ),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        )
    }
}

// ─── Context Cycling Timeline ─────────────────────────────────────────────────

@Composable
fun ContextCyclingTimeline(
    contexts: List<ContextEntity>,
    activeContextOrder: Int,
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
                text = "CONTEXT JOURNEY",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(16.dp))

            contexts.forEachIndexed { index, context ->
                val isActive = context.cycleOrder == activeContextOrder
                val isUnlocked = context.cycleOrder <= activeContextOrder
                val isLast = index == contexts.lastIndex

                ContextTimelineItem(
                    context = context,
                    isActive = isActive,
                    isUnlocked = isUnlocked,
                    isLast = isLast,
                    cycleLabel = when (context.cycleOrder) {
                        ContextEntity.CYCLE_INTRODUCTION -> "Introductory"
                        ContextEntity.CYCLE_NUANCED -> "Nuanced"
                        ContextEntity.CYCLE_TECHNICAL -> "Technical"
                        else -> "Context ${context.cycleOrder}"
                    },
                    scoreRangeLabel = when (context.cycleOrder) {
                        ContextEntity.CYCLE_INTRODUCTION -> "0 – 33 pts"
                        ContextEntity.CYCLE_NUANCED -> "34 – 66 pts"
                        ContextEntity.CYCLE_TECHNICAL -> "67 – 100 pts"
                        else -> ""
                    },
                )
            }
        }
    }
}

@Composable
private fun ContextTimelineItem(
    context: ContextEntity,
    isActive: Boolean,
    isUnlocked: Boolean,
    isLast: Boolean,
    cycleLabel: String,
    scoreRangeLabel: String,
) {
    val alpha = when {
        isActive -> 1f
        isUnlocked -> 0.75f
        else -> 0.35f
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(alpha)
            .height(IntrinsicSize.Min),
        verticalAlignment = Alignment.Top,
    ) {

        // ── Left rail ────────────────────────────────────────────────────────
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .width(32.dp)
                .fillMaxHeight(),
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        color = when {
                            isActive -> MaterialTheme.colorScheme.primary
                            isUnlocked -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        }
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "${context.cycleOrder}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isActive)
                        MaterialTheme.colorScheme.onPrimary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Connector line stretches to fill remaining column height
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .weight(1f)
                        .background(
                            color = if (isUnlocked)
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                            else
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        )
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // ── Right side: content ───────────────────────────────────────────────
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = cycleLabel,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isActive)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = scoreRangeLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (isActive) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    modifier = Modifier.padding(bottom = 6.dp),
                ) {
                    Text(
                        text = "● Active",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    )
                }
            }

            Text(
                text = context.meaning,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "\"${context.exampleUsage}\"",
                style = MaterialTheme.typography.bodySmall,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}


// ─── Performance Analytics Card ───────────────────────────────────────────────

@Composable
fun PerformanceAnalyticsCard(
    correctAnswers: Int,
    wrongAnswers: Int,
    successRate: Float,
    modifier: Modifier = Modifier,
) {
    val total = correctAnswers + wrongAnswers
    val animatedProgress by animateFloatAsState(
        targetValue = successRate,
        animationSpec = tween(durationMillis = 800),
        label = "successRateAnimation",
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "PERFORMANCE",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (total == 0) {
                Text(
                    text = "No quiz attempts yet. Start a quiz to track your progress!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                // Stats row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    StatItem(
                        label = "Correct",
                        value = correctAnswers.toString(),
                        color = Color(0xFF4CAF50),
                    )
                    VerticalDividerLine()
                    StatItem(
                        label = "Wrong",
                        value = wrongAnswers.toString(),
                        color = Color(0xFFE74141),
                    )
                    VerticalDividerLine()
                    StatItem(
                        label = "Total",
                        value = total.toString(),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Success rate label row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Success Rate",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "${(successRate * 100).toInt()}%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = successRateColor(successRate),
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Progress bar
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(50)),
                    color = successRateColor(successRate),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeCap = StrokeCap.Round,
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    color: Color,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = color,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun VerticalDividerLine() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(40.dp)
            .background(MaterialTheme.colorScheme.outlineVariant)
    )
}

// ─── Memory Jogger Section ────────────────────────────────────────────────────

@Composable
fun MemoryJoggerSection(
    notes: String?,
    isEditing: Boolean,
    notesInput: String,
    onEditClick: () -> Unit,
    onNotesChanged: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
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
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "MEMORY JOGGER",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Bold,
                )

                AnimatedVisibility(
                    visible = !isEditing,
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Notes",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            AnimatedVisibility(visible = !isEditing) {
                if (notes.isNullOrBlank()) {
                    Text(
                        text = "No notes yet. Tap the edit icon to add a personal memory hook for this word.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontStyle = FontStyle.Italic,
                    )
                } else {
                    Text(
                        text = notes,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 22.sp,
                    )
                }
            }

            AnimatedVisibility(visible = isEditing) {
                Column {
                    OutlinedTextField(
                        value = notesInput,
                        onValueChange = onNotesChanged,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                text = "e.g. Sounds like 'epic' — think of an epic story...",
                                style = MaterialTheme.typography.bodySmall,
                            )
                        },
                        minLines = 3,
                        maxLines = 6,
                        shape = RoundedCornerShape(12.dp),
                        label = { Text("Your memory hook") },
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TextButton(onClick = onCancel) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Cancel")
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = onSave,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                            ),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Save")
                        }
                    }
                }
            }
        }
    }
}

// ─── Delete Confirmation Dialog ───────────────────────────────────────────────

@Composable
fun DeleteConfirmationDialog(
    wordText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Delete \"$wordText\"?",
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Text(
                text = "This will permanently remove the word, all its contexts, synonyms, and quiz history. This action cannot be undone.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                ),
            ) {
                Text("Delete", color = MaterialTheme.colorScheme.onError)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

// ─── Helper Functions ─────────────────────────────────────────────────────────
@Composable
private fun successRateColor(rate: Float): Color {
    return when {
        rate >= 0.75f -> Color(0xFF4CAF50)
        rate >= 0.45f -> Color(0xFFFF9800)
        else -> MaterialTheme.colorScheme.error
    }
}

// ─── Previews ─────────────────────────────────────────────────────────────────

private fun previewState() = WordDetailsState(
    isLoading = false,
    word = WordEntity(
        id = 1L,
        word = "Ephemeral",
        phonetic = "ɪˈfem.ər.əl",
        notes = "Think of a mayfly — lives for just one day.",
        masteryScore = 55,
        correctAnswers = 8,
        wrongAnswers = 3,
        createdAt = System.currentTimeMillis(),
        lastTested = System.currentTimeMillis(),
    ),
    contexts = listOf(
        ContextEntity(
            id = 1L, wordId = 1L,
            meaning = "Lasting for a very short time.",
            exampleUsage = "The ephemeral beauty of cherry blossoms draws millions each spring.",
            cycleOrder = 1,
        ),
        ContextEntity(
            id = 2L, wordId = 1L,
            meaning = "Denoting or relating to plants that have a very short life cycle.",
            exampleUsage = "Desert ephemerals bloom only after rare rainfall events.",
            cycleOrder = 2,
        ),
        ContextEntity(
            id = 3L, wordId = 1L,
            meaning = "In computing, resources that exist only for the duration of a session.",
            exampleUsage = "Ephemeral containers are spun up and destroyed per request in serverless architectures.",
            cycleOrder = 3,
        ),
    ),
    synonyms = listOf(
        SynonymEntity(id = 1L, wordId = 1L, synonymText = "transient"),
        SynonymEntity(id = 2L, wordId = 1L, synonymText = "fleeting"),
        SynonymEntity(id = 3L, wordId = 1L, synonymText = "momentary"),
    ),
    activeContextOrder = 2,
    masteryStage = MasteryStage.COMPETENT,
    successRate = 0.73f,
)

@Preview(name = "Light Mode", showBackground = true, showSystemUi = true)
@Composable
private fun WordDetailsScreenLightPreview() {
    LexiMasterTheme(darkTheme = false) {
        WordDetailsScreen(
            state = previewState(),
            onAction = {},
            onNavigateBack = {},
            events = kotlinx.coroutines.flow.emptyFlow(),
        )
    }
}

@Preview(name = "Dark Mode", showBackground = true, showSystemUi = true)
@Composable
private fun WordDetailsScreenDarkPreview() {
    LexiMasterTheme(darkTheme = true) {
        WordDetailsScreen(
            state = previewState(),
            onAction = {},
            onNavigateBack = {},
            events = kotlinx.coroutines.flow.emptyFlow(),
        )
    }
}

@Preview(name = "Empty Notes - Light", showBackground = true)
@Composable
private fun WordDetailsEmptyNotesPreview() {
    LexiMasterTheme(darkTheme = false) {
        WordDetailsScreen(
            state = previewState().copy(
                word = previewState().word?.copy(notes = null),
            ),
            onAction = {},
            onNavigateBack = {},
            events = kotlinx.coroutines.flow.emptyFlow(),
        )
    }
}

@Preview(name = "Editing Notes", showBackground = true)
@Composable
private fun WordDetailsEditingNotesPreview() {
    LexiMasterTheme(darkTheme = false) {
        WordDetailsScreen(
            state = previewState().copy(
                isEditingNotes = true,
                notesInput = "Think of a mayfly — lives for just one day.",
            ),
            onAction = {},
            onNavigateBack = {},
            events = kotlinx.coroutines.flow.emptyFlow(),
        )
    }
}

