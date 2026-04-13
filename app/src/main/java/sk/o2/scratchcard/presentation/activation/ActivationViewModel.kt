package sk.o2.scratchcard.presentation.activation

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import sk.o2.scratchcard.di.ApplicationScope
import sk.o2.scratchcard.domain.model.ScratchCardState
import sk.o2.scratchcard.domain.repository.ScratchCardRepository
import sk.o2.scratchcard.domain.usecase.ActivateCardUseCase
import sk.o2.scratchcard.domain.usecase.ActivationError
import javax.inject.Inject

@HiltViewModel
class ActivationViewModel @Inject constructor(
    private val activateCardUseCase: ActivateCardUseCase,
    private val repository: ScratchCardRepository,
    @ApplicationScope private val appScope: CoroutineScope
) : ViewModel() {

    val cardState: StateFlow<ScratchCardState> = repository.state

    val isActivating: StateFlow<Boolean> = repository.isActivating

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun activate() {
        if (repository.isActivating.value) return

        repository.setActivating(true)
        _error.value = null

        appScope.launch {
            val result = activateCardUseCase()
            repository.setActivating(false)

            result.onFailure { throwable ->
                _error.value = mapErrorMessage(throwable)
            }
        }
    }

    fun dismissError() {
        _error.value = null
    }

    private fun mapErrorMessage(throwable: Throwable): String {
        return when (throwable) {
            is ActivationError.Network -> "Network error. Please check your connection and try again."
            is ActivationError.InvalidResponse -> "Invalid response from server."
            is ActivationError.ThresholdNotMet -> "Activation failed. The version does not meet the required threshold."
            is ActivationError.InvalidState -> throwable.message ?: "Invalid card state."
            is ActivationError.Unknown -> throwable.message ?: "An unexpected error occurred."
            else -> throwable.message ?: "An unexpected error occurred. Please try again."
        }
    }
}
