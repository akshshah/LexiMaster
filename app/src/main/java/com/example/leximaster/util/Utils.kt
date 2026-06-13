package com.example.leximaster.util

import com.example.leximaster.data.local.entity.WordEntity

object Utils {
    fun WordEntity.calculateSuccessRate(): Double{
        val total = this.correctAnswers + this.wrongAnswers
        if (total == 0) return 1.0 // Prioritize words with no history
        return this.correctAnswers.toDouble() / total.toDouble()
    }
}