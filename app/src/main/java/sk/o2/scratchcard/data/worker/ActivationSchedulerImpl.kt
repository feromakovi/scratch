package sk.o2.scratchcard.data.worker

import androidx.lifecycle.asFlow
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import sk.o2.scratchcard.domain.model.ActivationError
import sk.o2.scratchcard.domain.model.ActivationWorkState
import sk.o2.scratchcard.domain.scheduler.ActivationScheduler
import javax.inject.Inject

class ActivationSchedulerImpl @Inject constructor(
    private val workManager: WorkManager,
) : ActivationScheduler {

    override fun schedule() {
        val request = OneTimeWorkRequestBuilder<ActivationWorker>().build()
        workManager.enqueueUniqueWork(
            ActivationWorker.UNIQUE_WORK_NAME,
            ExistingWorkPolicy.KEEP,
            request
        )
    }

    override fun observeState(): Flow<ActivationWorkState> = workManager
        .getWorkInfosForUniqueWorkLiveData(ActivationWorker.UNIQUE_WORK_NAME)
        .asFlow()
        .map { workInfos -> workInfos.firstOrNull().toActivationWorkState() }
}

private fun WorkInfo?.toActivationWorkState(): ActivationWorkState {
    if (this == null) return ActivationWorkState.Idle

    return when (state) {
        WorkInfo.State.ENQUEUED,
        WorkInfo.State.RUNNING -> ActivationWorkState.Running

        WorkInfo.State.SUCCEEDED -> ActivationWorkState.Succeeded

        WorkInfo.State.FAILED -> {
            val error = ActivationWorker.parseError(outputData)
                ?: ActivationError.Unknown("Unknown error")
            ActivationWorkState.Failed(error)
        }

        WorkInfo.State.BLOCKED,
        WorkInfo.State.CANCELLED -> ActivationWorkState.Idle
    }
}
