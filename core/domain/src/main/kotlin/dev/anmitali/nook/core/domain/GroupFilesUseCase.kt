package dev.anmitali.nook.core.domain

import dev.anmitali.nook.core.model.DateBucket
import dev.anmitali.nook.core.model.FileGroup
import dev.anmitali.nook.core.model.FileGroupLabel
import dev.anmitali.nook.core.model.FileItem
import dev.anmitali.nook.core.model.GroupBy
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class GroupFilesUseCase @Inject constructor() {

    operator fun invoke(items: List<FileItem>, groupBy: GroupBy): List<FileGroup> = when (groupBy) {
        GroupBy.NONE -> listOf(FileGroup(FileGroupLabel.None, items))
        GroupBy.TYPE -> items.groupBy { it.type }
            .toSortedMap(compareBy { it.name })
            .map { (type, groupItems) -> FileGroup(FileGroupLabel.Type(type), groupItems) }
        GroupBy.DATE -> items.groupBy { dateBucketOf(it.lastModifiedEpochMillis) }
            .toSortedMap(compareBy { it.ordinal })
            .map { (bucket, groupItems) -> FileGroup(FileGroupLabel.Date(bucket), groupItems) }
    }

    private fun dateBucketOf(epochMillis: Long, now: Instant = Instant.now()): DateBucket {
        val zone = ZoneId.systemDefault()
        val fileDate = Instant.ofEpochMilli(epochMillis).atZone(zone).toLocalDate()
        val today = now.atZone(zone).toLocalDate()
        val daysBetween = ChronoUnit.DAYS.between(fileDate, today)
        return when {
            daysBetween <= 0L -> DateBucket.TODAY
            daysBetween == 1L -> DateBucket.YESTERDAY
            daysBetween in 2..6 -> DateBucket.THIS_WEEK
            else -> DateBucket.OLDER
        }
    }
}
