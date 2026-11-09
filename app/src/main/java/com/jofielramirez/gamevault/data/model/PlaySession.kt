package com.jofielramirez.gamevault.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * One block of time the user spent playing a game ("played 2.5 h of Zelda on Saturday,
 * reached 70%"). Sessions keep the history behind [Game.hoursPlayed], which makes
 * "recently played", playtime per month and gaming goals possible.
 */
@Entity(
    tableName = "play_sessions",
    foreignKeys = [
        ForeignKey(
            entity = Game::class,
            parentColumns = ["id"],
            childColumns = ["game_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("game_id"), Index("played_on")]
)
data class PlaySession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "game_id")
    val gameId: Long,
    @ColumnInfo(name = "played_on")
    val playedOn: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "duration_hours")
    val durationHours: Double,
    /** Completion % the user reached in this session, or null if they did not update it. */
    @ColumnInfo(name = "progress_after")
    val progressAfter: Int? = null,
    val note: String = ""
) {
    init {
        require(durationHours > 0.0) { "A session must last more than 0 hours" }
        require(durationHours <= MAX_SESSION_HOURS) { "A session cannot exceed $MAX_SESSION_HOURS hours" }
        require(progressAfter == null || progressAfter in 0..100) { "Progress must be between 0 and 100" }
    }

    companion object {
        const val MAX_SESSION_HOURS = 24.0
    }
}
