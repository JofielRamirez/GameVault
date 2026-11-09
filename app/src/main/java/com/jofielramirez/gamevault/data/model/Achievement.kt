package com.jofielramirez.gamevault.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * An in-game achievement or personal milestone the user tracks manually
 * ("Defeat Ganon", "Find all 120 shrines").
 */
@Entity(
    tableName = "achievements",
    foreignKeys = [
        ForeignKey(
            entity = Game::class,
            parentColumns = ["id"],
            childColumns = ["game_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("game_id")]
)
data class Achievement(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "game_id")
    val gameId: Long,
    val title: String,
    val description: String = "",
    @ColumnInfo(name = "is_unlocked")
    val isUnlocked: Boolean = false,
    /** When it was unlocked (epoch millis); must be set only for unlocked achievements. */
    @ColumnInfo(name = "unlocked_at")
    val unlockedAt: Long? = null
) {
    init {
        require(title.isNotBlank()) { "An achievement needs a title" }
        require(isUnlocked == (unlockedAt != null)) {
            "unlockedAt must be set if and only if the achievement is unlocked"
        }
    }

    fun unlock(at: Long = System.currentTimeMillis()): Achievement =
        copy(isUnlocked = true, unlockedAt = at)

    fun lock(): Achievement = copy(isUnlocked = false, unlockedAt = null)
}
