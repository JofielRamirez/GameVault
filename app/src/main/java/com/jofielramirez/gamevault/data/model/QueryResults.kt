package com.jofielramirez.gamevault.data.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Relation

/** Everything the Game Details screen needs, loaded in one transaction. */
data class GameDetails(
    @Embedded val game: Game,
    @Relation(parentColumn = "id", entityColumn = "game_id")
    val sessions: List<PlaySession>,
    @Relation(parentColumn = "id", entityColumn = "game_id")
    val achievements: List<Achievement>
) {
    val unlockedAchievements: Int get() = achievements.count { it.isUnlocked }
}

/** Totals shown on the dashboard cards. */
data class LibrarySummary(
    @ColumnInfo(name = "total_games") val totalGames: Int,
    @ColumnInfo(name = "playing_games") val playingGames: Int,
    @ColumnInfo(name = "completed_games") val completedGames: Int,
    @ColumnInfo(name = "total_hours") val totalHours: Double,
    @ColumnInfo(name = "average_completion") val averageCompletion: Double
)

/** One bar of the "games per platform" chart. */
data class PlatformCount(
    val platform: Platform,
    val count: Int
)

/** One slice of the "games per status" chart. */
data class StatusCount(
    val status: GameStatus,
    val count: Int
)
