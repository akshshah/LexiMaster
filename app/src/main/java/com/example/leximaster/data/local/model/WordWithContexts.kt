package com.example.leximaster.data.local.model

import androidx.room.Embedded
import androidx.room.Relation
import com.example.leximaster.data.local.entity.ContextEntity
import com.example.leximaster.data.local.entity.SynonymEntity
import com.example.leximaster.data.local.entity.WordEntity

data class WordWithContexts(
    @Embedded
    val word: WordEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "word_id"
    )
    val contexts: List<ContextEntity>,
)

data class WordWithSynonyms(
    @Embedded
    val word: WordEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "word_id"
    )
    val synonyms: List<SynonymEntity>,
)

data class WordComplete(
    @Embedded
    val word: WordEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "word_id"
    )
    val contexts: List<ContextEntity>,

    @Relation(
        parentColumn = "id",
        entityColumn = "word_id"
    )
    val synonyms: List<SynonymEntity>,
)