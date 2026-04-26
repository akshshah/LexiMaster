package com.example.leximaster.data.local.converter

import androidx.room.TypeConverter

enum class QuestionType {
    RECOGNITION,
    SYNONYM,
    RECALL,
}

enum class ScoreChangeReason {
    QUIZ_CORRECT,
    QUIZ_WRONG,
    MANUAL_EDIT,
    SYSTEM_DECAY,
}

class LexiMasterConverters {
    @TypeConverter
    fun fromQuestionType(value: QuestionType): String = value.name

    @TypeConverter
    fun toQuestionType(value: String): QuestionType? {
        return QuestionType.entries.find { it.name == value }
    }

    @TypeConverter
    fun fromScoreChangeReason(value: ScoreChangeReason): String = value.name

    @TypeConverter
    fun toScoreChangeReason(value: String): ScoreChangeReason? {
        return ScoreChangeReason.entries.find { it.name == value }
    }
}
