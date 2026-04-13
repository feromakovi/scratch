package sk.o2.scratchcard.domain.usecase

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import sk.o2.scratchcard.domain.model.ActivationError
import sk.o2.scratchcard.domain.model.ScratchCardState
import sk.o2.scratchcard.domain.repository.ActivationDataSource
import sk.o2.scratchcard.domain.repository.ScratchCardRepository
import java.io.IOException

class ActivateCardUseCaseTest {

    private val testCode = "test-uuid-789"
    private val stateFlow = MutableStateFlow<ScratchCardState>(ScratchCardState.Scratched(testCode))
    private val repository: ScratchCardRepository = mockk(relaxed = true) {
        every { state } returns stateFlow
    }
    private val activationDataSource: ActivationDataSource = mockk(relaxed = true)
    private val useCase = ActivateCardUseCase(repository, activationDataSource)

    @Test
    fun `activation succeeds when android version is greater than threshold`() = runTest {
        coEvery { activationDataSource.activate(testCode) } returns "287028"

        val result = useCase()

        assertTrue(result.isSuccess)
        verify { repository.updateState(ScratchCardState.Activated(testCode)) }
    }

    @Test
    fun `activation succeeds with version just above threshold`() = runTest {
        coEvery { activationDataSource.activate(testCode) } returns "277029"

        val result = useCase()

        assertTrue(result.isSuccess)
        verify { repository.updateState(ScratchCardState.Activated(testCode)) }
    }

    @Test
    fun `activation fails when android version equals threshold`() = runTest {
        coEvery { activationDataSource.activate(testCode) } returns "277028"

        val result = useCase()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ActivationError.ThresholdNotMet)
        verify(exactly = 0) { repository.updateState(any()) }
    }

    @Test
    fun `activation fails when android version is less than threshold`() = runTest {
        coEvery { activationDataSource.activate(testCode) } returns "100000"

        val result = useCase()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ActivationError.ThresholdNotMet)
    }

    @Test
    fun `activation fails when android version is zero`() = runTest {
        coEvery { activationDataSource.activate(testCode) } returns "0"

        val result = useCase()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ActivationError.ThresholdNotMet)
    }

    @Test
    fun `activation fails when android version is negative`() = runTest {
        coEvery { activationDataSource.activate(testCode) } returns "-1"

        val result = useCase()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ActivationError.ThresholdNotMet)
    }

    @Test
    fun `activation fails when android version is non-numeric`() = runTest {
        coEvery { activationDataSource.activate(testCode) } returns "not-a-number"

        val result = useCase()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ActivationError.InvalidResponse)
    }

    @Test
    fun `activation fails when android version is empty`() = runTest {
        coEvery { activationDataSource.activate(testCode) } returns ""

        val result = useCase()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ActivationError.InvalidResponse)
    }

    @Test
    fun `activation fails with Network error on IOException`() = runTest {
        coEvery { activationDataSource.activate(testCode) } throws IOException("No internet")

        val result = useCase()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ActivationError.Network)
    }

    @Test
    fun `activation fails with Unknown error on unexpected exception`() = runTest {
        coEvery { activationDataSource.activate(testCode) } throws RuntimeException("Something broke")

        val result = useCase()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ActivationError.Unknown)
    }

    @Test
    fun `activation fails when card is Unscratched`() = runTest {
        stateFlow.value = ScratchCardState.Unscratched

        val result = useCase()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ActivationError.InvalidState)
    }

    @Test
    fun `activation fails when card is already Activated`() = runTest {
        stateFlow.value = ScratchCardState.Activated(testCode)

        val result = useCase()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ActivationError.InvalidState)
    }

    @Test
    fun `activation threshold is 277028`() {
        assertEquals(277028, ActivateCardUseCase.ACTIVATION_THRESHOLD)
    }
}
