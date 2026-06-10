package com.example.leximaster.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class QuizQuestionResponse(
    val question: String,
    val options: List<String>,
    val correctIndex: Int
)
