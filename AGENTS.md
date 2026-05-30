# AGENTS.md

This file provides guidance to agents when working with code in this repository.

## Build Commands
- Build: `./gradlew assembleDebug`
- Clean: `./gradlew clean`
- Run unit tests: `./gradlew test` or `./gradlew testDebugUnitTest`
- Run single test class: `./gradlew test --tests "com.example.leximaster.ExampleUnitTest"`
- Run single test method: `./gradlew test --tests "com.example.leximaster.ExampleUnitTest.addition_isCorrect"`
- Run instrumented tests: `./gradlew connectedAndroidTest`

## Critical Business Logic (Non-Obvious)
- **Mastery Tiers**: Novice (0-30), Competent (31-70), Expert (71-99), Mastered (100)
- **Expert Scoring**: Points halved (+5 instead of +10) for scores 71-99
- **Context Cycling**: Score determines which context is tested: 0-33→Context 1, 34-66→Context 2, 67-100→Context 3
- **Score Decay**: Mastered words (score 100) decay -5 points after 30 days inactivity (checked during Random Test)
- **Streak Logic**: Increment if 24-48h since last activity; reset if ≥48h

## Architecture Patterns
- **MVI Pattern**: ViewModels use `StateFlow` for state, `Channel` for one-time events
- **Result Wrapper**: Use [`Result<T, E>`](app/src/main/java/com/example/leximaster/domain/Result.kt) for AI/network operations
- **DAO Returns**: All read operations return `Flow`, writes are `suspend` functions
- **Multi-table updates**: Must use `@Transaction` annotation

## Key Files
- Specs: [`specs/requirement.md`](specs/requirement.md), [`specs/schema.md`](specs/schema.md)
- Repository: [`LexiMasterRepository`](app/src/main/java/com/example/leximaster/data/repository/LexiMasterRepository.kt) contains all business logic
- DI Modules: [`DataModule`](app/src/main/java/com/example/leximaster/di/DataModule.kt), [`AppModule`](app/src/main/java/com/example/leximaster/di/AppModule.kt)

## API Key
- Gemini API key configured via `GEMINI_API_KEY` in `gradle.properties`
