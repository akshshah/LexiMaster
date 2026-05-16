# LexiMaster

LexiMaster is a premium, gamified vocabulary-building Android application designed to help users discover, track, and master new words. Built with an offline-first philosophy, LexiMaster combines local intelligence with the power of generative AI to create a dynamic, personalized learning environment.

This project represents a modern engineering milestone: it was developed end-to-end using an **AI-agentic workflow**—leveraging advanced AI tools for architectural decisions, code generation, and memory optimization—while maintaining rigorous human ownership over design, code review, and quality assurance.

---

## 🚀 Features

* **AI-Powered Word Discovery:** Integrated Gemini AI API to dynamically generate context-aware definitions, phonetic pronunciations, synonyms, and contextual examples for new vocabulary.
* **Gamified Streak Management:** A robust user profile tracking system that updates daily active streaks, records all-time high scores, and calculates "LexiPoints."
* **Intelligent Vocabulary Decay:** A background decay mechanism that simulates memory retention; mastered words gradually decay over time if not reviewed, prompting timely active recall.
* **Single-Pass Performance Metrics:** Highly optimized UI state management that processes complex vocabulary distribution statistics (Novice, Competent, Expert, Mastered) in a single memory pass over the local database.
* **Material 3 Premium UI:** A fully reactive, clean interface featuring fluid progress animations, custom metric cards, and out-of-the-box dynamic light/dark mode support.

---

## 🛠️ Architecture & Tech Stack

LexiMaster adheres strictly to **Modern Android Development (MAD)** best practices:

* **Language:** 100% Kotlin
* **UI Framework:** Jetpack Compose (Material 3)
* **Architecture:** MVVM (Model-View-ViewModel) + Clean Architecture principles
* **Asynchronous Execution:** Kotlin Coroutines & Advanced Flow APIs (`combine`, `map`, `stateIn`)
* **Local Database:** Room Database (Offline-first architecture with transactional DAOs)
* **AI Integration:** Google Gemini Client SDK

---

## 📊 Core Data Mechanics

### Vocabulary Mastery Brackets

The application tracks vocabulary mastery through an explicit score range ($0$ to $100$), categorization metrics are computed in real time to feed the dashboard and profile metrics cleanly:

| Stage | Score Range | Description |
| --- | --- | --- |
| 🌱 **Novice** | `0 .. 30` | Newly discovered words requiring heavy exposure. |
| 📖 **Competent** | `31 .. 70` | Words integrated into standard practice quizzes. |
| 🎯 **Expert** | `71 .. 99` | High retention words near full fluency. |
| 🏆 **Mastered** | `100` | Permanently unlocked words subject to memory decay rules. |

### Reactive UI Data Pipeline

ViewModels transform structural database streams directly into clean UI states using declarative pipelines. This eliminates boilerplate memory allocation and ensures data consistency during configuration changes (like screen rotations):

```kotlin
val state: StateFlow<UserProfileState> = combine(
    repository.observeUserProfile(),
    repository.getAllWords(),
    _uiState 
) { profile, words, localUiState ->
    // Single-pass analytics processing loop...
    localUiState.copy(
        username = profile?.username.orEmpty(),
        currentStreak = profile?.currentStreak ?: 0,
        noviceCount = calculatedNovice,
        /* ... */
    )
}.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = UserProfileState()
)

```

---

## 🤖 The Agentic Development Philosophy

LexiMaster was engineered by pairing human system architecture oversight with autonomous AI generation loops.

1. **Prompt-Driven Architecture:** System abstractions, data layers (Room Schemas), and state boundaries were defined using targeted technical criteria.
2. **Automated Optimization Blocks:** Complex logic routines—such as the manual `for` loop iteration replacing multiple high-overhead linear filtering operations (`.count`)—were delegated to AI agents under strict constraints for mobile performance execution.
3. **Continuous Review Loop:** Human review focused on structural correctness, verifying coroutine context switching (`Dispatchers.IO`), and resolving reactive stream race conditions.

---

## ⚡ Getting Started

### Prerequisites

* Android Studio Jellyfish (or newer)
* Android SDK 34+
* A Google AI Studio API Key (for Gemini integration)

### Installation

1. Clone the repository:
```bash
git clone https://github.com/yourusername/LexiMaster.git

```


2. Open the project in Android Studio.
3. Secure your API configuration by creating a `local.properties` file in your root directory and attaching your Gemini key:
```properties
GEMINI_API_KEY=your_actual_api_key_here

```


4. Sync Gradle and run the application on a device or emulator.
