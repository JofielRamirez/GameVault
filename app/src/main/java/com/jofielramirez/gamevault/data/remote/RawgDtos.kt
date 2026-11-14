package com.jofielramirez.gamevault.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/*
 * Only the JSON fields GameVault needs. Unknown fields are ignored and null lists fall back
 * to empty (see RawgClient.json), because RAWG data is community-edited and not every game
 * has a release date, an image or a genre.
 */

@Serializable
data class RawgSearchResponse(
    val count: Int = 0,
    val results: List<RawgGameDto> = emptyList()
)

@Serializable
data class RawgGameDto(
    val id: Int,
    val name: String,
    /** "2017-03-03", or null when RAWG does not know the date. */
    val released: String? = null,
    @SerialName("background_image")
    val backgroundImage: String? = null,
    val genres: List<RawgNamedDto> = emptyList(),
    @SerialName("parent_platforms")
    val parentPlatforms: List<RawgPlatformEntryDto> = emptyList()
)

@Serializable
data class RawgNamedDto(
    val id: Int = 0,
    val name: String,
    val slug: String = ""
)

@Serializable
data class RawgPlatformEntryDto(val platform: RawgNamedDto)
