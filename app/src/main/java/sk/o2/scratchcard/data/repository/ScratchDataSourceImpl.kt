package sk.o2.scratchcard.data.repository

import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import sk.o2.scratchcard.domain.dispatcher.DispatcherProvider
import sk.o2.scratchcard.domain.repository.ScratchDataSource
import java.util.UUID
import javax.inject.Inject

class ScratchDataSourceImpl @Inject constructor(
    private val dispatcherProvider: DispatcherProvider,
) : ScratchDataSource {

    override suspend fun scratch(): String = withContext(dispatcherProvider.io) {
        delay(SCRATCH_DELAY_MS)
        UUID.randomUUID().toString()
    }

    companion object {
        const val SCRATCH_DELAY_MS = 2000L
    }
}
