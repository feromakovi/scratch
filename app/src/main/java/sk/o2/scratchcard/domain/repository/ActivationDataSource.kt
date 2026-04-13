package sk.o2.scratchcard.domain.repository

interface ActivationDataSource {

    suspend fun activate(code: String): String
}
