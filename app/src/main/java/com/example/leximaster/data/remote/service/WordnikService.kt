package com.example.leximaster.data.remote.service

import com.example.leximaster.BuildConfig
import com.example.leximaster.data.remote.dto.WordOfTheDayResponse
import com.example.leximaster.data.remote.error.WordnikError
import com.example.leximaster.domain.Result
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import java.io.IOException

class WordnikService {
    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
            })
        }
    }

    suspend fun getWordOfTheDay(): Result<WordOfTheDayResponse, WordnikError> {
        return try {
            val apiKey = BuildConfig.WORDNIK_API_KEY
            val response: HttpResponse = client.get("https://api.wordnik.com/v4/words.json/wordOfTheDay") {
                url {
                    parameters.append("api_key", apiKey)
                }
            }

            when (response.status) {
                HttpStatusCode.OK -> {
                    Result.Success(response.body())
                }
                HttpStatusCode.Unauthorized, HttpStatusCode.Forbidden -> {
                    Result.Failure(WordnikError.InvalidApiKey("Invalid or missing API key"))
                }
                else -> {
                    Result.Failure(WordnikError.ApiError(response.status.value, "Server error: ${response.status}"))
                }
            }
        } catch (e: IOException) {
            Result.Failure(WordnikError.NetworkError(e.message ?: "Network error"))
        } catch (e: Exception) {
            Result.Failure(WordnikError.UnknownError(e.message ?: "Unknown error"))
        }
    }
}
