package dev.anmitali.nook.core.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.anmitali.nook.core.data.SharedPreferencesBookmarksRepository
import dev.anmitali.nook.core.domain.BookmarksRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class BookmarksModule {

    @Binds
    @Singleton
    abstract fun bindBookmarksRepository(impl: SharedPreferencesBookmarksRepository): BookmarksRepository
}
