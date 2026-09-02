package dev.anmitali.nook.core.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.anmitali.nook.core.domain.BookmarksRepository
import dev.anmitali.nook.core.model.Bookmark
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
private data class BookmarkDto(val path: String, val label: String)

@Singleton
class SharedPreferencesBookmarksRepository @Inject constructor(
    @ApplicationContext context: Context,
) : BookmarksRepository {

    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }
    private val bookmarks = MutableStateFlow(loadBookmarks())

    override fun observeBookmarks(): Flow<List<Bookmark>> = bookmarks.asStateFlow()

    override suspend fun addBookmark(bookmark: Bookmark) {
        val updated = bookmarks.value.filterNot { it.path == bookmark.path } + bookmark
        bookmarks.value = updated
        persist(updated)
    }

    override suspend fun removeBookmark(path: String) {
        val updated = bookmarks.value.filterNot { it.path == path }
        bookmarks.value = updated
        persist(updated)
    }

    private fun loadBookmarks(): List<Bookmark> {
        val raw = preferences.getString(KEY_BOOKMARKS, null) ?: return emptyList()
        return runCatching { json.decodeFromString<List<BookmarkDto>>(raw) }
            .getOrDefault(emptyList())
            .map { Bookmark(it.path, it.label) }
    }

    private fun persist(bookmarks: List<Bookmark>) {
        val dtos = bookmarks.map { BookmarkDto(it.path, it.label) }
        preferences.edit().putString(KEY_BOOKMARKS, json.encodeToString(dtos)).apply()
    }
}

private const val PREFERENCES_NAME = "nook_bookmarks"
private const val KEY_BOOKMARKS = "bookmarks"
