package com.jofielramirez.gamevault.data.remote

import com.jofielramirez.gamevault.data.model.Platform

/** A search result ready to fill the Add / Edit form. */
data class GameSuggestion(
    val rawgId: Int,
    val title: String,
    val releaseYear: Int?,
    val genre: String,
    /** Platforms the game was released on, mapped to GameVault's own list (no duplicates). */
    val platforms: List<Platform>,
    val coverUrl: String?
)
