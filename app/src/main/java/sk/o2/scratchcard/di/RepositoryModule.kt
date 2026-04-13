package sk.o2.scratchcard.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import sk.o2.scratchcard.data.repository.ScratchCardRepositoryImpl
import sk.o2.scratchcard.domain.repository.ScratchCardRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface RepositoryModule {

    @Binds
    @Singleton
    fun bindScratchCardRepository(impl: ScratchCardRepositoryImpl): ScratchCardRepository
}
