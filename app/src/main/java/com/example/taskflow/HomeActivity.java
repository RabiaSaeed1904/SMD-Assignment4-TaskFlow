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
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView rvProjects;
    private ProjectAdapter adapter;
    private EditText etSearch;
    private TextView tvProjectsCount;
    private Button btnNewProject;

    private final List<Project> allProjects = new ArrayList<>();
    private final List<Project> filteredList = new ArrayList<>();

    private final ActivityResultLauncher<Intent> projectActivityLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Project updatedProject = (Project) result.getData().getSerializableExtra("project");
                    if (updatedProject != null) {
                        for (int i = 0; i < allProjects.size(); i++) {
                            if (allProjects.get(i).getId().equals(updatedProject.getId())) {
                                allProjects.set(i, updatedProject);
                                break;
                            }
                        }
                        filterProjects();
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        if (allProjects.isEmpty()) {
            allProjects.addAll(SampleDataRepository.getWorkspaceProjects());
        }

        initViews();
        setupRecyclerView();
        setupSearch();
        setupNewProjectButton();
        filterProjects();
    }

    private void initViews() {
        rvProjects = findViewById(R.id.rvProjects);
        etSearch = findViewById(R.id.etSearchProjects);
        tvProjectsCount = findViewById(R.id.tvProjectsCount);
        btnNewProject = findViewById(R.id.btnNewProject);
    }

    private void setupRecyclerView() {
        adapter = new ProjectAdapter(this::openProjectActivity);
        rvProjects.setLayoutManager(new LinearLayoutManager(this));
        rvProjects.setAdapter(adapter);
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { filterProjects(); }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void filterProjects() {
        String query = etSearch.getText().toString().trim().toLowerCase(Locale.ROOT);
        filteredList.clear();

        for (Project p : allProjects) {
            if (query.isEmpty() ||
                    p.getName().toLowerCase(Locale.ROOT).contains(query) ||
                    p.getTag().toLowerCase(Locale.ROOT).contains(query)) {
                filteredList.add(p);
            }
        }

        tvProjectsCount.setText("Showing " + filteredList.size() + " projects");
        adapter.submitList(new ArrayList<>(filteredList));
    }

    private void setupNewProjectButton() {
        btnNewProject.setOnClickListener(v -> showNewProjectDialog());
    }

    private void showNewProjectDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_project, null);
        EditText etName = dialogView.findViewById(R.id.etProjectName);
        EditText etTag = dialogView.findViewById(R.id.etProjectTag);
        EditText etDesc = dialogView.findViewById(R.id.etProjectDescription);
        Button btnAttachment = dialogView.findViewById(R.id.btnProjectAttachment);

        final CatalogItem[] selectedAttachment = {null};

        btnAttachment.setOnClickListener(v -> {
            List<CatalogItem> catalog = SampleDataRepository.getLocalCatalog();
            String[] names = new String[catalog.size()];
            for(int i = 0; i < catalog.size(); i++) names[i] = catalog.get(i).getDisplayName();

            new AlertDialog.Builder(this)
                    .setTitle("Select Attachment")
                    .setItems(names, (dialog, which) -> {
                        selectedAttachment[0] = catalog.get(which);
                        btnAttachment.setText("Attachment: " + selectedAttachment[0].getDisplayName());
                    }).show();
        });

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("New Project")
                .setView(dialogView)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Add Project", null)
                .create();

        dialog.setOnShowListener(d -> {
            Button addBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            addBtn.setOnClickListener(v -> {
                String name = etName.getText().toString().trim();
                String tag = etTag.getText().toString().trim();
                String desc = etDesc.getText().toString().trim();

                if (name.isEmpty() || name.length() > 60) { etName.setError("1-60 chars"); return; }
                if (tag.isEmpty() || tag.length() > 20) { etTag.setError("1-20 chars"); return; }
                if (desc.isEmpty() || desc.length() > 500) { etDesc.setError("1-500 chars"); return; }

                Project newProject = new Project(tag, name, desc);
                if (selectedAttachment[0] != null) {
                    newProject.getAttachments().add(new AttachmentLink(selectedAttachment[0]));
                }

                allProjects.add(newProject);
                filterProjects();
                dialog.dismiss();
                Toast.makeText(this, "Project Added", Toast.LENGTH_SHORT).show();
            });
        });

        dialog.show();
    }

    private void openProjectActivity(Project project) {
        Intent intent = new Intent(this, ProjectActivity.class);
        intent.putExtra("project", project);
        projectActivityLauncher.launch(intent);
    }
}