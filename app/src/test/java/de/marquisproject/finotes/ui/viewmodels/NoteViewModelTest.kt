package de.marquisproject.finotes.ui.viewmodels

import de.marquisproject.finotes.utils.NoteRepositoryFake
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before

@OptIn(ExperimentalCoroutinesApi::class)
class NoteViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var fakeRepository: NoteRepositoryFake

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = NoteRepositoryFake()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // TODO() Create viewmodel, savedStateHandle and then test things

}
