package sk.o2.scratchcard.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import sk.o2.scratchcard.data.dispatcher.DefaultDispatcherProvider
import sk.o2.scratchcard.domain.dispatcher.DispatcherProvider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface DispatcherModule {

    @Binds
    @Singleton
    fun bindDispatcherProvider(impl: DefaultDispatcherProvider): DispatcherProvider
}
