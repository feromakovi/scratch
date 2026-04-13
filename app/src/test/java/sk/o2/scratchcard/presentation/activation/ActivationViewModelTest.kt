package sk.o2.scratchcard.presentation.activation

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import sk.o2.scratchcard.domain.model.ScratchCardState
import sk.o2.scratchcard.domain.repository.ScratchCardRepository
import sk.o2.scratchcard.domain.usecase.ActivateCardUseCase
import sk.o2.scratchcard.domain.model.ActivationError

@OptIn(ExperimentalCoroutinesApi::class)
class ActivationViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val testAppScope = TestScope(testDispatcher)

    private lateinit var repository: ScratchCardRepository
    private lateinit var activateCardUseCase: ActivateCardUseCase
    private lateinit var stateFlow: MutableStateFlow<ScratchCardState>
    private lateinit var isActivatingFlow: MutableStateFlow<Boolean>
    private lateinit var viewModel: ActivationViewModel

    private val testCode = "test-uuid-activation"

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
        stateFlow = MutableStateFlow(ScratchCardState.Scratched(testCode))
        isActivatingFlow = MutableStateFlow(false)

        every { repository.state } returns stateFlow
        every { repository.isActivating } returns isActivatingFlow
        every { repository.setActivating(any()) } answers {
            isActivatingFlow.value = firstArg()
        }

        activateCardUseCase = mockk(relaxed = true)
        viewModel = ActivationViewModel(activateCardUseCase, repository, testAppScope)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is not activating`() {
        assertFalse(viewModel.isActivating.value)
    }

    @Test
    fun `initial error is null`() {
        assertNull(viewModel.error.value)
    }

    @Test
    fun `activate sets isActivating to true while in progress`() = runTest {
        coEvery { activateCardUseCase() } coAnswers {
            delay(3000)
            Result.success(Unit)
        }

        viewModel.activate()
        testAppScope.advanceTimeBy(100)

        assertTrue(viewModel.isActivating.value)
    }

    @Test
    fun `activate sets isActivating to false after success`() = runTest {
        coEvery { activateCardUseCase() } returns Result.success(Unit)

        viewModel.activate()
        testAppScope.advanceUntilIdle()

        assertFalse(viewModel.isActivating.value)
    }

    @Test
    fun `activate sets isActivating to false after failure`() = runTest {
        coEvery { activateCardUseCase() } returns Result.failure(
            ActivationError.ThresholdNotMet("Below threshold")
        )

        viewModel.activate()
        testAppScope.advanceUntilIdle()

        assertFalse(viewModel.isActivating.value)
    }

    @Test
    fun `activate sets error on failure`() = runTest {
        coEvery { activateCardUseCase() } returns Result.failure(
            ActivationError.Network("Connection lost")
        )

        viewModel.activate()
        testAppScope.advanceUntilIdle()

        assertNotNull(viewModel.error.value)
    }

    @Test
    fun `activate clears previous error before starting`() = runTest {
        coEvery { activateCardUseCase() } returns Result.failure(
            ActivationError.Network("Connection lost")
        )
        viewModel.activate()
        testAppScope.advanceUntilIdle()
        assertNotNull(viewModel.error.value)

        isActivatingFlow.value = false

        coEvery { activateCardUseCase() } coAnswers {
            delay(1000)
            Result.success(Unit)
        }
        viewModel.activate()
        assertNull(viewModel.error.value)
    }

    @Test
    fun `dismissError clears the error`() = runTest {
        coEvery { activateCardUseCase() } returns Result.failure(
            ActivationError.ThresholdNotMet("Below threshold")
        )

        viewModel.activate()
        testAppScope.advanceUntilIdle()
        assertNotNull(viewModel.error.value)

        viewModel.dismissError()

        assertNull(viewModel.error.value)
    }

    @Test
    fun `activate is idempotent while already activating`() = runTest {
        coEvery { activateCardUseCase() } coAnswers {
            delay(3000)
            Result.success(Unit)
        }

        viewModel.activate()
        testAppScope.advanceTimeBy(100)
        viewModel.activate()
        viewModel.activate()
        testAppScope.advanceUntilIdle()

        verify(exactly = 1) { repository.setActivating(true) }
        verify(exactly = 1) { repository.setActivating(false) }
    }

    @Test
    fun `non-cancellation - activation completes even when viewModel is destroyed`() = runTest {
        coEvery { activateCardUseCase() } coAnswers {
            delay(3000)
            stateFlow.value = ScratchCardState.Activated(testCode)
            Result.success(Unit)
        }

        viewModel.activate()
        testAppScope.advanceTimeBy(500)

        @Suppress("UNUSED_VALUE")
        viewModel = ActivationViewModel(activateCardUseCase, repository, testAppScope)

        testAppScope.advanceUntilIdle()

        assertEquals(ScratchCardState.Activated(testCode), stateFlow.value)
        assertFalse(isActivatingFlow.value)
    }

    @Test
    fun `error is ActivationError type for Network error`() = runTest {
        coEvery { activateCardUseCase() } returns Result.failure(
            ActivationError.Network("timeout")
        )

        viewModel.activate()
        testAppScope.advanceUntilIdle()

        assertTrue(viewModel.error.value is ActivationError.Network)
    }

    @Test
    fun `error is ActivationError type for InvalidResponse error`() = runTest {
        coEvery { activateCardUseCase() } returns Result.failure(
            ActivationError.InvalidResponse("bad data")
        )

        viewModel.activate()
        testAppScope.advanceUntilIdle()

        assertTrue(viewModel.error.value is ActivationError.InvalidResponse)
    }

    @Test
    fun `error is ActivationError type for ThresholdNotMet error`() = runTest {
        coEvery { activateCardUseCase() } returns Result.failure(
            ActivationError.ThresholdNotMet("too low")
        )

        viewModel.activate()
        testAppScope.advanceUntilIdle()

        assertTrue(viewModel.error.value is ActivationError.ThresholdNotMet)
    }
}
