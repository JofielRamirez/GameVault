package com.jofielramirez.gamevault.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A video game in the user's personal library.
 *
 * [hoursPlayed] is the running total for the game. It can be entered directly when a game
 * is added (hours played before using the app) and grows every time a [PlaySession] is logged.
 */
@Entity(
    tableName = "games",
    indices = [Index("status"), Index("platform"), Index("last_updated")]
)
data class Game(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val platform: Platform,
    val genre: String = "",
    @ColumnInfo(name = "release_year")
    val releaseYear: Int? = null,
    /** Personal rating from 1 to 5 stars; 0 means "not rated yet". */
    val rating: Int = 0,
    @ColumnInfo(name = "hours_played")
    val hoursPlayed: Double = 0.0,
    @ColumnInfo(name = "completion_percent")
    val completionPercent: Int = 0,
    val status: GameStatus = GameStatus.WANT_TO_PLAY,
    /** content:// URI of a cover picked from the device, or null for the placeholder. */
    @ColumnInfo(name = "cover_uri")
    val coverUri: String? = null,
    val notes: String = "",
    @ColumnInfo(name = "date_added")
    val dateAdded: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "last_updated")
    val lastUpdated: Long = dateAdded
) {
    init {
        require(title.isNotBlank()) { "A game needs a title" }
        require(rating in 0..MAX_RATING) { "Rating must be between 0 and $MAX_RATING" }
        require(hoursPlayed >= 0.0) { "Hours played cannot be negative" }
        require(completionPercent in 0..100) { "Completion must be between 0 and 100" }
        require(releaseYear == null || releaseYear in FIRST_RELEASE_YEAR..LAST_RELEASE_YEAR) {
            "Release year must be between $FIRST_RELEASE_YEAR and $LAST_RELEASE_YEAR"
        }
    }

    val isCompleted: Boolean get() = status == GameStatus.COMPLETED

    companion object {
        const val MAX_RATING = 5
        const val FIRST_RELEASE_YEAR = 1950
        const val LAST_RELEASE_YEAR = 2100
    }
}
