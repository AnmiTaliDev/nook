package dev.anmitali.nook.core.model

enum class SortBy {
    NAME,
    DATE_MODIFIED,
    SIZE,
    TYPE,
}

enum class SortDirection {
    ASCENDING,
    DESCENDING,
}

data class SortOrder(
    val sortBy: SortBy = SortBy.NAME,
    val direction: SortDirection = SortDirection.ASCENDING,
)
