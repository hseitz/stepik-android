package org.stepic.droid.ui.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.stepic.droid.R;
import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.base.App;
import org.stepic.droid.base.Client;
import org.stepic.droid.base.FragmentBase;
import org.stepic.droid.core.joining.contract.JoiningListener;
import org.stepic.droid.core.presenters.ContinueCoursePresenter;
import org.stepic.droid.core.presenters.DroppingPresenter;
import org.stepic.droid.core.presenters.contracts.ContinueCourseView;
import org.stepic.droid.core.presenters.contracts.CoursesView;
import org.stepic.droid.core.presenters.contracts.DroppingView;
import org.stepic.droid.model.Course;
import org.stepic.droid.model.CoursesCarouselColorType;
import org.stepic.droid.model.Section;
import org.stepic.droid.storage.operations.Table;
import org.stepic.droid.ui.activities.contracts.RootScreen;
import org.stepic.droid.ui.adapters.CoursesAdapter;
import org.stepic.droid.ui.custom.StepikSwipeRefreshLayout;
import org.stepic.droid.ui.custom.TouchDispatchableFrameLayout;
import org.stepic.droid.ui.custom.WrapContentLinearLayoutManager;
import org.stepic.droid.ui.decorators.VerticalSpacesDecoration;
import org.stepic.droid.ui.dialogs.LoadingProgressDialogFragment;
import org.stepic.droid.util.ColorUtil;
import org.stepic.droid.util.KotlinUtil;
import org.stepic.droid.util.ProgressHelper;
import org.stepic.droid.util.StepikUtil;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;

