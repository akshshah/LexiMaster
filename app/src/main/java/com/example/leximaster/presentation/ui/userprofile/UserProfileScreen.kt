package com.example.leximaster.presentation.ui.userprofile

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.leximaster.data.repository.MasteryStage
import com.example.leximaster.ui.theme.LexiMasterTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    state: UserProfileState = UserProfileState(),
    onAction: (ProfileAction) -> Unit = {},
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Profile",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                },
                windowInsets = WindowInsets(0, 0, 0, 0),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                )
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ProfileIdentityHeader(
                username = state.username,
                onEditClick = { onAction(ProfileAction.OnEditUsernameClick) },
            )
            AchievementMetricsGrid(
                currentStreak = state.currentStreak,
                longestStreak = state.longestStreak,
                totalPoints = state.totalPoints,
                averagePoints = state.averagePoints,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            VocabularyDistributionCard(
                noviceCount = state.noviceCount,
                competentCount = state.competentCount,
                expertCount = state.expertCount,
                masteredCount = state.masteredCount,
                totalWords = state.totalWords,
                modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 12.dp),
            )
        }
    }

    if (state.isEditingUsername) {
        EditUsernameDialog(
            currentInput = state.usernameInput,
            onInputChange = { onAction(ProfileAction.OnUsernameInputChange(it)) },
            onConfirm = { onAction(ProfileAction.OnSaveUsername) },
            onDismiss = { onAction(ProfileAction.OnDismissEditUsername) },
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Profile Identity Header
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ProfileIdentityHeader(
    username: String,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accentColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        accentColor.copy(alpha = 0.5f),
                        accentColor.copy(alpha = 0.01f),
                    )
                )
            ),
    ) {
        // Decorative accent circle — top-right
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
                .padding(horizontal = 24.dp, vertical = 32.dp),
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Person,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Username + Edit
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = username.ifBlank { "Lexicographer" },
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = "Edit username",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Achievement Metrics Grid
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AchievementMetricsGrid(
    currentStreak: Int,
    longestStreak: Int,
    totalPoints: Int,
    averagePoints: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Achievements",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Streak Card
            MetricCard(
                modifier = Modifier.weight(1f),
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "🔥 Streak",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        StatText(label = "Current", value = currentStreak, textColor = MaterialTheme.colorScheme.onSecondaryContainer)
                        StatText(label = "Best", value = longestStreak, textColor = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                }
            }

            // Total Points Card
            MetricCard(
                modifier = Modifier.weight(1f),
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "⭐ Score",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        StatText(label = "Total", value = totalPoints, textColor = MaterialTheme.colorScheme.onTertiaryContainer)
                        StatText(label = "Average", value = averagePoints, textColor = MaterialTheme.colorScheme.onTertiaryContainer)
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricCard(
    modifier: Modifier = Modifier,
    containerColor: Color,
    contentColor: Color,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        content()
    }
}

@Composable
private fun StatText(
    label: String,
    value: Int,
    textColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = textColor,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = textColor.copy(alpha = 0.7f),
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Vocabulary Distribution Card
// ─────────────────────────────────────────────────────────────────────────────

private data class VocabStage(
    val label: String,
    val emoji: String,
    val count: Int,
    val color: Color,
)

@Composable
private fun VocabularyDistributionCard(
    noviceCount: Int,
    competentCount: Int,
    expertCount: Int,
    masteredCount: Int,
    totalWords: Int,
    modifier: Modifier = Modifier,
) {
    val stages = listOf(
        VocabStage(MasteryStage.NOVICE.displayName, "🌱", noviceCount, MasteryStage.NOVICE.color),
        VocabStage(MasteryStage.COMPETENT.displayName, "📖", competentCount, MasteryStage.COMPETENT.color),
        VocabStage(MasteryStage.EXPERT.displayName, "🎯", expertCount, MasteryStage.EXPERT.color),
        VocabStage(MasteryStage.MASTERED.displayName, "🏆", masteredCount, MasteryStage.MASTERED.color),
    )

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Vocabulary",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp),
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Header summary
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Total Words",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = totalWords.toString(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                // Stage rows
                stages.forEach { stage ->
                    VocabStageRow(
                        stage = stage,
                        totalWords = totalWords,
                    )
                }
            }
        }
    }
}

@Composable
private fun VocabStageRow(
    stage: VocabStage,
    totalWords: Int,
    modifier: Modifier = Modifier,
) {
    val fraction = if (totalWords > 0) stage.count.toFloat() / totalWords else 0f

    var animatedFraction by remember { mutableFloatStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = animatedFraction,
        animationSpec = tween(durationMillis = 600),
        label = "progress_${stage.label}",
    )

    LaunchedEffect(fraction) {
        animatedFraction = fraction
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = stage.emoji, style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = stage.label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "${(fraction * 100).toInt()}%",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = stage.count.toString(),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.width(28.dp),
                    textAlign = TextAlign.End,
                )
            }
        }

        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(50)),
            color = stage.color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeCap = StrokeCap.Round,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Edit Username Dialog
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun EditUsernameDialog(
    currentInput: String,
    onInputChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Edit Username",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            OutlinedTextField(
                value = currentInput,
                onValueChange = onInputChange,
                label = { Text("Username") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onConfirm() }),
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = currentInput.isNotBlank(),
            ) {
                Text("Save", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(20.dp),
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Previews
// ─────────────────────────────────────────────────────────────────────────────

private val previewState = UserProfileState(
    username = "Aksh",
    currentStreak = 7,
    longestStreak = 21,
    totalPoints = 1_340,
    noviceCount = 12,
    competentCount = 8,
    expertCount = 5,
    masteredCount = 3,
    isLoading = false,
)

@Preview(name = "Light Mode", showBackground = true)
@Composable
private fun UserProfileScreenLightPreview() {
    LexiMasterTheme(darkTheme = false) {
        UserProfileScreen(state = previewState)
    }
}

@Preview(name = "Dark Mode", showBackground = true)
@Composable
private fun UserProfileScreenDarkPreview() {
    LexiMasterTheme(darkTheme = true) {
        UserProfileScreen(state = previewState)
    }
}
