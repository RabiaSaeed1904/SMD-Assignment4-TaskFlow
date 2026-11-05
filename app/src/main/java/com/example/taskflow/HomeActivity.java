package com.example.taskflow;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {

    private static final int REQUEST_PROJECT = 100;
    private static final int REQUEST_ATTACHMENT = 200;

    private static final String PREFS_NAME = "taskflow_workspace";
    private static final String KEY_CREATION_DATE = "creation_date";
    private static final String KEY_PROJECTS = "saved_projects";

    private LinearLayout llUpcomingProjects;
    private TextView tvUpcomingCount;
    private TextView tvNoProjects;
    private TextView tvWorkspaceCreated;
    private Button btnNewProject;

    private Button btnDeleteProject;
    private EditText etSearchProjects;
    private String searchQuery = "";

    private Button DeleteProject;

    // Contains only projects created by the user.
    private final ArrayList<ProjectData> projects =
            new ArrayList<>();

    // Temporary attachments for the New Project dialog.
    private final ArrayList<AttachmentData> selectedAttachments =
            new ArrayList<>();

    private TextView selectedAttachmentText;

    // Sample project is separate from Upcoming projects.
    private ProjectData sampleProject;

    private boolean sampleProjectOpened = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initializeViews();
        setupWorkspaceDate();

        createSampleProject();
        restoreProjects();

        displayProjects();
        setupNewProjectButton();
        setupProjectSearch();
        setupBackButton();
    }

    private void initializeViews() {
        llUpcomingProjects =
                findViewById(R.id.llUpcomingProjects);

        tvUpcomingCount =
                findViewById(R.id.tvUpcomingCount);

        tvNoProjects =
                findViewById(R.id.tvNoProjects);

        tvWorkspaceCreated =
                findViewById(R.id.tvWorkspaceCreated);

        btnNewProject =
                findViewById(R.id.btnNewProject);
        btnDeleteProject =
                findViewById(R.id.btndeleteproject);

        etSearchProjects = findViewById(R.id.etSearchProjects);
    }

    /**
     * Save the workspace creation date on first launch.
     */
    private void setupWorkspaceDate() {
        SharedPreferences preferences =
                getSharedPreferences(
                        PREFS_NAME,
                        MODE_PRIVATE
                );

        String creationDate =
                preferences.getString(
                        KEY_CREATION_DATE,
                        null
                );

        if (creationDate == null) {
            creationDate =
                    new SimpleDateFormat(
                            "dd MMM yyyy",
                            Locale.ENGLISH
                    ).format(new Date());

            preferences.edit()
                    .putString(
                            KEY_CREATION_DATE,
                            creationDate
                    )
                    .apply();
        }

        tvWorkspaceCreated.setText(
                "Created : " + creationDate
        );
    }

    /**
     * Create a separate example project.
     *
     * The sample is never added to the projects list.
     */
    private void createSampleProject() {
        sampleProject = new ProjectData();

        sampleProject.name =
                "Service Page Website";

        sampleProject.tag =
                "User Interface";

        sampleProject.description =
                "Make a page displaying services for the company website "
                        + "with blue and red colour information on each product card.";

        sampleProject.comments = 10;

        sampleProject.attachments =
                new ArrayList<>();

        sampleProject.tasks =
                new ArrayList<>();
    }

    /**
     * Restore saved user-created projects.
     *
     * Removes the automatically generated sample
     * from older versions of TaskFlow.
     */
    private void restoreProjects() {
        projects.clear();

        SharedPreferences preferences =
                getSharedPreferences(
                        PREFS_NAME,
                        MODE_PRIVATE
                );

        String savedProjects =
                preferences.getString(
                        KEY_PROJECTS,
                        null
                );

        if (savedProjects == null) {
            return;
        }

        try {
            byte[] bytes =
                    android.util.Base64.decode(
                            savedProjects,
                            android.util.Base64.DEFAULT
                    );

            ObjectInputStream input =
                    new ObjectInputStream(
                            new ByteArrayInputStream(bytes)
                    );

            Object restored =
                    input.readObject();

            input.close();

            if (restored instanceof ArrayList) {
                @SuppressWarnings("unchecked")
                ArrayList<ProjectData> restoredProjects =
                        (ArrayList<ProjectData>) restored;

                projects.addAll(restoredProjects);
            }

            boolean legacySampleRemoved = false;

            for (int i = 0; i < projects.size(); i++) {
                ProjectData project = projects.get(i);

                if (isLegacySampleProject(project)) {
                    projects.remove(i);
                    legacySampleRemoved = true;
                    break;
                }
            }

            if (legacySampleRemoved) {
                saveProjects();
            }

        } catch (Exception exception) {
            exception.printStackTrace();

            Toast.makeText(
                    this,
                    "Unable to restore saved projects",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    /**
     * Identify the original automatically created sample.
     */
    private boolean isLegacySampleProject(
            ProjectData project
    ) {
        if (project == null) {
            return false;
        }

        if (!"Service Page Website".equals(project.name)) {
            return false;
        }

        if (!"User Interface".equals(project.tag)) {
            return false;
        }

        if (project.comments != 10) {
            return false;
        }

        if (project.description == null) {
            return false;
        }

        return project.description.startsWith(
                "Make a page displaying"
        );
    }

    /**
     * Save only user-created projects.
     */
    private void saveProjects() {
        try {
            ByteArrayOutputStream byteStream =
                    new ByteArrayOutputStream();

            ObjectOutputStream output =
                    new ObjectOutputStream(byteStream);

            output.writeObject(projects);
            output.close();

            String encoded =
                    android.util.Base64.encodeToString(
                            byteStream.toByteArray(),
                            android.util.Base64.DEFAULT
                    );

            getSharedPreferences(
                    PREFS_NAME,
                    MODE_PRIVATE
            ).edit()
                    .putString(KEY_PROJECTS, encoded)
                    .apply();

        } catch (Exception exception) {
            exception.printStackTrace();

            Toast.makeText(
                    this,
                    "Unable to save projects",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void setupNewProjectButton() {
        btnNewProject.setOnClickListener(
                view -> showNewProjectDialog()
        );
    }

    /**
     * New Project dialog.
     */
    private void showNewProjectDialog() {
        selectedAttachments.clear();

        LinearLayout container =
                new LinearLayout(this);

        container.setOrientation(
                LinearLayout.VERTICAL
        );

        int padding = dpToPx(20);

        container.setPadding(
                padding,
                dpToPx(8),
                padding,
                0
        );

        TextView nameLabel =
                createDialogLabel("Project Name");

        EditText etProjectName =
                createEditText("Enter project name");

        TextView tagLabel =
                createDialogLabel("Project Tag");

        EditText etProjectTag =
                createEditText("Enter project tag");

        TextView attachmentLabel =
                createDialogLabel("Attachment");

        Button btnAttachment =
                new Button(this);

        btnAttachment.setText(
                "+ Add file or photo"
        );

        btnAttachment.setAllCaps(false);
        btnAttachment.setTextColor(Color.WHITE);
        btnAttachment.setTextSize(12);
        btnAttachment.setGravity(Gravity.CENTER);
        btnAttachment.setMinHeight(0);

        GradientDrawable attachmentBackground =
                new GradientDrawable();

        attachmentBackground.setColor(
                Color.rgb(23, 23, 43)
        );

        attachmentBackground.setCornerRadius(
                dpToPx(8)
        );

        btnAttachment.setBackground(
                attachmentBackground
        );

        LinearLayout.LayoutParams attachmentParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dpToPx(46)
                );

        attachmentParams.setMargins(
                0,
                dpToPx(5),
                0,
                dpToPx(5)
        );

        btnAttachment.setLayoutParams(
                attachmentParams
        );

        selectedAttachmentText =
                new TextView(this);

        selectedAttachmentText.setText(
                "No attachments selected"
        );

        selectedAttachmentText.setTextSize(12);

        selectedAttachmentText.setTextColor(
                Color.rgb(140, 140, 145)
        );

        selectedAttachmentText.setPadding(
                dpToPx(3),
                dpToPx(3),
                dpToPx(3),
                dpToPx(9)
        );

        btnAttachment.setOnClickListener(
                view -> openFilePicker()
        );

        TextView descriptionLabel =
                createDialogLabel(
                        "Project Description"
                );

        EditText etProjectDescription =
                createEditText(
                        "Enter project description"
                );

        etProjectDescription.setMinLines(3);
        etProjectDescription.setGravity(Gravity.TOP);

        container.addView(nameLabel);
        container.addView(etProjectName);

        container.addView(tagLabel);
        container.addView(etProjectTag);

        container.addView(attachmentLabel);
        container.addView(btnAttachment);
        container.addView(selectedAttachmentText);

        container.addView(descriptionLabel);
        container.addView(etProjectDescription);

        ScrollView scrollView =
                new ScrollView(this);

        scrollView.addView(container);

        AlertDialog dialog =
                new AlertDialog.Builder(this)
                        .setTitle("New Project")
                        .setView(scrollView)
                        .setNegativeButton(
                                "Cancel",
                                null
                        )
                        .setPositiveButton(
                                "Add Project",
                                null
                        )
                        .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button addButton =
                    dialog.getButton(
                            AlertDialog.BUTTON_POSITIVE
                    );

            addButton.setTextColor(
                    Color.rgb(14, 165, 233)
            );

            addButton.setOnClickListener(view -> {
                String name =
                        etProjectName.getText()
                                .toString()
                                .trim();

                String tag =
                        etProjectTag.getText()
                                .toString()
                                .trim();

                String description =
                        etProjectDescription.getText()
                                .toString()
                                .trim();

                if (name.isEmpty()) {
                    etProjectName.setError(
                            "Project name is required"
                    );
                    etProjectName.requestFocus();
                    return;
                }

                if (tag.isEmpty()) {
                    etProjectTag.setError(
                            "Project tag is required"
                    );
                    etProjectTag.requestFocus();
                    return;
                }

                if (description.isEmpty()) {
                    etProjectDescription.setError(
                            "Project description is required"
                    );
                    etProjectDescription.requestFocus();
                    return;
                }

                ProjectData project =
                        new ProjectData();

                project.name = name;
                project.tag = tag;
                project.description = description;
                project.comments = 0;

                project.attachments =
                        new ArrayList<>(
                                selectedAttachments
                        );

                project.tasks =
                        new ArrayList<>();

                projects.add(project);

                saveProjects();
                displayProjects();

                dialog.dismiss();

                Toast.makeText(
                        HomeActivity.this,
                        "Project added",
                        Toast.LENGTH_SHORT
                ).show();
            });
        });

        dialog.show();
    }

    private TextView createDialogLabel(String text) {
        TextView label =
                new TextView(this);

        label.setText(text);
        label.setTextSize(12);

        label.setTextColor(
                Color.rgb(80, 80, 86)
        );

        label.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        label.setPadding(
                0,
                dpToPx(8),
                0,
                0
        );

        return label;
    }

    private EditText createEditText(String hint) {
        EditText editText =
                new EditText(this);

        editText.setHint(hint);

        editText.setHintTextColor(
                Color.rgb(165, 165, 170)
        );

        editText.setTextColor(
                Color.rgb(40, 40, 45)
        );

        editText.setTextSize(14);

        editText.setPadding(
                dpToPx(12),
                dpToPx(9),
                dpToPx(12),
                dpToPx(9)
        );

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        params.setMargins(
                0,
                dpToPx(4),
                0,
                dpToPx(5)
        );

        editText.setLayoutParams(params);

        return editText;
    }

    /**
     * Select multiple documents or images.
     */
    private void openFilePicker() {
        Intent intent =
                new Intent(
                        Intent.ACTION_OPEN_DOCUMENT
                );

        intent.setType("*/*");

        intent.addCategory(
                Intent.CATEGORY_OPENABLE
        );

        intent.putExtra(
                Intent.EXTRA_ALLOW_MULTIPLE,
                true
        );

        intent.addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION
        );

        intent.addFlags(
                Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
        );

        startActivityForResult(
                intent,
                REQUEST_ATTACHMENT
        );
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

        if (requestCode == REQUEST_ATTACHMENT
                && resultCode == RESULT_OK
                && data != null) {

            ClipData clipData =
                    data.getClipData();

            if (clipData != null) {
                for (int i = 0;
                     i < clipData.getItemCount();
                     i++) {

                    Uri uri =
                            clipData.getItemAt(i)
                                    .getUri();

                    addSelectedAttachment(
                            uri,
                            data
                    );
                }
            } else {
                addSelectedAttachment(
                        data.getData(),
                        data
                );
            }

            updateSelectedAttachmentText();
            return;
        }

        if (requestCode == REQUEST_PROJECT
                && resultCode == RESULT_OK
                && data != null) {
            int returnedIndex = data.getIntExtra("project_index", -1);
            ProjectData target = null;
            if (sampleProjectOpened && returnedIndex == -1) {
                target = sampleProject;
            } else if (returnedIndex >= 0 && returnedIndex < projects.size()) {
                target = projects.get(returnedIndex);
            }

            if (target != null) {
                try {
                    @SuppressWarnings("unchecked")
                    ArrayList<TaskData> updatedTasks =
                            (ArrayList<TaskData>) data.getSerializableExtra("tasks");
                    if (updatedTasks != null) target.tasks = updatedTasks;

                    @SuppressWarnings("unchecked")
                    ArrayList<AttachmentData> updatedAttachments =
                            (ArrayList<AttachmentData>) data.getSerializableExtra("attachments");
                    if (updatedAttachments != null) target.attachments = updatedAttachments;

                    if (data.hasExtra("project_description")) {
                        String updatedDescription = data.getStringExtra("project_description");
                        if (updatedDescription != null) target.description = updatedDescription;
                    }

                    // The demonstration project remains session-only.
                    if (returnedIndex >= 0) saveProjects();
                } catch (Exception exception) {
                    exception.printStackTrace();
                    Toast.makeText(this, "Unable to update project", Toast.LENGTH_SHORT).show();
                }
            }
            sampleProjectOpened = false;
            displayProjects();
        }
    }

    private void addSelectedAttachment(
            Uri uri,
            Intent resultData
    ) {
        if (uri == null) {
            return;
        }

        String uriString =
                uri.toString();

        for (AttachmentData attachment :
                selectedAttachments) {

            if (uriString.equals(attachment.uri)) {
                return;
            }
        }

        try {
            int takeFlags =
                    resultData.getFlags()
                            & Intent.FLAG_GRANT_READ_URI_PERMISSION;

            if (takeFlags != 0) {
                getContentResolver()
                        .takePersistableUriPermission(
                                uri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                        );
            }

        } catch (SecurityException exception) {
            exception.printStackTrace();
        }

        selectedAttachments.add(
                new AttachmentData(
                        getFileName(uri),
                        uriString
                )
        );
    }

    private void updateSelectedAttachmentText() {
        if (selectedAttachmentText == null) {
            return;
        }

        if (selectedAttachments.isEmpty()) {
            selectedAttachmentText.setText(
                    "No attachments selected"
            );
            return;
        }

        StringBuilder builder =
                new StringBuilder();

        builder.append(
                selectedAttachments.size()
        );

        builder.append(
                selectedAttachments.size() == 1
                        ? " attachment selected"
                        : " attachments selected"
        );

        for (AttachmentData attachment :
                selectedAttachments) {

            builder.append("\n• ");
            builder.append(attachment.fileName);
        }

        selectedAttachmentText.setText(
                builder.toString()
        );
    }

    private String getFileName(Uri uri) {
        String result = null;

        if ("content".equals(uri.getScheme())) {
            Cursor cursor = null;

            try {
                cursor =
                        getContentResolver().query(
                                uri,
                                null,
                                null,
                                null,
                                null
                        );

                if (cursor != null
                        && cursor.moveToFirst()) {

                    int index =
                            cursor.getColumnIndex(
                                    OpenableColumns.DISPLAY_NAME
                            );

                    if (index >= 0) {
                        result =
                                cursor.getString(index);
                    }
                }

            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }

        if (result == null) {
            result =
                    uri.getLastPathSegment();
        }

        if (result == null
                || result.trim().isEmpty()) {

            result = "Attachment";
        }

        return result;
    }

    private void setupProjectSearch() {
        if (etSearchProjects == null) return;
        etSearchProjects.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s.toString().trim().toLowerCase(Locale.ROOT);
                displayProjects();
            }
            @Override public void afterTextChanged(Editable s) { }
        });
    }

    private boolean matchesSearch(ProjectData project) {
        if (searchQuery.isEmpty()) return true;
        return (project.name != null && project.name.toLowerCase(Locale.ROOT).contains(searchQuery))
                || (project.tag != null && project.tag.toLowerCase(Locale.ROOT).contains(searchQuery))
                || (project.description != null && project.description.toLowerCase(Locale.ROOT).contains(searchQuery));
    }

    private void displayProjects() {
        llUpcomingProjects.removeAllViews();
        int shown = 0;
        for (int i = 0; i < projects.size(); i++) {
            if (matchesSearch(projects.get(i))) {
                addProjectCard(projects.get(i), i);
                shown++;
            }
        }
        // The count always represents real projects, never the sample.
        tvUpcomingCount.setText(String.valueOf(searchQuery.isEmpty() ? projects.size() : shown));
        tvNoProjects.setText(searchQuery.isEmpty()
                ? "No projects yet. Create your first project!"
                : "No matching projects found.");
        tvNoProjects.setVisibility(shown == 0 ? View.VISIBLE : View.GONE);
        if (matchesSearch(sampleProject)) addSampleSection();
    }

    private void addSampleSection() {
        TextView heading = new TextView(this);
        heading.setText("SAMPLE PROJECT");
        heading.setTextSize(11);
        heading.setTextColor(Color.rgb(145, 159, 182));
        heading.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.setMargins(dpToPx(3), dpToPx(22), 0, dpToPx(12));
        heading.setLayoutParams(params);
        llUpcomingProjects.addView(heading);
        addProjectCardInternal(sampleProject, -1, true);
    }

    private void addProjectCard(ProjectData project, int projectIndex) {
        addProjectCardInternal(project, projectIndex, false);
    }

    private void addProjectCardInternal(ProjectData project, int projectIndex, boolean isSample) {
        LinearLayout card = createCardContainer();
        card.addView(createTagView(project.tag == null ? "Project" : project.tag));
        card.addView(createProjectNameView(project.name == null ? "Untitled Project" : project.name));
        card.addView(createDescriptionView(project.description == null ? "" : project.description));

        int total = project.tasks == null ? 0 : project.tasks.size();
        int done = getCompletedTaskCount(project);
        int percentage = total == 0 ? 0 : Math.round(done * 100f / total);

        LinearLayout progressHeader = new LinearLayout(this);
        progressHeader.setOrientation(LinearLayout.HORIZONTAL);
        progressHeader.setGravity(Gravity.CENTER_VERTICAL);
        TextView taskCount = new TextView(this);
        taskCount.setText("◉  " + done + "/" + total + " tasks");
        taskCount.setTextColor(Color.rgb(124, 145, 175));
        taskCount.setTextSize(11);
        progressHeader.addView(taskCount, new LinearLayout.LayoutParams(0, -2, 1));
        TextView percentText = new TextView(this);
        percentText.setText(percentage + "%");
        percentText.setTextSize(11);
        percentText.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        percentText.setTextColor(percentage == 100 && total > 0
                ? Color.rgb(45, 153, 243) : Color.rgb(124, 145, 175));
        progressHeader.addView(percentText);
        card.addView(progressHeader);

        // A track with a filled portion proportional to completed tasks.
        LinearLayout track = new LinearLayout(this);
        track.setOrientation(LinearLayout.HORIZONTAL);
        track.setBackground(roundedBackground(Color.rgb(236, 241, 247), 6, 0));
        track.setClipToOutline(true);
        LinearLayout.LayoutParams trackParams = new LinearLayout.LayoutParams(-1, dpToPx(6));
        trackParams.setMargins(0, dpToPx(7), 0, dpToPx(14));
        track.setLayoutParams(trackParams);
        View fill = new View(this);
        fill.setBackground(roundedBackground(Color.rgb(48, 153, 243), 6, 0));
        track.addView(fill, new LinearLayout.LayoutParams(0, -1, percentage));
        View remainder = new View(this);
        track.addView(remainder, new LinearLayout.LayoutParams(0, -1, 100 - percentage));
        card.addView(track);

        View divider = new View(this);
        divider.setBackgroundColor(Color.rgb(237, 241, 247));
        LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(-1, dpToPx(1));
        dividerParams.setMargins(0, 0, 0, dpToPx(11));
        card.addView(divider, dividerParams);

        LinearLayout footer = new LinearLayout(this);
        footer.setOrientation(LinearLayout.HORIZONTAL);
        footer.setGravity(Gravity.CENTER_VERTICAL);
        int files = project.attachments == null ? 0 : project.attachments.size();
        TextView attachmentLabel = createMetaText("♧  " + files + (files == 1 ? " Attachment" : " Attachments"));
        footer.addView(attachmentLabel);
        TextView comments = createMetaText("▢  " + project.comments + (project.comments == 1 ? " Comment" : " Comments"));
        LinearLayout.LayoutParams commentParams = new LinearLayout.LayoutParams(-2, -2);
        commentParams.setMargins(dpToPx(12), 0, 0, 0);
        footer.addView(comments, commentParams);
        comments.setOnClickListener(v -> showCommentsUnavailable());
        if (isSample) {
            TextView example = createMetaText("Demo");
            example.setTextColor(Color.rgb(48, 153, 243));
            example.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            LinearLayout.LayoutParams exampleParams = new LinearLayout.LayoutParams(-2, -2);
            exampleParams.setMargins(dpToPx(10), 0, 0, 0);
            footer.addView(example, exampleParams);
        }
        card.addView(footer);
        card.setClickable(true);
        card.setFocusable(true);
        card.setOnClickListener(v -> {
            if (isSample) openSampleProject();
            else openProject(projectIndex);
        });
        llUpcomingProjects.addView(card);
    }

    private LinearLayout createCardContainer() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16));
        card.setBackground(roundedBackground(Color.WHITE, 16, Color.rgb(222, 231, 241)));
        card.setElevation(dpToPx(2));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.setMargins(dpToPx(2), dpToPx(2), dpToPx(2), dpToPx(14));
        card.setLayoutParams(params);
        return card;
    }

    private GradientDrawable roundedBackground(int color, int radiusDp, int strokeColor) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dpToPx(radiusDp));
        if (strokeColor != 0) drawable.setStroke(dpToPx(1), strokeColor);
        return drawable;
    }

    private TextView createTagView(String value) {
        TextView tag = new TextView(this);
        tag.setText(value);
        tag.setTextSize(10);
        tag.setTextColor(Color.rgb(112, 69, 255));
        tag.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        tag.setBackground(roundedBackground(Color.rgb(242, 237, 255), 6, 0));
        tag.setPadding(dpToPx(9), dpToPx(5), dpToPx(9), dpToPx(5));
        tag.setLayoutParams(new LinearLayout.LayoutParams(-2, -2));
        return tag;
    }

    private TextView createProjectNameView(String value) {
        TextView name = new TextView(this);
        name.setText(value);
        name.setTextColor(Color.rgb(24, 33, 52));
        name.setTextSize(16);
        name.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.setMargins(0, dpToPx(12), 0, 0);
        name.setLayoutParams(params);
        return name;
    }

    private TextView createDescriptionView(String value) {
        TextView description = new TextView(this);
        description.setText(value);
        description.setTextSize(12);
        description.setTextColor(Color.rgb(145, 162, 187));
        description.setMaxLines(2);
        description.setEllipsize(android.text.TextUtils.TruncateAt.END);
        description.setLineSpacing(dpToPx(2), 1f);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.setMargins(0, dpToPx(8), 0, dpToPx(14));
        description.setLayoutParams(params);
        return description;
    }

    private TextView createMetaText(String value) {
        TextView text = new TextView(this);
        text.setText(value);
        text.setTextSize(10);
        text.setTextColor(Color.rgb(123, 145, 174));
        return text;
    }

    private void showCommentsUnavailable() {
        Toast.makeText(
                this,
                "Comments are outside this assignment's scope",
                Toast.LENGTH_SHORT
        ).show();
    }

    /**
     * Display task checkboxes on project cards.
     *
     * Real project changes are saved.
     * Sample changes are temporary.
     */
    private void addTaskPreview(
            LinearLayout card,
            ProjectData project,
            boolean persistChanges
    ) {
        if (project.tasks == null
                || project.tasks.isEmpty()) {
            return;
        }

        LinearLayout taskPreview =
                new LinearLayout(this);

        taskPreview.setOrientation(
                LinearLayout.VERTICAL
        );

        taskPreview.setPadding(
                0,
                dpToPx(7),
                0,
                0
        );

        for (int i = 0;
             i < project.tasks.size();
             i++) {

            TaskData task =
                    project.tasks.get(i);

            CheckBox checkBox =
                    new CheckBox(this);

            checkBox.setText(
                    task.description
            );

            checkBox.setTextSize(11);

            checkBox.setTextColor(
                    Color.rgb(73, 73, 78)
            );

            checkBox.setGravity(
                    Gravity.CENTER_VERTICAL
            );

            checkBox.setMinHeight(
                    dpToPx(34)
            );

            checkBox.setChecked(
                    task.completed
            );

            final int taskIndex = i;

            checkBox.setOnCheckedChangeListener(
                    (buttonView, isChecked) -> {
                        if (taskIndex >= 0
                                && taskIndex
                                < project.tasks.size()) {

                            project.tasks
                                    .get(taskIndex)
                                    .completed = isChecked;

                            if (persistChanges) {
                                saveProjects();
                            }

                            displayProjects();
                        }
                    }
            );

            taskPreview.addView(checkBox);
        }

        card.addView(taskPreview);
    }

    private int getCompletedTaskCount(
            ProjectData project
    ) {
        int count = 0;

        if (project.tasks == null) {
            return count;
        }

        for (TaskData task : project.tasks) {
            if (task.completed) {
                count++;
            }
        }

        return count;
    }

    /**
     * Open a user-created project.
     */
    private void openProject(int projectIndex) {
        if (projectIndex < 0
                || projectIndex >= projects.size()) {
            return;
        }

        sampleProjectOpened = false;

        ProjectData project =
                projects.get(projectIndex);

        launchProject(
                project,
                projectIndex
        );
    }

    /**
     * Show a warning before opening the sample.
     *
     * This is the new behavior requested.
     */
    private void openSampleProject() {
        new AlertDialog.Builder(this)
                .setTitle("Sample Project")
                .setMessage(
                        "This is a sample project created to help you explore TaskFlow.\n\n"
                                + "You can add tasks, complete tasks, and explore the project features.\n\n"
                                + "Please note that changes made to this sample project "
                                + "will not be saved permanently.\n\n"
                                + "To save your work, create your own project using the New Project button."
                )
                .setNegativeButton(
                        "Cancel",
                        null
                )
                .setPositiveButton(
                        "Explore Sample",
                        (dialog, which) -> {
                            sampleProjectOpened = true;

                            launchProject(
                                    sampleProject,
                                    -1
                            );
                        }
                )
                .show();
    }

    /**
     * Pass project information to ProjectActivity.
     */
    private void launchProject(
            ProjectData project,
            int projectIndex
    ) {
        Intent intent =
                new Intent(
                        HomeActivity.this,
                        ProjectActivity.class
                );

        intent.putExtra(
                "project_index",
                projectIndex
        );

        intent.putExtra(
                "project_name",
                project.name
        );

        intent.putExtra(
                "project_tag",
                project.tag
        );

        intent.putExtra(
                "project_description",
                project.description
        );

        intent.putExtra(
                "attachments",
                project.attachments
        );

        intent.putExtra(
                "comments",
                project.comments
        );

        intent.putExtra(
                "tasks",
                project.tasks
        );

        startActivityForResult(
                intent,
                REQUEST_PROJECT
        );
    }

    private void setupBackButton() {
        OnBackPressedCallback callback =
                new OnBackPressedCallback(true) {

                    @Override
                    public void handleOnBackPressed() {
                        finish();
                    }
                };

        getOnBackPressedDispatcher()
                .addCallback(this, callback);
    }

    private int dpToPx(int dp) {
        float density =
                getResources()
                        .getDisplayMetrics()
                        .density;

        return Math.round(
                dp * density
        );
    }

    /**
     * Serializable data models.
     */
    public static class ProjectData
            implements Serializable {

        private static final long serialVersionUID = 1L;

        String name;
        String tag;
        String description;
        int comments;

        ArrayList<AttachmentData> attachments;
        ArrayList<TaskData> tasks;
    }

    public static class AttachmentData
            implements Serializable {

        private static final long serialVersionUID = 1L;

        String fileName;
        String uri;

        public AttachmentData(
                String fileName,
                String uri
        ) {
            this.fileName = fileName;
            this.uri = uri;
        }
    }

    public static class TaskData implements Serializable {
        // Keep this UID unchanged so older serialized project data can be restored.
        private static final long serialVersionUID = 1L;

        public static final String STATUS_TODO = "To do";
        public static final String STATUS_IN_PROGRESS = "In Progress";
        public static final String STATUS_DONE = "Done";
        public static final String PRIORITY_LOW = "Low";
        public static final String PRIORITY_NORMAL = "Normal";
        public static final String PRIORITY_HIGH = "High";

        String description;
        boolean completed;
        String status;
        String priority;
        // Epoch milliseconds at local start of day; 0 means no due date.
        long dueDateMillis;

        public TaskData(String description) {
            this.description = description;
            this.completed = false;
            this.status = STATUS_TODO;
            this.priority = PRIORITY_NORMAL;
            this.dueDateMillis = 0L;
        }

        public String getStatus() {
            // Older saved tasks did not have a status field.
            if (completed) return STATUS_DONE;
            if (STATUS_IN_PROGRESS.equals(status)) return STATUS_IN_PROGRESS;
            return STATUS_TODO;
        }

        public String getPriority() {
            if (PRIORITY_HIGH.equals(priority)) return PRIORITY_HIGH;
            if (PRIORITY_LOW.equals(priority)) return PRIORITY_LOW;
            return PRIORITY_NORMAL;
        }

        public void setStatus(String newStatus) {
            if (STATUS_DONE.equals(newStatus)) {
                status = STATUS_DONE;
                completed = true;
            } else if (STATUS_IN_PROGRESS.equals(newStatus)) {
                status = STATUS_IN_PROGRESS;
                completed = false;
            } else {
                status = STATUS_TODO;
                completed = false;
            }
        }

        public void setCompleted(boolean isCompleted) {
            completed = isCompleted;
            status = isCompleted ? STATUS_DONE : STATUS_TODO;
        }

        public boolean isOverdue() {
            if (completed || dueDateMillis <= 0L) return false;
            java.util.Calendar today = java.util.Calendar.getInstance();
            today.set(java.util.Calendar.HOUR_OF_DAY, 0);
            today.set(java.util.Calendar.MINUTE, 0);
            today.set(java.util.Calendar.SECOND, 0);
            today.set(java.util.Calendar.MILLISECOND, 0);
            return dueDateMillis < today.getTimeInMillis();
        }
    }
}
