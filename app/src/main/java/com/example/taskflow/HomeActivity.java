
package com.example.taskflow;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {

    public static final String EXTRA_PROJECT = "project";
    private static final String STATE_PROJECTS = "state_projects";

    private RecyclerView rvProjects;
    private ProjectAdapter adapter;
    private EditText etSearch;
    private TextView tvProjectsCount;
    private Button btnNewProject;

    private final List<Project> allProjects = new ArrayList<>();
    private final List<Project> filteredList = new ArrayList<>();

    private final ActivityResultLauncher<Intent> projectActivityLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {

                        if (result.getResultCode() != RESULT_OK
                                || result.getData() == null) {
                            return;
                        }

                        Serializable returned =
                                result.getData().getSerializableExtra(
                                        EXTRA_PROJECT
                                );

                        if (!(returned instanceof Project)) {
                            return;
                        }

                        Project updatedProject = (Project) returned;

                        for (int i = 0; i < allProjects.size(); i++) {

                            if (allProjects.get(i).getId()
                                    .equals(updatedProject.getId())) {

                                allProjects.set(i, updatedProject);
                                filterProjects();
                                return;
                            }
                        }

                        // A returned project must replace an
                        // existing project, never create a duplicate.
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);

        initViews();
        restoreOrInitializeProjects(savedInstanceState);
        setupRecyclerView();
        setupSearch();
        setupNewProjectButton();
        filterProjects();
    }

    private void restoreOrInitializeProjects(
            Bundle savedInstanceState
    ) {

        boolean restored = false;

        if (savedInstanceState != null
                && savedInstanceState.containsKey(STATE_PROJECTS)) {

            savedInstanceState.setClassLoader(
                    Project.class.getClassLoader()
            );

            Serializable state =
                    savedInstanceState.getSerializable(
                            STATE_PROJECTS
                    );

            if (state instanceof ArrayList<?>) {

                ArrayList<?> savedProjects =
                        (ArrayList<?>) state;

                boolean valid = true;

                for (Object item : savedProjects) {

                    if (!(item instanceof Project)) {
                        valid = false;
                        break;
                    }
                }

                if (valid) {

                    for (Object item : savedProjects) {
                        allProjects.add((Project) item);
                    }

                    restored = true;
                }
            }
        }

        if (!restored) {
            allProjects.addAll(
                    SampleDataRepository.getInitialProjects()
            );
        }
    }

    @Override
    protected void onSaveInstanceState(
            @NonNull Bundle outState
    ) {

        outState.putSerializable(
                STATE_PROJECTS,
                new ArrayList<>(allProjects)
        );

        super.onSaveInstanceState(outState);
    }

    private void initViews() {

        rvProjects = findViewById(
                R.id.rvProjects
        );

        etSearch = findViewById(
                R.id.etSearchProjects
        );

        tvProjectsCount = findViewById(
                R.id.tvProjectsCount
        );

        btnNewProject = findViewById(
                R.id.btnNewProject
        );
    }

    private void setupRecyclerView() {

        adapter = new ProjectAdapter(
                this::openProjectActivity
        );

        rvProjects.setLayoutManager(
                new LinearLayoutManager(this)
        );

        rvProjects.setAdapter(adapter);
    }

    private void setupSearch() {

        etSearch.addTextChangedListener(
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
                        filterProjects();
                    }

                    @Override
                    public void afterTextChanged(
                            Editable s
                    ) {
                    }
                }
        );
    }

    private void filterProjects() {

        String query = etSearch.getText()
                .toString()
                .trim()
                .toLowerCase(Locale.ROOT);

        filteredList.clear();

        for (Project project : allProjects) {

            String name = project.getName() == null
                    ? ""
                    : project.getName();

            String tag = project.getTag() == null
                    ? ""
                    : project.getTag();

            if (query.isEmpty()
                    || name.toLowerCase(Locale.ROOT)
                    .contains(query)
                    || tag.toLowerCase(Locale.ROOT)
                    .contains(query)) {

                filteredList.add(project);
            }
        }

        tvProjectsCount.setText(
                "Showing " + filteredList.size() + " projects"
        );

        adapter.submitList(
                new ArrayList<>(filteredList)
        );
    }

    private void setupNewProjectButton() {

        btnNewProject.setOnClickListener(
                v -> showNewProjectDialog()
        );
    }

    /**
     * Creates a new project with zero or more attachments.
     *
     * Attachments are selected from the local catalog.
     * Each catalog item can be selected only once.
     */
    private void showNewProjectDialog() {

        View dialogView = LayoutInflater.from(this)
                .inflate(
                        R.layout.dialog_add_project,
                        null
                );

        EditText etName = dialogView.findViewById(
                R.id.etProjectName
        );

        EditText etTag = dialogView.findViewById(
                R.id.etProjectTag
        );

        EditText etDesc = dialogView.findViewById(
                R.id.etProjectDescription
        );

        Button btnAttachment = dialogView.findViewById(
                R.id.btnProjectAttachment
        );

        // Multiple selected attachments are stored here.
        // The list is local to this new-project dialog.
        final List<CatalogItem> selectedAttachments =
                new ArrayList<>();

        updateAttachmentButton(
                btnAttachment,
                selectedAttachments
        );

        btnAttachment.setOnClickListener(
                v -> showMultipleAttachmentDialog(
                        selectedAttachments,
                        btnAttachment
                )
        );

        AlertDialog dialog =
                new AlertDialog.Builder(this)
                        .setTitle("New Project")
                        .setView(dialogView)
                        .setNegativeButton(
                                "Cancel",
                                null
                        )
                        .setPositiveButton(
                                "Add Project",
                                null
                        )
                        .create();

        dialog.setOnShowListener(
                ignored -> {

                    Button addButton =
                            dialog.getButton(
                                    AlertDialog.BUTTON_POSITIVE
                            );

                    addButton.setOnClickListener(v -> {

                        String name = etName.getText()
                                .toString()
                                .trim();

                        String tag = etTag.getText()
                                .toString()
                                .trim();

                        String description = etDesc.getText()
                                .toString()
                                .trim();

                        if (name.isEmpty()
                                || name.length() > 60) {

                            etName.setError(
                                    "Project name must be 1-60 characters"
                            );

                            etName.requestFocus();
                            return;
                        }

                        if (tag.isEmpty()
                                || tag.length() > 20) {

                            etTag.setError(
                                    "Project tag must be 1-20 characters"
                            );

                            etTag.requestFocus();
                            return;
                        }

                        if (description.isEmpty()
                                || description.length() > 500) {

                            etDesc.setError(
                                    "Description must be 1-500 characters"
                            );

                            etDesc.requestFocus();
                            return;
                        }

                        // Constructor:
                        // name, tag, description, comment count.
                        Project newProject = new Project(
                                name,
                                tag,
                                description,
                                0
                        );

                        // Add every selected catalog attachment.
                        // Each gets its own AttachmentLink.
                        for (CatalogItem item : selectedAttachments) {

                            newProject.getAttachments().add(
                                    new AttachmentLink(item)
                            );
                        }

                        allProjects.add(newProject);

                        filterProjects();

                        dialog.dismiss();

                        Toast.makeText(
                                this,
                                "Project Added with "
                                        + selectedAttachments.size()
                                        + " attachment(s)",
                                Toast.LENGTH_SHORT
                        ).show();
                    });
                }
        );

        dialog.show();
    }

    /**
     * Multi-choice attachment selection.
     *
     * Selection changes are committed only when
     * the user presses Done.
     *
     * Cancel leaves the previous selection unchanged.
     */
    private void showMultipleAttachmentDialog(
            List<CatalogItem> selectedAttachments,
            Button btnAttachment
    ) {

        List<CatalogItem> catalog =
                SampleDataRepository.getLocalCatalog();

        if (catalog.isEmpty()) {

            Toast.makeText(
                    this,
                    "No attachments available",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String[] names =
                new String[catalog.size()];

        boolean[] checked =
                new boolean[catalog.size()];

        for (int i = 0; i < catalog.size(); i++) {

            CatalogItem item = catalog.get(i);

            names[i] = item.getDisplayName();

            // Restore previously selected attachments.
            for (CatalogItem selected : selectedAttachments) {

                if (selected.getId().equals(item.getId())) {
                    checked[i] = true;
                    break;
                }
            }
        }

        new AlertDialog.Builder(this)
                .setTitle("Select Attachments")
                .setMultiChoiceItems(
                        names,
                        checked,
                        (dialog, which, isChecked) -> {

                            // Only update the temporary selection.
                            // Do not modify the project yet.
                            checked[which] = isChecked;
                        }
                )
                .setNegativeButton(
                        "Cancel",
                        null
                )
                .setNeutralButton(
                        "Clear Selection",
                        (dialog, which) -> {

                            selectedAttachments.clear();

                            updateAttachmentButton(
                                    btnAttachment,
                                    selectedAttachments
                            );
                        }
                )
                .setPositiveButton(
                        "Done",
                        (dialog, which) -> {

                            selectedAttachments.clear();

                            for (int i = 0; i < catalog.size(); i++) {

                                if (checked[i]) {

                                    CatalogItem item =
                                            catalog.get(i);

                                    boolean alreadySelected = false;

                                    for (CatalogItem selected :
                                            selectedAttachments) {

                                        if (selected.getId()
                                                .equals(item.getId())) {

                                            alreadySelected = true;
                                            break;
                                        }
                                    }

                                    if (!alreadySelected) {
                                        selectedAttachments.add(item);
                                    }
                                }
                            }

                            updateAttachmentButton(
                                    btnAttachment,
                                    selectedAttachments
                            );
                        }
                )
                .show();
    }

    /**
     * Displays the number of selected attachments
     * on the existing button without changing XML.
     */
    private void updateAttachmentButton(
            Button button,
            List<CatalogItem> selectedAttachments
    ) {

        int count = selectedAttachments.size();

        if (count == 0) {

            button.setText(
                    "+ Select Attachments"
            );

        } else if (count == 1) {

            button.setText(
                    "1 Attachment Selected"
            );

        } else {

            button.setText(
                    count + " Attachments Selected"
            );
        }
    }

    private void openProjectActivity(
            Project project
    ) {

        Intent intent = new Intent(
                this,
                ProjectActivity.class
        );

        intent.putExtra(
                EXTRA_PROJECT,
                project
        );

        projectActivityLauncher.launch(intent);
    }
}
