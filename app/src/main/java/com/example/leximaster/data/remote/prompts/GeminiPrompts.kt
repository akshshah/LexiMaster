package com.example.leximaster.data.remote.prompts

object GeminiPrompts {
    fun wordDiscovery(word: String): String =
        """
        You are helping a user master the word "$word".
        Generate information about this word with exactly 3 contexts representing different levels of mastery.

        Context 1 (cycleOrder: 1) should be the INTRODUCTION level - the most common, everyday usage.
        Context 2 (cycleOrder: 2) should be the NUANCED level - secondary meanings or subtle differences.
        Context 3 (cycleOrder: 3) should be the TECHNICAL level - idiomatic, industry-specific, or tone-specific usage.

        Also provide:
        - The phonetic pronunciation (include IPA if available, or null if not found)
        - Up to 5 synonyms (relevant and commonly used)

        Return ONLY valid JSON with this exact structure:
        {
          "phonetic": "string or null",
          "contexts": [
            {
              "meaning": "clear definition",
              "exampleUsage": "natural sentence usage",
              "cycleOrder": 1
            },
            {
              "meaning": "clear definition",
              "exampleUsage": "natural sentence usage",
              "cycleOrder": 2
            },
            {
              "meaning": "clear definition",
              "exampleUsage": "natural sentence usage",
              "cycleOrder": 3
            }
          ],
          "synonyms": ["word1", "word2", ...]
        }
        """.trimIndent()

    fun distractorGeneration(
        word: String,
        correctMeaning: String,
        exampleContext: String,
    ): String =
        """
        You are helping create a multiple choice question about "$word".

        The correct answer meaning is: "$correctMeaning"
        The example context is: "$exampleContext"

        Generate exactly 3 distractors - incorrect meanings that are:
        1. Plausible but wrong (related concepts that sound similar but don't fit)
        2. NOT synonyms or close variations of the word itself
        3. NOT the actual meaning provided above

        Return ONLY valid JSON with this exact structure:
        {
          "distractors": ["wrong meaning 1", "wrong meaning 2", "wrong meaning 3"]
        }
        """.trimIndent()

    val systemInstruction: String
        get() =
            """
            You are a helpful assistant for vocabulary learning. Always respond in valid JSON format
            with exact structure as specified in each prompt. Do not include markdown formatting,
            explanations, or extra text - only the JSON response.
            """.trimIndent()
}
