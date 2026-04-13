package sk.o2.scratchcard.domain.scheduler

import kotlinx.coroutines.flow.Flow
import sk.o2.scratchcard.domain.model.ActivationWorkState

interface ActivationScheduler {

    fun schedule()

    fun observeState(): Flow<ActivationWorkState>
}
