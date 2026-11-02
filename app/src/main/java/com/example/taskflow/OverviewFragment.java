package com.example.taskflow;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class OverviewFragment extends Fragment {

    private ProjectHost host;

    private TextView tvDescription;
    private TextView tvTaskCount;
    private TextView tvPercentage;
    private ProgressBar pbProgress;
    private TextView tvTodoCount;
    private TextView tvInProgressCount;
    private TextView tvDoneCount;
    private TextView tvOverdueCount;
    private TextView tvFilesSubtitle;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof ProjectHost) {
            host = (ProjectHost) context;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_overview, container, false);
        initViews(view);
        setupClickListeners(view);
        refreshProjectData();
        return view;
    }

    private void initViews(View v) {
        tvDescription = v.findViewById(R.id.tvOverviewDescription);
        tvTaskCount = v.findViewById(R.id.tvOverviewTaskCount);
        tvPercentage = v.findViewById(R.id.tvOverviewPercentage);
        pbProgress = v.findViewById(R.id.pbOverviewProgress);
        tvTodoCount = v.findViewById(R.id.tvBreakdownTodoCount);
        tvInProgressCount = v.findViewById(R.id.tvBreakdownInProgressCount);
        tvDoneCount = v.findViewById(R.id.tvBreakdownDoneCount);
        tvOverdueCount = v.findViewById(R.id.tvBreakdownOverdueCount);
        tvFilesSubtitle = v.findViewById(R.id.tvQuickLinkFilesSubtitle);
    }

    private void setupClickListeners(View v) {
        v.findViewById(R.id.btnEditProject).setOnClickListener(view -> showEditProjectDialog());

        // Breakdown cards route to Tasks tab with filters
        v.findViewById(R.id.cardBreakdownTodo).setOnClickListener(view -> {
            if (host != null) host.navigateToTasks(Task.STATUS_TODO, "All dates");
        });
        v.findViewById(R.id.cardBreakdownInProgress).setOnClickListener(view -> {
            if (host != null) host.navigateToTasks(Task.STATUS_IN_PROGRESS, "All dates");
        });
        v.findViewById(R.id.cardBreakdownDone).setOnClickListener(view -> {
            if (host != null) host.navigateToTasks(Task.STATUS_DONE, "All dates");
        });
        v.findViewById(R.id.cardBreakdownOverdue).setOnClickListener(view -> {
            if (host != null) host.navigateToTasks("All statuses", "Overdue");
        });

        // Quick Links
        v.findViewById(R.id.quickLinkAttachments).setOnClickListener(view -> {
            if (host != null) host.navigateToTab(2);
        });
        v.findViewById(R.id.quickLinkDiscussion).setOnClickListener(view -> {
            if (host != null) host.navigateToTab(3);
        });

        // Add Task Button calls our helper
        v.findViewById(R.id.btnOverviewAddTask).setOnClickListener(view -> {
            TaskDialogHelper.showAddTaskDialog(requireContext(), host.getProject(), () -> {
                if (host != null) host.notifyProjectChanged();
            });
        });
    }

    public void refreshProjectData() {
        if (host == null || host.getProject() == null || tvDescription == null) return;
        Project p = host.getProject();

        tvDescription.setText(p.getDescription());

        int total = p.getTasks().size();
        int done = p.getCompletedTaskCount();
        int percent = p.getProgressPercentage();

        tvTaskCount.setText(done + " of " + total + " tasks completed");
        tvPercentage.setText(percent + "%");
        pbProgress.setProgress(percent);

        tvTodoCount.setText(String.valueOf(p.getTodoCount()));
        tvInProgressCount.setText(String.valueOf(p.getInProgressCount()));
        tvDoneCount.setText(String.valueOf(done));
        tvOverdueCount.setText(String.valueOf(p.getOverdueCount()));

        int files = p.getAttachments().size();
        tvFilesSubtitle.setText(files + (files == 1 ? " file" : " files"));
    }

    private void showEditProjectDialog() {
        if (host == null) return;
        Project p = host.getProject();

        // Reuse the dialog layout from HomeActivity, but hide the attachment UI
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_new_project, null);
        EditText etName = dialogView.findViewById(R.id.etDialogProjectName);
        EditText etTag = dialogView.findViewById(R.id.etDialogProjectTag);
        EditText etDesc = dialogView.findViewById(R.id.etDialogProjectDesc);

        dialogView.findViewById(R.id.btnDialogAttachment).setVisibility(View.GONE);
        dialogView.findViewById(R.id.tvDialogAttachmentSelected).setVisibility(View.GONE);

        etName.setText(p.getName());
        etTag.setText(p.getTag());
        etDesc.setText(p.getDescription());

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Edit Project")
                .setView(dialogView)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save", null)
                .create();

        dialog.setOnShowListener(d -> {
            Button saveBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            saveBtn.setOnClickListener(v -> {
                String name = etName.getText().toString().trim();
                String tag = etTag.getText().toString().trim();
                String desc = etDesc.getText().toString().trim();

                if (name.isEmpty() || name.length() > 60) {
                    etName.setError("Name must be 1-60 characters");
                    etName.requestFocus();
                    return;
                }
                if (tag.isEmpty() || tag.length() > 20) {
                    etTag.setError("Tag must be 1-20 characters");
                    etTag.requestFocus();
                    return;
                }
                if (desc.isEmpty() || desc.length() > 500) {
                    etDesc.setError("Description must be 1-500 characters");
                    etDesc.requestFocus();
                    return;
                }

                p.setName(name);
                p.setTag(tag);
                p.setDescription(desc);

                host.notifyProjectChanged();
                dialog.dismiss();
                Toast.makeText(requireContext(), "Project updated", Toast.LENGTH_SHORT).show();
            });
        });

        dialog.show();
    }
}