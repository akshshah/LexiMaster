package com.example.leximaster.presentation.ui.quiz

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.leximaster.data.local.converter.QuestionType
import com.example.leximaster.data.local.entity.ContextEntity
import com.example.leximaster.data.local.entity.WordEntity
import com.example.leximaster.data.repository.MasteryStage
import com.example.leximaster.ui.theme.LexiMasterTheme

// ─────────────────────────────────────────────────────────────────────────────────
// Semantic Colors for Quiz Feedback
// ─────────────────────────────────────────────────────────────────────────────────

private val CorrectGreen = Color(0xFF10B981) // Emerald Green
private val IncorrectRed = Color(0xFFEF4444) // Crimson Red
private val CorrectGreenLight = Color(0xFFD1FAE5)
private val IncorrectRedLight = Color(0xFFFEE2E2)

// ─────────────────────────────────────────────────────────────────────────────────
// Main QuizScreen Composable
// ─────────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    state: QuizUiState,
    onAction: (QuizAction) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    var showQuitDialog by remember { mutableStateOf(false) }

    // Quit confirmation dialog
    if (showQuitDialog) {
        QuitQuizDialog(
            onConfirm = {
                showQuitDialog = false
                onAction(QuizAction.OnQuitQuiz)
            },
            onDismiss = { showQuitDialog = false },
        )
    }

    Scaffold(
        topBar = {
            QuizTopBar(
                currentQuestionIndex = state.currentQuestionIndex,
                totalQuestions = state.totalQuestions,
                currentScore = state.currentScore,
                onQuitClick = { showQuitDialog = true },
                isLoading = state.isLoading,
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.surface,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { paddingValues ->
        when {
            state.isLoading -> {
                LoadingOverlay()
            }

            state.errorMessage != null -> {
                ErrorState(
                    message = state.errorMessage ?: "An error occurred",
                    onRetry = { /* Retry logic if needed */ },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                )
            }

            state.currentWord != null && state.currentQuestionType != null -> {
                QuizContent(
                    state = state,
                    onAction = onAction,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────────
// Quiz Content
// ─────────────────────────────────────────────────────────────────────────────────

@Composable
private fun QuizContent(
    state: QuizUiState,
    onAction: (QuizAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Dynamic Prompt Canvas
        PromptCanvas(
            questionType = state.currentQuestionType!!,
            word = state.currentWord!!,
            activeContext = state.activeContext,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Option Selector Canvas
        OptionSelectorCanvas(
            options = state.options,
            selectedOption = state.selectedOption,
            isAnswerLocked = state.isAnswerLocked,
            feedback = state.feedback,
            onOptionSelected = { option ->
                if (!state.isAnswerLocked) {
                    onAction(QuizAction.OnSubmitAnswer(option))
                }
            },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Action Footer
        ActionFooter(
            isAnswerLocked = state.isAnswerLocked,
            feedback = state.feedback,
            isLastQuestion = state.isLastQuestion,
            onNextQuestion = { onAction(QuizAction.OnNextQuestion) },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

// ─────────────────────────────────────────────────────────────────────────────────
// Quiz Top Bar
// ─────────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuizTopBar(
    currentQuestionIndex: Int,
    totalQuestions: Int,
    currentScore: Int,
    onQuitClick: () -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    val animatedProgress by animateFloatAsState(
        targetValue = if (totalQuestions > 0) {
            (currentQuestionIndex + 1).toFloat() / totalQuestions
        } else {
            0f
        },
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "progress_animation",
    )

    Column(modifier = modifier.fillMaxWidth()) {
        TopAppBar(
            title = {
                // Progress indicator in center
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        strokeCap = StrokeCap.Round,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Question ${currentQuestionIndex + 1} of $totalQuestions",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
            navigationIcon = {
                IconButton(
                    onClick = onQuitClick,
                    enabled = !isLoading,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Exit Quiz",
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            },
            actions = {
                // Score chip
                ScoreChip(score = currentScore)
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            windowInsets = WindowInsets(0, 0, 0, 0),
        )
    }
}

@Composable
private fun ScoreChip(
    score: Int,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        ) {
            Text(
                text = "🏆",
                fontSize = 14.sp,
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = score.toString(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────────
// Prompt Canvas
// ─────────────────────────────────────────────────────────────────────────────────

@Composable
private fun PromptCanvas(
    questionType: QuestionType,
    word: WordEntity,
    activeContext: ContextEntity?,
    modifier: Modifier = Modifier,
) {
    when (questionType) {
        QuestionType.RECOGNITION -> {
            RecognitionPrompt(
                word = word,
                modifier = modifier,
            )
        }

        QuestionType.SYNONYM -> {
            SynonymPrompt(
                word = word,
                activeContext = activeContext,
                modifier = modifier,
            )
        }

        QuestionType.RECALL -> {
            RecallPrompt(
                word = word,
                activeContext = activeContext,
                modifier = modifier,
            )
        }
    }
}

@Composable
private fun RecognitionPrompt(
    word: WordEntity,
    modifier: Modifier = Modifier,
) {
    val accentColor = MaterialTheme.colorScheme.primary

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clipToBounds()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.08f),
                            accentColor.copy(alpha = 0.01f),
                        ),
                    ),
                ),
        ) {
            // Decorative accent circle
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 30.dp, y = (-30).dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.1f)),
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
            ) {
                // Question type label
                Text(
                    text = "WHAT DOES THIS WORD MEAN?",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold,
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Word display
                Text(
                    text = word.word,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                )

                // Phonetic
                if (!word.phonetic.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = word.phonetic,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    )
                }
            }
        }
    }
}

@Composable
private fun SynonymPrompt(
    word: WordEntity,
    activeContext: ContextEntity?,
    modifier: Modifier = Modifier,
) {
    if (activeContext == null) return

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
        ) {
            // Question type label
            Text(
                text = "FIND THE SYNONYM",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Example usage with highlighted word
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Example:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    HighlightedText(
                        text = activeContext.exampleUsage,
                        highlightWord = word.word,
                        highlightColor = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

@Composable
private fun RecallPrompt(
    word: WordEntity,
    activeContext: ContextEntity?,
    modifier: Modifier = Modifier,
) {
    if (activeContext == null) return

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
        ) {
            // Question type label
            Text(
                text = "FILL IN THE BLANK",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Cloze-deletion example
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Complete the sentence:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ClozeText(
                        text = activeContext.exampleUsage,
                        targetWord = word.word,
                    )
                }
            }
        }
    }
}

@Composable
private fun HighlightedText(
    text: String,
    highlightWord: String,
    highlightColor: Color,
    modifier: Modifier = Modifier,
) {
    val annotatedString = buildAnnotatedString {
        val lowerText = text.lowercase()
        val lowerWord = highlightWord.lowercase()
        var startIndex = 0

        while (true) {
            val foundIndex = lowerText.indexOf(lowerWord, startIndex)
            if (foundIndex == -1) {
                append(text.substring(startIndex))
                break
            }

            // Append text before the match
            append(text.substring(startIndex, foundIndex))

            // Append highlighted word
            withStyle(
                style = SpanStyle(
                    color = highlightColor,
                    fontWeight = FontWeight.Bold,
                    textDecoration = TextDecoration.Underline,
                ),
            ) {
                append(text.substring(foundIndex, foundIndex + highlightWord.length))
            }

            startIndex = foundIndex + highlightWord.length
        }
    }

    Text(
        text = annotatedString,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier,
    )
}

@Composable
private fun ClozeText(
    text: String,
    targetWord: String,
    modifier: Modifier = Modifier,
) {
    val blankPlaceholder = "_______"
    val lowerText = text.lowercase()
    val lowerWord = targetWord.lowercase()
    val foundIndex = lowerText.indexOf(lowerWord)

    val displayText = if (foundIndex != -1) {
        text.replaceRange(
            foundIndex,
            foundIndex + targetWord.length,
            blankPlaceholder,
        )
    } else {
        text
    }

    val annotatedString = buildAnnotatedString {
        val blankIndex = displayText.indexOf(blankPlaceholder)
        if (blankIndex != -1) {
            append(displayText.substring(0, blankIndex))
            withStyle(
                style = SpanStyle(
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                ),
            ) {
                append(blankPlaceholder)
            }
            append(displayText.substring(blankIndex + blankPlaceholder.length))
        } else {
            append(displayText)
        }
    }

    Text(
        text = annotatedString,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier,
    )
}

// ─────────────────────────────────────────────────────────────────────────────────
// Option Selector Canvas
// ─────────────────────────────────────────────────────────────────────────────────

@Composable
private fun OptionSelectorCanvas(
    options: List<String>,
    selectedOption: String?,
    isAnswerLocked: Boolean,
    feedback: QuizFeedback?,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val correctOption = feedback?.let { f ->
        options.find { it.equals(f.word.word, ignoreCase = true) || it.equals(f.context.meaning, ignoreCase = true) }
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        options.forEach { option ->
            OptionRow(
                text = option,
                isAnswerLocked = isAnswerLocked,
                isSelected = option == selectedOption,
                isCorrect = option == correctOption && feedback?.isCorrect == true,
                isWrong = option == selectedOption && feedback?.isCorrect == false,
                isCorrectAnswer = option == correctOption,
                onClick = { onOptionSelected(option) },
            )
        }
    }
}

@Composable
private fun OptionRow(
    text: String,
    isAnswerLocked: Boolean,
    isSelected: Boolean,
    isCorrect: Boolean,
    isWrong: Boolean,
    isCorrectAnswer: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val backgroundColor = when {
        isCorrect -> CorrectGreenLight
        isWrong -> IncorrectRedLight
        isCorrectAnswer -> CorrectGreenLight
        isSelected -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surface
    }

    val borderColor = when {
        isCorrect -> CorrectGreen
        isWrong -> IncorrectRed
        isCorrectAnswer -> CorrectGreen
        isSelected -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outlineVariant
    }

    val contentColor = when {
        isCorrect || isCorrectAnswer -> CorrectGreen
        isWrong -> IncorrectRed
        else -> MaterialTheme.colorScheme.onSurface
    }

    val alpha = if (isAnswerLocked && !isSelected && !isCorrect && !isWrong && !isCorrectAnswer) {
        0.4f
    } else {
        1f
    }

    val trailingIcon: ImageVector? = when {
        isCorrect || isCorrectAnswer -> Icons.Default.Check
        isWrong -> Icons.Default.Close
        else -> null
    }

    OutlinedButton(
        onClick = onClick,
        enabled = !isAnswerLocked,
        modifier = modifier
            .fillMaxWidth()
            .alpha(alpha),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = backgroundColor,
            contentColor = contentColor,
            disabledContainerColor = backgroundColor,
            disabledContentColor = contentColor,
        ),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected || isCorrectAnswer) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.weight(1f),
            )

            if (trailingIcon != null) {
                Icon(
                    imageVector = trailingIcon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────────
// Action Footer
// ─────────────────────────────────────────────────────────────────────────────────

@Composable
private fun ActionFooter(
    isAnswerLocked: Boolean,
    feedback: QuizFeedback?,
    isLastQuestion: Boolean,
    onNextQuestion: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = !isAnswerLocked,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        // Muted tip when no answer is locked
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Select an answer to continue",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            )
        }
    }

    AnimatedVisibility(
        visible = isAnswerLocked && feedback != null,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
        exit = fadeOut(),
    ) {
        feedback?.let { fb ->
            FeedbackPanel(
                feedback = fb,
                isLastQuestion = isLastQuestion,
                onNextQuestion = onNextQuestion,
                modifier = modifier,
            )
        }
    }
}

@Composable
private fun FeedbackPanel(
    feedback: QuizFeedback,
    isLastQuestion: Boolean,
    onNextQuestion: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isSuccess = feedback.isCorrect
    val accentColor = if (isSuccess) CorrectGreen else IncorrectRed
    val backgroundColor = if (isSuccess) CorrectGreenLight else IncorrectRedLight

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
        ) {
            // Feedback header
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = if (isSuccess) Icons.Default.Check else Icons.Default.Close,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(28.dp),
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = if (isSuccess) "Correct!" else "Incorrect",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Score delta
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = if (feedback.scoreDelta >= 0) "+${feedback.scoreDelta}" else "${feedback.scoreDelta}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = accentColor,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "to Mastery Score",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Black,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Mastery stage
            Text(
                text = "Mastery: ${feedback.masteryStage.displayName}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black,
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Next/Finish button
            Button(
                onClick = onNextQuestion,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentColor,
                    contentColor = Color.White,
                ),
            ) {
                Text(
                    text = if (isLastQuestion) "Finish Quiz" else "Next Question",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────────
// Loading & Error States
// ─────────────────────────────────────────────────────────────────────────────────

@Composable
private fun LoadingOverlay(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 4.dp,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Preparing your quiz...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp),
        ) {
            Text(
                text = "⚠️",
                fontSize = 48.sp,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────────
// Quit Dialog
// ─────────────────────────────────────────────────────────────────────────────────

@Composable
private fun QuitQuizDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Exit Quiz?",
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Text(
                text = "Your progress will be lost if you exit now. Are you sure you want to quit?",
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = "Exit",
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Continue Quiz",
                    fontWeight = FontWeight.SemiBold,
                )
            }
        },
    )
}

// ─────────────────────────────────────────────────────────────────────────────────
// Preview Parameter Providers
// ─────────────────────────────────────────────────────────────────────────────────

class QuizUiStateProvider : PreviewParameterProvider<QuizUiState> {
    override val values: Sequence<QuizUiState> = sequenceOf(
        // Loading state
        QuizUiState(
            isLoading = true,
        ),
        // Active question - Recognition
        QuizUiState(
            isLoading = false,
            currentQuestionIndex = 0,
            totalQuestions = 10,
            currentScore = 0,
            currentWord = WordEntity(
                id = 1,
                word = "Ephemeral",
                phonetic = "/ɪˈfem(ə)rəl/",
                notes = null,
                masteryScore = 25,
                createdAt = System.currentTimeMillis(),
            ),
            activeContext = ContextEntity(
                id = 1,
                wordId = 1,
                meaning = "Lasting for a very short time",
                exampleUsage = "The ephemeral beauty of cherry blossoms makes them even more precious.",
                cycleOrder = 1,
            ),
            currentQuestionType = QuestionType.RECOGNITION,
            options = listOf(
                "Lasting for a very short time",
                "Extremely rare and valuable",
                "Related to natural phenomena",
                "Having a pleasant fragrance",
            ),
            isAnswerLocked = false,
        ),
        // Locked - Correct answer
        QuizUiState(
            isLoading = false,
            currentQuestionIndex = 3,
            totalQuestions = 10,
            currentScore = 30,
            currentWord = WordEntity(
                id = 1,
                word = "Ephemeral",
                phonetic = "/ɪˈfem(ə)rəl/",
                notes = null,
                masteryScore = 35,
                createdAt = System.currentTimeMillis(),
            ),
            activeContext = ContextEntity(
                id = 1,
                wordId = 1,
                meaning = "Lasting for a very short time",
                exampleUsage = "The ephemeral beauty of cherry blossoms makes them even more precious.",
                cycleOrder = 1,
            ),
            currentQuestionType = QuestionType.RECOGNITION,
            options = listOf(
                "Lasting for a very short time",
                "Extremely rare and valuable",
                "Related to natural phenomena",
                "Having a pleasant fragrance",
            ),
            isAnswerLocked = true,
            feedback = QuizFeedback(
                isCorrect = true,
                previousScore = 25,
                newScore = 35,
                scoreDelta = 10,
                masteryStage = MasteryStage.COMPETENT,
                word = WordEntity(
                    id = 1,
                    word = "Ephemeral",
                    phonetic = "/ɪˈfem(ə)rəl/",
                    notes = null,
                    masteryScore = 35,
                    createdAt = System.currentTimeMillis(),
                ),
                context = ContextEntity(
                    id = 1,
                    wordId = 1,
                    meaning = "Lasting for a very short time",
                    exampleUsage = "The ephemeral beauty of cherry blossoms makes them even more precious.",
                    cycleOrder = 1,
                ),
            ),
        ),
        // Locked - Incorrect answer
        QuizUiState(
            isLoading = false,
            currentQuestionIndex = 5,
            totalQuestions = 10,
            currentScore = 40,
            currentWord = WordEntity(
                id = 2,
                word = "Ubiquitous",
                phonetic = "/juːˈbɪkwɪtəs/",
                notes = null,
                masteryScore = 15,
                createdAt = System.currentTimeMillis(),
            ),
            activeContext = ContextEntity(
                id = 2,
                wordId = 2,
                meaning = "Present, appearing, or found everywhere",
                exampleUsage = "Smartphones have become ubiquitous in modern society.",
                cycleOrder = 1,
            ),
            currentQuestionType = QuestionType.SYNONYM,
            options = listOf(
                "Ubiquitous",
                "Rare",
                "Scarce",
                "Limited",
            ),
            isAnswerLocked = true,
            feedback = QuizFeedback(
                isCorrect = false,
                previousScore = 25,
                newScore = 15,
                scoreDelta = -10,
                masteryStage = MasteryStage.NOVICE,
                word = WordEntity(
                    id = 2,
                    word = "Ubiquitous",
                    phonetic = "/juːˈbɪkwɪtəs/",
                    notes = null,
                    masteryScore = 15,
                    createdAt = System.currentTimeMillis(),
                ),
                context = ContextEntity(
                    id = 2,
                    wordId = 2,
                    meaning = "Present, appearing, or found everywhere",
                    exampleUsage = "Smartphones have become ubiquitous in modern society.",
                    cycleOrder = 1,
                ),
            ),
        ),
        // Error state
        QuizUiState(
            isLoading = false,
            errorMessage = "No words available for this quiz. Add some words to your library first.",
        ),
    )
}

// ─────────────────────────────────────────────────────────────────────────────────
// Previews
// ─────────────────────────────────────────────────────────────────────────────────

@Preview(name = "Quiz Screen States", showBackground = true)
@Composable
private fun QuizScreenPreview(
    @PreviewParameter(QuizUiStateProvider::class) state: QuizUiState,
) {
    LexiMasterTheme {
        Surface {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
            ) {
                when {
                    state.isLoading -> {
                        LoadingOverlay()
                    }

                    state.errorMessage != null -> {
                        ErrorState(
                            message = state.errorMessage,
                            onRetry = {},
                        )
                    }

                    state.currentWord != null -> {
                        PromptCanvas(
                            questionType = state.currentQuestionType!!,
                            word = state.currentWord,
                            activeContext = state.activeContext,
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OptionSelectorCanvas(
                            options = state.options,
                            selectedOption = state.selectedOption,
                            isAnswerLocked = state.isAnswerLocked,
                            feedback = state.feedback,
                            onOptionSelected = {},
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        ActionFooter(
                            isAnswerLocked = state.isAnswerLocked,
                            feedback = state.feedback,
                            isLastQuestion = state.isLastQuestion,
                            onNextQuestion = {},
                        )
                    }
                }
            }
        }
    }
}


@Preview(name = "Recognition Prompt", showBackground = true)
@Composable
private fun RecognitionPromptPreview() {
    LexiMasterTheme(darkTheme = true) {
        RecognitionPrompt(
            word = WordEntity(
                id = 1,
                word = "Ephemeral",
                phonetic = "/ɪˈfem(ə)rəl/",
                notes = null,
                masteryScore = 25,
                createdAt = System.currentTimeMillis(),
            ),
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "Synonym Prompt", showBackground = true)
@Composable
private fun SynonymPromptPreview() {
    LexiMasterTheme {
        SynonymPrompt(
            word = WordEntity(
                id = 1,
                word = "ephemeral",
                phonetic = "/ɪˈfem(ə)rəl/",
                notes = null,
                masteryScore = 25,
                createdAt = System.currentTimeMillis(),
            ),
            activeContext = ContextEntity(
                id = 1,
                wordId = 1,
                meaning = "Lasting for a very short time",
                exampleUsage = "The ephemeral beauty of cherry blossoms makes them even more precious.",
                cycleOrder = 1,
            ),
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "Recall Prompt", showBackground = true)
@Composable
private fun RecallPromptPreview() {
    LexiMasterTheme(darkTheme = true) {
        RecallPrompt(
            word = WordEntity(
                id = 1,
                word = "ephemeral",
                phonetic = "/ɪˈfem(ə)rəl/",
                notes = null,
                masteryScore = 25,
                createdAt = System.currentTimeMillis(),
            ),
            activeContext = ContextEntity(
                id = 1,
                wordId = 1,
                meaning = "Lasting for a very short time",
                exampleUsage = "The ephemeral beauty of cherry blossoms makes them even more precious.",
                cycleOrder = 1,
            ),
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "Feedback Panel - Success", showBackground = true)
@Composable
private fun FeedbackPanelSuccessPreview() {
    LexiMasterTheme {
        FeedbackPanel(
            feedback = QuizFeedback(
                isCorrect = true,
                previousScore = 25,
                newScore = 35,
                scoreDelta = 10,
                masteryStage = MasteryStage.COMPETENT,
                word = WordEntity(
                    id = 1,
                    word = "Ephemeral",
                    phonetic = "/ɪˈfem(ə)rəl/",
                    notes = null,
                    masteryScore = 35,
                    createdAt = System.currentTimeMillis(),
                ),
                context = ContextEntity(
                    id = 1,
                    wordId = 1,
                    meaning = "Lasting for a very short time",
                    exampleUsage = "The ephemeral beauty of cherry blossoms makes them even more precious.",
                    cycleOrder = 1,
                ),
            ),
            isLastQuestion = false,
            onNextQuestion = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "Feedback Panel - Incorrect", showBackground = true)
@Composable
private fun FeedbackPanelIncorrectPreview() {
    LexiMasterTheme {
        FeedbackPanel(
            feedback = QuizFeedback(
                isCorrect = false,
                previousScore = 25,
                newScore = 15,
                scoreDelta = -10,
                masteryStage = MasteryStage.NOVICE,
                word = WordEntity(
                    id = 2,
                    word = "Ubiquitous",
                    phonetic = "/juːˈbɪkwɪtəs/",
                    notes = null,
                    masteryScore = 15,
                    createdAt = System.currentTimeMillis(),
                ),
                context = ContextEntity(
                    id = 2,
                    wordId = 2,
                    meaning = "Present, appearing, or found everywhere",
                    exampleUsage = "Smartphones have become ubiquitous in modern society.",
                    cycleOrder = 1,
                ),
            ),
            isLastQuestion = true,
            onNextQuestion = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}