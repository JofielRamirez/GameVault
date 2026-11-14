# GameVault

GameVault is an Android app for gamers who want to keep their whole video game library in one place. You register the games you own on PC, PlayStation, Xbox, Nintendo Switch or mobile, log the hours you play, update your completion percentage, organize the backlog by status and track achievements. Your library is stored on the device and works offline; when you add a game you can search for it online so its cover, year, genre and platform are filled in for you.

Capstone project for **MDI2 117**, San Diego Global Knowledge University.

## Features

| Area | What works |
|---|---|
| **Home** | Dashboard with totals, a "Continue playing" row and recently updated games |
| **Library** | Search by title, status chips, sort (title, most played, progress, recently added) and platform / genre filters |
| **Statistics** | Total playtime, average completion, games by platform and by status, most played |
| **Add / Edit game** | **Search as you type** (RAWG catalog): picking a result fills title, year, genre, platform and cover. Everything can still be typed by hand. Status, hours, completion slider, 1–5 star rating and notes, with validation |
| **Covers** | Game art from the search on Home, Library and Details; initials placeholder when there is no image or no connection |
| **Game details** | Progress card, **Log session** (adds hours and updates progress), **Mark completed**, achievements checklist, notes, play history, delete with confirmation |
| **Empty state** | Explains the app and offers to add a first game or explore a sample library |
| **Persistence** | Everything is stored locally with Room and survives closing the app |

## Tech stack

| | |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose, Material 3 (light theme) |
| Navigation | Navigation Compose |
| Architecture | MVVM: Compose screens → ViewModels (StateFlow) → Repository → Room DAOs |
| Database | Room 2.8 with KSP, schema exported to `app/schemas` (version 2 via AutoMigration) |
| Game search | [RAWG Video Games Database API](https://rawg.io/apidocs) with Retrofit + kotlinx.serialization |
| Images | Coil 3 |
| Error tracking | Firebase Crashlytics (dependency added; needs `google-services.json` to report) |
| Tests | JUnit 4, Robolectric, Compose UI Test, kotlinx-coroutines-test |
| Min / target SDK | 24 / 37 |

## Project structure

```
app/src/main/java/com/jofielramirez/gamevault/
├── data/
│   ├── model/        Game, PlaySession, Achievement, enums and query results
│   ├── local/        GameDao, PlaySessionDao, AchievementDao, GameVaultDatabase
│   ├── remote/       RAWG API (Retrofit), JSON models and RawgMapper
│   ├── repository/   GameRepository + OfflineGameRepository, GameSearchRepository (RAWG)
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

- **Game**: title, platform, genre, release year, rating, hours played, completion %, status, cover URL, RAWG id, notes, date added and last updated.
- **PlaySession**: one block of play time with its date, duration and the progress reached.
- **Achievement**: a milestone tracked manually, with unlocked flag and date.

## Build and run

1. Open the project folder in Android Studio (**File → Open**).
2. *(Optional, for the game search)* Create a free account at [rawg.io](https://rawg.io/apidocs), get an API key and add it to `local.properties` in the project root (this file is not committed):
   ```
   RAWG_API_KEY=your_key_here
   ```
   Without a key the app works the same, but the form says that search is off and every field is typed by hand.
3. Let Gradle sync finish.
4. Run the `app` configuration on an emulator or device (Android 7.0+).
5. On first launch, tap **Explore with sample games** to load a demo library.

## Tests

```bash
./gradlew testDebugUnitTest            # 81 JVM tests: models, Room database and migration, repository, RAWG mapping, search, Compose UI (Robolectric)
./gradlew connectedDebugAndroidTest    # instrumented tests (needs a running emulator or device)
```

## Roadmap

- Adaptive layouts for tablets and landscape
- DataStore preferences (theme, default sort) and optional Firebase sync

Game data and images come from [RAWG](https://rawg.io).
