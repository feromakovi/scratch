package sk.o2.scratchcard.domain.model

sealed class ScratchCardState {

    data object Unscratched : ScratchCardState()

    data class Scratched(val code: String) : ScratchCardState()

    data class Activated(val code: String) : ScratchCardState()
}
