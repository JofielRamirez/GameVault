package com.jofielramirez.gamevault.ui.form

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jofielramirez.gamevault.data.local.GameVaultDatabase
import com.jofielramirez.gamevault.data.model.GameStatus
import com.jofielramirez.gamevault.data.model.Platform
import com.jofielramirez.gamevault.data.remote.GameSuggestion
import com.jofielramirez.gamevault.data.repository.GameSearchRepository
import com.jofielramirez.gamevault.data.repository.OfflineGameRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/** Search-as-you-type in the Add / Edit form, with a fake catalog instead of the RAWG API. */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
@Config(sdk = [35])
class GameFormSearchTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var db: GameVaultDatabase
    private lateinit var repository: OfflineGameRepository

    private val zelda = GameSuggestion(
        rawgId = 22511,
        title = "The Legend of Zelda: Breath of the Wild",
        releaseYear = 2017,
        genre = "Action",
        platforms = listOf(Platform.NINTENDO_SWITCH),
        coverUrl = "https://media.rawg.io/media/games/cc1/zelda.jpg"
    )

    /** Records every query and answers with [results] (or throws [failure]). */
    private class FakeCatalog(
        var results: List<GameSuggestion> = emptyList(),
        var failure: Exception? = null,
        override val isAvailable: Boolean = true
    ) : GameSearchRepository {
        val queries = mutableListOf<String>()
        var gate: CompletableDeferred<Unit>? = null

        override suspend fun search(query: String): List<GameSuggestion> {
            queries += query
            gate?.await()
            failure?.let { throw it }
            return results
        }
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, GameVaultDatabase::class.java).allowMainThreadQueries().build()
        repository = OfflineGameRepository(db)
    }

    @After
    fun tearDown() {
        db.close()
        Dispatchers.resetMain()
    }

    private fun viewModel(catalog: GameSearchRepository) = GameFormViewModel(SavedStateHandle(), repository, catalog)

    private fun GameFormViewModel.type(title: String) = updateForm(formState.copy(title = title))

    @Test
    fun typing_waitsForAPauseAndSearchesOnce() = runTest(dispatcher) {
        val catalog = FakeCatalog(results = listOf(zelda))
        val vm = viewModel(catalog)

        vm.type("zel")
        advanceTimeBy(100)
        vm.type("zeld")
        advanceTimeBy(100)
        vm.type("zelda")
        advanceUntilIdle()

        assertEquals(listOf("zelda"), catalog.queries)
        assertEquals(GameSearchState.Results("zelda", listOf(zelda)), vm.searchState)
    }

    @Test
    fun shortTitles_doNotSearch() = runTest(dispatcher) {
        val catalog = FakeCatalog(results = listOf(zelda))
        val vm = viewModel(catalog)

        vm.type("ze")
        advanceUntilIdle()

        assertTrue(catalog.queries.isEmpty())
        assertEquals(GameSearchState.Idle, vm.searchState)
    }

    @Test
    fun pickingAResult_fillsTitleYearGenrePlatformAndCover() = runTest(dispatcher) {
        val vm = viewModel(FakeCatalog(results = listOf(zelda)))
        vm.updateForm(vm.formState.copy(status = GameStatus.PLAYING, completionPercent = 40, rating = 4))
        vm.type("zelda")
        advanceUntilIdle()

        vm.selectSuggestion(zelda)
        advanceUntilIdle()

        val form = vm.formState
        assertEquals(zelda.title, form.title)
        assertEquals("2017", form.releaseYear)
        assertEquals("Action", form.genre)
        assertEquals(Platform.NINTENDO_SWITCH, form.platform)
        assertEquals(zelda.coverUrl, form.coverUrl)
        // The user's own progress is not touched
        assertEquals(GameStatus.PLAYING, form.status)
        assertEquals(40, form.completionPercent)
        assertEquals(4, form.rating)
        assertEquals(GameSearchState.Idle, vm.searchState)
    }

    @Test
    fun pickingAResult_keepsThePlatformWhenTheGameIsOnIt() {
        val multiPlatform = zelda.copy(platforms = listOf(Platform.PC, Platform.PLAYSTATION))
        val form = GameFormState(platform = Platform.PLAYSTATION).withSuggestion(multiPlatform)
        assertEquals(Platform.PLAYSTATION, form.platform)
        assertEquals(Platform.PC, GameFormState(platform = Platform.XBOX).withSuggestion(multiPlatform).platform)
    }

    @Test
    fun aResultArrivingAfterThePick_isIgnored() = runTest(dispatcher) {
        val catalog = FakeCatalog(results = listOf(zelda)).apply { gate = CompletableDeferred() }
        val vm = viewModel(catalog)
        vm.type("zelda")
        advanceTimeBy(GameFormViewModel.SEARCH_DEBOUNCE_MS + 1)
        assertEquals(GameSearchState.Loading, vm.searchState)

        vm.dismissSearch()
        catalog.gate!!.complete(Unit)
        advanceUntilIdle()

        assertEquals(GameSearchState.Idle, vm.searchState)
    }

    @Test
    fun networkErrors_showAMessageInsteadOfCrashing() = runTest(dispatcher) {
        val vm = viewModel(FakeCatalog(failure = java.io.IOException("offline")))

        vm.type("celeste")
        advanceUntilIdle()

        assertEquals(GameSearchState.Error, vm.searchState)
    }

    @Test
    fun withoutAnApiKey_theFormSaysSearchIsOff() = runTest(dispatcher) {
        val vm = viewModel(GameSearchRepository.Unavailable)

        vm.type("celeste")
        advanceUntilIdle()

        assertEquals(GameSearchState.NotConfigured, vm.searchState)
    }

    @Test
    fun savingAPickedGame_storesCoverAndRawgId() = runTest(dispatcher) {
        val vm = viewModel(FakeCatalog(results = listOf(zelda)))
        vm.type("zelda")
        advanceUntilIdle()
        vm.selectSuggestion(zelda)

        assertTrue(vm.save())

        val saved = repository.observeGames().first().single()
        assertEquals(zelda.title, saved.title)
        assertEquals(2017, saved.releaseYear)
        assertEquals("Action", saved.genre)
        assertEquals(zelda.coverUrl, saved.coverUri)
        assertEquals(22511, saved.rawgId)
    }

    @Test
    fun typedGenre_isSavedWithTheStandardSpelling() = runTest(dispatcher) {
        val vm = viewModel(GameSearchRepository.Unavailable)
        vm.updateForm(GameFormState(title = "Persona 5", genre = "rpg"))

        assertTrue(vm.save())

        assertEquals("RPG", repository.observeGames().first().single().genre)
    }
}
