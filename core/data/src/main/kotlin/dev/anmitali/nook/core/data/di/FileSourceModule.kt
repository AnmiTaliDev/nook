package dev.anmitali.nook.core.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.anmitali.nook.core.data.LocalFileSource
import dev.anmitali.nook.core.domain.FileSource
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FileSourceModule {

    @Binds
    @Singleton
    abstract fun bindFileSource(impl: LocalFileSource): FileSource
}
