# GameVault

GameVault is an Android app for gamers who want to keep their whole video game library in one place. You register the games you own on PC, PlayStation, Xbox, Nintendo Switch or mobile, log the hours you play, update your completion percentage, organize the backlog by status and track achievements. All of it works offline.

Capstone project for **MDI2 117**, San Diego Global Knowledge University.

## Features (Development Phase 1)

| Area | What works |
|---|---|
| **Library** | Summary cards (games, playing, completed, hours), search by title, status filters, game cards with status, hours and a progress bar |
| **Add / Edit game** | Title, platform, genre, year, status, hours, completion slider, 1–5 star rating and notes, with validation |
| **Game details** | Progress card, **Log session** (adds hours and updates progress), **Mark completed**, achievements checklist, notes, play history, delete with confirmation |
| **Empty state** | Explains the app and offers to add a first game or explore a sample library |
| **Persistence** | Everything is stored locally with Room and survives closing the app |

## Tech stack

| | |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose, Material 3 (custom dark theme) |
| Navigation | Navigation Compose |
| Architecture | MVVM: Compose screens → ViewModels (StateFlow) → Repository → Room DAOs |
| Database | Room 2.8 with KSP, schema exported to `app/schemas` |
| Error tracking | Firebase Crashlytics (dependency added; needs `google-services.json` to report) |
| Tests | JUnit 4, Robolectric, Compose UI Test, kotlinx-coroutines-test |
| Min / target SDK | 24 / 37 |

## Project structure

```
app/src/main/java/com/jofielramirez/gamevault/
├── data/
│   ├── model/        Game, PlaySession, Achievement, enums and query results
│   ├── local/        GameDao, PlaySessionDao, AchievementDao, GameVaultDatabase
│   ├── repository/   GameRepository (interface) + OfflineGameRepository
│   └── SampleLibrary.kt
├── ui/
│   ├── library/      Library screen + ViewModel
│   ├── form/         Add / Edit screen + ViewModel
│   ├── details/      Game details screen + ViewModel
│   ├── components/   Shared composables (cover, status chip, progress bar, stars…)
│   ├── navigation/   NavHost and routes
│   └── theme/        Colors, typography and shapes
├── AppContainer.kt   Manual dependency injection
└── GameVaultApplication.kt
```

## Data model

```
games (1) ──< (many) play_sessions     ON DELETE CASCADE
games (1) ──< (many) achievements      ON DELETE CASCADE
```

- **Game**: title, platform, genre, release year, rating, hours played, completion %, status, cover URI, notes, date added and last updated.
- **PlaySession**: one block of play time with its date, duration and the progress reached.
- **Achievement**: a milestone tracked manually, with unlocked flag and date.

## Build and run

1. Open the project folder in Android Studio (**File → Open**).
2. Let Gradle sync finish.
3. Run the `app` configuration on an emulator or device (Android 7.0+).
4. On first launch, tap **Explore with sample games** to load a demo library.

## Tests

```bash
./gradlew testDebugUnitTest            # 39 JVM tests: models, Room database, repository, Compose UI (Robolectric)
./gradlew connectedDebugAndroidTest    # instrumented tests (needs a running emulator or device)
```

## Roadmap

- Statistics screen with charts by platform and status
- Sorting options and filters by platform and genre
- Cover images (photo picker + Coil) and game search through an external API
- Adaptive layouts for tablets and landscape
- DataStore preferences (theme, default sort) and optional Firebase sync
