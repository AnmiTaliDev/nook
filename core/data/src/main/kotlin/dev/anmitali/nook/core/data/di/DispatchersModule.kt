package dev.anmitali.nook.core.data.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.anmitali.nook.core.common.NookDispatchers
import kotlinx.coroutines.Dispatchers

@Module
@InstallIn(SingletonComponent::class)
object DispatchersModule {

    @Provides
    fun provideNookDispatchers(): NookDispatchers = NookDispatchers(
        main = Dispatchers.Main,
        default = Dispatchers.Default,
        io = Dispatchers.IO,
    )
}
