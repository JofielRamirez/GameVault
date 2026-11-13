package com.jofielramirez.gamevault.data.model

/**
 * Keeps a game's status and completion percentage from contradicting each other.
 *
 * The same rule is used by the Add / Edit form (while the user edits), by the repository
 * (before saving) and by play sessions, so every part of the app agrees.
 *
 * To decide which value wins, it compares against the previous values (null for a new game):
 * 1. Choosing **Completed** fills the progress to 100%.
 * 2. Lowering the progress of a completed game moves it back to **Currently Playing**.
 * 3. Reaching **100%** marks the game as Completed.
 * 4. Making progress on a **Want to Play** game means the user started it → Currently Playing.
 *
 * An explicit status choice is respected otherwise (e.g. re-opening a 100% game to replay it).
 */
object ProgressRules {

    fun reconcile(
        previousStatus: GameStatus?,
        previousProgress: Int?,
        status: GameStatus,
        progress: Int
    ): Pair<GameStatus, Int> {
        val isNew = previousStatus == null || previousProgress == null
        val statusChanged = isNew || status != previousStatus
        val progressChanged = isNew || progress != previousProgress

        return when {
            status == GameStatus.COMPLETED && progress < 100 && statusChanged -> GameStatus.COMPLETED to 100
            status == GameStatus.COMPLETED && progress < 100 -> GameStatus.PLAYING to progress
            progress == 100 && status != GameStatus.COMPLETED && progressChanged -> GameStatus.COMPLETED to 100
            status == GameStatus.WANT_TO_PLAY && progress > 0 && progressChanged -> GameStatus.PLAYING to progress
            else -> status to progress
        }
    }
}

/** Returns this game with [ProgressRules] applied, comparing against [previous] (null for a new game). */
fun Game.reconciledWith(previous: Game?): Game {
    val (newStatus, newProgress) = ProgressRules.reconcile(
        previous?.status, previous?.completionPercent, status, completionPercent
    )
    return if (newStatus == status && newProgress == completionPercent) this
    else copy(status = newStatus, completionPercent = newProgress)
}