public abstract class CourseListFragmentBase extends FragmentBase
        implements SwipeRefreshLayout.OnRefreshListener,
        CoursesView,
        ContinueCourseView,
        JoiningListener,
        DroppingView {

    private static final String continueLoadingTag = "continueLoadingTag";

    @BindView(R.id.swipe_refresh_layout_mycourses)
    protected StepikSwipeRefreshLayout swipeRefreshLayout;

    @BindView(R.id.list_of_courses)
    protected RecyclerView listOfCoursesView;

    @BindView(R.id.reportProblem)
    protected View reportConnectionProblem;

    @BindView(R.id.empty_courses)
    protected View emptyCoursesView;

    @BindView(R.id.empty_courses_anonymous_button)
    protected Button signInButton;

    @BindView(R.id.empty_courses_button)
    protected Button findCourseButton;

    @BindView(R.id.empty_courses_text)
    protected TextView emptyCoursesTextView;

    @BindView(R.id.root_fragment_view)
    protected TouchDispatchableFrameLayout rootView;

    @BindView(R.id.loadProgressbarOnEmptyScreen)
    protected ProgressBar progressBarOnEmptyScreen;

    @BindView(R.id.empty_search)
    protected ViewGroup emptySearch;

    protected List<Course> courses;
    protected CoursesAdapter coursesAdapter;

    private RecyclerView.OnScrollListener listOfCoursesViewListener;
    private LinearLayoutManager layoutManager;

    @Inject
    protected ContinueCoursePresenter continueCoursePresenter;

    @Inject
    Client<JoiningListener> joiningListenerClient;

    @Inject
    protected DroppingPresenter droppingPresenter;

    @Override
    protected void injectComponent() {
        App.Companion
                .componentManager()
                .courseGeneralComponent()
                .courseListComponentBuilder()
                .build()
                .inject(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_courses, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressBarOnEmptyScreen.setVisibility(View.GONE);

        swipeRefreshLayout.setOnRefreshListener(this);

        if (courses == null) courses = new ArrayList<>();
        boolean showMore = getCourseType() == Table.enrolled;
        coursesAdapter = new CoursesAdapter(getActivity(), courses, continueCoursePresenter, droppingPresenter, true, showMore, CoursesCarouselColorType.Light);
        listOfCoursesView.setAdapter(coursesAdapter);
        layoutManager = new WrapContentLinearLayoutManager(getContext());
        listOfCoursesView.setLayoutManager(layoutManager);
        listOfCoursesView.addItemDecoration(new VerticalSpacesDecoration(getResources().getDimensionPixelSize(R.dimen.course_list_between_items_padding)));

        listOfCoursesViewListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) //check for scroll down
                {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int pastVisibleItems = layoutManager.findFirstVisibleItemPosition();

                    if ((visibleItemCount + pastVisibleItems) >= totalItemCount && StepikUtil.INSTANCE.isInternetAvailable()) {
                        onNeedDownloadNextPage();
                    }
                }

            }
        };
        listOfCoursesView.addOnScrollListener(listOfCoursesViewListener);
        registerForContextMenu(listOfCoursesView);


        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getAnalytic().reportEvent(Analytic.Anonymous.AUTH_CENTER);
                getScreenManager().showLaunchScreen(getActivity());
            }
        });

        findCourseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Activity parent = getActivity();
                if (parent == null || !(parent instanceof RootScreen)) {
                    return;
                }
                getAnalytic().reportEvent(Analytic.Interaction.CLICK_FIND_COURSE_EMPTY_SCREEN);
                if (getSharedPreferenceHelper().getAuthResponseFromStore() == null) {
                    getAnalytic().reportEvent(Analytic.Anonymous.BROWSE_COURSES_CENTER);
                }
                ((RootScreen) parent).showFindCourses();
            }
        });
        joiningListenerClient.subscribe(this);
        continueCoursePresenter.attachView(this);
        droppingPresenter.attachView(this);
    }

    @Override
    public void onDestroyView() {
        joiningListenerClient.unsubscribe(this);
        continueCoursePresenter.detachView(this);
        droppingPresenter.detachView(this);
        if (listOfCoursesView != null) {
            // do not set adapter to null, because fade out animation for fragment will not working
            unregisterForContextMenu(listOfCoursesView);
        }
        super.onDestroyView();
    }

    protected abstract Table getCourseType();

    public final void updateEnrollment(Course courseForUpdate, long enrollment) {
        boolean inList = false;
        int position = -1;
        for (int i = 0; i < courses.size(); i++) {
            Course courseItem = courses.get(i);
            if (courseItem.getCourseId() == courseForUpdate.getCourseId()) {
                courseItem.setEnrollment((int) courseItem.getCourseId());
                courseForUpdate = courseItem;
                inList = true;
                position = i;
                break;
            }
        }
        if (getCourseType() == Table.enrolled && !inList) {
            courses.add(courseForUpdate);
            coursesAdapter.notifyDataSetChanged();
        } else if (inList) {
            coursesAdapter.notifyItemChanged(position);
        }

    }

    @Override
    public void showLoading() {
        ProgressHelper.dismiss(progressBarOnEmptyScreen);
        reportConnectionProblem.setVisibility(View.GONE);

        if (courses.isEmpty()) {
            setBackgroundColorToRootView(R.color.new_cover);
            ProgressHelper.activate(swipeRefreshLayout);
            coursesAdapter.showLoadingFooter(false);
        } else if (swipeRefreshLayout != null && !swipeRefreshLayout.isRefreshing()) {
            ProgressHelper.dismiss(swipeRefreshLayout);
            coursesAdapter.showLoadingFooter(true);
        } else {
            ProgressHelper.dismiss(swipeRefreshLayout);
            coursesAdapter.showLoadingFooter(false);
        }
    }

    @Override
    public void showEmptyCourses() {
        ProgressHelper.dismiss(progressBarOnEmptyScreen);
        ProgressHelper.dismiss(swipeRefreshLayout);
        reportConnectionProblem.setVisibility(View.GONE);
        if (courses.isEmpty()) {
            setBackgroundColorToRootView(R.color.old_cover);
            showEmptyScreen(true);
            getLocalReminder().remindAboutApp();
        }
    }

    @Override
    public void showConnectionProblem() {
        ProgressHelper.dismiss(progressBarOnEmptyScreen);
        ProgressHelper.dismiss(swipeRefreshLayout);
        coursesAdapter.showLoadingFooter(false);

        if (courses == null || courses.isEmpty()) {
            //screen is clear due to error connection
            showEmptyScreen(false);
            setBackgroundColorToRootView(R.color.old_cover);
            reportConnectionProblem.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public final void showCourses(@NonNull List<Course> courses) {
        ProgressHelper.dismiss(progressBarOnEmptyScreen);
        ProgressHelper.dismiss(swipeRefreshLayout);
        coursesAdapter.showLoadingFooter(false);
        setBackgroundColorToRootView(R.color.new_cover);
        reportConnectionProblem.setVisibility(View.GONE);
        showEmptyScreen(false);
        List<Course> finalCourses;
        if (getCourseType() == null) {
            finalCourses = KotlinUtil.INSTANCE.getListOldPlusUpdated(this.courses, courses);
        } else {
            finalCourses = courses;
        }
        this.courses.clear();
        this.courses.addAll(finalCourses);
        coursesAdapter.notifyDataSetChanged();
    }

    protected abstract void onNeedDownloadNextPage();

    protected abstract void showEmptyScreen(boolean isShow);

    @Override
    public void onShowContinueCourseLoadingDialog() {
        DialogFragment loadingProgressDialogFragment = LoadingProgressDialogFragment.Companion.newInstance();
        if (!loadingProgressDialogFragment.isAdded()) {
            loadingProgressDialogFragment.show(getFragmentManager(), continueLoadingTag);
        }
    }

    @Override
    public void onOpenStep(long courseId, @NotNull Section section, long lessonId, long unitId, int stepPosition) {
        ProgressHelper.dismiss(getFragmentManager(), continueLoadingTag);
        getScreenManager().continueCourse(getActivity(), courseId, section, lessonId, unitId, stepPosition);
    }

    @Override
    public void onAnyProblemWhileContinue(@NotNull Course course) {
        ProgressHelper.dismiss(getFragmentManager(), continueLoadingTag);
        getScreenManager().showSections(getActivity(), course);
    }

    @Override
    public void onPause() {
        super.onPause();
        ProgressHelper.dismiss(getFragmentManager(), continueLoadingTag);
    }

    @Override
    public void onSuccessJoin(@Nullable Course joinedCourse) {
        updateEnrollment(joinedCourse, joinedCourse.getEnrollment());
    }

    protected final void setBackgroundColorToRootView(@ColorRes int colorRes) {
        rootView.setBackgroundColor(ColorUtil.INSTANCE.getColorArgb(colorRes, getContext()));
    }

    @Override
    public final void onUserHasNotPermissionsToDrop() {
        Toast.makeText(getContext(), R.string.cant_drop, Toast.LENGTH_SHORT).show();
    }
}
