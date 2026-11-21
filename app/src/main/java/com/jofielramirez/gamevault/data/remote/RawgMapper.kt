package com.jofielramirez.gamevault.data.remote

import com.jofielramirez.gamevault.data.model.Game
import com.jofielramirez.gamevault.data.model.Platform

/** Turns RAWG's JSON objects into [GameSuggestion]s. Pure functions, so they are unit tested. */
object RawgMapper {

    fun toSuggestion(dto: RawgGameDto): GameSuggestion = GameSuggestion(
        rawgId = dto.id,
        title = dto.name.trim(),
        releaseYear = releaseYear(dto.released),
        genre = mainGenre(dto.genres.map { it.name }),
        platforms = platforms(dto.parentPlatforms.map { it.platform.slug }, dto.platforms.map { it.platform.slug }),
        coverUrl = dto.backgroundImage?.takeIf { it.isNotBlank() }
    )

    /** "2017-03-03" -> 2017. Unknown or out-of-range dates give null so the form stays valid. */
    fun releaseYear(released: String?): Int? =
        released?.take(4)?.toIntOrNull()?.takeIf { it in Game.FIRST_RELEASE_YEAR..Game.LAST_RELEASE_YEAR }

    /**
     * RAWG lists several genres per game ("Action", "Indie", "Platformer"). The library filter
     * works with one genre per game, so the first one is used, skipping labels that describe
     * the studio or the audience ("Indie", "Casual"...) rather than the gameplay when possible.
     */
    fun mainGenre(genres: List<String>): String {
        val names = genres.map { it.trim() }.filter { it.isNotEmpty() }
        return names.firstOrNull { it.lowercase() !in NON_GAMEPLAY_GENRES } ?: names.firstOrNull().orEmpty()
    }

    /**
     * RAWG "parent platform" slugs -> GameVault platforms, in GameVault's order.
     * RAWG groups every Nintendo console as "nintendo"; when the exact consoles are known,
     * only a Switch release counts as Nintendo Switch and older consoles (Wii, 3DS) become Other.
     */
    fun platforms(slugs: List<String>, detailedSlugs: List<String> = emptyList()): List<Platform> {
        val onSwitch = detailedSlugs.isEmpty() || detailedSlugs.any { it.lowercase().startsWith("nintendo-switch") }
        val mapped = slugs.map { slug ->
            when (slug.lowercase()) {
                "pc", "mac", "linux" -> Platform.PC
                "playstation" -> Platform.PLAYSTATION
                "xbox" -> Platform.XBOX
                "nintendo" -> if (onSwitch) Platform.NINTENDO_SWITCH else Platform.OTHER
                "ios", "android" -> Platform.MOBILE
                else -> Platform.OTHER
            }
        }.toSet()
        return Platform.entries.filter { it in mapped }
    }

    private val NON_GAMEPLAY_GENRES = setOf("indie", "casual", "family", "educational", "massively multiplayer")
}

/**
 * RAWG's original images are full-HD backgrounds. Its media server can return a smaller crop,
 * which loads much faster in lists: .../media/games/x.jpg -> .../media/crop/600/400/games/x.jpg
 * Any other URL (or one that is already resized) is returned unchanged.
 */
fun rawgThumbnailUrl(url: String): String {
    val marker = "media.rawg.io/media/"
    val index = url.indexOf(marker)
    if (index < 0) return url
    val rest = url.substring(index + marker.length)
    if (rest.startsWith("crop/") || rest.startsWith("resize/")) return url
    return url.substring(0, index + marker.length) + "crop/600/400/" + rest
}
