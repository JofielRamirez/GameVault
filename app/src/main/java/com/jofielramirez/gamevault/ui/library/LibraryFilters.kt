package com.jofielramirez.gamevault.ui.library

import com.jofielramirez.gamevault.data.model.Game
import com.jofielramirez.gamevault.data.model.GameStatus
import com.jofielramirez.gamevault.data.model.Platform

enum class SortOrder(val label: String) {
    TITLE("Title (A–Z)"),
    PLAYTIME("Most played"),
    PROGRESS("Highest progress"),
    DATE_ADDED("Recently added")
}

/** Everything the user chose to narrow down or reorder the library. */
data class LibraryFilter(
    val status: GameStatus? = null,
    val platform: Platform? = null,
    val genre: String? = null,
    val sort: SortOrder = SortOrder.TITLE
) {
    val hasActiveFilters: Boolean get() = status != null || platform != null || genre != null
}

/** Pure functions so the filtering and sorting rules can be unit tested without Android. */
object LibraryFilters {

    fun apply(games: List<Game>, filter: LibraryFilter): List<Game> {
        val filtered = games.filter { game ->
            (filter.status == null || game.status == filter.status) &&
                (filter.platform == null || game.platform == filter.platform) &&
                (filter.genre == null || game.genre.trim().equals(filter.genre, ignoreCase = true))
        }
        val byTitle = compareBy<Game> { it.title.lowercase() }
        return when (filter.sort) {
            SortOrder.TITLE -> filtered.sortedWith(byTitle)
            SortOrder.PLAYTIME -> filtered.sortedWith(compareByDescending<Game> { it.hoursPlayed }.then(byTitle))
            SortOrder.PROGRESS -> filtered.sortedWith(compareByDescending<Game> { it.completionPercent }.then(byTitle))
            SortOrder.DATE_ADDED -> filtered.sortedWith(compareByDescending<Game> { it.dateAdded }.then(byTitle))
        }
    }

    /** Genres the user actually typed, without duplicates that differ only in capitalization. */
    fun genresOf(games: List<Game>): List<String> =
        games.map { it.genre.trim() }
            .filter { it.isNotEmpty() }
            .distinctBy { it.lowercase() }
            .sortedBy { it.lowercase() }

    /** Only the platforms that appear in the library, in enum order. */
    fun platformsOf(games: List<Game>): List<Platform> =
        Platform.entries.filter { platform -> games.any { it.platform == platform } }
}
