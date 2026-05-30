package com.example.leximaster.data.remote.prompts

import com.example.leximaster.data.local.converter.QuestionType

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

        If the user's input is misspelled, identify the most likely intended word and provide the response for that corrected word. Return the corrected spelling in the 'word' field of the JSON response.

        Return ONLY valid JSON with this exact structure:
        {
          "word": "string",
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
        questionType: QuestionType
    ): String {
        val specificInstruction = when (questionType) {
            QuestionType.RECOGNITION -> """
            TASK: Generate exactly 3 distractors — incorrect definitions of "$word".
            
            Correct definition: "$correctMeaning"
            Example sentence: "$exampleContext"
            
            Rules:
            1. Each distractor must come from a semantically adjacent domain (e.g., if the word means "to withdraw quietly", a distractor might be "to argue forcefully" — plausible register, wrong direction).
            2. Distractors must be challenging: a learner who half-knows the word should find them genuinely tempting.
            3. Must NOT restate, paraphrase, or hint at the correct definition.
            4. Must NOT be synonyms of "$word" or of each other.
            5. Match the grammatical form of the correct definition (e.g., if it's a noun phrase, all distractors are noun phrases).
        """.trimIndent()

            QuestionType.SYNONYM -> """
            TASK: Generate exactly 3 single-word distractors that could plausibly replace "$word" in the sentence below, but are contextually wrong.
            
            Target word: "$word"
            Correct meaning: "$correctMeaning"
            Sentence: "$exampleContext"
            
            Rules:
            1. Each distractor must be a single word that fits the sentence's grammar and register (same part of speech as "$word").
            2. Must be thematically related to the sentence's domain but semantically divergent from the correct meaning.
            3. Must NOT be a true synonym, near-synonym, or antonym of "$word".
            4. Must NOT make the sentence obviously nonsensical — the wrongness should require genuine comprehension to detect.
            5. Distractors must be distinct from each other in meaning.
        """.trimIndent()

            QuestionType.RECALL -> """
            TASK: Generate exactly 3 distractors that are plausible-but-wrong definitions a language learner would confabulate for "$word".
            
            Correct definition: "$correctMeaning"
            Example sentence: "$exampleContext"
            
            Rules:
            1. Model the kinds of mistakes real learners make: false cognates, surface-level sound associations, or blending with a related word they already know.
            2. Each distractor should feel like a confident (but wrong) guess — not obviously absurd.
            3. Must NOT accurately define "$word" or lead the learner toward the correct meaning.
            4. Must NOT be true synonyms of "$word".
            5. Vary the error type across the 3 distractors (e.g., one false cognate, one plausible-but-wrong domain, one meaning-reversal).
        """.trimIndent()
        }

        return """
        You are an expert lexicographer designing a rigorous vocabulary quiz for language learners.
        Target word: "$word"
        
        $specificInstruction
        
        Return ONLY a raw JSON object with no markdown, no backticks, no extra text:
        {"distractors": ["distractor 1", "distractor 2", "distractor 3"]}
    """.trimIndent()
    }

    val systemInstruction: String
        get() =
            """
            You are a helpful assistant for vocabulary learning. Always respond in valid JSON format
            with exact structure as specified in each prompt. Do not include markdown formatting,
            explanations, or extra text - only the JSON response.
            """.trimIndent()
}
