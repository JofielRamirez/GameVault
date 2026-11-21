package com.jofielramirez.gamevault.data

import com.jofielramirez.gamevault.data.model.Achievement
import com.jofielramirez.gamevault.data.model.Game
import com.jofielramirez.gamevault.data.model.GameStatus
import com.jofielramirez.gamevault.data.model.PlaySession
import com.jofielramirez.gamevault.data.model.Platform
import com.jofielramirez.gamevault.data.repository.GameRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Demo library offered from the empty state, so a new user (or a reviewer)
 * can explore the app without typing ten games first.
 */
object SampleLibrary {

    private const val DAY = 24L * 60 * 60 * 1000
    private const val RAWG_MEDIA = "https://media.rawg.io/media/games/"

    /** Two quick taps on "Explore with sample games" used to load the library twice. */
    private val loading = Mutex()

    /**
     * Adds the demo games, unless the library already has games. Genres, covers and ids
     * are the ones the RAWG search returns, so the samples behave like games added by the user.
     */
    suspend fun populate(repository: GameRepository, now: Long = System.currentTimeMillis()): Unit = loading.withLock {
        if (repository.observeSummary().first().totalGames > 0) return@withLock
        repository.addGame(Game(title = "Hollow Knight", platform = Platform.NINTENDO_SWITCH, genre = "Platformer", releaseYear = 2017, rawgId = 9767, coverUri = RAWG_MEDIA + "4cf/4cfc6b7f1850590a4634b08bfab308ab.jpg"))
        repository.addGame(Game(title = "Cyberpunk 2077", platform = Platform.PLAYSTATION, genre = "Shooter", releaseYear = 2020, rawgId = 41494, coverUri = RAWG_MEDIA + "26d/26d4437715bee60138dab4a7c8c59c92.jpg", rating = 2, hoursPlayed = 6.0, completionPercent = 15, status = GameStatus.DROPPED, notes = "Stopped after the prologue; maybe I will retry with the expansion."))
        repository.addGame(Game(title = "Halo Infinite", platform = Platform.XBOX, genre = "Shooter", releaseYear = 2021, rawgId = 58751, coverUri = RAWG_MEDIA + "e1f/e1ffbeb1bac25b19749ad285ca29e158.jpg", rating = 3, hoursPlayed = 12.0, completionPercent = 30, status = GameStatus.ON_HOLD))
        repository.addGame(Game(title = "Celeste", platform = Platform.PC, genre = "Platformer", releaseYear = 2018, rawgId = 22121, coverUri = RAWG_MEDIA + "594/59487800889ebac294c7c2c070d02356.jpg", rating = 4, hoursPlayed = 9.5, completionPercent = 100, status = GameStatus.COMPLETED))
        repository.addGame(Game(title = "Hades", platform = Platform.PC, genre = "Adventure", releaseYear = 2020, rawgId = 274755, coverUri = RAWG_MEDIA + "1f4/1f47a270b8f241e4676b14d39ec620f7.jpg", rating = 5, hoursPlayed = 60.0, completionPercent = 100, status = GameStatus.COMPLETED, notes = "Every run tells a bit more of the story. Best soundtrack."))
        repository.addGame(Game(title = "Elden Ring", platform = Platform.PLAYSTATION, genre = "Action", releaseYear = 2022, rawgId = 326243, coverUri = RAWG_MEDIA + "b29/b294fdd866dcdb643e7bab370a552855.jpg", rating = 5, hoursPlayed = 82.0, completionPercent = 48, status = GameStatus.PLAYING))

        val zeldaId = repository.addGame(
            Game(
                title = "The Legend of Zelda: Breath of the Wild",
                platform = Platform.NINTENDO_SWITCH,
                genre = "Adventure",
                releaseYear = 2017,
                rawgId = 22511,
                coverUri = RAWG_MEDIA + "cc1/cc196a5ad763955d6532cdba236f730c.jpg",
                rating = 5,
                hoursPlayed = 30.0,
                completionPercent = 55,
                status = GameStatus.PLAYING,
                notes = "Favorite moment: gliding off the Great Plateau for the first time."
            )
        )
        repository.logPlaySession(PlaySession(gameId = zeldaId, playedOn = now - 3 * DAY, durationHours = 2.0, progressAfter = 60))
        repository.logPlaySession(PlaySession(gameId = zeldaId, playedOn = now - DAY, durationHours = 3.0, progressAfter = 65, note = "Freed Vah Medoh"))

        val unlocked = now - 2 * DAY
        repository.addAchievement(Achievement(gameId = zeldaId, title = "Free Vah Ruta", description = "Calm the Divine Beast of the Zora", isUnlocked = true, unlockedAt = unlocked))
        repository.addAchievement(Achievement(gameId = zeldaId, title = "Free Vah Medoh", description = "Calm the Divine Beast of the Rito", isUnlocked = true, unlockedAt = now - DAY))
        repository.addAchievement(Achievement(gameId = zeldaId, title = "Complete 50 shrines"))
        repository.addAchievement(Achievement(gameId = zeldaId, title = "Defeat Calamity Ganon"))
    }
}
