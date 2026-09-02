package dev.anmitali.nook.core.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.anmitali.nook.core.data.LocalArchiveRepository
import dev.anmitali.nook.core.domain.ArchiveRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ArchiveModule {

    @Binds
    @Singleton
    abstract fun bindArchiveRepository(impl: LocalArchiveRepository): ArchiveRepository
}
