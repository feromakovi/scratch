package sk.o2.scratchcard.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import sk.o2.scratchcard.domain.model.ActivationError
import sk.o2.scratchcard.domain.usecase.ActivateCardUseCase

@HiltWorker
class ActivationWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val activateCardUseCase: ActivateCardUseCase,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return activateCardUseCase()
            .fold(
                onSuccess = { Result.success() },
                onFailure = { throwable ->
                    val error = throwable as? ActivationError
                        ?: ActivationError.Unknown(throwable.message ?: "Unknown error")
                    Result.failure(error.toOutputData())
                }
            )
    }

    companion object {
        const val UNIQUE_WORK_NAME = "activation"
        const val KEY_ERROR_TYPE = "errorType"
        const val KEY_ERROR_MESSAGE = "errorMessage"

        fun parseError(outputData: Data): ActivationError? {
            val type = outputData.getString(KEY_ERROR_TYPE) ?: return null
            val message = outputData.getString(KEY_ERROR_MESSAGE) ?: ""
            return when (type) {
                "Network" -> ActivationError.Network(message)
                "InvalidResponse" -> ActivationError.InvalidResponse(message)
                "ThresholdNotMet" -> ActivationError.ThresholdNotMet(message)
                "InvalidState" -> ActivationError.InvalidState(message)
                else -> ActivationError.Unknown(message)
            }
        }
    }
}

private fun ActivationError.toOutputData(): Data {
    val type = when (this) {
        is ActivationError.Network -> "Network"
        is ActivationError.InvalidResponse -> "InvalidResponse"
        is ActivationError.ThresholdNotMet -> "ThresholdNotMet"
        is ActivationError.InvalidState -> "InvalidState"
        is ActivationError.Unknown -> "Unknown"
    }
    return Data.Builder()
        .putString(ActivationWorker.KEY_ERROR_TYPE, type)
        .putString(ActivationWorker.KEY_ERROR_MESSAGE, message)
        .build()
}
