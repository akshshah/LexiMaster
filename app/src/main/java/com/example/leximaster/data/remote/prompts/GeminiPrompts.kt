package com.example.leximaster.data.remote.prompts

import com.example.leximaster.data.local.converter.DifficultyLevel
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

    fun quizQuestionGeneration(
        word: String,
        correctMeaning: String,
        exampleContext: String,
        questionType: QuestionType,
        difficultyLevel: DifficultyLevel,
    ): String {

        val difficultyInstruction = when (difficultyLevel) {
            DifficultyLevel.Beginner -> """
            DIFFICULTY: Beginner
            - Wrong options must be obviously different in meaning — a learner should be able to eliminate them with basic reasoning.
            - Avoid rare, archaic, or highly technical vocabulary in the wrong options.
            - Prefer common, everyday English words for distractors.
        """.trimIndent()

            DifficultyLevel.Intermediate -> """
            DIFFICULTY: Intermediate
            - Wrong options must be plausible enough to require genuine understanding of "$word" to eliminate.
            - Distractors can include moderately uncommon words from adjacent semantic domains.
            - Avoid extremely rare or archaic vocabulary.
        """.trimIndent()

            DifficultyLevel.Advanced -> """
            DIFFICULTY: Advanced
            - Wrong options must be highly deceptive — a learner must have deep knowledge of "$word" to distinguish them.
            - Distractors should use sophisticated, nuanced vocabulary including rare, archaic, or domain-specific words.
            - Exploit subtle connotation differences, register shifts, and near-synonyms to maximise challenge.
        """.trimIndent()
        }

        val specificInstruction = when (questionType) {

            QuestionType.RECOGNITION -> """
            QUESTION TYPE: Definition Recognition
            TASK: Ask the user what "$word" means. Generate 3 wrong definition options and 1 correct one.
            
            Correct definition for reference: "$correctMeaning"
            
            Question format:
            - Use exactly: "What does '$word' mean?"
            
            Rules for all 4 options (including the correct one):
            1. All options must be short definition phrases (not single words).
            2. The 3 wrong options must come from semantically adjacent domains — plausible-sounding definitions that a learner who half-knows the word would find genuinely tempting.
            3. Wrong options must NOT paraphrase, hint at, or overlap with "$correctMeaning".
            4. Wrong options must NOT be synonyms of each other.
            5. All options must match the same grammatical form (e.g. all noun phrases, or all verb phrases).
        """.trimIndent()

            QuestionType.SYNONYM -> """
            QUESTION TYPE: Synonym Identification
            TASK: Ask the user to find the best synonym for "$word". Generate 3 wrong word options and 1 correct synonym.
            
            Correct meaning for reference: "$correctMeaning"
            
            Question format:
            - Use exactly: "Which word is closest in meaning to '$word'?"
            
            Rules for all 4 options:
            1. ALL options must be single words only — no phrases.
            2. The 1 correct option must be a genuine synonym of "$word" that closely matches "$correctMeaning".
            3. The 3 wrong options must be real English words that are deceptively confusable — they can be:
               - Words that sound/look phonetically similar to "$word"
               - Antonyms of "$word"
               - Words from the same semantic domain but with a different meaning
            4. Wrong options must NOT be actual synonyms or near-synonyms of "$word".
            5. All 4 options must be the same part of speech as "$word".
        """.trimIndent()

            QuestionType.RECALL -> """
            QUESTION TYPE: Fill in the Blank (Recall)
            TASK: Create a fill-in-the-blank sentence where the correct answer is the word "$word" itself.
            
            Correct meaning for reference: "$correctMeaning"
            Original example context: "$exampleContext"
            
            Question format:
            - Adapt the example context sentence above into a fill-in-the-blank by replacing "$word" with "_______".
            - Only adapt as much as needed for naturalness — stay as close to the original as possible.
            - The blank must be clearly and unambiguously filled only by "$word" given the sentence context.
            
            Rules for all 4 options:
            1. ALL options must be single words only.
            2. The 1 correct option is "$word" itself.
            3. The 3 wrong options must be real English words that:
               - Fit the sentence grammatically (same part of speech as "$word")
               - Look or sound deceptively similar to "$word", OR belong to the same semantic domain
               - Do NOT make sense in the blank contextually when read carefully
            4. Wrong options must NOT be synonyms of "$word".
        """.trimIndent()
        }

        return """
        You are an expert lexicographer designing a rigorous vocabulary quiz for language learners.
        Target word: "$word"
        
        $difficultyInstruction
        
        $specificInstruction
        
        OUTPUT RULES:
        1. Shuffle the 4 options so the correct answer is NOT always in the same position.
        2. Return ONLY a raw JSON object — no markdown, no backticks, no explanation.
        3. Use this exact structure:
       
        {"question": "...", "options": ["...", "...", "...", "..."], "correctIndex": 0}
        
        4. correct_index MUST be an integer in the range 0 to 3 (inclusive). Any other value is invalid.
        5. correct_index must be the 0-based index of the correct option in the options array.
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
