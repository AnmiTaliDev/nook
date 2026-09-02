package dev.anmitali.nook.core.domain

import dev.anmitali.nook.core.model.Bookmark
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class GetBookmarksUseCase @Inject constructor(
    private val bookmarksRepository: BookmarksRepository,
) {
    operator fun invoke(): Flow<List<Bookmark>> = bookmarksRepository.observeBookmarks()
}
