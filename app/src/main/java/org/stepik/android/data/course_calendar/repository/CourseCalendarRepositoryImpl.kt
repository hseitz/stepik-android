package org.stepik.android.data.course_calendar.repository

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.data.course_calendar.source.CourseCalendarCacheDataSource
import org.stepik.android.domain.course_calendar.model.SectionDateEvent
import org.stepik.android.domain.course_calendar.repository.CourseCalendarRepository
import javax.inject.Inject

class CourseCalendarRepositoryImpl
@Inject
constructor(
    private val courseCalendarCacheDataSource: CourseCalendarCacheDataSource
) : CourseCalendarRepository {

    override fun getSectionDateEventsByIds(vararg ids: Long): Single<List<SectionDateEvent>> =
        courseCalendarCacheDataSource.getSectionDateEventsByIds(*ids)

    override fun saveSectionDateEvents(events: List<SectionDateEvent>): Completable =
        courseCalendarCacheDataSource.saveSectionDateEvents(events)

    override fun removeSectionDateEventsByIds(vararg ids: Long): Completable =
        courseCalendarCacheDataSource.removeSectionDatesEventsByIds(*ids)
}