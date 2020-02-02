package org.stepik.android.presentation.attempts

import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.attempts.interactor.AttemptsInteractor
import org.stepik.android.presentation.base.PresenterBase
import org.stepik.android.view.injection.attempts.AttemptsBus
import javax.inject.Inject

class AttemptsPresenter
@Inject
constructor(
    private val attemptsInteractor: AttemptsInteractor,
    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler,
    @AttemptsBus
    private val attemptsObservable: Observable<Unit>
) : PresenterBase<AttemptsView>() {
    private var state: AttemptsView.State = AttemptsView.State.Idle
        set(value) {
            field = value
            view?.setState(state)
        }

    override fun attachView(view: AttemptsView) {
        super.attachView(view)
        view.setState(state)
    }

    init {
        subscribeForAttemptsUpdates()
    }

    fun fetchAttemptCacheItems(forceUpdate: Boolean = false) {
        if (state == AttemptsView.State.Idle || forceUpdate) {
            state = AttemptsView.State.Loading
            compositeDisposable += attemptsInteractor
                .fetchAttemptCacheItems()
                .subscribeOn(backgroundScheduler)
                .observeOn(mainScheduler)
                .subscribeBy(
                    onSuccess = { attempts ->
                        state = if (attempts.isEmpty()) {
                            AttemptsView.State.Empty
                        } else {
                            AttemptsView.State.AttemptsLoaded(attempts)
                        }
                    },
                    onError = { state = AttemptsView.State.Error; it.printStackTrace() }
                )
        }
    }

    private fun subscribeForAttemptsUpdates() {
        compositeDisposable += attemptsObservable
            .subscribeOn(mainScheduler)
            .observeOn(backgroundScheduler)
            .subscribeBy(
                onNext = { fetchAttemptCacheItems(forceUpdate = true) },
                onError = { it.printStackTrace() }
            )
    }
}