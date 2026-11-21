# GameVault

GameVault is an Android app for gamers who want their whole video game library in one place. You add the games you own or want to play on PC, PlayStation, Xbox, Nintendo Switch or mobile. Then you log the hours you play, update your completion percentage, organize the backlog by status, track achievements and see statistics about your gaming.

To add a game you just search its name. The app finds it in the RAWG catalog and fills in the cover, year, genre and platform for you. Your library is stored on the phone, needs no account and works offline.

Capstone project for **MDI2 117 – MDI2 120** at San Diego Global Knowledge University, by **Jofiel Ramírez**.

## Features

| Area | What it does |
|---|---|
| **Home** | Totals (games, hours, playing, completed), a "Continue playing" row with covers, recently updated games, and a shortcut to Settings |
| **Library** | Search by title, status chips, sorting (title, most played, highest progress, recently added) and filters by platform and genre. It uses one column on phones and a grid on tablets |
| **Add / Edit game** | **Search as you type** in the RAWG catalog: picking a result fills in the title, year, genre, platform and cover. You can also choose a cover from the gallery or type everything by hand. Includes validation, a status/progress rule and duplicate warnings ("Already in your library") |
| **Game details** | Cover, progress card, **Log session** (adds hours and updates progress), **Complete**, achievements checklist, notes, play history and delete with confirmation |
| **Statistics** | Total playtime, average completion, games by platform, status and genre, hours played per month and most played games |
| **Settings** | Theme (system / light / dark), default library order, load sample games, delete all data, crash-report opt-out and an About section |
| **Quality** | Firebase Crashlytics, StrictMode in debug builds, R8-optimized signed release build, TalkBack labels, and a layout that adapts to tablets (navigation rail and grid) |

## Tech stack

| | |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose, Material 3 (light and dark themes) |
| Architecture | MVVM: Compose screens → ViewModels (StateFlow) → repositories → Room / RAWG API |
| Navigation | Navigation Compose (bottom bar on phones, navigation rail on tablets) |
| Local data | Room 2.8 (schema v2 with AutoMigration), DataStore Preferences for settings |
| Network | Retrofit 3 + kotlinx.serialization (RAWG Video Games Database API), Coil 3 for images |
| Error tracking | Firebase Crashlytics |
| Tests | JUnit 4, Robolectric, Compose UI Test, kotlinx-coroutines-test, AndroidX Test (instrumented) |
| Min / target SDK | 24 (Android 7.0) / 37 |

## Project structure

```
app/src/main/java/com/jofielramirez/gamevault/
├── data/
│   ├── model/         Game, PlaySession, Achievement, enums, ProgressRules, Genres
│   ├── local/         Room DAOs and GameVaultDatabase
│   ├── remote/        RAWG API (Retrofit), JSON models, RawgMapper
│   ├── repository/    GameRepository (Room) and GameSearchRepository (RAWG)
│   ├── preferences/   Settings stored with DataStore
│   ├── CoverStorage.kt  Covers picked from the gallery
│   └── SampleLibrary.kt Demo games
├── ui/
│   ├── home/  library/  form/  details/  stats/  settings/   Screens and ViewModels
│   ├── components/    Shared composables (cover, chips, charts, adaptive layout)
│   ├── navigation/    NavHost, bottom bar and navigation rail
│   └── theme/         Colors, typography and shapes
├── AppContainer.kt      Manual dependency injection
├── CrashReporter.kt     Crashlytics wrapper
└── GameVaultApplication.kt
```

## How to run the project

**Requirements:** Android Studio (2025 or newer) with the Android SDK, and an emulator or Android phone (Android 7.0 or newer).

1. Open the project folder in Android Studio (**File → Open**) and wait for Gradle sync to finish.
2. Optional, for the game search: create a free account at [rawg.io/apidocs](https://rawg.io/apidocs), get an API key and add it to `local.properties` in the project root:
   ```
   RAWG_API_KEY=your_key_here
   ```
   Without a key everything works, but the form says the search is off and the fields are typed by hand.
3. Optional, for crash reports: put your Firebase project's `google-services.json` in the `app/` folder. Without it the app builds the same, just without Crashlytics.
4. Select the `app` configuration and press **Run** (or `./gradlew installDebug`).
5. On first launch, tap **Explore with sample games** to load a demo library, or **Add your first game**.

### Release build

`./gradlew assembleRelease` builds a minified, signed APK (`app/build/outputs/apk/release/app-release.apk`) when a `keystore.properties` file exists in the project root with `storeFile`, `storePassword`, `keyAlias` and `keyPassword`. The key and its passwords are not stored in Git.

## Tests

```bash
./gradlew testDebugUnitTest            # 103 JVM tests: unit, integration (full app flows) and Compose UI (Robolectric)
./gradlew connectedDebugAndroidTest    # instrumented tests: dialogs and end-to-end flows (emulator or device)
```

The integration tests run the whole app with an in-memory database and a fake RAWG catalog, so they need neither internet nor an API key.

## Data model

```
games (1) ──< (many) play_sessions     ON DELETE CASCADE
games (1) ──< (many) achievements      ON DELETE CASCADE
```

- **Game**: title, platform, genre, release year, rating, hours played, completion %, status, cover, RAWG id, notes and dates.
- **PlaySession**: one block of play time with its date, duration and the progress reached.
- **Achievement**: a milestone tracked by hand, with an unlocked flag and date.

Game data and images come from [RAWG](https://rawg.io).
