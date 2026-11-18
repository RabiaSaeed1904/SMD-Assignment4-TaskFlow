
package com.example.taskflow;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.tabs.TabLayout;

import java.io.Serializable;

public class ProjectActivity extends AppCompatActivity implements ProjectHost {

    private static final String STATE_CURRENT_PROJECT = "state_current_project";
    private static final String STATE_SELECTED_TAB = "state_selected_tab";
    private static final String ARG_PROJECT_ID = "project_id";

    // Preserve the original Fragment tags for restoration.
    private static final String TAG_OVERVIEW = "0";
    private static final String TAG_TASKS = "1";
    private static final String TAG_FILES = "2";
    private static final String TAG_DISCUSSION = "3";

    private Project currentProject;

    private TextView tvHeaderTag;
    private TextView tvHeaderName;
    private TabLayout tabLayout;

    private OverviewFragment overviewFragment;
    private TasksFragment tasksFragment;
    private FilesFragment filesFragment;
    private DiscussionFragment discussionFragment;

    private Fragment activeFragment;

    private int selectedTabIndex = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_project);

        // First attempt to restore the latest working project.
        if (savedInstanceState != null) {

            savedInstanceState.setClassLoader(
                    Project.class.getClassLoader()
            );

            Serializable saved =
                    savedInstanceState.getSerializable(
                            STATE_CURRENT_PROJECT
                    );

            if (saved instanceof Project) {
                currentProject = (Project) saved;
            }

            selectedTabIndex = validTabIndex(
                    savedInstanceState.getInt(
                            STATE_SELECTED_TAB,
                            0
                    )
            );
        }

        // On a fresh launch, read the project from HomeActivity.
        if (currentProject == null) {

            Serializable launched =
                    getIntent().getSerializableExtra(
                            HomeActivity.EXTRA_PROJECT
                    );

            if (launched instanceof Project) {
                currentProject = (Project) launched;
            }
        }

        if (currentProject == null) {
            finish();
            return;
        }

        initViews();

        updateHeader();

        setupFragments();

        setupTabs();

        setupNavigation();
    }

    private void initViews() {

        tvHeaderTag = findViewById(
                R.id.tvHeaderProjectTag
        );

        tvHeaderName = findViewById(
                R.id.tvHeaderProjectName
        );

        tabLayout = findViewById(
                R.id.tabLayout
        );

        // Invite is UI-only for this assignment.
        findViewById(R.id.btnInvite)
                .setOnClickListener(v ->
                        Toast.makeText(
                                this,
                                "Invite is not functional in this assignment",
                                Toast.LENGTH_SHORT
                        ).show()
                );
    }

    private void updateHeader() {

        tvHeaderTag.setText(
                currentProject.getTag() == null
                        ? "Project"
                        : currentProject.getTag()
        );

        tvHeaderName.setText(
                currentProject.getName() == null
                        ? "Untitled"
                        : currentProject.getName()
        );
    }

    private int validTabIndex(int requested) {

        if (requested < 0 || requested > 3) {
            return 0;
        }

        return requested;
    }

    /**
     * Supply the stable project ID to newly created Fragments.
     *
     * The Fragment still reads the current Project object
     * through ProjectHost, rather than making a separate copy.
     */
    private <T extends Fragment> T withProjectId(T fragment) {

        Bundle arguments = new Bundle();

        arguments.putString(
                ARG_PROJECT_ID,
                currentProject.getId()
        );

        fragment.setArguments(arguments);

        return fragment;
    }

    /**
     * Restore Fragment instances when possible.
     *
     * Otherwise create the four Fragments and attach them
     * to the same container.
     */
    private void setupFragments() {

        FragmentManager fm =
                getSupportFragmentManager();

        FragmentTransaction transaction =
                fm.beginTransaction();

        overviewFragment =
                (OverviewFragment) fm.findFragmentByTag(
                        TAG_OVERVIEW
                );

        tasksFragment =
                (TasksFragment) fm.findFragmentByTag(
                        TAG_TASKS
                );

        filesFragment =
                (FilesFragment) fm.findFragmentByTag(
                        TAG_FILES
                );

        discussionFragment =
                (DiscussionFragment) fm.findFragmentByTag(
                        TAG_DISCUSSION
                );

        if (overviewFragment == null) {

            overviewFragment =
                    withProjectId(new OverviewFragment());

            transaction.add(
                    R.id.fragmentContainer,
                    overviewFragment,
                    TAG_OVERVIEW
            );
        }

        if (tasksFragment == null) {

            tasksFragment =
                    withProjectId(new TasksFragment());

            transaction.add(
                    R.id.fragmentContainer,
                    tasksFragment,
                    TAG_TASKS
            );
        }

        if (filesFragment == null) {

            filesFragment =
                    withProjectId(new FilesFragment());

            transaction.add(
                    R.id.fragmentContainer,
                    filesFragment,
                    TAG_FILES
            );
        }

        if (discussionFragment == null) {

            discussionFragment =
                    withProjectId(new DiscussionFragment());

            transaction.add(
                    R.id.fragmentContainer,
                    discussionFragment,
                    TAG_DISCUSSION
            );
        }

        Fragment selected =
                fragmentForTab(selectedTabIndex);

        // Ensure exactly one Fragment is visible.
        transaction.hide(overviewFragment);
        transaction.hide(tasksFragment);
        transaction.hide(filesFragment);
        transaction.hide(discussionFragment);

        transaction.show(selected);

        // Execute before configuring the TabLayout.
        transaction.commitNow();

        activeFragment = selected;
    }

    private Fragment fragmentForTab(int tabIndex) {

        switch (validTabIndex(tabIndex)) {

            case 1:
                return tasksFragment;

            case 2:
                return filesFragment;

            case 3:
                return discussionFragment;

            default:
                return overviewFragment;
        }
    }

    /**
     * Set up the four tabs and restore the selected indicator.
     */
    private void setupTabs() {

        tabLayout.removeAllTabs();

        tabLayout.addTab(
                tabLayout.newTab()
                        .setText("Overview")
        );

        tabLayout.addTab(
                tabLayout.newTab()
                        .setText("Tasks")
        );

        tabLayout.addTab(
                tabLayout.newTab()
                        .setText("Files")
        );

        tabLayout.addTab(
                tabLayout.newTab()
                        .setText("Discussion")
        );

        updateTabCounts();

        // Align the selected indicator with the visible Fragment.
        TabLayout.Tab restoredTab =
                tabLayout.getTabAt(selectedTabIndex);

        if (restoredTab != null) {
            restoredTab.select();
        }

        tabLayout.addOnTabSelectedListener(
                new TabLayout.OnTabSelectedListener() {

                    @Override
                    public void onTabSelected(
                            TabLayout.Tab tab
                    ) {
                        showTab(tab.getPosition());
                    }

                    @Override
                    public void onTabUnselected(
                            TabLayout.Tab tab
                    ) {
                    }

                    @Override
                    public void onTabReselected(
                            TabLayout.Tab tab
                    ) {
                    }
                }
        );
    }

    /**
     * Change the visible Fragment when a tab is selected.
     */
    private void showTab(int position) {

        int index = validTabIndex(position);

        Fragment target =
                fragmentForTab(index);

        if (target == null) {
            return;
        }

        if (target == activeFragment) {
            selectedTabIndex = index;
            return;
        }

        FragmentManager fm =
                getSupportFragmentManager();

        if (fm.isStateSaved()) {
            return;
        }

        FragmentTransaction transaction =
                fm.beginTransaction();

        if (activeFragment != null) {
            transaction.hide(activeFragment);
        }

        transaction.show(target);

        transaction.commit();

        activeFragment = target;
        selectedTabIndex = index;
    }

    /**
     * Update displayed tab counts from the current project.
     */
    private void updateTabCounts() {

        TabLayout.Tab tasksTab =
                tabLayout.getTabAt(1);

        TabLayout.Tab filesTab =
                tabLayout.getTabAt(2);

        TabLayout.Tab discussionTab =
                tabLayout.getTabAt(3);

        if (tasksTab != null) {

            tasksTab.setText(
                    "Tasks ("
                            + currentProject.getTasks().size()
                            + ")"
            );
        }

        if (filesTab != null) {

            filesTab.setText(
                    "Files ("
                            + currentProject.getAttachments().size()
                            + ")"
            );
        }

        if (discussionTab != null) {

            discussionTab.setText(
                    "Discussion ("
                            + currentProject.getCommentCount()
                            + ")"
            );
        }
    }

    /**
     * Return to the existing HomeActivity.
     */
    private void setupNavigation() {

        findViewById(R.id.btnBack)
                .setOnClickListener(
                        v -> exitWithResult()
                );

        getOnBackPressedDispatcher()
                .addCallback(
                        this,
                        new OnBackPressedCallback(true) {

                            @Override
                            public void handleOnBackPressed() {
                                exitWithResult();
                            }
                        }
                );
    }

    private void exitWithResult() {

        Intent result =
                new Intent();

        result.putExtra(
                HomeActivity.EXTRA_PROJECT,
                currentProject
        );

        setResult(
                RESULT_OK,
                result
        );

        finish();
    }

    /**
     * Preserve the working project and selected tab
     * during Activity recreation.
     */
    @Override
    protected void onSaveInstanceState(
            @NonNull Bundle outState
    ) {

        outState.putSerializable(
                STATE_CURRENT_PROJECT,
                currentProject
        );

        outState.putInt(
                STATE_SELECTED_TAB,
                selectedTabIndex
        );

        super.onSaveInstanceState(outState);
    }

    /**
     * ProjectHost implementation.
     */
    @Override
    public Project getProject() {
        return currentProject;
    }

    /**
     * Called by Fragments after a project change.
     *
     * Refresh the header, tab counts and summaries.
     */
    @Override
    public void notifyProjectChanged() {

        if (currentProject == null) {
            return;
        }

        updateHeader();
        updateTabCounts();

        if (overviewFragment != null
                && overviewFragment.isAdded()) {

            overviewFragment.refreshProjectData();
        }

        if (tasksFragment != null
                && tasksFragment.isAdded()) {

            tasksFragment.refreshList();
        }

        if (filesFragment != null
                && filesFragment.isAdded()) {

            filesFragment.refreshList();
        }
    }

    /**
     * Called by OverviewFragment breakdown cards.
     */
    @Override
    public void navigateToTasks(
            String statusFilter,
            String dateFilter
    ) {

        TabLayout.Tab tasksTab =
                tabLayout.getTabAt(1);

        if (tasksTab != null) {
            tasksTab.select();
        }

        if (tasksFragment != null) {

            tasksFragment.applyDirectFilters(
                    statusFilter,
                    dateFilter
            );
        }
    }

    /**
     * Called by OverviewFragment quick links.
     */
    @Override
    public void navigateToTab(int tabIndex) {

        TabLayout.Tab tab =
                tabLayout.getTabAt(
                        validTabIndex(tabIndex)
                );

        if (tab != null) {
            tab.select();
        }
    }
}
