package com.example.leximaster.data.local.model

import com.example.leximaster.data.local.entity.ContextEntity

fun getCurrentContext(wordWithContexts: WordWithContexts): ContextEntity? {
    val word = wordWithContexts.word
    val contexts = wordWithContexts.contexts
    return when {
        word.masteryScore <= ContextEntity.SCORE_CYCLE_1_MAX -> {
            contexts.find { it.cycleOrder == ContextEntity.CYCLE_INTRODUCTION }
        }
        word.masteryScore <= ContextEntity.SCORE_CYCLE_2_MAX -> {
            contexts.find { it.cycleOrder == ContextEntity.CYCLE_NUANCED }
        }
        else -> {
            contexts.find { it.cycleOrder == ContextEntity.CYCLE_TECHNICAL }
        }
    }
}
