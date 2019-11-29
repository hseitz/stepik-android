package org.stepic.droid.di.mainscreen

import dagger.Subcomponent
import org.stepic.droid.ui.activities.MainFeedActivity
import org.stepik.android.view.injection.user.UserDataModule
import org.stepik.android.view.injection.user_profile.UserProfileDataModule

@MainScreenScope
@Subcomponent(modules = [
    MainScreenModule::class,
    UserDataModule::class,
    UserProfileDataModule::class
])
interface MainScreenComponent {

    @Subcomponent.Builder
    interface Builder {
        fun build(): MainScreenComponent
    }

    fun inject(mainFeedActivity: MainFeedActivity)
}
