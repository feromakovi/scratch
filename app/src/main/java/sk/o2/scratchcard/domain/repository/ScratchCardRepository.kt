package sk.o2.scratchcard.domain.repository

import kotlinx.coroutines.flow.StateFlow
import sk.o2.scratchcard.domain.model.ScratchCardState

interface ScratchCardRepository {

    val state: StateFlow<ScratchCardState>

    fun updateState(newState: ScratchCardState)
}
