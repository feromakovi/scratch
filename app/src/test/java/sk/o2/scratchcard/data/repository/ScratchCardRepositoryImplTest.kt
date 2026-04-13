package sk.o2.scratchcard.data.repository

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import sk.o2.scratchcard.data.api.O2Api
import sk.o2.scratchcard.data.model.VersionResponse
import sk.o2.scratchcard.domain.dispatcher.DispatcherProvider
import sk.o2.scratchcard.domain.model.ScratchCardState
import java.io.IOException
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class ScratchCardRepositoryImplTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var api: O2Api
    private lateinit var dispatcherProvider: DispatcherProvider
    private lateinit var repository: ScratchCardRepositoryImpl

    @Before
    fun setup() {
        api = mockk()
        dispatcherProvider = object : DispatcherProvider {
            override val main = testDispatcher
            override val io = testDispatcher
            override val default = testDispatcher
        }
        repository = ScratchCardRepositoryImpl(api, dispatcherProvider)
    }

    @Test
    fun `initial state is Unscratched`() {
        assertEquals(ScratchCardState.Unscratched, repository.state.value)
    }

    @Test
    fun `initial isActivating is false`() {
        assertFalse(repository.isActivating.value)
    }

    @Test
    fun `scratch returns valid UUID after delay`() = runTest(testDispatcher) {
        val result = repository.scratch()
        assertNotNull(UUID.fromString(result))
    }

    @Test
    fun `scratch takes approximately 2 seconds`() = runTest(testDispatcher) {
        var completed = false

        val job = launch {
            repository.scratch()
            completed = true
        }

        advanceTimeBy(1999)
        assertFalse("Should not complete before 2 seconds", completed)

        advanceTimeBy(2)
        assertTrue("Should complete after 2 seconds", completed)

        job.join()
    }

    @Test
    fun `activate returns android version from API`() = runTest(testDispatcher) {
        coEvery { api.getVersion("test-code") } returns VersionResponse("287028")

        val result = repository.activate("test-code")

        assertEquals("287028", result)
    }

    @Test(expected = IOException::class)
    fun `activate throws on network failure`() = runTest(testDispatcher) {
        coEvery { api.getVersion(any()) } throws IOException("No connection")
        repository.activate("test-code")
    }

    @Test
    fun `updateState allows Unscratched to Scratched`() {
        repository.updateState(ScratchCardState.Scratched("code-123"))
        assertEquals(ScratchCardState.Scratched("code-123"), repository.state.value)
    }

    @Test
    fun `updateState allows Scratched to Activated`() {
        repository.updateState(ScratchCardState.Scratched("code-123"))
        repository.updateState(ScratchCardState.Activated("code-123"))
        assertEquals(ScratchCardState.Activated("code-123"), repository.state.value)
    }

    @Test(expected = IllegalStateException::class)
    fun `updateState rejects Unscratched to Activated`() {
        repository.updateState(ScratchCardState.Activated("code-123"))
    }

    @Test(expected = IllegalStateException::class)
    fun `updateState rejects Scratched to Unscratched`() {
        repository.updateState(ScratchCardState.Scratched("code-123"))
        repository.updateState(ScratchCardState.Unscratched)
    }

    @Test(expected = IllegalStateException::class)
    fun `updateState rejects Activated to Scratched`() {
        repository.updateState(ScratchCardState.Scratched("code-123"))
        repository.updateState(ScratchCardState.Activated("code-123"))
        repository.updateState(ScratchCardState.Scratched("another-code"))
    }

    @Test(expected = IllegalStateException::class)
    fun `updateState rejects Activated to Unscratched`() {
        repository.updateState(ScratchCardState.Scratched("code-123"))
        repository.updateState(ScratchCardState.Activated("code-123"))
        repository.updateState(ScratchCardState.Unscratched)
    }

    @Test(expected = IllegalStateException::class)
    fun `updateState rejects double scratch`() {
        repository.updateState(ScratchCardState.Scratched("code-123"))
        repository.updateState(ScratchCardState.Scratched("code-456"))
    }

    @Test
    fun `setActivating updates isActivating flow`() {
        repository.setActivating(true)
        assertTrue(repository.isActivating.value)

        repository.setActivating(false)
        assertFalse(repository.isActivating.value)
    }
}
