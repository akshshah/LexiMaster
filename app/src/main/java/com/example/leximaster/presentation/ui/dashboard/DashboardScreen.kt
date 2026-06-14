package com.example.leximaster.presentation.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DashboardScreen(
    state: DashboardState,
    onAction: (DashboardAction) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        DashboardTopSection(state, onAction = onAction)
        WordOfTheDayCard(
            wotdState = state.wordOfTheDayState,
            onRetry = { onAction(DashboardAction.RetryWordOfTheDay) }
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun DashboardScreenPreview() {
    com.example.leximaster.ui.theme.LexiMasterTheme {
        DashboardScreen(
            state = DashboardState(
                greetingName = "Alex",
                streakCount = 14,
                masteredWordsCount = 260,
                totalWordsCount = 360,
                noviceCount = 30,
                competentCount = 45,
                expertCount = 35,
                wordOfTheDayState = WordOfTheDayState.Success(
                    com.example.leximaster.data.remote.dto.WordOfTheDayResponse(
                        word = "Ephemeral",
                        definitions = listOf(
                            com.example.leximaster.data.remote.dto.WordnikDefinition(
                                text = "Lasting for a very short time.",
                                partOfSpeech = "adjective"
                            )
                        ),
                        examples = listOf(
                            com.example.leximaster.data.remote.dto.WordnikExample(
                                text = "The beauty of a sunset is ephemeral, yet deeply moving."
                            )
                        )
                    )
                )
            )
        )
    }
}
