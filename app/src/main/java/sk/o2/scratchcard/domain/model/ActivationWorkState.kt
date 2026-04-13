package sk.o2.scratchcard.domain.model

sealed class ActivationWorkState {
    data object Idle : ActivationWorkState()
    data object Running : ActivationWorkState()
    data object Succeeded : ActivationWorkState()
    data class Failed(val error: ActivationError) : ActivationWorkState()
}
