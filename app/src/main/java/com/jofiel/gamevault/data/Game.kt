package com.jofiel.gamevault.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A game in the user's personal library.
 *
 * MVP scope: everything needed to track a game locally. Cover images,
 * achievements and cloud sync are intentionally left for later iterations.
 */
@Entity(tableName = "games")
data class Game(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val platform: Platform,
    val genre: String = "",
    val status: GameStatus = GameStatus.WANT_TO_PLAY,
    val hoursPlayed: Int = 0,
    /** Completion percentage, 0..100. */
    val progress: Int = 0,
    /** Personal rating, 0..5 (0 = not rated). */
    val rating: Int = 0,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)

enum class Platform(val label: String) {
    PC("PC"),
    PLAYSTATION("PlayStation"),
    XBOX("Xbox"),
    NINTENDO_SWITCH("Nintendo Switch"),
    MOBILE("Mobile"),
    OTHER("Other"),
}

enum class GameStatus(val label: String) {
    WANT_TO_PLAY("Want to Play"),
    PLAYING("Playing"),
    COMPLETED("Completed"),
    ON_HOLD("On Hold"),
    DROPPED("Dropped"),
}
