package org.stepik.android.domain.course.interactor

import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import okhttp3.ResponseBody
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.util.then
import org.stepik.android.domain.course.repository.CourseRepository
import org.stepik.android.domain.course.repository.EnrollmentRepository
import org.stepik.android.domain.personal_deadlines.repository.DeadlinesRepository
import org.stepik.android.domain.profile.repository.ProfileRepository
import org.stepik.android.domain.user_courses.repository.UserCoursesRepository
import org.stepik.android.model.Course
import org.stepik.android.domain.user_courses.model.UserCourse
import org.stepik.android.view.injection.course.EnrollmentCourseUpdates
import retrofit2.HttpException
import retrofit2.Response
import java.net.HttpURLConnection
import javax.inject.Inject

class CourseEnrollmentInteractor
@Inject
constructor(
    private val enrollmentRepository: EnrollmentRepository,
    private val sharedPreferenceHelper: SharedPreferenceHelper,

    private val courseRepository: CourseRepository,
    private val userCoursesRepository: UserCoursesRepository,
    private val profileRepository: ProfileRepository,

    private val deadlinesRepository: DeadlinesRepository,
    @EnrollmentCourseUpdates
    private val enrollmentSubject: PublishSubject<Course>
) {
    companion object {
        private val UNAUTHORIZED_EXCEPTION_STUB =
            HttpException(Response.error<Nothing>(HttpURLConnection.HTTP_UNAUTHORIZED, ResponseBody.create(null, "")))
    }

    private val requireAuthorization: Completable =
        Completable.create { emitter ->
            if (sharedPreferenceHelper.authResponseFromStore != null) {
                emitter.onComplete()
            } else {
                emitter.onError(UNAUTHORIZED_EXCEPTION_STUB)
            }
        }

    fun enrollCourse(courseId: Long): Single<Course> =
        requireAuthorization then
        enrollmentRepository
            .addEnrollment(courseId)
            .andThen(addUserCourse(courseId))
            .andThen(courseRepository.getCourse(courseId, canUseCache = false).toSingle())
            .doOnSuccess(enrollmentSubject::onNext) // notify everyone about changes

    fun dropCourse(courseId: Long): Single<Course> =
        requireAuthorization then
        enrollmentRepository
            .removeEnrollment(courseId)
            .andThen(deadlinesRepository.removeDeadlineRecordByCourseId(courseId).onErrorComplete())
            .andThen(userCoursesRepository.removeUserCourse(courseId))
            .andThen(courseRepository.getCourse(courseId, canUseCache = false).toSingle())
            .doOnSuccess(enrollmentSubject::onNext) // notify everyone about changes

    private fun addUserCourse(courseId: Long): Completable =
        profileRepository.getProfile().flatMapCompletable { profile ->
            userCoursesRepository.addUserCourse(
                UserCourse(
                    id = 0,
                    user = profile.id,
                    course = courseId,
                    isFavorite = false,
                    isPinned = false,
                    isArchived = false,
                    lastViewed = null
                )
            )
        }
}