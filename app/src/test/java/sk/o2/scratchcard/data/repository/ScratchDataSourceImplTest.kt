package sk.o2.scratchcard.data.repository

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import sk.o2.scratchcard.common.TestDispatcherProvider
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class ScratchDataSourceImplTest {

    private val testDispatcher = StandardTestDispatcher()
    private val testDispatcherProvider = TestDispatcherProvider(testDispatcher)
    private val dataSource = ScratchDataSourceImpl(testDispatcherProvider)

    @Test
    fun `scratch returns valid UUID after delay`() = runTest(testDispatcher) {
        val result = dataSource.scratch()
        assertNotNull(UUID.fromString(result))
    }

    @Test
    fun `scratch takes approximately 2 seconds`() = runTest(testDispatcher) {
        var completed = false

        val job = launch {
            dataSource.scratch()
            completed = true
        }

        advanceTimeBy(1999)
        assertFalse("Should not complete before 2 seconds", completed)

        advanceTimeBy(2)
        assertTrue("Should complete after 2 seconds", completed)

        job.join()
    }
}
