package org.stepik.android.cache.progress

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import org.stepic.droid.storage.operations.DatabaseFacade
import org.stepic.droid.util.maybeFirst
import org.stepik.android.data.progress.source.ProgressCacheDataSource
import org.stepik.android.model.Progress
import javax.inject.Inject

class ProgressCacheDataSourceImpl
@Inject
constructor(
    private val databaseFacade: DatabaseFacade
) : ProgressCacheDataSource {
    override fun getProgress(progressId: String): Maybe<Progress> =
        getProgresses(progressId)
            .maybeFirst()

    override fun getProgresses(vararg progressIds: String): Single<List<Progress>> =
        Single.fromCallable {
            databaseFacade.getProgresses(progressIds.toList())
        }

    override fun saveProgress(progress: Progress): Completable =
        saveProgresses(listOf(progress))

    override fun saveProgresses(progresses: List<Progress>): Completable =
        Completable.fromAction {
            databaseFacade.addProgresses(progresses)
        }
}