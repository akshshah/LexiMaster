# LexiMaster - Active Vocabulary Mastery

A lightweight guide for contributing to LexiMaster, an Android app that helps users achieve active mastery of vocabulary through AI-powered MCQ testing.

## Where to Find Things

- **Requirements & Specs**: `specs/requirement.md` — defines product behavior, scoring logic, testing modes, and technical stack
- **Database Schema**: `specs/schema.md` — Room entities, relations, and mastery thresholds
- **Android Skills**: Use when working on this project:
  - `android-compose-ui` — UI components, recomposition, animations, design system
  - `android-presentation-mvi` — MVI pattern for ViewModels, State, Action, Event
  - `android-data-layer` — repositories, DTOs, mappers, Ktor HttpClient, Room, offline-first
  - `android-error-handling` — Result<T, E> wrapper, DataError types, error mapping
  - `android-navigation` — type-safe Compose Navigation, route objects, nav graphs
  - `android-di-koin` — Koin modules, ViewModel injection, wiring in Application
  - `android-module-structure` — package organization (mirror module structure within :app)

## Architecture Decisions

- **Pattern**: MVI (Model-View-Intent) — State, Action, Event flow for complex quiz states
- **Structure**: Single module (`app/`) with packages mirroring multi-module architecture
- **Navigation**: Type-safe Compose Navigation with route objects
- **DI**: Koin — lightweight Kotlin DI with modules per layer
- **AI Provider**: Google Gemini API for question generation and nuance explanation

## Key Domains

- Dictionary, contexts, meanings — `WordEntity`, `ContextEntity`, `SynonymEntity`
- Quiz engine, scoring (0-100), mastery decay — `ScoreHistoryEntity` with QUIZ_CORRECT/QUIZ_WRONG/SYSTEM_DECAY
- Tests (New Test, Random Test), MCQ generation — Context cycling by mastery tier
- Analytics, post-quiz reports, word trends — Score history ledger and struggle analysis

## Core Business Logic Facts

- **Mastery Tiers**: 0–30 (Novice), 31–70 (Competent), 71–99 (Expert), 100 (Mastered)
- **Score Decay**: Mastered words (score 100) drop 5 points after 30 days inactivity during Random Test
- **Context Cycling**: Score 0–33 → Context 1, Score 34–66 → Context 2, Score 67–100 → Context 3
- **Expert Scoring**: Points halved (+5 instead of +10) in 71–99 tier due to difficulty
- **Room Entities**: `words`, `word_contexts`, `synonyms`, `score_history` — all use `Flow` and `@Transaction`

## When Adding Features

1. Read the relevant sections in `specs/requirement.md` and `specs/schema.md` for requirements
2. Use the appropriate android-* skills for implementation patterns
3. Keep CLAUDE.md light — this file only points to resources and facts, not rules