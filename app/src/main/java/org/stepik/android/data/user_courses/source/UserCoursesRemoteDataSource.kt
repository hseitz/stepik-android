package org.stepik.android.data.user_courses.source

import io.reactivex.Maybe
import io.reactivex.Single
import org.stepic.droid.util.PagedList
import org.stepik.android.domain.user_courses.model.UserCourse

interface UserCoursesRemoteDataSource {
    fun getUserCourses(page: Int): Single<PagedList<UserCourse>>
    fun getUserCourseByCourseId(courseId: Long): Maybe<UserCourse>
    fun saveUserCourse(userCourseId: Long, userCourse: UserCourse): Single<UserCourse>
}