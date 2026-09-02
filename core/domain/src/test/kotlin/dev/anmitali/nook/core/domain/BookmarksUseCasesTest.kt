package dev.anmitali.nook.core.domain

import dev.anmitali.nook.core.model.Bookmark
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class BookmarksUseCasesTest {

    @Test
    fun `getBookmarks returns the current bookmarks`() = runTest {
        val bookmark = Bookmark("/sd/Photos", "Photos")
        val useCase = GetBookmarksUseCase(FakeBookmarksRepository(listOf(bookmark)))

        assertEquals(listOf(bookmark), useCase().first())
    }

    @Test
    fun `addBookmark adds a new bookmark`() = runTest {
        val repository = FakeBookmarksRepository()
        val useCase = AddBookmarkUseCase(repository)

        useCase(Bookmark("/sd/Photos", "Photos"))

        assertEquals(listOf(Bookmark("/sd/Photos", "Photos")), repository.observeBookmarks().first())
    }

    @Test
    fun `removeBookmark removes a bookmark by path`() = runTest {
        val bookmark = Bookmark("/sd/Photos", "Photos")
        val repository = FakeBookmarksRepository(listOf(bookmark))
        val useCase = RemoveBookmarkUseCase(repository)

        useCase("/sd/Photos")

        assertEquals(emptyList<Bookmark>(), repository.observeBookmarks().first())
    }
}
