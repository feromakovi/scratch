package sk.o2.scratchcard.presentation.activation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import sk.o2.scratchcard.domain.model.ActivationError
import sk.o2.scratchcard.domain.model.ActivationWorkState
import sk.o2.scratchcard.domain.model.ScratchCardState
import sk.o2.scratchcard.domain.repository.ScratchCardRepository
import sk.o2.scratchcard.domain.scheduler.ActivationScheduler
import javax.inject.Inject

@HiltViewModel
class ActivationViewModel @Inject constructor(
    private val scheduler: ActivationScheduler,
    private val repository: ScratchCardRepository,
) : ViewModel() {

    val cardState: StateFlow<ScratchCardState> = repository.state

    private val _isActivating = MutableStateFlow(false)
    val isActivating: StateFlow<Boolean> = _isActivating.asStateFlow()

    private val _error = MutableStateFlow<ActivationError?>(null)
    val error: StateFlow<ActivationError?> = _error.asStateFlow()

    private var errorDismissed = false

    init {
        scheduler.observeState()
            .onEach { state -> processState(state) }
            .launchIn(viewModelScope)
    }

    private fun processState(state: ActivationWorkState) {
        _isActivating.value = state is ActivationWorkState.Running

        when (state) {
            is ActivationWorkState.Failed -> {
                if (!errorDismissed) {
                    _error.value = state.error
                }
            }
            is ActivationWorkState.Succeeded,
            is ActivationWorkState.Running -> {
                _error.value = null
            }
            is ActivationWorkState.Idle -> Unit
        }
    }

    fun activate() {
        errorDismissed = false
        _error.value = null
        scheduler.schedule()
    }

    fun dismissError() {
        errorDismissed = true
        _error.value = null
    }
}
