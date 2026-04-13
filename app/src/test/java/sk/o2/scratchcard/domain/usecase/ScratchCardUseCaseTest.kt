package sk.o2.scratchcard.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import sk.o2.scratchcard.domain.model.ScratchCardState
import sk.o2.scratchcard.domain.repository.ScratchCardRepository
import sk.o2.scratchcard.domain.repository.ScratchDataSource

class ScratchCardUseCaseTest {

    private val stateFlow = MutableStateFlow<ScratchCardState>(ScratchCardState.Unscratched)
    private val repository: ScratchCardRepository = mockk(relaxed = true) {
        every { state } returns stateFlow
    }
    private val scratchDataSource: ScratchDataSource = mockk(relaxed = true)
    private val useCase = ScratchCardUseCase(repository, scratchDataSource)

    @Test
    fun `scratch succeeds when card is Unscratched`() = runTest {
        val expectedCode = "test-uuid-123"
        coEvery { scratchDataSource.scratch() } returns expectedCode

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals(expectedCode, result.getOrNull())
    }

    @Test
    fun `scratch transitions state to Scratched with generated code`() = runTest {
        val expectedCode = "test-uuid-456"
        coEvery { scratchDataSource.scratch() } returns expectedCode

        useCase()

        verify { repository.updateState(ScratchCardState.Scratched(expectedCode)) }
    }

    @Test
    fun `scratch fails when card is already Scratched`() = runTest {
        stateFlow.value = ScratchCardState.Scratched("existing-code")

        val result = useCase()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalStateException)
    }

    @Test
    fun `scratch fails when card is already Activated`() = runTest {
        stateFlow.value = ScratchCardState.Activated("existing-code")

        val result = useCase()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalStateException)
    }

    @Test
    fun `scratch does not call data source when state is not Unscratched`() = runTest {
        stateFlow.value = ScratchCardState.Scratched("existing-code")

        useCase()

        coVerify(exactly = 0) { scratchDataSource.scratch() }
        verify(exactly = 0) { repository.updateState(any()) }
    }

    @Test
    fun `scratch returns failure when data source throws`() = runTest {
        coEvery { scratchDataSource.scratch() } throws RuntimeException("scratch failed")

        val result = useCase()

        assertTrue(result.isFailure)
        assertEquals("scratch failed", result.exceptionOrNull()?.message)
    }
}
