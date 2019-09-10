package org.stepic.droid.di.comment

import dagger.Subcomponent
import org.stepic.droid.ui.fragments.CommentsFragment
import org.stepik.android.view.injection.comment.CommentDataModule
import org.stepik.android.view.injection.discussion_proxy.DiscussionProxyDataModule
import org.stepik.android.view.injection.vote.VoteDataModule

@CommentsScope
@Subcomponent(modules = [
    CommentsModule::class,
    CommentDataModule::class,
    DiscussionProxyDataModule::class,
    VoteDataModule::class
])
interface CommentsComponent {

    @Subcomponent.Builder
    interface Builder {
        fun build(): CommentsComponent
    }

    fun inject(commentsFragment: CommentsFragment)
}
