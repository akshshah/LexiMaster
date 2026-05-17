package com.example.leximaster.presentation.ui.dashboard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.leximaster.R
import com.example.leximaster.data.repository.MasteryStage
import com.example.leximaster.ui.theme.LexiMasterTheme
import com.example.leximaster.ui.theme.streakBg
import com.example.leximaster.ui.theme.streakBorder

@Composable
fun DashboardTopSection(
    state: DashboardState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        DashboardHeader(greetingName = state.greetingName)

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MasteryRingSection(
                masteredCount = state.masteredWordsCount,
                totalCount = state.totalWordsCount,
                modifier = Modifier.size(190.dp)
            )
            StreakSection(
                streakCount = state.streakCount,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MasteryTierCard(
                name = MasteryStage.NOVICE.displayName,
                count = state.noviceCount,
                color = MasteryStage.NOVICE.color,
                modifier = Modifier.weight(1f)
            )
            MasteryTierCard(
                name = MasteryStage.COMPETENT.displayName,
                count = state.competentCount,
                color = MasteryStage.COMPETENT.color,
                modifier = Modifier.weight(1f)
            )
            MasteryTierCard(
                name = MasteryStage.EXPERT.displayName,
                count = state.expertCount,
                color = MasteryStage.EXPERT.color,
                modifier = Modifier.weight(1f)
            )
            MasteryTierCard(
                name = MasteryStage.MASTERED.displayName,
                count = state.masteredWordsCount,
                color = MasteryStage.MASTERED.color,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
fun DashboardHeader(
    greetingName: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
        Text(
            text = "Hello, $greetingName!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Your vocabulary overview",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun StreakSection(
    streakCount: Int,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Image(
            painter = painterResource(id = R.drawable.streak),
            contentDescription = "streak",
            modifier = Modifier
                .size(110.dp)
                .padding(bottom = 12.dp)
        )
        Surface(
            color = streakBg,
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(2.dp, color = streakBorder)
        ) {
            Text(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        text = "STREAK: $streakCount Days",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
    }
}

@Composable
fun MasteryRingSection(
    masteredCount: Int,
    totalCount: Int,
    modifier: Modifier = Modifier
) {
    val percentage = if (totalCount > 0) (masteredCount.toFloat() / totalCount.toFloat()) else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = percentage,
        animationSpec = tween(durationMillis = 1200),
        label = "RingAnimation"
    )
    val primaryColor = MaterialTheme.colorScheme.primary
    val ringTrackColor = MaterialTheme.colorScheme.primaryContainer

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        Canvas(modifier = Modifier.size(160.dp)) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(primaryColor.copy(alpha = 0.15f), Color.Transparent),
                    radius = size.minDimension / 1.1f
                ),
                radius = size.minDimension / 1.8f
            )

            drawArc(
                color = ringTrackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
            )

            drawArc(
                color = primaryColor,
                startAngle = -90f,
                sweepAngle = animatedProgress * 360f,
                useCenter = false,
                style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        // Inner Text Container
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "${(percentage * 100).toInt()}%",
                fontSize = 42.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface,
                letterSpacing = (-1).sp
            )
            Text(
                text = "Mastered",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "$masteredCount / $totalCount Words",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun MasteryTierCard(
    name: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.8f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}


@Preview
@Composable
fun PreviewModularDashboardSection() {
    LexiMasterTheme {
        DashboardTopSection(
            state = DashboardState(
                greetingName = "Alex",
                streakCount = 14,
                masteredWordsCount = 260,
                totalWordsCount = 360,
                noviceCount = 30,
                competentCount = 45,
                expertCount = 35,
            )
        )
    }
}