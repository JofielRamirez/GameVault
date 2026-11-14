package com.jofielramirez.gamevault.data.model

/**
 * Genre names used by the RAWG catalog. Search results already use them; text typed by hand
 * is matched against this list so "rpg" and "RPG" end up as the same filter in the library.
 */
object Genres {
    val standard = listOf(
        "Action", "Adventure", "Arcade", "Board Games", "Card", "Casual", "Educational", "Family",
        "Fighting", "Indie", "Massively Multiplayer", "Platformer", "Puzzle", "Racing", "RPG",
        "Shooter", "Simulation", "Sports", "Strategy"
    )

    /** Trims the text and, when it is a known genre in any capitalization, uses the standard spelling. */
    fun normalize(genre: String): String {
        val trimmed = genre.trim().replace(Regex("\\s+"), " ")
        return standard.firstOrNull { it.equals(trimmed, ignoreCase = true) } ?: trimmed
    }
}
