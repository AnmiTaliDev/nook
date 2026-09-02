package dev.anmitali.nook.core.domain

import dev.anmitali.nook.core.model.Bookmark
import javax.inject.Inject

class AddBookmarkUseCase @Inject constructor(
    private val bookmarksRepository: BookmarksRepository,
) {
    suspend operator fun invoke(bookmark: Bookmark) = bookmarksRepository.addBookmark(bookmark)
}
