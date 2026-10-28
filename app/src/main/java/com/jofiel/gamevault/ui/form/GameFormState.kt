package com.jofiel.gamevault.ui.form

import com.jofiel.gamevault.data.Game
import com.jofiel.gamevault.data.GameStatus
import com.jofiel.gamevault.data.Platform

/** Everything the Add / Edit form shows. Hours stay a String so the text field can be empty while typing. */
data class GameFormState(
    val id: Long = 0,
    val title: String = "",
    val platform: Platform = Platform.PC,
    val genre: String = "",
    val status: GameStatus = GameStatus.WANT_TO_PLAY,
    val hoursPlayed: String = "",
    val progress: Int = 0,
    val rating: Int = 0,
    val notes: String = "",
    val createdAt: Long? = null,
) {
    val isEditing: Boolean get() = id != 0L
}

data class GameFormErrors(
    val title: String? = null,
    val hours: String? = null,
) {
    val hasErrors: Boolean get() = title != null || hours != null
}

/** Pure validation rules, kept out of the ViewModel so they can be unit-tested on the JVM. */
object GameFormValidator {
    const val MAX_TITLE_LENGTH = 100
    const val MAX_HOURS = 99_999

    fun validate(state: GameFormState): GameFormErrors {
        val title = state.title.trim()
        val titleError = when {
            title.isEmpty() -> "Title is required"
            title.length > MAX_TITLE_LENGTH -> "Title must be $MAX_TITLE_LENGTH characters or less"
            else -> null
        }

        val hoursText = state.hoursPlayed.trim()
        val hours = hoursText.toIntOrNull()
        val hoursError = when {
            hoursText.isEmpty() -> null // empty means 0 hours
            hours == null -> "Enter a whole number of hours"
            hours < 0 -> "Hours can't be negative"
            hours > MAX_HOURS -> "Hours must be $MAX_HOURS or less"
            else -> null
        }

        return GameFormErrors(title = titleError, hours = hoursError)
    }
}

fun GameFormState.toGame(now: Long = System.currentTimeMillis()): Game = Game(
    id = id,
    title = title.trim(),
    platform = platform,
    genre = genre.trim(),
    status = status,
    hoursPlayed = hoursPlayed.trim().toIntOrNull() ?: 0,
    progress = progress.coerceIn(0, 100),
    rating = rating.coerceIn(0, 5),
    notes = notes.trim(),
    createdAt = createdAt ?: now,
    updatedAt = now,
)

fun Game.toFormState(): GameFormState = GameFormState(
    id = id,
    title = title,
    platform = platform,
    genre = genre,
    status = status,
    hoursPlayed = hoursPlayed.toString(),
    progress = progress,
    rating = rating,
    notes = notes,
    createdAt = createdAt,
)
