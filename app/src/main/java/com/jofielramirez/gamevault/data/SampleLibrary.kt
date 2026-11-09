package com.jofielramirez.gamevault.data

import com.jofielramirez.gamevault.data.model.Achievement
import com.jofielramirez.gamevault.data.model.Game
import com.jofielramirez.gamevault.data.model.GameStatus
import com.jofielramirez.gamevault.data.model.PlaySession
import com.jofielramirez.gamevault.data.model.Platform
import com.jofielramirez.gamevault.data.repository.GameRepository

/**
 * Demo library offered from the empty state, so a new user (or a reviewer)
 * can explore the app without typing ten games first.
 */
object SampleLibrary {

    private const val DAY = 24L * 60 * 60 * 1000

    suspend fun populate(repository: GameRepository, now: Long = System.currentTimeMillis()) {
        repository.addGame(Game(title = "Hollow Knight", platform = Platform.NINTENDO_SWITCH, genre = "Metroidvania", releaseYear = 2017))
        repository.addGame(Game(title = "Cyberpunk 2077", platform = Platform.PLAYSTATION, genre = "RPG", releaseYear = 2020, rating = 2, hoursPlayed = 6.0, completionPercent = 15, status = GameStatus.DROPPED, notes = "Stopped after the prologue; maybe I will retry with the expansion."))
        repository.addGame(Game(title = "Halo Infinite", platform = Platform.XBOX, genre = "Shooter", releaseYear = 2021, rating = 3, hoursPlayed = 12.0, completionPercent = 30, status = GameStatus.ON_HOLD))
        repository.addGame(Game(title = "Celeste", platform = Platform.PC, genre = "Platformer", releaseYear = 2018, rating = 4, hoursPlayed = 9.5, completionPercent = 100, status = GameStatus.COMPLETED))
        repository.addGame(Game(title = "Hades", platform = Platform.PC, genre = "Roguelike", releaseYear = 2020, rating = 5, hoursPlayed = 60.0, completionPercent = 100, status = GameStatus.COMPLETED, notes = "Every run tells a bit more of the story. Best soundtrack."))
        repository.addGame(Game(title = "Elden Ring", platform = Platform.PLAYSTATION, genre = "Action RPG", releaseYear = 2022, rating = 5, hoursPlayed = 82.0, completionPercent = 48, status = GameStatus.PLAYING))

        val zeldaId = repository.addGame(
            Game(
                title = "The Legend of Zelda: Breath of the Wild",
                platform = Platform.NINTENDO_SWITCH,
                genre = "Action-adventure",
                releaseYear = 2017,
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
