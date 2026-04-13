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
import org.junit.Before
import org.junit.Test
import sk.o2.scratchcard.domain.model.ScratchCardState
import sk.o2.scratchcard.domain.repository.ScratchCardRepository

class ScratchCardUseCaseTest {

    private lateinit var repository: ScratchCardRepository
    private lateinit var useCase: ScratchCardUseCase
    private lateinit var stateFlow: MutableStateFlow<ScratchCardState>

    @Before
    fun setup() {
        repository = mockk(relaxed = true)
        stateFlow = MutableStateFlow(ScratchCardState.Unscratched)
        every { repository.state } returns stateFlow
        useCase = ScratchCardUseCase(repository)
    }

    @Test
    fun `scratch succeeds when card is Unscratched`() = runTest {
        val expectedCode = "test-uuid-123"
        coEvery { repository.scratch() } returns expectedCode

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals(expectedCode, result.getOrNull())
    }

    @Test
    fun `scratch transitions state to Scratched with generated code`() = runTest {
        val expectedCode = "test-uuid-456"
        coEvery { repository.scratch() } returns expectedCode

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
    fun `scratch does not call repository when state is not Unscratched`() = runTest {
        stateFlow.value = ScratchCardState.Scratched("existing-code")

        useCase()

        coVerify(exactly = 0) { repository.scratch() }
        verify(exactly = 0) { repository.updateState(any()) }
    }

    @Test
    fun `scratch returns failure when repository scratch throws`() = runTest {
        coEvery { repository.scratch() } throws RuntimeException("scratch failed")

        val result = useCase()

        assertTrue(result.isFailure)
        assertEquals("scratch failed", result.exceptionOrNull()?.message)
    }
}
