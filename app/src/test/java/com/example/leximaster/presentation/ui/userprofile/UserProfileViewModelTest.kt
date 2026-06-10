package com.example.leximaster.presentation.ui.userprofile

import app.cash.turbine.test
import com.example.leximaster.data.local.entity.UserProfileEntity
import com.example.leximaster.data.local.entity.WordEntity
import com.example.leximaster.data.repository.LexiMasterRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UserProfileViewModelTest {

    private val repository = mockk<LexiMasterRepository>()
    private lateinit var viewModel: UserProfileViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        every { repository.observeUserProfile() } returns flowOf(
            UserProfileEntity(
                username = "TestUser",
                currentStreak = 5,
                longestStreak = 10
            )
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `state calculates totalPoints and averagePoints correctly`() = runTest {
        val words = listOf(
            WordEntity(id = 1, word = "Word1", phonetic = null, notes = null, masteryScore = 20, createdAt = 0L),
            WordEntity(id = 2, word = "Word2", phonetic = null, notes = null, masteryScore = 80, createdAt = 0L),
            WordEntity(id = 3, word = "Word3", phonetic = null, notes = null, masteryScore = 100, createdAt = 0L),
        )
        every { repository.getAllWords() } returns flowOf(words)

        viewModel = UserProfileViewModel(repository)

        viewModel.state.test {
            var state = awaitItem()
            // Skip initial default state if necessary
            if (state.isLoading && state.totalPoints == 0) {
                state = awaitItem()
            }

            assertEquals(200, state.totalPoints)
            assertEquals(66, state.averagePoints) // 200 / 3 = 66
            assertEquals(1, state.noviceCount)
            assertEquals(0, state.competentCount)
            assertEquals(1, state.expertCount)
            assertEquals(1, state.masteredCount)
            assertEquals("TestUser", state.username)
            assertEquals(5, state.currentStreak)
            assertEquals(10, state.longestStreak)
        }
    }

    @Test
    fun `state handles empty words list`() = runTest {
        every { repository.getAllWords() } returns flowOf(emptyList())

        viewModel = UserProfileViewModel(repository)

        viewModel.state.test {
            var state = awaitItem()
            if (state.isLoading) {
                state = awaitItem()
            }

            assertEquals(0, state.totalPoints)
            assertEquals(0, state.averagePoints)
            assertEquals(0, state.totalWords)
        }
    }
}
