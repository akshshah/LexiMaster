# LexiMaster AI: Product Requirements Document

## 1. Project Vision
LexiMaster AI is an "Active Mastery" Android application designed to move users from passive vocabulary recognition to active real-world application. Using Generative AI, the app acts as a linguistic coach, testing users on nuance, tone, and the "most-used context" of words.

---

## 2. Core Functional Requirements

### 2.1 Vocabulary Entry & Curation (The "Preview Step")
* **Input:** User provides a single word.
* **AI Discovery:** System must fetch the top 2–3 most common real-world applications/contexts for that word.
* **User Curation:** User must be able to "check off" which specific contexts/meanings they wish to master.
* **Contextual Anchoring:** Every word added must be stored with its "Most Used Context" as the primary testing trigger.

### 2.2 The Mastery Engine (Scoring Logic)
* **Scale:** 0–100 Mastery Points per word.
* **Difficulty Tiers:**
    * **0–30 (Novice):** Simple definitions and high-frequency usage.
    * **31–70 (Competent):** Complex scenarios and "Thesaurus Trap" logic (distinguishing between near-synonyms).
    * **71–99 (Expert):** Secondary meanings and subtle nuances in tone/formality.
    * **100 (Mastered):** Word is retired from "New Test" rotation.
* **Score Decay:** Mastered words (Score 100) drop by 5 points after 30 days of inactivity to trigger a "Random Test" refresher.

### 2.3 AI-Powered Testing (MCQ)
* **Question Format:** Multiple Choice Questions (MCQ) only.
* **Weighted Scoring:**
    * **Perfect Fit (+10 pts):** Best word for the specific nuance.
    * **Dictionary Fit (+4 pts):** Meaning is correct, but context is slightly awkward.
    * **Near Miss (0 pts):** Synonym used in the wrong "vibe" or tone.
    * **Incorrect (-5 pts):** Wrong word entirely.
* **Interleaved Practice:** Tests must mix different words from the dictionary in a single 10-question session.
* **Multi-Meaning Testing:** For polysemous words, the AI must cycle through all user-selected meanings as the score increases.

### 2.4 Feedback & Analytics
* **Immediate Feedback:** Explanation of *why* a specific choice was the "Perfect Fit" vs. a "Dictionary Fit" provided instantly after answering.
* **Post-Quiz Report:** Summary of mastery progress, trending words, and domain-specific insights (e.g., "You are struggling with Legal contexts").

---

## 3. Testing Modes
1.  **New Test:** Prioritizes words with low scores (0-40), recently decayed words, and words nearing the "Mastery" finish line.
2.  **Random Test:** Pulls from the entire dictionary, including "Mastered" words, for overall maintenance.

---

## 4. Technical Stack & Architecture

### 4.1 Mobile Frontend
* **Platform:** Android (Kotlin)
* **UI Framework:** Jetpack Compose (Modern, state-driven UI).
* **Architecture:** MVVM or MVI to handle complex quiz states and score animations.

### 4.2 Data & Intelligence
* **Local Database:** Room (Persistence for Word Entities, Contexts, and Score History).
* **AI Integration:** Google Gemini API (Question generation, nuance explanation, and initial word research).
* **Offline Support:** Basic dictionary access and score viewing must work offline; Quizzes require an active AI connection.

---

## 5. Non-Functional Requirements
* **Contextual Accuracy:** AI must prioritize real-world corpus frequency over obscure dictionary definitions.
* **Low Latency:** AI question generation should feel near-instant (streamed or pre-fetched).
* **Engagement:** Use of "Daily Mastery Goals" to encourage consistent usage.

---

## 6. Glossary of Terms
* **Thesaurus Trap:** The tendency to use a synonym that is technically correct but contextually inappropriate.
* **Interleaved Practice:** Mixing different topics or words in one session to improve long-term retention.
* **Most Used Context:** The domain (Business, Social, Academic) where a word appears most frequently in modern language.
  """
