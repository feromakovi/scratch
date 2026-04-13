package sk.o2.scratchcard.domain.repository

import kotlinx.coroutines.flow.StateFlow
import sk.o2.scratchcard.domain.model.ScratchCardState

interface ScratchCardRepository {

    val state: StateFlow<ScratchCardState>

    val isActivating: StateFlow<Boolean>

    suspend fun scratch(): String

    suspend fun activate(code: String): String

    fun updateState(newState: ScratchCardState)

    fun setActivating(value: Boolean)
}
