package sk.o2.scratchcard.domain.usecase

import sk.o2.scratchcard.domain.model.ActivationError
import sk.o2.scratchcard.domain.model.ScratchCardState
import sk.o2.scratchcard.domain.repository.ActivationDataSource
import sk.o2.scratchcard.domain.repository.ScratchCardRepository
import java.io.IOException
import javax.inject.Inject

class ActivateCardUseCase @Inject constructor(
    private val repository: ScratchCardRepository,
    private val activationDataSource: ActivationDataSource,
) {

    suspend operator fun invoke(): Result<Unit> {
        val current = repository.state.value
        if (current !is ScratchCardState.Scratched) {
            return Result.failure(
                ActivationError.InvalidState(
                    "Card must be in Scratched state, currently: ${current::class.simpleName}"
                )
            )
        }

        return runCatching {
            val androidVersion = activationDataSource.activate(current.code)
            val versionInt = androidVersion.toIntOrNull()
                ?: throw ActivationError.InvalidResponse(
                    "Server returned non-numeric version: $androidVersion"
                )

            if (versionInt > ACTIVATION_THRESHOLD) {
                repository.updateState(ScratchCardState.Activated(current.code))
            } else {
                throw ActivationError.ThresholdNotMet(
                    "Version $versionInt does not exceed threshold $ACTIVATION_THRESHOLD"
                )
            }
        }.recoverCatching { error ->
            when (error) {
                is ActivationError -> throw error
                is IOException -> throw ActivationError.Network(error.message ?: "Network error")
                else -> throw ActivationError.Unknown(error.message ?: "Unknown error")
            }
        }
    }

    companion object {
        const val ACTIVATION_THRESHOLD = 277028
    }
}
