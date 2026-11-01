package com.example.taskflow;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {

    public static final String EXTRA_PROJECT = "extra_project";

    private ListView lvProjects;
    private TextView tvProjectsCount;
    private TextView tvNoProjects;
    private EditText etSearchProjects;
    private Button btnNewProject;

    private final ArrayList<Project> allProjects = new ArrayList<>();
    private final ArrayList<Project> displayedProjects = new ArrayList<>();
    private ProjectAdapter adapter;

    private ActivityResultLauncher<Intent> projectLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initViews();
        setupActivityResult();

        // Seed with the 5 predefined sample projects
        allProjects.addAll(SampleDataRepository.getInitialProjects());
        displayedProjects.addAll(allProjects);

        adapter = new ProjectAdapter(this, displayedProjects);
        lvProjects.setAdapter(adapter);

        setupSearch();
        setupNewProjectButton();
        setupListClick();
        updateCountLabel();
    }

    private void initViews() {
        lvProjects = findViewById(R.id.lvProjects);
        tvProjectsCount = findViewById(R.id.tvProjectsCount);
        tvNoProjects = findViewById(R.id.tvNoProjects);
        etSearchProjects = findViewById(R.id.etSearchProjects);
        btnNewProject = findViewById(R.id.btnNewProject);
    }

    private void setupActivityResult() {
        projectLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Project updated = (Project) result.getData().getSerializableExtra(EXTRA_PROJECT);
                        if (updated != null) {
                            // Find and update the existing project strictly by ID
                            for (int i = 0; i < allProjects.size(); i++) {
                                if (allProjects.get(i).getId().equals(updated.getId())) {
                                    allProjects.set(i, updated);
                                    break;
                                }
                            }
                            filterProjects(etSearchProjects.getText().toString());
                        }
                    }
                }
        );
    }

    private void setupListClick() {
        lvProjects.setOnItemClickListener((parent, view, position, id) -> {
            Project selected = displayedProjects.get(position);
            Intent intent = new Intent(HomeActivity.this, ProjectActivity.class);
            intent.putExtra(EXTRA_PROJECT, selected);
            projectLauncher.launch(intent);
        });
    }

    private void setupSearch() {
        etSearchProjects.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterProjects(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void filterProjects(String query) {
        String cleanQuery = (query == null) ? "" : query.trim().toLowerCase(Locale.ROOT);
        displayedProjects.clear();

        for (Project p : allProjects) {
            boolean nameMatches = p.getName() != null && p.getName().toLowerCase(Locale.ROOT).contains(cleanQuery);
            boolean tagMatches = p.getTag() != null && p.getTag().toLowerCase(Locale.ROOT).contains(cleanQuery);

            // Search must match any part of project name or tag
            if (nameMatches || tagMatches) {
                displayedProjects.add(p);
            }
        }
        adapter.notifyDataSetChanged();
        updateCountLabel();
    }

    private void updateCountLabel() {
        int count = displayedProjects.size();
        tvProjectsCount.setText("Showing " + count + (count == 1 ? " project" : " projects"));
        tvNoProjects.setVisibility(count == 0 ? View.VISIBLE : View.GONE);
    }

    private void setupNewProjectButton() {
        btnNewProject.setOnClickListener(v -> showNewProjectDialog());
    }

    private void showNewProjectDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_new_project, null);
        EditText etName = dialogView.findViewById(R.id.etDialogProjectName);
        EditText etTag = dialogView.findViewById(R.id.etDialogProjectTag);
        EditText etDesc = dialogView.findViewById(R.id.etDialogProjectDesc);
        Button btnSelectAttachment = dialogView.findViewById(R.id.btnDialogAttachment);
        TextView tvAttachmentInfo = dialogView.findViewById(R.id.tvDialogAttachmentSelected);

        final CatalogItem[] chosenItem = new CatalogItem[1];

        btnSelectAttachment.setOnClickListener(v -> {
            List<CatalogItem> catalog = SampleDataRepository.getLocalCatalog();
            String[] names = new String[catalog.size()];
            for (int i = 0; i < catalog.size(); i++) {
                names[i] = catalog.get(i).getDisplayName() + " (" + catalog.get(i).getType() + ")";
            }

            new AlertDialog.Builder(HomeActivity.this)
                    .setTitle("Select Catalog Attachment")
                    .setSingleChoiceItems(names, -1, (dialog, which) -> {
                        chosenItem[0] = catalog.get(which);
                        tvAttachmentInfo.setText("Selected: " + chosenItem[0].getDisplayName());
                        dialog.dismiss();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
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

                // Validation rules
                if (name.isEmpty() || name.length() > 60) {
                    etName.setError("Name must be between 1 and 60 characters");
                    etName.requestFocus();
                    return;
                }
                if (tag.isEmpty() || tag.length() > 20) {
                    etTag.setError("Tag must be between 1 and 20 characters");
                    etTag.requestFocus();
                    return;
                }
                if (desc.isEmpty() || desc.length() > 500) {
                    etDesc.setError("Description must be between 1 and 500 characters");
                    etDesc.requestFocus();
                    return;
                }

                Project newProj = new Project(name, tag, desc, 0);
                if (chosenItem[0] != null) {
                    newProj.getAttachments().add(new AttachmentLink(chosenItem[0]));
                }

                allProjects.add(newProj);
                filterProjects(etSearchProjects.getText().toString());
                dialog.dismiss();
                Toast.makeText(HomeActivity.this, "Project created successfully", Toast.LENGTH_SHORT).show();
            });
        });

        dialog.show();
    }
}