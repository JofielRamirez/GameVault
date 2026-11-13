package com.jofielramirez.gamevault.ui.library

import com.jofielramirez.gamevault.data.model.Game
import com.jofielramirez.gamevault.data.model.GameStatus
import com.jofielramirez.gamevault.data.model.Platform
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LibraryFiltersTest {

    private val zelda = Game(id = 1, title = "Zelda", platform = Platform.NINTENDO_SWITCH, genre = "Adventure",
        hoursPlayed = 35.0, completionPercent = 65, status = GameStatus.PLAYING, dateAdded = 300)
    private val hades = Game(id = 2, title = "hades", platform = Platform.PC, genre = "Roguelike",
        hoursPlayed = 60.0, completionPercent = 100, status = GameStatus.COMPLETED, dateAdded = 100)
    private val celeste = Game(id = 3, title = "Celeste", platform = Platform.PC, genre = "platformer ",
        hoursPlayed = 9.5, completionPercent = 100, status = GameStatus.COMPLETED, dateAdded = 200)
    private val games = listOf(zelda, hades, celeste)

    private fun titles(filter: LibraryFilter) = LibraryFilters.apply(games, filter).map { it.title }

    @Test
    fun sortByTitle_ignoresCapitalization() {
        assertEquals(listOf("Celeste", "hades", "Zelda"), titles(LibraryFilter(sort = SortOrder.TITLE)))
    }

    @Test
    fun sortByPlaytime_putsTheMostPlayedFirst() {
        assertEquals(listOf("hades", "Zelda", "Celeste"), titles(LibraryFilter(sort = SortOrder.PLAYTIME)))
    }

    @Test
    fun sortByProgress_breaksTiesByTitle() {
        assertEquals(listOf("Celeste", "hades", "Zelda"), titles(LibraryFilter(sort = SortOrder.PROGRESS)))
    }

    @Test
    fun sortByDateAdded_putsTheNewestFirst() {
        assertEquals(listOf("Zelda", "Celeste", "hades"), titles(LibraryFilter(sort = SortOrder.DATE_ADDED)))
    }

    @Test
    fun filters_combineStatusPlatformAndGenre() {
        assertEquals(listOf("Celeste", "hades"), titles(LibraryFilter(platform = Platform.PC)))
        assertEquals(listOf("Celeste"), titles(LibraryFilter(platform = Platform.PC, genre = "Platformer")))
        assertEquals(listOf("Zelda"), titles(LibraryFilter(status = GameStatus.PLAYING)))
        assertTrue(titles(LibraryFilter(status = GameStatus.PLAYING, platform = Platform.PC)).isEmpty())
    }

    @Test
    fun availableOptions_comeFromTheLibrary() {
        assertEquals(listOf(Platform.PC, Platform.NINTENDO_SWITCH), LibraryFilters.platformsOf(games))
        assertEquals(listOf("Adventure", "platformer", "Roguelike"), LibraryFilters.genresOf(games))
    }

    @Test
    fun hasActiveFilters_ignoresTheSortOrder() {
        assertFalse(LibraryFilter(sort = SortOrder.PLAYTIME).hasActiveFilters)
        assertTrue(LibraryFilter(genre = "RPG").hasActiveFilters)
    }
}
