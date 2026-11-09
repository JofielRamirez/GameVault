package com.jofielramirez.gamevault.data.model

/**
 * Backlog status of a game. Room stores enums by their [name] (TEXT column),
 * so renaming a constant requires a database migration.
 */
enum class GameStatus(val displayName: String) {
    WANT_TO_PLAY("Want to Play"),
    PLAYING("Currently Playing"),
    COMPLETED("Completed"),
    ON_HOLD("On Hold"),
    DROPPED("Dropped")
}
