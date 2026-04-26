package com.example.leximaster.data.local.converter

import com.example.leximaster.data.local.entity.SynonymEntity

/**
 * Extension function to convert a list of synonym strings to SynonymEntity list.
 */
fun List<String>.toSynonymEntity(wordId: Long): List<SynonymEntity> {
    return this
        .map { it.trim().lowercase() }
        .filter { it.isNotEmpty() }
        .map { SynonymEntity(wordId = wordId, synonymText = it) }
}

/**
 * Extension function to convert SynonymEntity list back to string list.
 */
fun List<SynonymEntity>.toListString(): List<String> {
    return this.map { it.synonymText }
}
