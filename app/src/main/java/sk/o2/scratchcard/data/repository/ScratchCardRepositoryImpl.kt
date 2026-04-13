package sk.o2.scratchcard.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import sk.o2.scratchcard.domain.model.ScratchCardState
import sk.o2.scratchcard.domain.repository.ScratchCardRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScratchCardRepositoryImpl @Inject constructor() : ScratchCardRepository {

    private val _state = MutableStateFlow<ScratchCardState>(ScratchCardState.Unscratched)
    override val state: StateFlow<ScratchCardState> = _state.asStateFlow()

    private val _isActivating = MutableStateFlow(false)
    override val isActivating: StateFlow<Boolean> = _isActivating.asStateFlow()

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
}
