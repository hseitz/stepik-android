package org.stepik.android.cache.user_courses

import io.reactivex.Completable
import io.reactivex.Single
import org.stepic.droid.storage.dao.IDao
import org.stepik.android.cache.user_courses.structure.DbStructureUserCourse
import org.stepik.android.data.user_courses.source.UserCoursesCacheDataSource
import org.stepik.android.domain.user_courses.model.UserCourse
import javax.inject.Inject

class UserCoursesCacheDataSourceImpl
@Inject
constructor(
    private val userCourseDao: IDao<UserCourse>
) : UserCoursesCacheDataSource {
    override fun getUserCourses(): Single<List<UserCourse>> =
        Single.fromCallable {
            userCourseDao.getAll()
        }

    override fun getUserCourse(courseId: Long): Single<UserCourse> =
        Single.fromCallable {
            userCourseDao.get(DbStructureUserCourse.Columns.COURSE, courseId.toString())
        }

    override fun saveUserCourses(userCourses: List<UserCourse>): Completable =
        Completable.fromAction {
            userCourseDao.insertOrReplaceAll(userCourses)
        }

    override fun removeUserCourse(courseId: Long): Completable =
        Completable.fromAction {
            userCourseDao.remove(DbStructureUserCourse.Columns.ID, courseId.toString())
        }
}