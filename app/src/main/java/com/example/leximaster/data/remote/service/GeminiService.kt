package com.example.leximaster.data.remote.service

import com.example.leximaster.data.local.converter.DifficultyLevel
import com.example.leximaster.data.local.converter.QuestionType
import com.example.leximaster.data.remote.config.GenerativeModelFactory
import com.example.leximaster.data.remote.dto.GeminiWordResponse
import com.example.leximaster.data.remote.dto.QuizQuestionResponse
import com.example.leximaster.data.remote.error.AiError
import com.example.leximaster.data.remote.prompts.GeminiPrompts
import com.example.leximaster.domain.Result.Failure
import com.example.leximaster.domain.Result.Success
import com.google.ai.client.generativeai.type.FinishReason
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.IOException
import com.example.leximaster.domain.Result as AppResult

class GeminiService(
    private val modelFactory: GenerativeModelFactory,
) {
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    suspend fun discoverWordData(word: String): AppResult<GeminiWordResponse, AiError> =
        try {
            val model = modelFactory.createModel()
            val prompt = GeminiPrompts.wordDiscovery(word)

            val response = model.generateContent(prompt)
            if (response.candidates.firstOrNull()?.finishReason == FinishReason.SAFETY) {
                return Failure(AiError.UnknownError("Response blocked by safety filters"))
            }

            val text = response.text ?: return Failure(
                AiError.UnknownError("Empty response from AI"),
            )

            val result = json.decodeFromString<GeminiWordResponse>(text)
            Success(result)
        } catch (e: SerializationException) {
            Failure(AiError.SerializationError(e.message ?: "Failed to parse JSON response"))
        } catch (e: IOException) {
            Failure(AiError.NetworkError(e.message ?: "Network error"))
        } catch (e: Exception) {
            handleGenericException(e)
        }

    suspend fun generateQuizQuestion(
        word: String,
        correctMeaning: String,
        exampleContext: String,
        questionType: QuestionType,
        difficultyLevel: DifficultyLevel,
    ): AppResult<QuizQuestionResponse, AiError> =
        try {
            val model = modelFactory.createModel()
            val prompt = GeminiPrompts.quizQuestionGeneration(
                word,
                correctMeaning,
                exampleContext,
                questionType,
                difficultyLevel
            )

            val response = model.generateContent(prompt)
            if (response.candidates.firstOrNull()?.finishReason == FinishReason.SAFETY) {
                return Failure(AiError.UnknownError("Response blocked by safety filters"))
            }

            val text = response.text ?: return Failure(
                AiError.UnknownError("Empty response from AI"),
            )

            val result = json.decodeFromString<QuizQuestionResponse>(text)
            Success(result)
        } catch (e: SerializationException) {
            Failure(AiError.SerializationError(e.message ?: "Failed to parse JSON response"))
        } catch (e: IOException) {
            Failure(AiError.NetworkError(e.message ?: "Network error"))
        } catch (e: Exception) {
            handleGenericException(e)
        }

    private fun handleGenericException(e: Exception): AppResult<Nothing, AiError> {
        val message = e.message ?: "Unknown error"
        return when {
            message.contains("429") -> {
                Failure(AiError.RateLimitError("Rate limit exceeded: $message"))
            }
            message.startsWith("4") -> {
                Failure(AiError.ApiError(
                    code = message.substringBefore(" "),
                    msg = message,
                ))
            }
            message.startsWith("5") -> {
                Failure(AiError.ApiError(
                    code = message.substringBefore(" "),
                    msg = "Server error: $message",
                ))
            }
            else -> {
                Failure(AiError.UnknownError(message))
            }
        }
    }
}
