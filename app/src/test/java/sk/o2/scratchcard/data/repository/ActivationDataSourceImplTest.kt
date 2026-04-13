package sk.o2.scratchcard.data.repository

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import sk.o2.scratchcard.common.TestDispatcherProvider
import sk.o2.scratchcard.data.api.O2Api
import sk.o2.scratchcard.data.model.VersionResponse
import java.io.IOException

class ActivationDataSourceImplTest {

    private val testDispatcher = StandardTestDispatcher()
    private val testDispatcherProvider = TestDispatcherProvider(testDispatcher)
    private val api = mockk<O2Api>()
    private val dataSource = ActivationDataSourceImpl(api, testDispatcherProvider)

    @Test
    fun `activate returns android version from API`() = runTest(testDispatcher) {
        coEvery { api.getVersion("test-code") } returns VersionResponse("287028")

        val result = dataSource.activate("test-code")

        assertEquals("287028", result)
    }

    @Test(expected = IOException::class)
    fun `activate throws on network failure`() = runTest(testDispatcher) {
        coEvery { api.getVersion(any()) } throws IOException("No connection")
        dataSource.activate("test-code")
    }
}
