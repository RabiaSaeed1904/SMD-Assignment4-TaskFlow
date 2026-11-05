package com.example.taskflow;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class TasksFragment extends Fragment {

    private ProjectHost host;

    private EditText etSearch;
    private Spinner spStatus, spPriority, spDate, spSort;
    private TextView tvStatusCount;
    private TextView btnReset;
    private ListView lvTasks;
    private View layoutEmpty;
    private TextView tvEmptyMsg;
    private Button btnEmptyAction;

    private final List<Task> filteredTasks = new ArrayList<>();
    private TaskAdapter adapter;

    private final String[] statusChoices = {"All statuses", "To do", "In progress", "Done"};
    private final String[] priorityChoices = {"All priorities", "High", "Normal", "Low"};
    private final String[] dateChoices = {"All dates", "Today", "Overdue", "No due date"};
    private final String[] sortChoices = {"Due date ↑", "Due date ↓", "Priority", "Description"};

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof ProjectHost) host = (ProjectHost) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tasks, container, false);
        initViews(view);
        setupSpinners();
        setupList();
        setupSearch();
        setupActions(view);
        applyFilters();
        return view;
    }

    private void initViews(View v) {
        etSearch = v.findViewById(R.id.etSearchTasks);
        spStatus = v.findViewById(R.id.spinnerStatus);
        spPriority = v.findViewById(R.id.spinnerPriority);
        spDate = v.findViewById(R.id.spinnerDate);
        spSort = v.findViewById(R.id.spinnerSort);
        tvStatusCount = v.findViewById(R.id.tvTaskCountStatus);
        btnReset = v.findViewById(R.id.btnResetFilters);
        lvTasks = v.findViewById(R.id.lvTasks);
        layoutEmpty = v.findViewById(R.id.layoutTasksEmpty);
        tvEmptyMsg = v.findViewById(R.id.tvTasksEmptyMessage);
        btnEmptyAction = v.findViewById(R.id.btnTasksEmptyAction);
    }

    private void setupSpinners() {
        // Updated to use the custom item_spinner layout
        spStatus.setAdapter(new ArrayAdapter<>(requireContext(), R.layout.item_spinner, statusChoices));
        spPriority.setAdapter(new ArrayAdapter<>(requireContext(), R.layout.item_spinner, priorityChoices));
        spDate.setAdapter(new ArrayAdapter<>(requireContext(), R.layout.item_spinner, dateChoices));
        spSort.setAdapter(new ArrayAdapter<>(requireContext(), R.layout.item_spinner, sortChoices));

        AdapterView.OnItemSelectedListener filterListener = new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) { applyFilters(); }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        };

        spStatus.setOnItemSelectedListener(filterListener);
        spPriority.setOnItemSelectedListener(filterListener);
        spDate.setOnItemSelectedListener(filterListener);
        spSort.setOnItemSelectedListener(filterListener);
    }

    private void setupList() {
        adapter = new TaskAdapter(requireContext(), filteredTasks, () -> {
            if (host != null) host.notifyProjectChanged();
            applyFilters();
        });
        lvTasks.setAdapter(adapter);
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { applyFilters(); }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void setupActions(View v) {
        btnReset.setOnClickListener(view -> resetFilters());
        v.findViewById(R.id.btnTasksAddTask).setOnClickListener(view -> openAddTaskDialog());
    }

    private void resetFilters() {
        etSearch.setText("");
        spStatus.setSelection(0);
        spPriority.setSelection(0);
        spDate.setSelection(0);
        spSort.setSelection(0);
        applyFilters();
    }

    public void applyDirectFilters(String targetStatus, String targetDate) {
        if (targetStatus != null) {
            for (int i = 0; i < statusChoices.length; i++) {
                if (statusChoices[i].equalsIgnoreCase(targetStatus)) {
                    spStatus.setSelection(i);
                    break;
                }
            }
        }
        if (targetDate != null) {
            for (int i = 0; i < dateChoices.length; i++) {
                if (dateChoices[i].equalsIgnoreCase(targetDate)) {
                    spDate.setSelection(i);
                    break;
                }
            }
        }
        applyFilters();
    }

    public void refreshList() {
        applyFilters();
    }

    private void applyFilters() {
        if (host == null || host.getProject() == null) return;
        List<Task> all = host.getProject().getTasks();
        filteredTasks.clear();

        String query = etSearch.getText().toString().trim().toLowerCase(Locale.ROOT);
        int statusPos = spStatus.getSelectedItemPosition();
        int priorityPos = spPriority.getSelectedItemPosition();
        int datePos = spDate.getSelectedItemPosition();
        int sortPos = spSort.getSelectedItemPosition();

        for (Task t : all) {
            if (!query.isEmpty() && !t.getDescription().toLowerCase(Locale.ROOT).contains(query)) {
                continue;
            }
            if (statusPos == 1 && !Task.STATUS_TODO.equals(t.getStatus())) continue;
            if (statusPos == 2 && !Task.STATUS_IN_PROGRESS.equals(t.getStatus())) continue;
            if (statusPos == 3 && !Task.STATUS_DONE.equals(t.getStatus())) continue;

            if (priorityPos == 1 && !Task.PRIORITY_HIGH.equals(t.getPriority())) continue;
            if (priorityPos == 2 && !Task.PRIORITY_NORMAL.equals(t.getPriority())) continue;
            if (priorityPos == 3 && !Task.PRIORITY_LOW.equals(t.getPriority())) continue;

            if (datePos == 1 && !t.isDueToday()) continue;
            if (datePos == 2 && !t.isOverdue()) continue;
            if (datePos == 3 && t.getDueDateMillis() > 0L) continue;

            filteredTasks.add(t);
        }

        Collections.sort(filteredTasks, (t1, t2) -> {
            switch (sortPos) {
                case 1:
                    return Long.compare(t2.getDueDateMillis() == 0 ? Long.MIN_VALUE : t2.getDueDateMillis(),
                            t1.getDueDateMillis() == 0 ? Long.MIN_VALUE : t1.getDueDateMillis());
                case 2:
                    return Integer.compare(getPriorityRank(t2.getPriority()), getPriorityRank(t1.getPriority()));
                case 3:
                    return t1.getDescription().compareToIgnoreCase(t2.getDescription());
                default:
                    long d1 = t1.getDueDateMillis() == 0 ? Long.MAX_VALUE : t1.getDueDateMillis();
                    long d2 = t2.getDueDateMillis() == 0 ? Long.MAX_VALUE : t2.getDueDateMillis();
                    return Long.compare(d1, d2);
            }
        });

        adapter.notifyDataSetChanged();
        tvStatusCount.setText("Showing " + filteredTasks.size() + " of " + all.size() + " tasks");

        if (filteredTasks.isEmpty()) {
            lvTasks.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
            if (all.isEmpty()) {
                tvEmptyMsg.setText("No tasks yet");
                btnEmptyAction.setText("+ Add Task");
                btnEmptyAction.setOnClickListener(v -> openAddTaskDialog());
            } else {
                tvEmptyMsg.setText("No tasks match these filters");
                btnEmptyAction.setText("Reset Filters");
                btnEmptyAction.setOnClickListener(v -> resetFilters());
            }
        } else {
            lvTasks.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);
        }
    }

    private int getPriorityRank(String p) {
        if (Task.PRIORITY_HIGH.equals(p)) return 3;
        if (Task.PRIORITY_NORMAL.equals(p)) return 2;
        return 1;
    }

    private void openAddTaskDialog() {
        TaskDialogHelper.showAddTaskDialog(requireContext(), host.getProject(), () -> {
            if (host != null) host.notifyProjectChanged();
            applyFilters();
        });
    }
}