package sk.o2.scratchcard.data.repository

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import sk.o2.scratchcard.data.api.O2Api
import sk.o2.scratchcard.domain.dispatcher.DispatcherProvider
import sk.o2.scratchcard.domain.model.ScratchCardState
import sk.o2.scratchcard.domain.repository.ScratchCardRepository
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScratchCardRepositoryImpl @Inject constructor(
    private val api: O2Api,
    private val dispatcherProvider: DispatcherProvider
) : ScratchCardRepository {

    private val _state = MutableStateFlow<ScratchCardState>(ScratchCardState.Unscratched)
    override val state: StateFlow<ScratchCardState> = _state.asStateFlow()

    private val _isActivating = MutableStateFlow(false)
    override val isActivating: StateFlow<Boolean> = _isActivating.asStateFlow()

    override suspend fun scratch(): String = withContext(dispatcherProvider.io) {
        delay(SCRATCH_DELAY_MS)
        UUID.randomUUID().toString()
    }

    override suspend fun activate(code: String): String = withContext(dispatcherProvider.io) {
        val response = api.getVersion(code)
        response.android
    }

    override fun updateState(newState: ScratchCardState) {
        _state.update { current ->
            validateTransition(from = current, to = newState)
            newState
        }
    }

    override fun setActivating(value: Boolean) {
        _isActivating.value = value
    }

    private fun validateTransition(from: ScratchCardState, to: ScratchCardState) {
        val isValid = when (from) {
            is ScratchCardState.Unscratched -> to is ScratchCardState.Scratched
            is ScratchCardState.Scratched -> to is ScratchCardState.Activated
            is ScratchCardState.Activated -> false
        }
        if (!isValid) {
            throw IllegalStateException(
                "Invalid state transition: ${from::class.simpleName} -> ${to::class.simpleName}"
            )
        }
    }

    companion object {
        const val SCRATCH_DELAY_MS = 2000L
    }
}
