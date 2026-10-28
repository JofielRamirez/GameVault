# GameVault

GameVault is a personal video game library and progress tracker for Android. Gamers register the games they own across PC, PlayStation, Xbox and Nintendo Switch, record hours played and completion percentage, and organize their backlog by status — all in one place.

Capstone project for **MDI2-118** (Android Mobile Development Immersive, San Diego Global Knowledge University).

## Current stage: MVP

This first version follows a strict MVP boundary:

1. **Compose screens for list and form** — a Library list and one Add / Edit form.
2. **Local persistence with Room** — no accounts, no backend.
3. **Tested on an emulator** — create, edit, delete and relaunch.

> Remote sync is added only when a real user story needs it.

### In scope (v0.1)

| Feature | Where |
| --- | --- |
| Library list with title, platform, genre, status, hours and a progress bar | `ui/library/LibraryScreen.kt` |
| Empty state for a new library | `ui/library/LibraryScreen.kt` |
| Add a game: title (required), platform, genre, status, hours, completion %, rating, notes | `ui/form/GameFormScreen.kt` |
| Edit any game from the list (same form, pre-filled) | `ui/form/GameFormScreen.kt` |
| Delete with confirmation | `ui/form/GameFormScreen.kt` |
| Form validation (required title, valid hours) | `ui/form/GameFormState.kt` |
| Data survives closing the app | `data/AppDatabase.kt` (Room) |

### Not yet (next iterations)

- Game Details screen with quick +1 h / progress updates
- Dashboard with totals and recently updated games
- Statistics screen (by platform, by status, most played)
- Search, filters and sorting
- Cover images (Photo Picker + Coil)
- Wishlist, goals and achievements
- Accounts and cloud sync (Firebase / Supabase) — only when a user story requires it

## Tech stack

| | |
| --- | --- |
| Language | Kotlin 2.0 |
| UI | Jetpack Compose + Material 3 |
| Navigation | Navigation Compose |
| Architecture | MVVM (ViewModel + StateFlow / Compose state) + Repository |
| Database | Room 2.6 (KSP) |
| Tests | JUnit 4, Room instrumented tests, Compose UI tests |
| Min / target SDK | 26 / 35 |

## Architecture

```
UI (Compose)            LibraryScreen ─────────── GameFormScreen
                             │                         │
ViewModel               LibraryViewModel          GameFormViewModel
                             │                         │
Repository                   └──────── GameRepository ─┘
                                             │
Room                              GameDao  →  AppDatabase (gamevault.db)
```

- The DAO returns `Flow<List<Game>>`, so the library refreshes by itself after every create, edit or delete.
- `GameVaultApp` creates the database and repository once (manual dependency injection, no Hilt yet).
- `AppViewModelProvider` builds the ViewModels with that shared repository.

```
app/src/main/java/com/jofiel/gamevault/
├── GameVaultApp.kt          # Application: database + repository
├── MainActivity.kt
├── data/                    # Game entity, enums, DAO, database, repository
├── navigation/              # NavHost: library ⇄ form?gameId=
└── ui/
    ├── library/             # list screen + ViewModel
    ├── form/                # add / edit screen, ViewModel, validation
    ├── components/          # DropdownField
    └── theme/               # dark-first purple/blue theme
```

## Getting started

1. Clone the repository:
   ```bash
   git clone https://github.com/JofielRamirez/GameVault.git
   ```
2. Open the folder in **Android Studio** (Ladybug or newer) and let Gradle sync.
3. Start an emulator (API 26+) and press **Run ▶**.

## Testing

```bash
./gradlew test                    # JVM unit tests (validation, mapping)
./gradlew connectedAndroidTest    # Room + UI tests on a running emulator
```

The manual emulator checklist (create, edit, delete, relaunch) is in [`docs/MVP_TEST_CHECKLIST.md`](docs/MVP_TEST_CHECKLIST.md).

## Author

Jofiel Oryares Ramírez Astorga — Software Engineering, UABC FCITEC
