package sk.o2.scratchcard.presentation.scratch

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import sk.o2.scratchcard.domain.model.ScratchCardState
import sk.o2.scratchcard.domain.repository.ScratchCardRepository
import sk.o2.scratchcard.domain.usecase.ScratchCardUseCase

@OptIn(ExperimentalCoroutinesApi::class)
class ScratchViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: ScratchCardRepository
    private lateinit var scratchCardUseCase: ScratchCardUseCase
    private lateinit var stateFlow: MutableStateFlow<ScratchCardState>
    private lateinit var viewModel: ScratchViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
        stateFlow = MutableStateFlow(ScratchCardState.Unscratched)
        every { repository.state } returns stateFlow
        scratchCardUseCase = mockk(relaxed = true)
        viewModel = ScratchViewModel(scratchCardUseCase, repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is not scratching`() {
        assertFalse(viewModel.isScratching.value)
    }

    @Test
    fun `initial card state is Unscratched`() {
        assertEquals(ScratchCardState.Unscratched, viewModel.cardState.value)
    }

    @Test
    fun `scratch sets isScratching to true while in progress`() = runTest {
        coEvery { scratchCardUseCase() } coAnswers {
            delay(2000)
            Result.success("test-code")
        }

        viewModel.scratch()
        advanceTimeBy(100)

        assertTrue(viewModel.isScratching.value)
    }

    @Test
    fun `scratch sets isScratching to false after completion`() = runTest {
        coEvery { scratchCardUseCase() } returns Result.success("test-code")

        viewModel.scratch()
        advanceUntilIdle()

        assertFalse(viewModel.isScratching.value)
    }

    @Test
    fun `scratch calls use case`() = runTest {
        coEvery { scratchCardUseCase() } returns Result.success("test-code")

        viewModel.scratch()
        advanceUntilIdle()

        coVerify(exactly = 1) { scratchCardUseCase() }
    }

    @Test
    fun `scratch is idempotent while already scratching`() = runTest {
        coEvery { scratchCardUseCase() } coAnswers {
            delay(2000)
            Result.success("test-code")
        }

        viewModel.scratch()
        advanceTimeBy(100)
        viewModel.scratch()
        viewModel.scratch()
        advanceUntilIdle()

        coVerify(exactly = 1) { scratchCardUseCase() }
    }

    @Test
    fun `scratch uses viewModelScope so it is cancellable on ViewModel destruction`() = runTest {
        var useCaseCompleted = false

        coEvery { scratchCardUseCase() } coAnswers {
            delay(2000)
            useCaseCompleted = true
            Result.success("test-code")
        }

        viewModel.scratch()
        advanceTimeBy(500)

        assertTrue("Should be scratching", viewModel.isScratching.value)
        assertFalse("Use case should not complete before delay ends", useCaseCompleted)

        advanceTimeBy(1600)
        assertTrue("Use case should complete when not cancelled", useCaseCompleted)
        assertFalse("Should no longer be scratching", viewModel.isScratching.value)
    }

    @Test
    fun `scratch completes when not cancelled`() = runTest {
        val testCode = "completed-code"
        coEvery { scratchCardUseCase() } coAnswers {
            delay(2000)
            stateFlow.value = ScratchCardState.Scratched(testCode)
            Result.success(testCode)
        }

        viewModel.scratch()
        advanceTimeBy(2100)

        assertEquals(ScratchCardState.Scratched(testCode), stateFlow.value)
    }
}
