# MVP test checklist (emulator)

Run these on an Android emulator (API 26+) before calling the MVP done.
Mark each step with ✅ or ❌ and note anything unexpected.

## Automated tests

| Command | What it checks |
| --- | --- |
| `./gradlew test` | Form validation and Game ↔ form mapping (JVM, no emulator needed). |
| `./gradlew connectedAndroidTest` | Room create / edit / delete, data surviving a database close + reopen, and the full create → edit → delete flow through the real UI. Needs a running emulator. |

In Android Studio you can also right-click `app/src/androidTest` → **Run 'All Tests'**.

## Manual checks

### 1. Create
- [ ] Fresh install shows the empty state ("Your library is empty").
- [ ] Tap **Add game**, leave the title empty and tap **Save game** → "Title is required" appears and nothing is saved.
- [ ] Fill in title, platform, genre, status, hours, completion, rating and notes → **Save game**.
- [ ] The game appears at the top of the library with the right platform, status, hours and progress bar.
- [ ] The header count ("N games · H h played") goes up.

### 2. Edit
- [ ] Tap the game → the form opens with every field filled in.
- [ ] Change hours and completion → **Save changes** → the card shows the new values and moves to the top.
- [ ] Set status to **Completed** → completion jumps to 100%.
- [ ] Tap the back arrow without saving → nothing changes.

### 3. Delete
- [ ] Open a game → trash icon → **Cancel** → the game is still there.
- [ ] Open it again → trash icon → **Delete** → the game disappears from the library.

### 4. Relaunch (persistence)
- [ ] Add at least two games.
- [ ] Close the app completely: Recents → swipe it away, **or** Settings → Apps → GameVault → **Force stop**.
- [ ] Open GameVault again → every game is still there with the same data.
- [ ] Rotate the screen while on the form with unsaved text → the text is kept.

## Results

| Date | Emulator / API | Result | Notes |
| --- | --- | --- | --- |
|  |  |  |  |
