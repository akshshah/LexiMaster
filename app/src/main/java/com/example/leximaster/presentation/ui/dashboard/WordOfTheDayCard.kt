package com.example.leximaster.presentation.ui.dashboard

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.leximaster.data.remote.dto.WordOfTheDayResponse
import com.example.leximaster.data.remote.dto.WordnikDefinition
import com.example.leximaster.data.remote.dto.WordnikExample
import com.example.leximaster.ui.theme.LexiMasterTheme

@Composable
fun WordOfTheDayCard(
    wotdState: WordOfTheDayState,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column {
        Text(
            text = "WORD OF THE DAY",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(12.dp))
        Card(
            modifier = modifier
                .fillMaxWidth()
                .animateContentSize(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier .padding(20.dp)){
                when (wotdState) {
                    is WordOfTheDayState.Loading -> {
                        WotdLoadingContent()
                    }

                    is WordOfTheDayState.Success -> {
                        WotdSuccessContent(wotdState.wordData)
                    }

                    is WordOfTheDayState.Error -> {
                        WotdErrorContent(wotdState.message, onRetry)
                    }
                }
            }
        }
    }
}

@Composable
private fun WotdSuccessContent(wordData: WordOfTheDayResponse) {
    val definition = wordData.definitions.firstOrNull()
    val example = wordData.examples?.firstOrNull()

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = wordData.word.lowercase().replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        if (definition?.partOfSpeech != null) {
            Spacer(modifier = Modifier.width(10.dp))
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = definition.partOfSpeech,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    if (definition != null) {
        Text(
            text = definition.text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    if (example != null) {
        Spacer(modifier = Modifier.height(16.dp))
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "\"${example.text}\"",
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodyMedium,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    } else {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "No usage example available for today.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun WotdLoadingContent() {
    Column {
        ShimmerItem(modifier = Modifier
            .height(40.dp)
            .fillMaxWidth(0.5f))
        Spacer(modifier = Modifier.height(12.dp))
        ShimmerItem(modifier = Modifier
            .height(20.dp)
            .fillMaxWidth())
        ShimmerItem(modifier = Modifier
            .height(20.dp)
            .fillMaxWidth(0.8f)
            .padding(top = 4.dp))
        Spacer(modifier = Modifier.height(16.dp))
        ShimmerItem(modifier = Modifier
            .height(60.dp)
            .fillMaxWidth())
    }
}

@Composable
private fun WotdErrorContent(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Couldn't load word",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        TextButton(
            onClick = onRetry, shape = RoundedCornerShape(100.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(4.dp))
            Text("Retry")
        }
    }
}

@Composable
fun ShimmerItem(modifier: Modifier = Modifier) {
    val shimmerColors = listOf(
        Color.LightGray.copy(alpha = 0.6f),
        Color.LightGray.copy(alpha = 0.2f),
        Color.LightGray.copy(alpha = 0.6f),
    )

    val transition = rememberInfiniteTransition(label = "Shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ShimmerTranslate"
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnim, y = translateAnim)
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(brush)
    )
}

@Preview
@Composable
fun PreviewWotdCardSuccess() {
    LexiMasterTheme(darkTheme = false) {
        WordOfTheDayCard(
            wotdState = WordOfTheDayState.Success(
                WordOfTheDayResponse(
                    word = "Ephemeral",
                    definitions = listOf(
                        WordnikDefinition(
                            text = "Lasting for a very short time.",
                            partOfSpeech = "adjective"
                        )
                    ),
                    examples = listOf(
                        WordnikExample(
                            text = "The beauty of a sunset is ephemeral, yet deeply moving."
                        )
                    )
                )
            ),
            onRetry = {}
        )
    }
}

@Preview
@Composable
fun PreviewWotdCardLoading() {
    LexiMasterTheme {
        WordOfTheDayCard(
            wotdState = WordOfTheDayState.Error(
                message = "Invalid or missing API key"
            ),
            onRetry = {}
        )
    }
}
