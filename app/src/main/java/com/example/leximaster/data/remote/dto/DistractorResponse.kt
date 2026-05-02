package com.example.leximaster.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class DistractorResponse(
    val distractors: List<String>,
)
