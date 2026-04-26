# Epic Implementation Specification: LexiMaster Data Layer

## 1. Executive Summary
This document defines the relational SQLite architecture for LexiMaster. It supports multi-context testing, reverse synonym lookups, historical progress tracking, and global user state management.

## 2. Database Schema (Room Entities)

### 2.1 Vocabulary & Support Tables
| Table | Entity | Description |
| :--- | :--- | :--- |
| `words` | `WordEntity` | Primary vocabulary storage with mastery scores (0–100). |
| `contexts` | `ContextEntity` | One-to-many relationship with words for polysemous meanings. |
| `synonyms` | `SynonymEntity` | Normalized synonyms for fast reverse-lookups and discovery. |

### 2.2 Activity & Analytics Tables
| Table | Entity | Description |
| :--- | :--- | :--- |
| `score_history` | `ScoreHistoryEntity` | Immutable ledger of score changes for trend analysis. |
| `quiz_sessions` | `QuizSessionEntity` | Header records for a 10-question quiz sitting. |
| `quiz_attempts` | `QuizAttemptEntity` | Granular per-question data (response time, correctness). |

### 2.3 User State Tables
| Table | Entity | Description |
| :--- | :--- | :--- |
| `user_profile` | `UserProfileEntity` | Singleton table (ID=1) for streaks, XP, and global points. |

---

## 3. Detailed Column Mapping (Code-Aligned)

### 3.1 `score_history`
* **Indices:** `[word_id]`, `[word_id, change_timestamp]`
* **Columns:** `previous_score`, `new_score`, `score_delta`, `score_change_reason`, `change_timestamp`.

### 3.2 `quiz_attempts`
* **Indices:** `[session_id]`, `[word_id]`, `[context_id]`
* **Columns:** `session_id`, `word_id`, `context_id`, `question_type`, `is_correct`, `response_time_ms`.

### 3.3 `user_profile`
* **Constraint:** PK hardcoded to `1`.
* **Columns:** `username`, `total_points`, `current_streak`, `longest_streak`, `last_active_date`, `xp_level`.

---

## 4. Core Business Logic & Constants

### 4.1 Mastery Thresholds & Tier Logic
* **Novice (0–30):** Standard point increments (+10).
* **Competent (31–70):** Standard point increments (+10).
* **Expert (71–99):** The "Grind"—Correct answers only grant +5 points.
* **Mastered (100):** Retired from standard rotation; subject to decay logic.

### 4.2 Automated Maintenance Logic
* **Just-In-Time Decay:** Mastered words decay by -5 points if `(CurrentTime - last_tested) > 30 days`.
* **Data Pruning:** Granular `quiz_attempts` records should be pruned after 90 days.
* **Streak Management:** * **Increment:** If `24h <= (Now - last_active_date) < 48h`.
    * **Reset:** If `(Now - last_active_date) >= 48h`.

### 4.3 Context Cycling Rule
Questions must pull from the `contexts` table based on current `mastery_score`:
* **Cycle 1 (Intro):** Score 0–33.
* **Cycle 2 (Nuanced):** Score 34–66.
* **Cycle 3 (Technical):** Score 67–100.

---

## 5. Technical Constraints
* **Integrity:** All child tables (contexts, synonyms, history, attempts) MUST use `ForeignKey.CASCADE` on `word_id` or `session_id`.
* **Reactivity:** All DAO read operations must return `Flow` to drive the Jetpack Compose UI.
* **Concurrency:** Write operations must be `suspend` functions. Multi-table updates (e.g., finishing a quiz) must be wrapped in a `@Transaction`.