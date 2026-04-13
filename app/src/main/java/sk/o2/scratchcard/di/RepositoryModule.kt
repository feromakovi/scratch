package sk.o2.scratchcard.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import sk.o2.scratchcard.data.repository.ActivationDataSourceImpl
import sk.o2.scratchcard.data.repository.ScratchCardRepositoryImpl
import sk.o2.scratchcard.data.repository.ScratchDataSourceImpl
import sk.o2.scratchcard.data.worker.ActivationSchedulerImpl
import sk.o2.scratchcard.domain.repository.ActivationDataSource
import sk.o2.scratchcard.domain.repository.ScratchCardRepository
import sk.o2.scratchcard.domain.repository.ScratchDataSource
import sk.o2.scratchcard.domain.scheduler.ActivationScheduler
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface RepositoryModule {

    @Binds
    @Singleton
    fun bindScratchCardRepository(impl: ScratchCardRepositoryImpl): ScratchCardRepository

    @Binds
    fun bindScratchDataSource(impl: ScratchDataSourceImpl): ScratchDataSource

    @Binds
    fun bindActivationDataSource(impl: ActivationDataSourceImpl): ActivationDataSource

    @Binds
    fun bindActivationScheduler(impl: ActivationSchedulerImpl): ActivationScheduler
}
