
package com.example.taskflow;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

public class ProjectActivity extends AppCompatActivity {

    private static final String EXTRA_PROJECT_INDEX = "project_index";
    private static final String EXTRA_PROJECT_NAME = "project_name";
    private static final String EXTRA_PROJECT_TAG = "project_tag";
    private static final String EXTRA_PROJECT_DESCRIPTION = "project_description";
    private static final String EXTRA_ATTACHMENTS = "attachments";
    private static final String EXTRA_TASKS = "tasks";
    private static final String EXTRA_COMMENTS = "comments";

    private static final String STATE_TASKS = "state_tasks";
    private static final String STATE_DESCRIPTION = "state_description";
    private static final String STATE_ATTACHMENTS = "state_attachments";

    private static final int REQUEST_ADD_ATTACHMENT = 2101;

    private static final int BLUE = Color.rgb(48, 153, 243);
    private static final int DARK = Color.rgb(32, 44, 64);
    private static final int SECONDARY = Color.rgb(88, 103, 125);
    private static final int MUTED = Color.rgb(113, 128, 150);
    private static final int BORDER = Color.rgb(223, 230, 239);
    private static final int RED = Color.rgb(200, 61, 69);
    private static final int LIGHT_BG = Color.rgb(249, 250, 252);

    private TextView tvProjectName;
    private TextView tvProjectTag;
    private TextView tvProjectDescription;
    private TextView tvTaskCount;
    private TextView tvCommentsCount;
    private TextView tvProgressPercent;
    private TextView tvBreakdown;

    private TextView tabOverview;
    private TextView tabTasks;
    private TextView tabFiles;
    private TextView tabDiscussion;

    private ProgressBar pbProjectProgress;

    private LinearLayout llTasks;
    private LinearLayout llAttachments;

    private View llOverviewContent;
    private View llLegacyContent;
    private View taskSection;
    private View fileSection;
    private View btnBack;
    private View btnComments;

    private Button btnInvite;
    private Button btnAddTask;

    private TextView discussionPlaceholder;
    private TextView emptyTaskMessage;
    private TextView emptyFileMessage;
    private TextView taskResultsCount;
    private TextView resetFiltersButton;
    private LinearLayout todayTasksContainer;
    private TextView todayTasksSummary;

    private EditText taskSearchInput;
    private EditText fileSearchInput;

    private Spinner statusFilterSpinner;
    private Spinner priorityFilterSpinner;
    private Spinner dateFilterSpinner;
    private Spinner sortSpinner;

    private LinearLayout fileCategoryRow;
    private LinearLayout fileListContainer;
    private TextView fileCountLabel;
    private Button addAttachmentButton;

    private String taskSearchQuery = "";
    private String fileSearchQuery = "";
    private String projectDescription = "";
    private String selectedFileCategory = "All";

    private int statusFilter = 0;
    private int priorityFilter = 0;
    private int dateFilter = 0;
    private int sortOption = 0;

    private int projectIndex = -1;
    private int commentsCount = 0;

    private boolean resettingFilters = false;

    private ArrayList<HomeActivity.TaskData> tasks =
            new ArrayList<>();

