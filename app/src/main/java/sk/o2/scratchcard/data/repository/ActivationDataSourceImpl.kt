package sk.o2.scratchcard.data.repository

import kotlinx.coroutines.withContext
import sk.o2.scratchcard.data.api.O2Api
import sk.o2.scratchcard.domain.dispatcher.DispatcherProvider
import sk.o2.scratchcard.domain.repository.ActivationDataSource
import javax.inject.Inject

class ActivationDataSourceImpl @Inject constructor(
    private val api: O2Api,
    private val dispatcherProvider: DispatcherProvider,
) : ActivationDataSource {

    override suspend fun activate(code: String): String = withContext(dispatcherProvider.io) {
        val response = api.getVersion(code)
        response.android
    }
}
