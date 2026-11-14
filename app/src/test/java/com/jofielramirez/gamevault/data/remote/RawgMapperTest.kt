package com.jofielramirez.gamevault.data.remote

import com.jofielramirez.gamevault.data.model.Genres
import com.jofielramirez.gamevault.data.model.Platform
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Parsing and mapping of RAWG search results (plain JVM, no network). */
class RawgMapperTest {

    // Shortened copy of a real /api/games?search= response, with extra fields RAWG sends
    private val searchJson = """
        {
          "count": 2,
          "next": null,
          "results": [
            {
              "id": 22511,
              "slug": "the-legend-of-zelda-breath-of-the-wild",
              "name": "The Legend of Zelda: Breath of the Wild",
              "released": "2017-03-03",
              "background_image": "https://media.rawg.io/media/games/cc1/zelda.jpg",
              "rating": 4.5,
              "genres": [{"id": 4, "name": "Action", "slug": "action"}, {"id": 3, "name": "Adventure", "slug": "adventure"}],
              "parent_platforms": [{"platform": {"id": 7, "name": "Nintendo", "slug": "nintendo"}}]
            },
            {
              "id": 9767,
              "name": "Hollow Knight",
              "released": null,
              "background_image": null,
              "genres": null,
              "parent_platforms": [
                {"platform": {"id": 1, "name": "PC", "slug": "pc"}},
                {"platform": {"id": 5, "name": "Apple Macintosh", "slug": "mac"}},
                {"platform": {"id": 7, "name": "Nintendo", "slug": "nintendo"}},
                {"platform": {"id": 2, "name": "PlayStation", "slug": "playstation"}},
                {"platform": {"id": 3, "name": "Xbox", "slug": "xbox"}}
              ]
            }
          ]
        }
    """.trimIndent()

    private fun parse(): List<GameSuggestion> =
        RawgClient.json.decodeFromString<RawgSearchResponse>(searchJson).results.map(RawgMapper::toSuggestion)

    @Test
    fun searchResponse_isMappedToSuggestions() {
        val zelda = parse().first()

        assertEquals(22511, zelda.rawgId)
        assertEquals("The Legend of Zelda: Breath of the Wild", zelda.title)
        assertEquals(2017, zelda.releaseYear)
        assertEquals("Action", zelda.genre)
        assertEquals(listOf(Platform.NINTENDO_SWITCH), zelda.platforms)
        assertEquals("https://media.rawg.io/media/games/cc1/zelda.jpg", zelda.coverUrl)
    }

    @Test
    fun missingDateImageAndGenres_doNotBreakParsing() {
        val hollowKnight = parse()[1]

        assertNull(hollowKnight.releaseYear)
        assertNull(hollowKnight.coverUrl)
        assertEquals("", hollowKnight.genre)
    }

    @Test
    fun platforms_areMappedWithoutDuplicatesInAppOrder() {
        // pc + mac both become PC; result follows the Platform enum order
        assertEquals(
            listOf(Platform.PC, Platform.PLAYSTATION, Platform.XBOX, Platform.NINTENDO_SWITCH),
            parse()[1].platforms
        )
        assertEquals(
            listOf(Platform.MOBILE, Platform.OTHER),
            RawgMapper.platforms(listOf("ios", "android", "sega"))
        )
    }

    @Test
    fun mainGenre_skipsIndieWhenThereIsAGameplayGenre() {
        assertEquals("Platformer", RawgMapper.mainGenre(listOf("Indie", "Platformer", "Action")))
        assertEquals("Indie", RawgMapper.mainGenre(listOf("Indie")))
        assertEquals("", RawgMapper.mainGenre(emptyList()))
    }

    @Test
    fun releaseYear_ignoresUnknownOrInvalidDates() {
        assertEquals(1998, RawgMapper.releaseYear("1998-11-21"))
        assertNull(RawgMapper.releaseYear(null))
        assertNull(RawgMapper.releaseYear("TBA"))
        assertNull(RawgMapper.releaseYear("1890-01-01"))
    }

    @Test
    fun thumbnailUrl_requestsASmallerCropFromRawgOnly() {
        assertEquals(
            "https://media.rawg.io/media/crop/600/400/games/cc1/zelda.jpg",
            rawgThumbnailUrl("https://media.rawg.io/media/games/cc1/zelda.jpg")
        )
        val alreadySmall = "https://media.rawg.io/media/resize/640/-/games/cc1/zelda.jpg"
        assertEquals(alreadySmall, rawgThumbnailUrl(alreadySmall))
        assertEquals("https://example.com/cover.png", rawgThumbnailUrl("https://example.com/cover.png"))
    }

    @Test
    fun typedGenres_useTheStandardSpelling() {
        assertEquals("RPG", Genres.normalize("  rpg "))
        assertEquals("Board Games", Genres.normalize("board   games"))
        assertEquals("Metroidvania", Genres.normalize("Metroidvania"))
    }
}
