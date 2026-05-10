# Navigation System Documentation

## Overview
This documentation provides a comprehensive overview of the current navigation system and will serve as a reference for future improvements.
The LexiMaster app uses a composable-based navigation system with Jetpack Compose and follows the MVI (Model-View-Intent) pattern. The navigation architecture is well-structured with clear separation of concerns.

## Key Components

### 1. Navigation Routes
- `DashboardRoute`: Main dashboard screen
- `LibraryRoute`: Word library screen
- `ProfileRoute`: User profile screen
- `WordDiscoveryRoute`: Word discovery screen

### 2. Navigation Components
- **Navigation Host**: `LexiNavHost.kt` - Central navigation controller
- **Bottom Navigation**: `BottomNavigation.kt` - Tab-based navigation
- **Main Screen**: `MainScreen.kt` - Coordinates navigation and UI
- **Main Activity**: `MainActivity.kt` - Entry point

## Navigation Flow

### 1. Initial Flow
- `MainActivity` → `MainScreen` → `LexiNavHost` (startDestination = `DashboardRoute`)

### 2. Bottom Navigation
- **Dashboard**: `DashboardRoute` (Home icon)
- **Library**: `LibraryRoute` (List icon)
- **Profile**: `ProfileRoute` (Person icon)

### 3. Special Navigation
- `WordDiscoveryRoute` is only accessible from Library Screen via FAB (+ button)
- Bottom navigation bar is hidden when user is on `WordDiscoveryRoute`

## Navigation Implementation

### 1. Unidirectional Data Flow
- UI → Actions → ViewModel → Events → NavHost → Navigation

### 2. Event-Driven Navigation
- UI triggers actions → ViewModel handles → emits events → NavHost consumes → triggers navigation

### 3. Type Safety
- Uses `@Serializable` data objects for routes
- Route definitions are type-safe

