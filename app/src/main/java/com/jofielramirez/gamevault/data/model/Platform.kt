package com.jofielramirez.gamevault.data.model

/** Platform a game is played on. Stored by [name] (TEXT column). */
enum class Platform(val displayName: String) {
    PC("PC"),
    PLAYSTATION("PlayStation"),
    XBOX("Xbox"),
    NINTENDO_SWITCH("Nintendo Switch"),
    MOBILE("Mobile"),
    OTHER("Other")
}
