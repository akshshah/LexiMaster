# LexiMaster - Active Vocabulary Mastery

A lightweight guide for contributing to LexiMaster, an Android app that helps users achieve active mastery of vocabulary through AI-powered MCQ testing.

## Where to Find Things

- **Requirements & Specs**: `specs/requirement.md` — defines product behavior, scoring logic, testing modes, and technical stack
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

- Dictionary, contexts, meanings
- Quiz engine, scoring (0-100), mastery decay
- Tests (New Test, Random Test), MCQ generation
- Analytics, post-quiz reports, word trends

## When Adding Features

1. Read the relevant sections in `specs/requirement.md` for behavioral requirements
2. Use the appropriate android-* skills for implementation patterns
3. Keep CLAUDE.md light — this file only points to resources, not project-specific rules