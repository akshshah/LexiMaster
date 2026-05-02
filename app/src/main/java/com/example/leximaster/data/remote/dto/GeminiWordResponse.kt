package com.example.leximaster.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class GeminiWordResponse(
    val phonetic: String?,
    val contexts: List<ContextWithOrder>,
    val synonyms: List<String>,
)

@Serializable
data class ContextWithOrder(
    val meaning: String,
    val exampleUsage: String,
    val cycleOrder: Int, // 1 = INTRODUCTION, 2 = NUANCED, 3 = TECHNICAL
)
