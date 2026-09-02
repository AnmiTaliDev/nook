package dev.anmitali.nook.core.domain

import dev.anmitali.nook.core.model.Bookmark
import kotlinx.coroutines.flow.Flow

interface BookmarksRepository {
    fun observeBookmarks(): Flow<List<Bookmark>>

    suspend fun addBookmark(bookmark: Bookmark)

    suspend fun removeBookmark(path: String)
}
