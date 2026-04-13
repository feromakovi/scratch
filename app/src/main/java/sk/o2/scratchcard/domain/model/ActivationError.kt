package sk.o2.scratchcard.domain.model

sealed class ActivationError(message: String) : Exception(message) {
    class Network(message: String) : ActivationError(message)
    class InvalidResponse(message: String) : ActivationError(message)
    class ThresholdNotMet(message: String) : ActivationError(message)
    class InvalidState(message: String) : ActivationError(message)
    class Unknown(message: String) : ActivationError(message)
}
