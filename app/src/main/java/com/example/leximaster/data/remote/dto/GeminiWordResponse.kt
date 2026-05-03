package com.example.leximaster.data.remote.dto

import android.os.Parcelable
import kotlinx.serialization.Serializable
import kotlinx.parcelize.Parcelize

@Serializable
@Parcelize
data class GeminiWordResponse(
    val phonetic: String?,
    val contexts: List<ContextWithOrder>,
    val synonyms: List<String>,
) : Parcelable

@Serializable
@Parcelize
data class ContextWithOrder(
    val meaning: String,
    val exampleUsage: String,
    val cycleOrder: Int, // 1 = INTRODUCTION, 2 = NUANCED, 3 = TECHNICAL
) : Parcelable
