package org.stepik.android.presentation.course_reviews

import org.stepic.droid.util.PagedList
import org.stepik.android.domain.course_reviews.model.CourseReviewItem

interface CourseReviewsView {
    sealed class State {
        object Idle : State()
        object Loading : State()
        object EmptyContent : State()
        object NetworkError : State()

        class CourseReviewsCache(val courseReviewItems: PagedList<CourseReviewItem.Data>) : State()
        class CourseReviewsRemote(val courseReviewItems: PagedList<CourseReviewItem.Data>) : State()
        class CourseReviewsRemoteLoading(val courseReviewItems: PagedList<CourseReviewItem.Data>) : State()
    }

    fun setState(state: State)
    fun showNetworkError()
}