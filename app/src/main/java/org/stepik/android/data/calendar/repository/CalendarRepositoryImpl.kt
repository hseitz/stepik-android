package org.stepik.android.data.calendar.repository

import io.reactivex.Single
import org.stepic.droid.model.CalendarItem
import org.stepik.android.data.calendar.source.CalendarCacheDataSource
import org.stepik.android.domain.calendar.model.CalendarEventData
import org.stepik.android.domain.calendar.repository.CalendarRepository
import javax.inject.Inject

class CalendarRepositoryImpl
@Inject
constructor(
    private val calendarCacheDataSource: CalendarCacheDataSource
) : CalendarRepository {

    override fun syncCalendarEventData(calendarEventData: CalendarEventData, calendarItem: CalendarItem): Single<Long> {
        return calendarCacheDataSource.syncCalendarEventData(calendarEventData, calendarItem)
    }

    override fun getCalendarItems(): Single<List<CalendarItem>> =
            calendarCacheDataSource.getCalendarPrimaryItems()
}