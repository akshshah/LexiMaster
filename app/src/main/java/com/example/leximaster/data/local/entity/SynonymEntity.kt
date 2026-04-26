package com.example.leximaster.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Normalized synonyms for reverse-lookup and discovery.
 *
 * @property id Primary Key
 * @property wordId Foreign Key to WordEntity.id
 * @property synonymText Individual synonym (should be stored in lowercase)
 */
@Entity(
    tableName = "synonyms",
    foreignKeys = [
        ForeignKey(
            entity = WordEntity::class,
            parentColumns = ["id"],
            childColumns = ["word_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["word_id"]),
        Index(value = ["synonym_text"])
    ]
)
data class SynonymEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "word_id")
    val wordId: Long,

    @ColumnInfo(name = "synonym_text")
    val synonymText: String,
)
