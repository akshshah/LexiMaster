package com.example.leximaster.data.remote.config

import android.util.Log
import com.example.leximaster.BuildConfig
import com.example.leximaster.data.remote.prompts.GeminiPrompts
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig

object GenerativeModelFactory {
    private const val TAG = "GenerativeModelFactory"

    fun createModel(): GenerativeModel {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty()) {
            Log.e(TAG, "GEMINI_API_KEY is not configured")
        }

        return GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = apiKey,
            systemInstruction = content { text(GeminiPrompts.systemInstruction) },
            generationConfig = generationConfig {
                responseMimeType = "application/json"
            }
        )
    }
}