    private ArrayList<HomeActivity.AttachmentData> attachments =
            new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project);

        initializeViews();
        receiveProjectInformation();

        if (savedInstanceState != null) {
            restoreSavedState(savedInstanceState);
        }

        setupBackButton();
        setupInviteButton();
        setupCommentsButton();
        setupAddTaskButton();

        setupTabs();
        setupTaskControls();
        setupFileControls();
        setupEditBriefAndScope();
        setupTodayTasksOverview();

        displayTasks();
        displayAttachments();
        updateTaskCount();
        updateCommentsCount();
    }

    private void initializeViews() {
        tvProjectName = findViewById(R.id.tvProjectName);
        tvProjectTag = findViewById(R.id.tvProjectTag);
        tvProjectDescription = findViewById(R.id.tvProjectDescription);
        tvTaskCount = findViewById(R.id.tvTaskCount);
        tvCommentsCount = findViewById(R.id.tvCommentsCount);

        tvProgressPercent = findViewById(R.id.tvProgressPercent);
        tvBreakdown = findViewById(R.id.tvBreakdown);
        pbProjectProgress = findViewById(R.id.pbProjectProgress);

        tabOverview = findViewById(R.id.tabOverview);
        tabTasks = findViewById(R.id.tabTasks);
        tabFiles = findViewById(R.id.tabFiles);
        tabDiscussion = findViewById(R.id.tabDiscussion);

        llTasks = findViewById(R.id.llTasks);
        llAttachments = findViewById(R.id.llAttachments);

        llOverviewContent = findViewById(R.id.llOverviewContent);
        llLegacyContent = findViewById(R.id.llLegacyContent);

        int taskSectionId = getResources().getIdentifier(
                "taskSection", "id", getPackageName()
        );

        taskSection = findViewById(taskSectionId);
        fileSection = findViewById(R.id.fileSection);

        btnBack = findViewById(R.id.btnBack);
        btnComments = findViewById(R.id.btnComments);
        btnInvite = findViewById(R.id.btnInvite);
        btnAddTask = findViewById(R.id.btnAddTask);
    }

    private void receiveProjectInformation() {
        Intent intent = getIntent();

        projectIndex = intent.getIntExtra(
                EXTRA_PROJECT_INDEX, -1
        );

        commentsCount = intent.getIntExtra(
                EXTRA_COMMENTS, 0
        );

        String name = intent.getStringExtra(EXTRA_PROJECT_NAME);
        String tag = intent.getStringExtra(EXTRA_PROJECT_TAG);
        String description = intent.getStringExtra(
                EXTRA_PROJECT_DESCRIPTION
        );

        projectDescription =
                description == null ? "" : description;

        tvProjectName.setText(name == null ? "" : name);
        tvProjectTag.setText(tag == null ? "" : tag);
        tvProjectDescription.setText(projectDescription);

        try {
            @SuppressWarnings("unchecked")
            ArrayList<HomeActivity.TaskData> incomingTasks =
                    (ArrayList<HomeActivity.TaskData>)
                            intent.getSerializableExtra(EXTRA_TASKS);

            if (incomingTasks != null) {
                tasks = incomingTasks;
            }
        } catch (Exception ignored) {
            tasks = new ArrayList<>();
        }

        try {
            @SuppressWarnings("unchecked")
            ArrayList<HomeActivity.AttachmentData> incomingFiles =
                    (ArrayList<HomeActivity.AttachmentData>)
                            intent.getSerializableExtra(EXTRA_ATTACHMENTS);

            if (incomingFiles != null) {
                attachments = incomingFiles;
            }
        } catch (Exception ignored) {
            attachments = new ArrayList<>();
        }
    }

    private void restoreSavedState(Bundle state) {
        try {
            @SuppressWarnings("unchecked")
            ArrayList<HomeActivity.TaskData> savedTasks =
                    (ArrayList<HomeActivity.TaskData>)
                            state.getSerializable(STATE_TASKS);

            if (savedTasks != null) {
                tasks = savedTasks;
            }
        } catch (Exception ignored) {
        }

        try {
            @SuppressWarnings("unchecked")
            ArrayList<HomeActivity.AttachmentData> savedAttachments =
                    (ArrayList<HomeActivity.AttachmentData>)
                            state.getSerializable(STATE_ATTACHMENTS);

            if (savedAttachments != null) {
                attachments = savedAttachments;
            }
        } catch (Exception ignored) {
        }

        String savedDescription =
                state.getString(STATE_DESCRIPTION);

        if (savedDescription != null) {
            projectDescription = savedDescription;
            tvProjectDescription.setText(projectDescription);
        }
    }

    // =====================================================
    // EDIT BRIEF & SCOPE
    // =====================================================

    private void setupEditBriefAndScope() {
        if (tvProjectDescription == null) {
            return;
        }

        if (!(tvProjectDescription.getParent()
                instanceof ViewGroup)) {
            return;
        }

        ViewGroup parent =
                (ViewGroup) tvProjectDescription.getParent();

        TextView editButton = new TextView(this);
        editButton.setText("Edit Project");
        editButton.setTextSize(13);
        editButton.setTypeface(null, Typeface.BOLD);
        editButton.setTextColor(BLUE);
        editButton.setGravity(Gravity.CENTER_VERTICAL);
        editButton.setPadding(
                dp(8), dp(10), dp(8), dp(10)
        );
        editButton.setClickable(true);
        editButton.setFocusable(true);

        editButton.setOnClickListener(
                v -> showEditBriefDialog()
        );

        int position =
                parent.indexOfChild(tvProjectDescription);

        if (parent instanceof LinearLayout) {
            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(-2, -2);

            params.topMargin = dp(6);

            parent.addView(
                    editButton,
                    position + 1,
                    params
            );
        } else {
            tvProjectDescription.setOnClickListener(
                    v -> showEditBriefDialog()
            );
        }
    }

    private void showEditBriefDialog() {
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(
                dp(20), dp(12), dp(20), dp(8)
        );

        TextView label = new TextView(this);
        label.setText("PROJECT DESCRIPTION");
        label.setTextSize(12);
        label.setTypeface(null, Typeface.BOLD);
        label.setTextColor(DARK);
        content.addView(label);

        EditText descriptionInput = new EditText(this);
        descriptionInput.setText(projectDescription);
        descriptionInput.setSelection(
                descriptionInput.getText().length()
        );

        descriptionInput.setHint("Describe the project...");
        descriptionInput.setTextSize(14);
        descriptionInput.setTextColor(DARK);
        descriptionInput.setHintTextColor(MUTED);
        descriptionInput.setGravity(Gravity.TOP);
        descriptionInput.setMinLines(4);
        descriptionInput.setMaxLines(8);

        descriptionInput.setInputType(
                InputType.TYPE_CLASS_TEXT
                        | InputType.TYPE_TEXT_FLAG_MULTI_LINE
                        | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
        );

        descriptionInput.setPadding(
                dp(12), dp(12), dp(12), dp(12)
        );

        descriptionInput.setBackground(
                roundedBackground(LIGHT_BG, BORDER, 12)
        );

        LinearLayout.LayoutParams inputParams =
                new LinearLayout.LayoutParams(-1, -2);

        inputParams.topMargin = dp(12);

        content.addView(descriptionInput, inputParams);

        TextView helper = new TextView(this);
        helper.setText(
                "This description will also appear on the Home project card."
        );
        helper.setTextSize(12);
        helper.setTextColor(SECONDARY);

        LinearLayout.LayoutParams helperParams =
                new LinearLayout.LayoutParams(-1, -2);

        helperParams.topMargin = dp(10);

        content.addView(helper, helperParams);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Edit Brief & Scope")
                .setView(content)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save Changes", null)
                .create();

        dialog.setOnShowListener(ignored -> {
            Button saveButton =
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE);

            saveButton.setTextColor(BLUE);

            saveButton.setOnClickListener(v -> {
                String updated =
                        descriptionInput.getText()
                                .toString()
                                .trim();

                if (updated.isEmpty()) {
                    descriptionInput.setError(
                            "Project description cannot be empty"
                    );
                    descriptionInput.requestFocus();
                    return;
                }

                projectDescription = updated;
                tvProjectDescription.setText(updated);

                Toast.makeText(
                        this,
                        "Project description updated",
                        Toast.LENGTH_SHORT
                ).show();

                dialog.dismiss();
            });
        });

        dialog.show();
    }

    // =====================================================
    // TABS
    // =====================================================

    private void setupTabs() {
        tabOverview.setOnClickListener(v -> showTab(0));
        tabTasks.setOnClickListener(v -> showTab(1));
        tabFiles.setOnClickListener(v -> showTab(2));
        tabDiscussion.setOnClickListener(v -> showTab(3));

        View quickTasks = findViewById(R.id.quickLinkTasks);
        View quickFiles = findViewById(R.id.quickLinkFiles);

        if (quickTasks != null) {
            quickTasks.setOnClickListener(v -> showTab(1));
        }

        if (quickFiles != null) {
            quickFiles.setOnClickListener(v -> showTab(2));
        }

        discussionPlaceholder = new TextView(this);
        discussionPlaceholder.setText(
                "Discussion\n\nComing Soon"
        );
        discussionPlaceholder.setGravity(Gravity.CENTER);
        discussionPlaceholder.setTextSize(18);
        discussionPlaceholder.setTextColor(SECONDARY);
        discussionPlaceholder.setPadding(
                0, dp(100), 0, dp(100)
        );

        if (llLegacyContent.getParent()
                instanceof LinearLayout) {
            LinearLayout parent =
                    (LinearLayout) llLegacyContent.getParent();

            int position =
                    parent.indexOfChild(llLegacyContent);

            parent.addView(
                    discussionPlaceholder,
                    position + 1
            );
        }

        showTab(0);
    }

    private void showTab(int tab) {
        llOverviewContent.setVisibility(
                tab == 0 ? View.VISIBLE : View.GONE
        );

        llLegacyContent.setVisibility(
                tab == 1 || tab == 2
                        ? View.VISIBLE
                        : View.GONE
        );

        if (taskSection != null) {
            taskSection.setVisibility(
                    tab == 1 ? View.VISIBLE : View.GONE
            );
        }

        if (fileSection != null) {
            fileSection.setVisibility(
                    tab == 2 ? View.VISIBLE : View.GONE
            );
        }

        if (discussionPlaceholder != null) {
            discussionPlaceholder.setVisibility(
                    tab == 3 ? View.VISIBLE : View.GONE
            );
        }

        btnAddTask.setVisibility(
                tab == 0 || tab == 1
                        ? View.VISIBLE
                        : View.GONE
        );

        TextView[] tabs = {
                tabOverview,
                tabTasks,
                tabFiles,
                tabDiscussion
        };

        for (int i = 0; i < tabs.length; i++) {
            tabs[i].setTextColor(
                    i == tab ? BLUE : SECONDARY
            );

            tabs[i].setTypeface(
                    null,
                    i == tab
                            ? Typeface.BOLD
                            : Typeface.NORMAL
            );
        }
    }

    // =====================================================
    // TODAY'S TASKS - OVERVIEW CHECKLIST
    // =====================================================

    private void setupTodayTasksOverview() {
        if (!(llOverviewContent instanceof LinearLayout)) return;
        LinearLayout overview = (LinearLayout) llOverviewContent;

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(16), dp(16), dp(16));
        card.setBackground(roundedBackground(Color.WHITE, BORDER, 14));

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(-1, -2);
        cardParams.topMargin = dp(14);
        overview.addView(card, cardParams);

        TextView heading = new TextView(this);
        heading.setText("Today's Tasks");
        heading.setTextSize(17);
        heading.setTextColor(DARK);
        heading.setTypeface(null, Typeface.BOLD);
        card.addView(heading);

        todayTasksSummary = new TextView(this);
        todayTasksSummary.setTextSize(12);
        todayTasksSummary.setTextColor(SECONDARY);
        LinearLayout.LayoutParams summaryParams =
                new LinearLayout.LayoutParams(-1, -2);
        summaryParams.topMargin = dp(6);
        card.addView(todayTasksSummary, summaryParams);

        todayTasksContainer = new LinearLayout(this);
        todayTasksContainer.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams listParams =
                new LinearLayout.LayoutParams(-1, -2);
        listParams.topMargin = dp(10);
        card.addView(todayTasksContainer, listParams);

        TextView viewAll = new TextView(this);
        viewAll.setText("View all tasks");
        viewAll.setTextSize(13);
        viewAll.setTypeface(null, Typeface.BOLD);
        viewAll.setTextColor(BLUE);
        viewAll.setPadding(0, dp(12), 0, dp(6));
        viewAll.setOnClickListener(v -> showTab(1));
        card.addView(viewAll);

        displayTodayTasks();
    }

    private void displayTodayTasks() {
        if (todayTasksContainer == null) return;
        todayTasksContainer.removeAllViews();

        long start = startOfToday();
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.setTimeInMillis(start);
        tomorrow.add(Calendar.DAY_OF_MONTH, 1);
        long end = tomorrow.getTimeInMillis();

        int totalToday = 0;
        int completedToday = 0;
        for (HomeActivity.TaskData task : tasks) {
            if (task == null || task.dueDateMillis < start
                    || task.dueDateMillis >= end) continue;

            totalToday++;
            if (task.completed) completedToday++;

            CheckBox checkbox = new CheckBox(this);
            checkbox.setText(task.description == null ? "" : task.description);
            checkbox.setTextSize(14);
            checkbox.setTextColor(task.completed ? SECONDARY : DARK);
            checkbox.setButtonTintList(ColorStateList.valueOf(BLUE));
            checkbox.setChecked(task.completed);
            checkbox.setPadding(dp(2), dp(5), dp(2), dp(5));
            checkbox.setPaintFlags(task.completed
                    ? checkbox.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG
                    : checkbox.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            checkbox.setOnCheckedChangeListener((button, checked) -> {
                task.setCompleted(checked);
                displayTasks();
            });
            todayTasksContainer.addView(checkbox);
        }

        todayTasksSummary.setText(completedToday + " of " + totalToday
                + " tasks completed today");
        if (totalToday == 0) {
            TextView empty = new TextView(this);
            empty.setText("No tasks scheduled for today.");
            empty.setTextSize(13);
            empty.setTextColor(SECONDARY);
            empty.setPadding(0, dp(12), 0, dp(12));
            todayTasksContainer.addView(empty);
        }
    }

    // =====================================================
    // TASK FILTERS AND SORTING
    // =====================================================

    private void setupTaskControls() {
        if (!(taskSection instanceof LinearLayout)) {
            return;
        }

        LinearLayout section =
                (LinearLayout) taskSection;

        LinearLayout controls = new LinearLayout(this);
        controls.setOrientation(LinearLayout.VERTICAL);
        controls.setPadding(
                0, dp(12), 0, dp(12)
        );

        section.addView(
                controls,
                Math.min(1, section.getChildCount())
        );

        // Search
        taskSearchInput = new EditText(this);
        taskSearchInput.setSingleLine(true);
        taskSearchInput.setTextSize(14);
        taskSearchInput.setTextColor(DARK);
        taskSearchInput.setHintTextColor(MUTED);
        taskSearchInput.setHint("Search tasks in project...");
        taskSearchInput.setPadding(
                dp(14), 0, dp(14), 0
        );

        taskSearchInput.setBackground(
                roundedBackground(Color.WHITE, BORDER, 10)
        );

        controls.addView(
                taskSearchInput,
                new LinearLayout.LayoutParams(-1, dp(46))
        );

        taskSearchInput.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(
                            CharSequence s,
                            int start,
                            int count,
                            int after
                    ) {
                    }

                    @Override
                    public void onTextChanged(
                            CharSequence s,
                            int start,
                            int before,
                            int count
                    ) {
                        taskSearchQuery = s.toString();

                        if (!resettingFilters) {
                            displayTasks();
                        }
                    }

                    @Override
                    public void afterTextChanged(
                            Editable s
                    ) {
                    }
                }
        );

        // Compact horizontally scrollable filter row.
        // This keeps all four controls on one line
        // without clipping their labels.
        HorizontalScrollView filterScroll =
                new HorizontalScrollView(this);

        filterScroll.setHorizontalScrollBarEnabled(false);
        filterScroll.setFillViewport(false);

        LinearLayout.LayoutParams filterScrollParams =
                new LinearLayout.LayoutParams(-1, dp(46));

        filterScrollParams.topMargin = dp(10);
        controls.addView(filterScroll, filterScrollParams);

        LinearLayout filterRow = new LinearLayout(this);
        filterRow.setOrientation(LinearLayout.HORIZONTAL);
        filterRow.setGravity(Gravity.CENTER_VERTICAL);

        filterScroll.addView(filterRow);

        statusFilterSpinner = createFilterSpinner(
                new String[]{
                        "Status: All",
                        "Status: To do",
                        "Status: In Progress",
                        "Status: Done",
                        "Status: Overdue"
                }
        );

        priorityFilterSpinner = createFilterSpinner(
                new String[]{
                        "Priority: All",
                        "Priority: Low",
                        "Priority: Normal",
                        "Priority: High"
                }
        );

        dateFilterSpinner = createFilterSpinner(
                new String[]{
                        "Date: All",
                        "Date: Overdue",
                        "Date: Today",
                        "Date: Next 7 Days",
                        "Date: No Date"
                }
        );

        sortSpinner = createFilterSpinner(
                new String[]{
                        "Sort: Default",
                        "Sort: Due Earliest",
                        "Sort: Due Latest",
                        "Sort: High Priority",
                        "Sort: Name A-Z"
                }
        );

        addCompactSpinner(filterRow, statusFilterSpinner, 112);
        addCompactSpinner(filterRow, priorityFilterSpinner, 124);
        addCompactSpinner(filterRow, dateFilterSpinner, 112);
        addCompactSpinner(filterRow, sortSpinner, 150);

        statusFilterSpinner.setOnItemSelectedListener(
                new SimpleSelectionListener(position -> {
                    statusFilter = position;

                    if (!resettingFilters) {
                        displayTasks();
                    }
                })
        );

        priorityFilterSpinner.setOnItemSelectedListener(
                new SimpleSelectionListener(position -> {
                    priorityFilter = position;

                    if (!resettingFilters) {
                        displayTasks();
                    }
                })
        );

        dateFilterSpinner.setOnItemSelectedListener(
                new SimpleSelectionListener(position -> {
                    dateFilter = position;

                    if (!resettingFilters) {
                        displayTasks();
                    }
                })
        );

        sortSpinner.setOnItemSelectedListener(
                new SimpleSelectionListener(position -> {
                    sortOption = position;

                    if (!resettingFilters) {
                        displayTasks();
                    }
                })
        );

        // Result count + Reset Filters
        LinearLayout resultsRow = new LinearLayout(this);
        resultsRow.setOrientation(LinearLayout.HORIZONTAL);
        resultsRow.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout.LayoutParams resultsParams =
                new LinearLayout.LayoutParams(-1, -2);

        resultsParams.topMargin = dp(10);
        controls.addView(resultsRow, resultsParams);

        taskResultsCount = new TextView(this);
        taskResultsCount.setTextSize(12);
        taskResultsCount.setTextColor(SECONDARY);

        resultsRow.addView(
                taskResultsCount,
                new LinearLayout.LayoutParams(
                        0, dp(40), 1f
                )
        );

        taskResultsCount.setGravity(Gravity.CENTER_VERTICAL);

        resetFiltersButton = new TextView(this);
        resetFiltersButton.setText("Reset filters");
        resetFiltersButton.setTextSize(12);
        resetFiltersButton.setTypeface(null, Typeface.BOLD);
        resetFiltersButton.setTextColor(BLUE);
        resetFiltersButton.setGravity(Gravity.CENTER);
        resetFiltersButton.setPadding(
                dp(8), dp(8), dp(4), dp(8)
        );
        resetFiltersButton.setClickable(true);
        resetFiltersButton.setFocusable(true);

        resultsRow.addView(resetFiltersButton);

        resetFiltersButton.setOnClickListener(
                v -> resetTaskFilters()
        );

        emptyTaskMessage = new TextView(this);
        emptyTaskMessage.setTextSize(14);
        emptyTaskMessage.setTextColor(SECONDARY);
        emptyTaskMessage.setGravity(Gravity.CENTER);
        emptyTaskMessage.setPadding(
                dp(8), dp(32), dp(8), dp(32)
        );

        section.addView(emptyTaskMessage);
    }

    private Spinner createFilterSpinner(String[] options) {
        Spinner spinner = new Spinner(this);

        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(
                        this,
                        android.R.layout.simple_spinner_item,
                        options
                ) {
                    @Override
                    public View getView(
                            int position,
                            View convertView,
                            ViewGroup parent
                    ) {
                        View view = super.getView(
                                position, convertView, parent
                        );

                        if (view instanceof TextView) {
                            TextView text = (TextView) view;
                            text.setTextColor(DARK);
                            text.setTextSize(12);
                            text.setSingleLine(true);
                            text.setPadding(
                                    dp(8), 0, dp(4), 0
                            );
                        }

                        return view;
                    }

                    @Override
                    public View getDropDownView(
                            int position,
                            View convertView,
                            ViewGroup parent
                    ) {
                        View view = super.getDropDownView(
                                position, convertView, parent
                        );

                        if (view instanceof TextView) {
                            TextView text = (TextView) view;
                            text.setTextColor(DARK);
                            text.setTextSize(14);
                            text.setPadding(
                                    dp(14), dp(12),
                                    dp(14), dp(12)
                            );
                        }

                        return view;
                    }
                };

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinner.setAdapter(adapter);
        spinner.setBackground(
                roundedBackground(Color.WHITE, BORDER, 8)
        );

        return spinner;
    }

    private void addCompactSpinner(
            LinearLayout row,
            Spinner spinner,
            int widthDp
    ) {
        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        dp(widthDp), dp(38)
                );

        params.rightMargin = dp(7);
        row.addView(spinner, params);
    }

    private void resetTaskFilters() {
        resettingFilters = true;

        taskSearchQuery = "";
        statusFilter = 0;
        priorityFilter = 0;
        dateFilter = 0;
        sortOption = 0;

        if (taskSearchInput != null) {
            taskSearchInput.setText("");
        }

        if (statusFilterSpinner != null) {
            statusFilterSpinner.setSelection(0);
        }

        if (priorityFilterSpinner != null) {
            priorityFilterSpinner.setSelection(0);
        }

        if (dateFilterSpinner != null) {
            dateFilterSpinner.setSelection(0);
        }

        if (sortSpinner != null) {
            sortSpinner.setSelection(0);
        }

        resettingFilters = false;

        displayTasks();
    }

    private void displayTasks() {
        if (llTasks == null) {
            return;
        }

        llTasks.removeAllViews();

        ArrayList<HomeActivity.TaskData> filtered =
                new ArrayList<>();

        for (HomeActivity.TaskData task : tasks) {
            if (task != null && matchesTaskFilters(task)) {
                filtered.add(task);
            }
        }

        sortTasks(filtered);

        for (HomeActivity.TaskData task : filtered) {
            addTaskView(task);
        }

        if (taskResultsCount != null) {
            taskResultsCount.setText(
                    "Showing " + filtered.size()
                            + " of " + tasks.size()
                            + " tasks"
            );
        }

        if (resetFiltersButton != null) {
            boolean hasFilters =
                    !taskSearchQuery.isEmpty()
                            || statusFilter != 0
                            || priorityFilter != 0
                            || dateFilter != 0
                            || sortOption != 0;

            resetFiltersButton.setVisibility(
                    hasFilters ? View.VISIBLE : View.VISIBLE
            );

            resetFiltersButton.setAlpha(
                    hasFilters ? 1f : 0.65f
            );
        }

        if (emptyTaskMessage != null) {
            emptyTaskMessage.setVisibility(
                    filtered.isEmpty()
                            ? View.VISIBLE
                            : View.GONE
            );

            emptyTaskMessage.setText(
                    tasks.isEmpty()
                            ? "No tasks yet. Add your first task."
                            : "No tasks match your filters."
            );
        }

        updateTaskCount();
        displayTodayTasks();
    }

    private boolean matchesTaskFilters(
            HomeActivity.TaskData task
    ) {
        String description =
                task.description == null
                        ? ""
                        : task.description;

        if (!description.toLowerCase(Locale.ROOT).contains(
                taskSearchQuery.toLowerCase(Locale.ROOT)
        )) {
            return false;
        }

        String status = task.getStatus();
        String priority = task.getPriority();

        switch (statusFilter) {
            case 1:
                if (!HomeActivity.TaskData.STATUS_TODO.equals(status)) {
                    return false;
                }
                break;

            case 2:
                if (!HomeActivity.TaskData.STATUS_IN_PROGRESS.equals(status)) {
                    return false;
                }
                break;

            case 3:
                if (!HomeActivity.TaskData.STATUS_DONE.equals(status)) {
                    return false;
                }
                break;

            case 4:
                if (!task.isOverdue()) {
                    return false;
                }
                break;

            default:
                break;
        }

        switch (priorityFilter) {
            case 1:
                if (!HomeActivity.TaskData.PRIORITY_LOW.equals(priority)) {
                    return false;
                }
                break;

            case 2:
                if (!HomeActivity.TaskData.PRIORITY_NORMAL.equals(priority)) {
                    return false;
                }
                break;

            case 3:
                if (!HomeActivity.TaskData.PRIORITY_HIGH.equals(priority)) {
                    return false;
                }
                break;

            default:
                break;
        }

        if (dateFilter == 0) {
            return true;
        }

        if (dateFilter == 4) {
            return task.dueDateMillis <= 0L;
        }

        if (dateFilter == 1) {
            return task.isOverdue();
        }

        if (task.dueDateMillis <= 0L) {
            return false;
        }

        long today = startOfToday();
        long due = task.dueDateMillis;

        Calendar tomorrow = Calendar.getInstance();
        tomorrow.setTimeInMillis(today);
        tomorrow.add(Calendar.DAY_OF_MONTH, 1);

        if (dateFilter == 2) {
            return due >= today
                    && due < tomorrow.getTimeInMillis();
        }

        if (dateFilter == 3) {
            Calendar end = Calendar.getInstance();
            end.setTimeInMillis(today);
            end.add(Calendar.DAY_OF_MONTH, 7);

            return due >= today
                    && due < end.getTimeInMillis();
        }

        return true;
    }

    private void sortTasks(
            ArrayList<HomeActivity.TaskData> list
    ) {
        switch (sortOption) {
            case 1:
                Collections.sort(
                        list,
                        (a, b) -> Long.compare(
                                sortableDueDate(a),
                                sortableDueDate(b)
                        )
                );
                break;

            case 2:
                Collections.sort(
                        list,
                        (a, b) -> Long.compare(
                                sortableDueDate(b),
                                sortableDueDate(a)
                        )
                );
                break;

            case 3:
                Collections.sort(
                        list,
                        (a, b) -> Integer.compare(
                                priorityRank(b),
                                priorityRank(a)
                        )
                );
                break;

            case 4:
                Collections.sort(
                        list,
                        (a, b) -> {
                            String first =
                                    a.description == null
                                            ? ""
                                            : a.description;

                            String second =
                                    b.description == null
                                            ? ""
                                            : b.description;

                            return first.compareToIgnoreCase(second);
                        }
                );
                break;

            default:
                break;
        }
    }

    private long sortableDueDate(
            HomeActivity.TaskData task
    ) {
        return task.dueDateMillis <= 0L
                ? Long.MAX_VALUE
                : task.dueDateMillis;
    }

    private int priorityRank(
            HomeActivity.TaskData task
    ) {
        String priority = task.getPriority();

        if (HomeActivity.TaskData.PRIORITY_HIGH.equals(priority)) {
            return 3;
        }

        if (HomeActivity.TaskData.PRIORITY_NORMAL.equals(priority)) {
            return 2;
        }

        return 1;
    }

    private long startOfToday() {
        Calendar calendar = Calendar.getInstance();

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTimeInMillis();
    }

    private void addTaskView(
            HomeActivity.TaskData task
    ) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(
                dp(12), dp(10), dp(12), dp(12)
        );

        card.setBackground(
                roundedBackground(
                        task.completed
                                ? Color.rgb(250, 251, 253)
                                : Color.WHITE,
                        BORDER,
                        10
                )
        );

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(-1, -2);

        cardParams.bottomMargin = dp(8);
        llTasks.addView(card, cardParams);

        CheckBox checkbox = new CheckBox(this);

        checkbox.setText(
                task.description == null
                        ? ""
                        : task.description
        );

        checkbox.setTextSize(14);
        checkbox.setTypeface(null, Typeface.BOLD);
        checkbox.setTextColor(
                task.completed ? SECONDARY : DARK
        );
        checkbox.setChecked(task.completed);

        checkbox.setButtonTintList(
                ColorStateList.valueOf(BLUE)
        );

        // Match the reference: completed task titles
        // are visibly struck through.
        if (task.completed) {
            checkbox.setPaintFlags(
                    checkbox.getPaintFlags()
                            | Paint.STRIKE_THRU_TEXT_FLAG
            );
        } else {
            checkbox.setPaintFlags(
                    checkbox.getPaintFlags()
                            & ~Paint.STRIKE_THRU_TEXT_FLAG
            );
        }

        card.addView(checkbox);

        TextView metadata = new TextView(this);
        metadata.setTextSize(12);
        metadata.setTextColor(
                task.isOverdue() ? RED : SECONDARY
        );
        metadata.setPadding(
                dp(35), 0, 0, dp(2)
        );

        String dueText = "No due date";

        if (task.dueDateMillis > 0L) {
            dueText = new SimpleDateFormat(
                    "dd MMM yyyy",
                    Locale.getDefault()
            ).format(
                    new Date(task.dueDateMillis)
            );

            if (task.isOverdue()) {
                dueText += " (Overdue)";
            }
        }

        metadata.setText(
                task.getStatus()
                        + " • "
                        + task.getPriority()
                        + " • "
                        + dueText
        );

        card.addView(metadata);

        checkbox.setOnCheckedChangeListener(
                (button, checked) -> {
                    task.setCompleted(checked);
                    displayTasks();
                }
        );
    }

    private void updateTaskCount() {
        int total = tasks.size();
        int done = 0;
        int inProgress = 0;
        int overdue = 0;
        int todo = 0;

        for (HomeActivity.TaskData task : tasks) {
            if (task == null) {
                continue;
            }

            if (task.completed) {
                done++;
            } else if (
                    HomeActivity.TaskData.STATUS_IN_PROGRESS.equals(
                            task.getStatus()
                    )
            ) {
                inProgress++;
            } else {
                todo++;
            }

            if (task.isOverdue()) {
                overdue++;
            }
        }

        int percentage =
                total == 0
                        ? 0
                        : Math.round(100f * done / total);

        tvTaskCount.setText(
                done + "/" + total + " tasks completed"
        );

        pbProjectProgress.setProgress(percentage);

        tvProgressPercent.setText(
                percentage + "% Complete"
        );

        tvBreakdown.setText(
                "To Do: " + todo
                        + "    In Progress: " + inProgress
                        + "\nDone: " + done
                        + "    Overdue: " + overdue
        );

        tabTasks.setText("Tasks (" + total + ")");
        tabFiles.setText(
                "Files (" + attachments.size() + ")"
        );
    }

    // =====================================================
    // ADD TASK
    // =====================================================

    private void setupAddTaskButton() {
        btnAddTask.setOnClickListener(
                v -> showAddTaskDialog()
        );
    }

    private void showAddTaskDialog() {
        AlertDialog dialog =
                new AlertDialog.Builder(this).create();

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(
                dp(22), dp(20), dp(22), dp(24)
        );

        content.setBackground(
                roundedBackground(
                        Color.WHITE,
                        Color.WHITE,
                        20
                )
        );

        TextView title = new TextView(this);
        title.setText("Add New Task");
        title.setTextSize(20);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextColor(DARK);
        content.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText(
                tvProjectName.getText().toString()
        );
        subtitle.setTextSize(13);
        subtitle.setTextColor(SECONDARY);

        LinearLayout.LayoutParams subtitleParams =
                new LinearLayout.LayoutParams(-1, -2);

        subtitleParams.topMargin = dp(6);
        content.addView(subtitle, subtitleParams);

        EditText description = new EditText(this);
        description.setHint("Enter task description");
        description.setTextSize(14);
        description.setTextColor(DARK);
        description.setHintTextColor(MUTED);
        description.setGravity(Gravity.TOP);
        description.setMinLines(3);
        description.setMaxLines(5);

        description.setFilters(
                new InputFilter[]{
                        new InputFilter.LengthFilter(500)
                }
        );

        description.setPadding(
                dp(12), dp(12), dp(12), dp(12)
        );

        description.setBackground(
                roundedBackground(LIGHT_BG, BORDER, 12)
        );

        LinearLayout.LayoutParams descriptionParams =
                new LinearLayout.LayoutParams(-1, -2);

        descriptionParams.topMargin = dp(16);
        content.addView(description, descriptionParams);

        TextView counter = new TextView(this);
        counter.setText("0 / 500 characters");
        counter.setTextSize(11);
        counter.setTextColor(SECONDARY);
        counter.setGravity(Gravity.END);
        content.addView(counter);

        description.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(
                            CharSequence s,
                            int start,
                            int count,
                            int after
                    ) {
                    }

                    @Override
                    public void onTextChanged(
                            CharSequence s,
                            int start,
                            int before,
                            int count
                    ) {
                        counter.setText(
                                s.length()
                                        + " / 500 characters"
                        );
                    }

                    @Override
                    public void afterTextChanged(
                            Editable s
                    ) {
                    }
                }
        );

        addDialogLabel(content, "STATUS");

        String[] statuses = {
                "To do",
                "In Progress",
                "Done"
        };

        Spinner statusSpinner =
                createFilterSpinner(statuses);

        content.addView(
                statusSpinner,
                new LinearLayout.LayoutParams(-1, dp(48))
        );

        addDialogLabel(content, "PRIORITY");

        String[] priorities = {
                "Normal",
                "Low",
                "High"
        };

        Spinner prioritySpinner =
                createFilterSpinner(priorities);

        content.addView(
                prioritySpinner,
                new LinearLayout.LayoutParams(-1, dp(48))
        );

        addDialogLabel(content, "DUE DATE (OPTIONAL)");

        final long[] selectedDueDate = {0L};

        Button dateButton = new Button(this);
        dateButton.setText("Select due date");
        dateButton.setAllCaps(false);

        content.addView(
                dateButton,
                new LinearLayout.LayoutParams(-1, dp(48))
        );

        dateButton.setOnClickListener(v -> {
            Calendar today = Calendar.getInstance();

            DatePickerDialog picker =
                    new DatePickerDialog(
                            this,
                            (view, year, month, day) -> {
                                Calendar selected =
                                        Calendar.getInstance();

                                selected.clear();
                                selected.set(year, month, day);

                                selectedDueDate[0] =
                                        selected.getTimeInMillis();

                                dateButton.setText(
                                        new SimpleDateFormat(
                                                "dd MMM yyyy",
                                                Locale.getDefault()
                                        ).format(
                                                selected.getTime()
                                        )
                                );
                            },
                            today.get(Calendar.YEAR),
                            today.get(Calendar.MONTH),
                            today.get(Calendar.DAY_OF_MONTH)
                    );

            picker.show();
        });

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout.LayoutParams actionsParams =
                new LinearLayout.LayoutParams(-1, dp(50));

        actionsParams.topMargin = dp(18);
        content.addView(actions, actionsParams);

        Button cancel = new Button(this);
        cancel.setText("Cancel");
        cancel.setAllCaps(false);
        cancel.setOnClickListener(v -> dialog.dismiss());

        actions.addView(
                cancel,
                new LinearLayout.LayoutParams(0, -1, 1f)
        );

        Button add = new Button(this);
        add.setText("Add Task");
        add.setAllCaps(false);
        add.setTextColor(Color.WHITE);

        add.setBackgroundTintList(
                ColorStateList.valueOf(BLUE)
        );

        LinearLayout.LayoutParams addParams =
                new LinearLayout.LayoutParams(0, -1, 1f);

        addParams.leftMargin = dp(10);
        actions.addView(add, addParams);

        add.setOnClickListener(v -> {
            String text =
                    description.getText().toString().trim();

            if (text.isEmpty()) {
                description.setError(
                        "Task description is required"
                );
                description.requestFocus();
                return;
            }

            HomeActivity.TaskData task =
                    new HomeActivity.TaskData(text);

            task.setStatus(
                    statuses[
                            statusSpinner.getSelectedItemPosition()
                            ]
            );

            task.priority =
                    priorities[
                            prioritySpinner.getSelectedItemPosition()
                            ];

            task.dueDateMillis = selectedDueDate[0];
            tasks.add(task);

            resetTaskFilters();

            displayTasks();
            showTab(1);
            dialog.dismiss();

            Toast.makeText(
                    this,
                    "Task added",
                    Toast.LENGTH_SHORT
            ).show();
        });

        ScrollView scroll = new ScrollView(this);
        scroll.addView(content);

        dialog.setView(scroll);
        dialog.show();

        Window window = dialog.getWindow();

        if (window != null) {
            window.setBackgroundDrawableResource(
                    android.R.color.transparent
            );

            window.setGravity(Gravity.BOTTOM);

            window.setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
                            | WindowManager.LayoutParams
                            .SOFT_INPUT_STATE_ALWAYS_HIDDEN
            );

            window.setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT
            );
        }
    }

    private void addDialogLabel(
            LinearLayout content,
            String text
    ) {
        TextView label = new TextView(this);
        label.setText(text);
        label.setTextSize(11);
        label.setTypeface(null, Typeface.BOLD);
        label.setTextColor(DARK);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(-1, -2);

        params.topMargin = dp(14);
        content.addView(label, params);
    }

    // =====================================================
    // FILES TAB
    // =====================================================

    private void setupFileControls() {
        if (!(fileSection instanceof LinearLayout)) {
            return;
        }

        LinearLayout section =
                (LinearLayout) fileSection;

        fileSearchInput = new EditText(this);
        fileSearchInput.setSingleLine(true);
        fileSearchInput.setTextSize(14);
        fileSearchInput.setTextColor(DARK);
        fileSearchInput.setHintTextColor(MUTED);
        fileSearchInput.setHint("Search files...");
        fileSearchInput.setPadding(
                dp(14), 0, dp(14), 0
        );

        fileSearchInput.setBackground(
                roundedBackground(Color.WHITE, BORDER, 12)
        );

        LinearLayout.LayoutParams searchParams =
                new LinearLayout.LayoutParams(-1, dp(48));

        searchParams.topMargin = dp(12);

        section.addView(
                fileSearchInput,
                Math.min(1, section.getChildCount()),
                searchParams
        );

        fileSearchInput.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(
                            CharSequence s,
                            int start,
                            int count,
                            int after
                    ) {
                    }

                    @Override
                    public void onTextChanged(
                            CharSequence s,
                            int start,
                            int before,
                            int count
                    ) {
                        fileSearchQuery = s.toString();
                        displayAttachments();
                    }

                    @Override
                    public void afterTextChanged(
                            Editable s
                    ) {
                    }
                }
        );

        HorizontalScrollView categoryScroll =
                new HorizontalScrollView(this);

        categoryScroll.setHorizontalScrollBarEnabled(false);

        LinearLayout.LayoutParams categoryParams =
                new LinearLayout.LayoutParams(-1, dp(48));

        categoryParams.topMargin = dp(12);

        section.addView(categoryScroll, categoryParams);

        fileCategoryRow = new LinearLayout(this);
        fileCategoryRow.setOrientation(LinearLayout.HORIZONTAL);
        fileCategoryRow.setGravity(Gravity.CENTER_VERTICAL);

        categoryScroll.addView(fileCategoryRow);

        fileCountLabel = new TextView(this);
        fileCountLabel.setTextSize(14);
        fileCountLabel.setTypeface(null, Typeface.BOLD);
        fileCountLabel.setTextColor(DARK);

        LinearLayout.LayoutParams countParams =
                new LinearLayout.LayoutParams(-1, -2);

        countParams.topMargin = dp(16);
        countParams.bottomMargin = dp(10);

        section.addView(fileCountLabel, countParams);

        if (llAttachments != null) {
            llAttachments.setVisibility(View.GONE);
        }

        fileListContainer = new LinearLayout(this);
        fileListContainer.setOrientation(LinearLayout.VERTICAL);

        section.addView(
                fileListContainer,
                new LinearLayout.LayoutParams(-1, -2)
        );

        emptyFileMessage = new TextView(this);
        emptyFileMessage.setTextSize(14);
        emptyFileMessage.setTextColor(SECONDARY);
        emptyFileMessage.setGravity(Gravity.CENTER);
        emptyFileMessage.setPadding(
                dp(8), dp(30), dp(8), dp(30)
        );

        section.addView(emptyFileMessage);

        addAttachmentButton = new Button(this);
        addAttachmentButton.setText("+ Add Attachment");
        addAttachmentButton.setAllCaps(false);
        addAttachmentButton.setTextColor(Color.WHITE);

        addAttachmentButton.setBackgroundTintList(
                ColorStateList.valueOf(BLUE)
        );

        LinearLayout.LayoutParams buttonParams =
                new LinearLayout.LayoutParams(-1, dp(52));

        buttonParams.topMargin = dp(16);
        buttonParams.bottomMargin = dp(16);

        section.addView(
                addAttachmentButton,
                buttonParams
        );

        addAttachmentButton.setOnClickListener(
                v -> launchAttachmentPicker()
        );
    }

    private void displayAttachments() {
        if (fileListContainer == null) {
            return;
        }

        fileListContainer.removeAllViews();

        int visibleCount = 0;

        for (HomeActivity.AttachmentData attachment : attachments) {
            if (attachment == null) {
                continue;
            }

            String name = attachment.fileName == null
                    ? "Attachment"
                    : attachment.fileName;

            if (!name.toLowerCase(Locale.ROOT).contains(
                    fileSearchQuery.toLowerCase(Locale.ROOT)
            )) {
                continue;
            }

            String category = getFileCategory(name);

            if (!selectedFileCategory.equals("All")
                    && !selectedFileCategory.equals(category)) {
                continue;
            }

            addFileListItem(attachment);
            visibleCount++;
        }

        if (fileCountLabel != null) {
            fileCountLabel.setText(
                    visibleCount + " file"
                            + (visibleCount == 1 ? "" : "s")
            );
        }

        if (emptyFileMessage != null) {
            emptyFileMessage.setVisibility(
                    visibleCount == 0
                            ? View.VISIBLE
                            : View.GONE
            );

            emptyFileMessage.setText(
                    attachments.isEmpty()
                            ? "No attachments yet."
                            : "No matching files found."
            );
        }

        updateFileCategoryButtons();
        updateTaskCount();
    }

    private String getFileCategory(String fileName) {
        String lower =
                fileName.toLowerCase(Locale.ROOT);

        if (lower.endsWith(".png")
                || lower.endsWith(".jpg")
                || lower.endsWith(".jpeg")
                || lower.endsWith(".gif")
                || lower.endsWith(".webp")
                || lower.endsWith(".bmp")
                || lower.endsWith(".heic")) {
            return "Images";
        }

        if (lower.endsWith(".txt")
                || lower.endsWith(".rtf")
                || lower.endsWith(".doc")
                || lower.endsWith(".docx")) {
            return "Text";
        }

        if (lower.endsWith(".pdf")) {
            return "PDF";
        }

        return "Other";
    }

    private void updateFileCategoryButtons() {
        if (fileCategoryRow == null) {
            return;
        }

        fileCategoryRow.removeAllViews();

        String[] categories = {
                "All", "Images", "Text", "PDF", "Other"
        };

        for (String category : categories) {
            int count = 0;

            for (HomeActivity.AttachmentData attachment : attachments) {
                if (attachment == null) {
                    continue;
                }

                String name = attachment.fileName == null
                        ? ""
                        : attachment.fileName;

                if (category.equals("All")
                        || category.equals(getFileCategory(name))) {
                    count++;
                }
            }

            final String chosenCategory = category;

            TextView chip = new TextView(this);
            chip.setText(category + " (" + count + ")");
            chip.setTextSize(12);
            chip.setGravity(Gravity.CENTER);
            chip.setPadding(
                    dp(14), dp(9), dp(14), dp(9)
            );

            boolean selected =
                    selectedFileCategory.equals(category);

            chip.setTextColor(
                    selected ? Color.WHITE : SECONDARY
            );

            chip.setTypeface(
                    null,
                    selected
                            ? Typeface.BOLD
                            : Typeface.NORMAL
            );

            chip.setBackground(
                    roundedBackground(
                            selected ? BLUE : Color.WHITE,
                            selected ? BLUE : BORDER,
                            10
                    )
            );

            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(-2, -2);

            params.rightMargin = dp(8);

            fileCategoryRow.addView(chip, params);

            chip.setOnClickListener(v -> {
                selectedFileCategory = chosenCategory;
                displayAttachments();
            });
        }
    }

    private void addFileListItem(
            HomeActivity.AttachmentData attachment
    ) {
        String name = attachment.fileName == null
                ? "Attachment"
                : attachment.fileName;

        String category = getFileCategory(name);

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(
                dp(12), dp(12), dp(12), dp(12)
        );

        card.setBackground(
                roundedBackground(Color.WHITE, BORDER, 12)
        );

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(-1, -2);

        cardParams.bottomMargin = dp(10);

        fileListContainer.addView(card, cardParams);

        TextView icon = new TextView(this);
        icon.setText(getFileType(name));
        icon.setTextSize(11);
        icon.setTypeface(null, Typeface.BOLD);
        icon.setGravity(Gravity.CENTER);
        icon.setTextColor(BLUE);

        icon.setBackground(
                roundedBackground(
                        Color.rgb(229, 240, 255),
                        Color.rgb(229, 240, 255),
                        10
                )
        );

        card.addView(
                icon,
                new LinearLayout.LayoutParams(
                        dp(48), dp(48)
                )
        );

        LinearLayout details = new LinearLayout(this);
        details.setOrientation(LinearLayout.VERTICAL);
        details.setPadding(
                dp(12), 0, dp(8), 0
        );

        card.addView(
                details,
                new LinearLayout.LayoutParams(
                        0, -2, 1f
                )
        );

        TextView fileName = new TextView(this);
        fileName.setText(name);
        fileName.setTextSize(13);
        fileName.setTypeface(null, Typeface.BOLD);
        fileName.setTextColor(DARK);
        fileName.setMaxLines(2);

        details.addView(fileName);

        TextView fileInfo = new TextView(this);
        fileInfo.setText(
                category + " • "
                        + getAttachmentSize(attachment.uri)
        );
        fileInfo.setTextSize(11);
        fileInfo.setTextColor(SECONDARY);

        details.addView(fileInfo);

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.VERTICAL);

        card.addView(actions);

        TextView preview = new TextView(this);
        preview.setText("Preview");
        preview.setTextSize(12);
        preview.setTextColor(BLUE);
        preview.setGravity(Gravity.CENTER);
        preview.setPadding(
                dp(8), dp(8), dp(8), dp(8)
        );

        actions.addView(preview);

        preview.setOnClickListener(
                v -> openAttachment(attachment.uri)
        );

        TextView delete = new TextView(this);
        delete.setText("Delete");
        delete.setTextSize(12);
        delete.setTextColor(RED);
        delete.setGravity(Gravity.CENTER);
        delete.setPadding(
                dp(8), dp(8), dp(8), dp(8)
        );

        actions.addView(delete);

        delete.setOnClickListener(
                v -> confirmDeleteAttachment(attachment)
        );
    }

    private String getFileType(String name) {
        String lower =
                name.toLowerCase(Locale.ROOT);

        if (lower.endsWith(".pdf")) {
            return "PDF";
        }

        if (lower.endsWith(".png")
                || lower.endsWith(".jpg")
                || lower.endsWith(".jpeg")
                || lower.endsWith(".gif")
                || lower.endsWith(".webp")
                || lower.endsWith(".bmp")
                || lower.endsWith(".heic")) {
            return "IMG";
        }

        if (lower.endsWith(".doc")
                || lower.endsWith(".docx")) {
            return "DOC";
        }

        if (lower.endsWith(".txt")
                || lower.endsWith(".rtf")) {
            return "TXT";
        }

        if (lower.endsWith(".xls")
                || lower.endsWith(".xlsx")
                || lower.endsWith(".csv")) {
            return "XLS";
        }

        if (lower.endsWith(".ppt")
                || lower.endsWith(".pptx")) {
            return "PPT";
        }

        if (lower.endsWith(".zip")
                || lower.endsWith(".rar")
                || lower.endsWith(".7z")) {
            return "ZIP";
        }

        if (lower.endsWith(".fig")) {
            return "FIG";
        }

        return "FILE";
    }

    private String getAttachmentSize(String uriString) {
        if (uriString == null || uriString.isEmpty()) {
            return "Unknown size";
        }

        try {
            Uri uri = Uri.parse(uriString);

            try (Cursor cursor = getContentResolver().query(
                    uri,
                    new String[]{OpenableColumns.SIZE},
                    null,
                    null,
                    null
            )) {
                if (cursor != null && cursor.moveToFirst()) {
                    int column = cursor.getColumnIndex(
                            OpenableColumns.SIZE
                    );

                    if (column >= 0
                            && !cursor.isNull(column)) {
                        long bytes = cursor.getLong(column);

                        if (bytes < 1024) {
                            return bytes + " B";
                        }

                        if (bytes < 1024L * 1024L) {
                            return String.format(
                                    Locale.getDefault(),
                                    "%.1f KB",
                                    bytes / 1024.0
                            );
                        }

                        return String.format(
                                Locale.getDefault(),
                                "%.1f MB",
                                bytes / (1024.0 * 1024.0)
                        );
                    }
                }
            }
        } catch (Exception ignored) {
        }

        return "Unknown size";
    }

    private void confirmDeleteAttachment(
            HomeActivity.AttachmentData attachment
    ) {
        String name = attachment.fileName == null
                ? "this attachment"
                : attachment.fileName;

        new AlertDialog.Builder(this)
                .setTitle("Delete Attachment")
                .setMessage(
                        "Remove \"" + name
                                + "\" from this project?"
                )
                .setNegativeButton("Cancel", null)
                .setPositiveButton(
                        "Delete",
                        (dialog, which) -> {
                            attachments.remove(attachment);
                            displayAttachments();

                            Toast.makeText(
                                    this,
                                    "Attachment removed",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                )
                .show();
    }

    private void launchAttachmentPicker() {
        Intent intent = new Intent(
                Intent.ACTION_OPEN_DOCUMENT
        );

        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        intent.putExtra(
                Intent.EXTRA_ALLOW_MULTIPLE,
                true
        );

        intent.addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION
                        | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
        );

        try {
            startActivityForResult(
                    intent,
                    REQUEST_ADD_ATTACHMENT
            );
        } catch (ActivityNotFoundException exception) {
            Toast.makeText(
                    this,
                    "No document picker available",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data
    ) {
        super.onActivityResult(
                requestCode,
                resultCode,
                data
        );

        if (requestCode != REQUEST_ADD_ATTACHMENT
                || resultCode != RESULT_OK
                || data == null) {
            return;
        }

        int added = 0;

        if (data.getClipData() != null) {
            for (int i = 0;
                 i < data.getClipData().getItemCount();
                 i++) {

                Uri uri = data.getClipData()
                        .getItemAt(i)
                        .getUri();

                if (addSelectedAttachment(
                        uri,
                        data.getFlags()
                )) {
                    added++;
                }
            }
        } else if (data.getData() != null) {
            if (addSelectedAttachment(
                    data.getData(),
                    data.getFlags()
            )) {
                added++;
            }
        }

        displayAttachments();

        Toast.makeText(
                this,
                added + " attachment"
                        + (added == 1 ? "" : "s")
                        + " added",
                Toast.LENGTH_SHORT
        ).show();
    }

    private boolean addSelectedAttachment(
            Uri uri,
            int resultFlags
    ) {
        if (uri == null) {
            return false;
        }

        String uriString = uri.toString();

        for (HomeActivity.AttachmentData existing : attachments) {
            if (existing != null
                    && uriString.equals(existing.uri)) {
                return false;
            }
        }

        try {
            int flags = resultFlags
                    & Intent.FLAG_GRANT_READ_URI_PERMISSION;

            if (flags != 0) {
                getContentResolver()
                        .takePersistableUriPermission(
                                uri,
                                flags
                        );
            }
        } catch (Exception ignored) {
        }

        String name = getSelectedFileName(uri);

        HomeActivity.AttachmentData attachment =
                new HomeActivity.AttachmentData(
                        name,
                        uriString
                );

        attachments.add(attachment);
        return true;
    }

    private String getSelectedFileName(Uri uri) {
        try (Cursor cursor = getContentResolver().query(
                uri,
                new String[]{OpenableColumns.DISPLAY_NAME},
                null,
                null,
                null
        )) {
            if (cursor != null && cursor.moveToFirst()) {
                int column = cursor.getColumnIndex(
                        OpenableColumns.DISPLAY_NAME
                );

                if (column >= 0) {
                    String name = cursor.getString(column);

                    if (name != null
                            && !name.trim().isEmpty()) {
                        return name;
                    }
                }
            }
        } catch (Exception ignored) {
        }

        String lastSegment = uri.getLastPathSegment();

        return lastSegment == null
                ? "Attachment"
                : lastSegment;
    }

    private void openAttachment(String uriString) {
        if (uriString == null
                || uriString.trim().isEmpty()) {
            Toast.makeText(
                    this,
                    "Attachment unavailable",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        try {
            Uri uri = Uri.parse(uriString);

            String mime =
                    getContentResolver().getType(uri);

            Intent intent =
                    new Intent(Intent.ACTION_VIEW);

            intent.setDataAndType(
                    uri,
                    mime == null ? "*/*" : mime
            );

            intent.addFlags(
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
            );

            startActivity(intent);

        } catch (ActivityNotFoundException exception) {
            Toast.makeText(
                    this,
                    "No app available to open this file",
                    Toast.LENGTH_SHORT
            ).show();

        } catch (Exception exception) {
            Toast.makeText(
                    this,
                    "Unable to open attachment",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    // =====================================================
    // BACK NAVIGATION AND HOME SYNCHRONIZATION
    // =====================================================

    private void setupBackButton() {
        btnBack.setOnClickListener(
                v -> returnToHome()
        );

        getOnBackPressedDispatcher().addCallback(
                this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        returnToHome();
                    }
                }
        );
    }

    private void returnToHome() {
        Intent result = new Intent();

        result.putExtra(
                EXTRA_PROJECT_INDEX,
                projectIndex
        );

        result.putExtra(
                EXTRA_TASKS,
                tasks
        );

        result.putExtra(
                EXTRA_ATTACHMENTS,
                attachments
        );

        result.putExtra(
                EXTRA_PROJECT_DESCRIPTION,
                projectDescription
        );

        setResult(RESULT_OK, result);
        finish();
    }

    private void setupInviteButton() {
        if (btnInvite != null) {
            btnInvite.setOnClickListener(v ->
                    Toast.makeText(
                            this,
                            "Invitations are outside the assignment scope",
                            Toast.LENGTH_SHORT
                    ).show()
            );
        }
    }

    private void setupCommentsButton() {
        if (btnComments != null) {
            btnComments.setOnClickListener(v ->
                    Toast.makeText(
                            this,
                            "Comments are outside the assignment scope",
                            Toast.LENGTH_SHORT
                    ).show()
            );
        }
    }

    private void updateCommentsCount() {
        if (tvCommentsCount != null) {
            tvCommentsCount.setText(
                    commentsCount == 1
                            ? "1 comment"
                            : commentsCount + " comments"
            );
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(
                STATE_TASKS,
                tasks
        );

        outState.putSerializable(
                STATE_ATTACHMENTS,
                attachments
        );

        outState.putString(
                STATE_DESCRIPTION,
                projectDescription
        );

        super.onSaveInstanceState(outState);
    }

    // =====================================================
    // HELPERS
    // =====================================================

    private GradientDrawable roundedBackground(
            int backgroundColor,
            int strokeColor,
            int radiusDp
    ) {
        GradientDrawable drawable =
                new GradientDrawable();

        drawable.setColor(backgroundColor);
        drawable.setCornerRadius(dp(radiusDp));
        drawable.setStroke(
                dp(1),
                strokeColor
        );

        return drawable;
    }

    private int dp(int value) {
        return Math.round(
                value * getResources()
                        .getDisplayMetrics()
                        .density
        );
    }

    private interface SelectionCallback {
        void onSelected(int position);
    }

    private static class SimpleSelectionListener
            implements AdapterView.OnItemSelectedListener {

        private final SelectionCallback callback;

        SimpleSelectionListener(
                SelectionCallback callback
        ) {
            this.callback = callback;
        }

        @Override
        public void onItemSelected(
                AdapterView<?> parent,
                View view,
                int position,
                long id
        ) {
            callback.onSelected(position);
        }

        @Override
        public void onNothingSelected(
                AdapterView<?> parent
        ) {
        }
    }
}
