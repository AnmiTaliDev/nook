package dev.anmitali.nook.core.domain

import javax.inject.Inject

class RemoveBookmarkUseCase @Inject constructor(
    private val bookmarksRepository: BookmarksRepository,
) {
    suspend operator fun invoke(path: String) = bookmarksRepository.removeBookmark(path)
}
