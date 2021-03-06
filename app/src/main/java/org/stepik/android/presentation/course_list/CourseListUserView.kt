package org.stepik.android.presentation.course_list

import org.stepik.android.domain.user_courses.model.UserCourse
import org.stepik.android.presentation.course_continue.CourseContinueView

interface CourseListUserView : CourseContinueView {
    sealed class State {
        object Idle : State()
        object Loading : State()
        object NetworkError : State()
        object EmptyLogin : State()
        data class Data(val userCourses: List<UserCourse>, val courseListViewState: CourseListView.State) : State()
    }

    fun setState(state: State)
    fun showNetworkError()
}