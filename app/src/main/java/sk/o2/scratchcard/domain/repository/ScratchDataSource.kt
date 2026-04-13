package sk.o2.scratchcard.domain.repository

interface ScratchDataSource {

    suspend fun scratch(): String
}
