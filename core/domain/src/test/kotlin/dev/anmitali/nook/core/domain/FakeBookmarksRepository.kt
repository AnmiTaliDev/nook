package dev.anmitali.nook.core.domain

import dev.anmitali.nook.core.model.Bookmark
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeBookmarksRepository(initial: List<Bookmark> = emptyList()) : BookmarksRepository {

    private val bookmarks = MutableStateFlow(initial)

    override fun observeBookmarks(): Flow<List<Bookmark>> = bookmarks.asStateFlow()

    override suspend fun addBookmark(bookmark: Bookmark) {
        bookmarks.value = bookmarks.value + bookmark
    }

    override suspend fun removeBookmark(path: String) {
        bookmarks.value = bookmarks.value.filterNot { it.path == path }
    }
}
