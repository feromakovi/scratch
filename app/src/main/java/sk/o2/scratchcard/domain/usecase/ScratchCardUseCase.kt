package sk.o2.scratchcard.domain.usecase

import sk.o2.scratchcard.domain.model.ScratchCardState
import sk.o2.scratchcard.domain.repository.ScratchCardRepository
import sk.o2.scratchcard.domain.repository.ScratchDataSource
import javax.inject.Inject

class ScratchCardUseCase @Inject constructor(
    private val repository: ScratchCardRepository,
    private val scratchDataSource: ScratchDataSource,
) {

    suspend operator fun invoke(): Result<String> {
        val current = repository.state.value
        if (current !is ScratchCardState.Unscratched) {
            return Result.failure(
                IllegalStateException("Card is already in ${current::class.simpleName} state")
            )
        }

        return runCatching {
            val code = scratchDataSource.scratch()
            repository.updateState(ScratchCardState.Scratched(code))
            code
        }
    }
}
