package com.jofielramirez.gamevault.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jofielramirez.gamevault.data.model.Game
import com.jofielramirez.gamevault.data.model.GenreCount
import com.jofielramirez.gamevault.data.model.LibrarySummary
import com.jofielramirez.gamevault.data.model.PlatformCount
import com.jofielramirez.gamevault.data.model.StatusCount
import com.jofielramirez.gamevault.data.repository.GameRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import com.jofielramirez.gamevault.data.model.PlaySession
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

/** Hours logged with "Log session" in one calendar month. */
data class MonthHours(val label: String, val hours: Double)

data class StatsUiState(
    val summary: LibrarySummary = LibrarySummary(0, 0, 0, 0.0, 0.0),
    val byPlatform: List<PlatformCount> = emptyList(),
    val byStatus: List<StatusCount> = emptyList(),
    val mostPlayed: List<Game> = emptyList(),
    val byGenre: List<GenreCount> = emptyList(),
    val monthlyHours: List<MonthHours> = emptyList(),
    val isLoading: Boolean = true
) {
    val isEmpty: Boolean get() = !isLoading && summary.totalGames == 0

    /** Share of the library that is completed, 0–100. */
    val completionRate: Int
        get() = if (summary.totalGames == 0) 0 else (summary.completedGames * 100.0 / summary.totalGames).roundToInt()
}

class StatsViewModel(
    repository: GameRepository,
    now: Long = System.currentTimeMillis()
) : ViewModel() {

    private val firstMonth = StatsCalculations.startOfMonth(now, monthsBack = MONTHS - 1)

    private val libraryStats = combine(
        repository.observeSummary(),
        repository.observeCountByPlatform(),
        repository.observeCountByStatus(),
        repository.observeMostPlayed(limit = MOST_PLAYED_LIMIT)
    ) { summary, byPlatform, byStatus, mostPlayed ->
        StatsUiState(summary, byPlatform, byStatus, mostPlayed, isLoading = false)
    }

    val uiState: StateFlow<StatsUiState> = combine(
        libraryStats,
        repository.observeCountByGenre(),
        repository.observeSessionsSince(firstMonth)
    ) { stats, byGenre, sessions ->
        stats.copy(
            byGenre = byGenre.take(GENRE_LIMIT),
            monthlyHours = StatsCalculations.hoursPerMonth(sessions, now, MONTHS)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StatsUiState())

    companion object {
        const val MONTHS = 6
        const val GENRE_LIMIT = 6
        const val MOST_PLAYED_LIMIT = 5
    }
}

/** Pure date math for the monthly chart, so it can be unit tested. */
object StatsCalculations {

    /** Midnight of the first day of the month, [monthsBack] months before the month of [now]. */
    fun startOfMonth(now: Long, monthsBack: Int = 0): Long = Calendar.getInstance().run {
        timeInMillis = now
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        add(Calendar.MONTH, -monthsBack)
        timeInMillis
    }

    /** Hours of the sessions in each of the last [months] months, oldest first ("Jun", "Jul"...). */
    fun hoursPerMonth(sessions: List<PlaySession>, now: Long, months: Int): List<MonthHours> {
        val labelFormat = SimpleDateFormat("MMM", Locale.US)
        return (months - 1 downTo 0).map { back ->
            val start = startOfMonth(now, back)
            val end = startOfMonth(now, back - 1)
            val hours = sessions.filter { it.playedOn in start until end }.sumOf { it.durationHours }
            MonthHours(labelFormat.format(Date(start)), hours)
        }
    }
}
