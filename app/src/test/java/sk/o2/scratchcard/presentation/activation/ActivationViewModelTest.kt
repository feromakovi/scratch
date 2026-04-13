package sk.o2.scratchcard.presentation.activation

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import sk.o2.scratchcard.domain.model.ActivationError
import sk.o2.scratchcard.domain.model.ActivationWorkState
import sk.o2.scratchcard.domain.model.ScratchCardState
import sk.o2.scratchcard.domain.repository.ScratchCardRepository
import sk.o2.scratchcard.domain.scheduler.ActivationScheduler

@OptIn(ExperimentalCoroutinesApi::class)
class ActivationViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var scheduler: ActivationScheduler
    private lateinit var repository: ScratchCardRepository
    private lateinit var stateFlow: MutableStateFlow<ScratchCardState>
    private lateinit var workStateFlow: MutableSharedFlow<ActivationWorkState>
    private lateinit var viewModel: ActivationViewModel

    private val testCode = "test-uuid-activation"

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        stateFlow = MutableStateFlow(ScratchCardState.Scratched(testCode))
        repository = mockk(relaxed = true) {
            every { state } returns stateFlow
        }
        workStateFlow = MutableSharedFlow()
        scheduler = mockk(relaxed = true) {
            every { observeState() } returns workStateFlow
        }
        viewModel = ActivationViewModel(scheduler, repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is not activating`() = runTest {
        advanceUntilIdle()
        assertFalse(viewModel.isActivating.value)
    }

    @Test
    fun `initial error is null`() = runTest {
        advanceUntilIdle()
        assertNull(viewModel.error.value)
    }

    @Test
    fun `activate calls scheduler schedule`() = runTest {
        advanceUntilIdle()

        viewModel.activate()

        verify { scheduler.schedule() }
    }

    @Test
    fun `isActivating is true when state is Running`() = runTest {
        advanceUntilIdle()

        workStateFlow.emit(ActivationWorkState.Running)
        advanceUntilIdle()

        assertTrue(viewModel.isActivating.value)
    }

    @Test
    fun `isActivating is false when state is Succeeded`() = runTest {
        advanceUntilIdle()

        workStateFlow.emit(ActivationWorkState.Succeeded)
        advanceUntilIdle()

        assertFalse(viewModel.isActivating.value)
    }

    @Test
    fun `isActivating is false when state is Failed`() = runTest {
        advanceUntilIdle()

        workStateFlow.emit(ActivationWorkState.Failed(ActivationError.Network("timeout")))
        advanceUntilIdle()

        assertFalse(viewModel.isActivating.value)
    }

    @Test
    fun `isActivating is false when state is Idle`() = runTest {
        advanceUntilIdle()

        workStateFlow.emit(ActivationWorkState.Idle)
        advanceUntilIdle()

        assertFalse(viewModel.isActivating.value)
    }

    @Test
    fun `error is set when state is Failed with Network error`() = runTest {
        advanceUntilIdle()

        workStateFlow.emit(ActivationWorkState.Failed(ActivationError.Network("Connection lost")))
        advanceUntilIdle()

        assertTrue(viewModel.error.value is ActivationError.Network)
        assertEquals("Connection lost", viewModel.error.value?.message)
    }

    @Test
    fun `error is ThresholdNotMet when state is Failed with threshold error`() = runTest {
        advanceUntilIdle()

        workStateFlow.emit(
            ActivationWorkState.Failed(ActivationError.ThresholdNotMet("too low"))
        )
        advanceUntilIdle()

        assertTrue(viewModel.error.value is ActivationError.ThresholdNotMet)
    }

    @Test
    fun `error is InvalidResponse when state is Failed with parse error`() = runTest {
        advanceUntilIdle()

        workStateFlow.emit(
            ActivationWorkState.Failed(ActivationError.InvalidResponse("bad data"))
        )
        advanceUntilIdle()

        assertTrue(viewModel.error.value is ActivationError.InvalidResponse)
    }

    @Test
    fun `error is cleared on success after failure`() = runTest {
        advanceUntilIdle()

        workStateFlow.emit(ActivationWorkState.Failed(ActivationError.Network("timeout")))
        advanceUntilIdle()
        assertTrue(viewModel.error.value is ActivationError.Network)

        workStateFlow.emit(ActivationWorkState.Succeeded)
        advanceUntilIdle()
        assertNull(viewModel.error.value)
    }

    @Test
    fun `dismissError clears the error`() = runTest {
        advanceUntilIdle()

        workStateFlow.emit(ActivationWorkState.Failed(ActivationError.Network("timeout")))
        advanceUntilIdle()
        assertTrue(viewModel.error.value is ActivationError.Network)

        viewModel.dismissError()
        assertNull(viewModel.error.value)
    }

    @Test
    fun `dismissed error does not reappear from same state emission`() = runTest {
        advanceUntilIdle()

        workStateFlow.emit(ActivationWorkState.Failed(ActivationError.Network("timeout")))
        advanceUntilIdle()

        viewModel.dismissError()
        assertNull(viewModel.error.value)

        workStateFlow.emit(ActivationWorkState.Failed(ActivationError.Network("timeout")))
        advanceUntilIdle()
        assertNull(viewModel.error.value)
    }

    @Test
    fun `activate clears previous error`() = runTest {
        advanceUntilIdle()

        workStateFlow.emit(ActivationWorkState.Failed(ActivationError.Network("timeout")))
        advanceUntilIdle()
        assertTrue(viewModel.error.value is ActivationError.Network)

        viewModel.activate()
        assertNull(viewModel.error.value)
    }
}
