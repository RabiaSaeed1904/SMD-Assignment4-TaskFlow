package com.example.taskflow;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.tabs.TabLayout;

public class ProjectActivity extends AppCompatActivity implements ProjectHost {

    private Project currentProject;
    private TextView tvHeaderTag;
    private TextView tvHeaderName;
    private TabLayout tabLayout;

    private OverviewFragment overviewFragment;
    private TasksFragment tasksFragment;
    private FilesFragment filesFragment;
    private DiscussionFragment discussionFragment;
    private Fragment activeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project);

        currentProject = (Project) getIntent().getSerializableExtra(HomeActivity.EXTRA_PROJECT);
        if (currentProject == null) {
            finish();
            return;
        }

        initViews();
        setupHeader();
        setupFragments(savedInstanceState);
        setupTabs();
        setupNavigation();
    }

    private void initViews() {
        tvHeaderTag = findViewById(R.id.tvHeaderProjectTag);
        tvHeaderName = findViewById(R.id.tvHeaderProjectName);
        tabLayout = findViewById(R.id.tabLayout);

        findViewById(R.id.btnInvite).setOnClickListener(v ->
                Toast.makeText(this, "Invite is not functional in this assignment", Toast.LENGTH_SHORT).show()
        );
    }

    private void setupHeader() {
        tvHeaderTag.setText(currentProject.getTag() != null ? currentProject.getTag() : "Project");
        tvHeaderName.setText(currentProject.getName() != null ? currentProject.getName() : "Untitled");
    }

    private void setupFragments(Bundle savedInstanceState) {
        FragmentManager fm = getSupportFragmentManager();

        if (savedInstanceState == null) {
            overviewFragment = new OverviewFragment();
            tasksFragment = new TasksFragment();
            filesFragment = new FilesFragment();
            discussionFragment = new DiscussionFragment();

            fm.beginTransaction()
                    .add(R.id.fragmentContainer, overviewFragment, "0")
                    .add(R.id.fragmentContainer, tasksFragment, "1").hide(tasksFragment)
                    .add(R.id.fragmentContainer, filesFragment, "2").hide(filesFragment)
                    .add(R.id.fragmentContainer, discussionFragment, "3").hide(discussionFragment)
                    .commit();
            activeFragment = overviewFragment;
        } else {
            overviewFragment = (OverviewFragment) fm.findFragmentByTag("0");
            tasksFragment = (TasksFragment) fm.findFragmentByTag("1");
            filesFragment = (FilesFragment) fm.findFragmentByTag("2");
            discussionFragment = (DiscussionFragment) fm.findFragmentByTag("3");
            activeFragment = fm.findFragmentById(R.id.fragmentContainer);
        }
    }

    private void setupTabs() {
        tabLayout.removeAllTabs();
        tabLayout.addTab(tabLayout.newTab().setText("Overview"));
        tabLayout.addTab(tabLayout.newTab().setText("Tasks (" + currentProject.getTasks().size() + ")"));
        tabLayout.addTab(tabLayout.newTab().setText("Files (" + currentProject.getAttachments().size() + ")"));
        tabLayout.addTab(tabLayout.newTab().setText("Discussion (" + currentProject.getCommentCount() + ")"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switchFragment(tab.getPosition());
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void switchFragment(int index) {
        Fragment target;
        switch (index) {
            case 1: target = tasksFragment; break;
            case 2: target = filesFragment; break;
            case 3: target = discussionFragment; break;
            default: target = overviewFragment; break;
        }

        if (target != null && target != activeFragment) {
            getSupportFragmentManager().beginTransaction()
                    .hide(activeFragment)
                    .show(target)
                    .commit();
            activeFragment = target;
        }
    }

    private void setupNavigation() {
        findViewById(R.id.btnBack).setOnClickListener(v -> exitWithResult());
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                exitWithResult();
            }
        });
    }

    private void exitWithResult() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra(HomeActivity.EXTRA_PROJECT, currentProject);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    @Override
    public Project getProject() {
        return currentProject;
    }

    @Override
    public void notifyProjectChanged() {
        setupHeader();

        // Update tab labels with live counts
        TabLayout.Tab tasksTab = tabLayout.getTabAt(1);
        if (tasksTab != null) tasksTab.setText("Tasks (" + currentProject.getTasks().size() + ")");

        TabLayout.Tab filesTab = tabLayout.getTabAt(2);
        if (filesTab != null) filesTab.setText("Files (" + currentProject.getAttachments().size() + ")");

        if (overviewFragment != null && overviewFragment.isAdded()) {
            overviewFragment.refreshProjectData();
        }
        if (tasksFragment != null && tasksFragment.isAdded()) {
            tasksFragment.refreshList();
        }
        if (filesFragment != null && filesFragment.isAdded()) {
            filesFragment.refreshList();
        }
    }

    @Override
    public void navigateToTasks(String statusFilter, String dateFilter) {
        TabLayout.Tab tab = tabLayout.getTabAt(1);
        if (tab != null) tab.select();
        if (tasksFragment != null) {
            tasksFragment.applyDirectFilters(statusFilter, dateFilter);
        }
    }

    @Override
    public void navigateToTab(int tabIndex) {
        TabLayout.Tab tab = tabLayout.getTabAt(tabIndex);
        if (tab != null) tab.select();
    }
}