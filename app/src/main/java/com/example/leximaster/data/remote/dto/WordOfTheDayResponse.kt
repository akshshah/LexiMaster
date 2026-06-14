package com.example.leximaster.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class WordOfTheDayResponse(
    val word: String,
    val definitions: List<WordnikDefinition>,
    val examples: List<WordnikExample>? = null,
    val note: String? = null,
    val publishDate: String? = null
)

@Serializable
data class WordnikDefinition(
    val text: String,
    val partOfSpeech: String? = null,
    val source: String? = null
)

@Serializable
data class WordnikExample(
    val text: String,
    val title: String? = null,
    val url: String? = null
)
