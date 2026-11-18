
package com.example.taskflow;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class TasksFragment extends Fragment {

    private static final String STATE_SEARCH = "tasks_search";
    private static final String STATE_STATUS = "tasks_status";
    private static final String STATE_PRIORITY = "tasks_priority";
    private static final String STATE_DATE = "tasks_date";
    private static final String STATE_SORT = "tasks_sort";

    private static final int SORT_CUSTOM = 4;

    private static final int GROUP_OVERDUE = 0;
    private static final int GROUP_IN_PROGRESS = 1;
    private static final int GROUP_TODO = 2;
    private static final int GROUP_DONE = 3;

    private ProjectHost host;

    private EditText etSearch;

    private Spinner spStatus;
    private Spinner spPriority;
    private Spinner spDate;
    private Spinner spSort;

    private TextView tvStatusCount;
    private TextView btnReset;
    private TextView tvEmptyMsg;

    private RecyclerView rvTasks;
    private View layoutEmpty;
    private Button btnEmptyAction;

    private TaskAdapter adapter;

    private AlertDialog pendingDeleteDialog;

    private boolean dragChanged = false;

    private String pendingDirectStatus;
    private String pendingDirectDate;

    private final String[] statusChoices = {
            "All statuses",
            "To do",
            "In progress",
            "Done"
    };

    private final String[] priorityChoices = {
            "All priorities",
            "High",
            "Normal",
            "Low"
    };

    private final String[] dateChoices = {
            "All dates",
            "Today",
            "Overdue",
            "No due date"
    };

    private final String[] sortChoices = {
            "Due date ↑",
            "Due date ↓",
            "Priority",
            "Description",
            "Custom order"
    };

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (!(context instanceof ProjectHost)) {
            throw new IllegalStateException(
                    "TasksFragment requires ProjectHost"
            );
        }

        host = (ProjectHost) context;
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(
                R.layout.fragment_tasks,
                container,
                false
        );

        initViews(view);

        setupRecyclerView();
        setupSpinners(savedInstanceState);
        setupSearch();
        setupActions(view);

        if (savedInstanceState != null) {
            etSearch.setText(
                    savedInstanceState.getString(
                            STATE_SEARCH,
                            ""
                    )
            );
        }

        if (pendingDirectStatus != null
                || pendingDirectDate != null) {

            applyDirectFilters(
                    pendingDirectStatus,
                    pendingDirectDate
            );

            pendingDirectStatus = null;
            pendingDirectDate = null;
        }

        applyFilters();

        return view;
    }

    @Override
    public void onSaveInstanceState(
            @NonNull Bundle outState
    ) {
        super.onSaveInstanceState(outState);

        if (etSearch != null) {
            outState.putString(
                    STATE_SEARCH,
                    etSearch.getText().toString()
            );
        }

        if (spStatus != null) {
            outState.putInt(
                    STATE_STATUS,
                    spStatus.getSelectedItemPosition()
            );
        }

        if (spPriority != null) {
            outState.putInt(
                    STATE_PRIORITY,
                    spPriority.getSelectedItemPosition()
            );
        }

        if (spDate != null) {
            outState.putInt(
                    STATE_DATE,
                    spDate.getSelectedItemPosition()
            );
        }

        if (spSort != null) {
            outState.putInt(
                    STATE_SORT,
                    spSort.getSelectedItemPosition()
            );
        }
    }

    @Override
    public void onDestroyView() {

        if (pendingDeleteDialog != null) {
            AlertDialog dialog = pendingDeleteDialog;
            pendingDeleteDialog = null;
            dialog.dismiss();
        }

        if (rvTasks != null) {
            rvTasks.setAdapter(null);
        }

        adapter = null;

        etSearch = null;
        spStatus = null;
        spPriority = null;
        spDate = null;
        spSort = null;

        tvStatusCount = null;
        btnReset = null;
        rvTasks = null;
        layoutEmpty = null;
        tvEmptyMsg = null;
        btnEmptyAction = null;

        super.onDestroyView();
    }

    @Override
    public void onDetach() {
        host = null;
        super.onDetach();
    }

    private void initViews(View view) {

        etSearch = view.findViewById(
                R.id.etSearchTasks
        );

        spStatus = view.findViewById(
                R.id.spinnerStatus
        );

        spPriority = view.findViewById(
                R.id.spinnerPriority
        );

        spDate = view.findViewById(
                R.id.spinnerDate
        );

        spSort = view.findViewById(
                R.id.spinnerSort
        );

        tvStatusCount = view.findViewById(
                R.id.tvTaskCountStatus
        );

        btnReset = view.findViewById(
                R.id.btnResetFilters
        );

        rvTasks = view.findViewById(
                R.id.rvTasks
        );

        layoutEmpty = view.findViewById(
                R.id.layoutTasksEmpty
        );

        tvEmptyMsg = view.findViewById(
                R.id.tvTasksEmptyMessage
        );

        btnEmptyAction = view.findViewById(
                R.id.btnTasksEmptyAction
        );
    }

    private void setupSpinners(
            @Nullable Bundle savedInstanceState
    ) {

        spStatus.setAdapter(
                new ArrayAdapter<>(
                        requireContext(),
                        R.layout.item_spinner,
                        statusChoices
                )
        );

        spPriority.setAdapter(
                new ArrayAdapter<>(
                        requireContext(),
                        R.layout.item_spinner,
                        priorityChoices
                )
        );

        spDate.setAdapter(
                new ArrayAdapter<>(
                        requireContext(),
                        R.layout.item_spinner,
                        dateChoices
                )
        );

        spSort.setAdapter(
                new ArrayAdapter<>(
                        requireContext(),
                        R.layout.item_spinner,
                        sortChoices
                )
        );

        if (savedInstanceState != null) {

            spStatus.setSelection(
                    savedInstanceState.getInt(
                            STATE_STATUS,
                            0
                    )
            );

            spPriority.setSelection(
                    savedInstanceState.getInt(
                            STATE_PRIORITY,
                            0
                    )
            );

            spDate.setSelection(
                    savedInstanceState.getInt(
                            STATE_DATE,
                            0
                    )
            );

            spSort.setSelection(
                    savedInstanceState.getInt(
                            STATE_SORT,
                            0
                    )
            );
        }

        AdapterView.OnItemSelectedListener listener =
                new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent,
                            View view,
                            int position,
                            long id
                    ) {
                        applyFilters();
                    }

                    @Override
                    public void onNothingSelected(
                            AdapterView<?> parent
                    ) {
                    }
                };

        spStatus.setOnItemSelectedListener(listener);
        spPriority.setOnItemSelectedListener(listener);
        spDate.setOnItemSelectedListener(listener);
        spSort.setOnItemSelectedListener(listener);
    }

    private void setupRecyclerView() {

        adapter = new TaskAdapter(
                (task, checked) -> {

                    Task current = findTaskById(
                            task.getId()
                    );

                    if (current == null) {
                        applyFilters();
                        return;
                    }

                    current.setStatus(
                            checked
                                    ? Task.STATUS_DONE
                                    : Task.STATUS_TODO
                    );

                    notifyProjectUpdated();
                }
        );

        rvTasks.setLayoutManager(
                new LinearLayoutManager(
                        requireContext()
                )
        );

        rvTasks.setAdapter(adapter);

        setupItemTouchHelper();
    }

    private void setupItemTouchHelper() {

        ItemTouchHelper.SimpleCallback callback =
                new ItemTouchHelper.SimpleCallback(
                        ItemTouchHelper.UP
                                | ItemTouchHelper.DOWN,
                        ItemTouchHelper.LEFT
                                | ItemTouchHelper.RIGHT
                ) {

                    @Override
                    public int getMovementFlags(
                            @NonNull RecyclerView recyclerView,
                            @NonNull RecyclerView.ViewHolder viewHolder
                    ) {

                        if (pendingDeleteDialog != null
                                || viewHolder instanceof
                                TaskAdapter.HeaderViewHolder) {

                            return makeMovementFlags(0, 0);
                        }

                        return makeMovementFlags(
                                ItemTouchHelper.UP
                                        | ItemTouchHelper.DOWN,
                                ItemTouchHelper.LEFT
                                        | ItemTouchHelper.RIGHT
                        );
                    }

                    @Override
                    public boolean isLongPressDragEnabled() {
                        return true;
                    }

                    @Override
                    public boolean onMove(
                            @NonNull RecyclerView recyclerView,
                            @NonNull RecyclerView.ViewHolder source,
                            @NonNull RecyclerView.ViewHolder target
                    ) {

                        if (source instanceof
                                TaskAdapter.HeaderViewHolder
                                || target instanceof
                                TaskAdapter.HeaderViewHolder) {
                            return false;
                        }

                        int from =
                                source.getBindingAdapterPosition();

                        int to =
                                target.getBindingAdapterPosition();

                        List<TaskListItem> items =
                                adapter.getItems();

                        if (!isValidPosition(from, items)
                                || !isValidPosition(to, items)) {
                            return false;
                        }

                        if (!(items.get(from)
                                instanceof TaskListItem.TaskItem)
                                || !(items.get(to)
                                instanceof TaskListItem.TaskItem)) {
                            return false;
                        }

                        Task sourceTask =
                                ((TaskListItem.TaskItem)
                                        items.get(from)).getTask();

                        Task targetTask =
                                ((TaskListItem.TaskItem)
                                        items.get(to)).getTask();

                        // Never move tasks across status groups.
                        if (getTaskGroupId(sourceTask)
                                != getTaskGroupId(targetTask)) {
                            return false;
                        }

                        if (from == to) {
                            return false;
                        }

                        // Move the dragged element rather than
                        // swapping distant elements.
                        TaskListItem moved =
                                items.remove(from);

                        items.add(to, moved);

                        adapter.notifyItemMoved(from, to);

                        dragChanged = true;

                        return true;
                    }

                    @Override
                    public void onSelectedChanged(
                            @Nullable RecyclerView.ViewHolder viewHolder,
                            int actionState
                    ) {

                        if (actionState ==
                                ItemTouchHelper.ACTION_STATE_DRAG) {
                            dragChanged = false;
                        }

                        super.onSelectedChanged(
                                viewHolder,
                                actionState
                        );
                    }

                    @Override
                    public void clearView(
                            @NonNull RecyclerView recyclerView,
                            @NonNull RecyclerView.ViewHolder viewHolder
                    ) {

                        super.clearView(
                                recyclerView,
                                viewHolder
                        );

                        if (!dragChanged) {
                            return;
                        }

                        dragChanged = false;

                        // Transfer the visible drag order
                        // into the real project task collection.
                        saveVisibleOrderToProject();

                        // Refresh after the drag animation has
                        // completed to avoid updating rows
                        // while RecyclerView is laying out.
                        recyclerView.post(() -> {

                            if (adapter == null
                                    || spSort == null
                                    || host == null) {
                                return;
                            }

                            // Explicitly select manual ordering
                            // so automatic sorting does not
                            // immediately undo the drag.
                            spSort.setSelection(SORT_CUSTOM);

                            notifyProjectUpdated();
                        });
                    }

                    @Override
                    public void onSwiped(
                            @NonNull RecyclerView.ViewHolder viewHolder,
                            int direction
                    ) {

                        int position =
                                viewHolder.getBindingAdapterPosition();

                        List<TaskListItem> items =
                                adapter.getItems();

                        if (!isValidPosition(position, items)
                                || !(items.get(position)
                                instanceof TaskListItem.TaskItem)) {

                            applyFilters();
                            return;
                        }

                        Task swipedTask =
                                ((TaskListItem.TaskItem)
                                        items.get(position))
                                        .getTask();

                        Task currentTask =
                                findTaskById(swipedTask.getId());

                        if (currentTask == null) {
                            applyFilters();
                            return;
                        }

                        if (direction ==
                                ItemTouchHelper.LEFT) {

                            completeTaskFromSwipe(currentTask);

                        } else if (direction ==
                                ItemTouchHelper.RIGHT) {

                            confirmTaskDeletion(currentTask);
                        }
                    }

                    @Override
                    public void onChildDraw(
                            @NonNull Canvas canvas,
                            @NonNull RecyclerView recyclerView,
                            @NonNull RecyclerView.ViewHolder viewHolder,
                            float dX,
                            float dY,
                            int actionState,
                            boolean isCurrentlyActive
                    ) {

                        if (actionState ==
                                ItemTouchHelper.ACTION_STATE_SWIPE
                                && !(viewHolder instanceof
                                TaskAdapter.HeaderViewHolder)) {

                            View row = viewHolder.itemView;

                            if (dX != 0f) {
                                ColorDrawable background =
                                        new ColorDrawable(
                                                dX > 0f
                                                        ? Color.parseColor(
                                                        "#E74C3C"
                                                )
                                                        : Color.parseColor(
                                                        "#27AE60"
                                                )
                                        );

                                if (dX > 0f) {

                                    background.setBounds(
                                            row.getLeft(),
                                            row.getTop(),
                                            row.getLeft()
                                                    + Math.round(dX),
                                            row.getBottom()
                                    );

                                } else {

                                    background.setBounds(
                                            row.getRight()
                                                    + Math.round(dX),
                                            row.getTop(),
                                            row.getRight(),
                                            row.getBottom()
                                    );
                                }

                                background.draw(canvas);
                            }
                        }

                        super.onChildDraw(
                                canvas,
                                recyclerView,
                                viewHolder,
                                dX,
                                dY,
                                actionState,
                                isCurrentlyActive
                        );
                    }
                };

        new ItemTouchHelper(callback)
                .attachToRecyclerView(rvTasks);
    }

    private boolean isValidPosition(
            int position,
            List<TaskListItem> items
    ) {

        return position != RecyclerView.NO_POSITION
                && position >= 0
                && position < items.size();
    }

    private int getTaskGroupId(Task task) {

        if (task.isOverdue()) {
            return GROUP_OVERDUE;
        }

        if (Task.STATUS_IN_PROGRESS.equals(
                task.getStatus()
        )) {
            return GROUP_IN_PROGRESS;
        }

        if (Task.STATUS_DONE.equals(
                task.getStatus()
        )) {
            return GROUP_DONE;
        }

        return GROUP_TODO;
    }

    /**
     * Save the order shown in RecyclerView into
     * the actual project task collection.
     *
     * Only visible task slots are reordered.
     * Tasks hidden by filters remain in the project.
     */
    private void saveVisibleOrderToProject() {

        if (host == null
                || host.getProject() == null
                || adapter == null) {
            return;
        }

        List<Task> projectTasks =
                host.getProject().getTasks();

        List<List<Task>> grouped =
                new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            grouped.add(new ArrayList<>());
        }

        // Read the final visual order after dragging.
        for (TaskListItem item : adapter.getItems()) {

            if (!(item instanceof
                    TaskListItem.TaskItem)) {
                continue;
            }

            Task task =
                    ((TaskListItem.TaskItem) item)
                            .getTask();

            grouped.get(
                    getTaskGroupId(task)
            ).add(task);
        }

        // Replace only the positions belonging to
        // visible tasks of each group.
        for (List<Task> orderedGroup : grouped) {

            if (orderedGroup.isEmpty()) {
                continue;
            }

            Set<String> visibleIds =
                    new HashSet<>();

            for (Task task : orderedGroup) {
                visibleIds.add(task.getId());
            }

            int orderedIndex = 0;

            for (int i = 0;
                 i < projectTasks.size();
                 i++) {

                Task existing =
                        projectTasks.get(i);

                if (visibleIds.contains(
                        existing.getId()
                )) {

                    projectTasks.set(
                            i,
                            orderedGroup.get(
                                    orderedIndex++
                            )
                    );
                }
            }
        }
    }

    private void completeTaskFromSwipe(Task task) {

        if (Task.STATUS_DONE.equals(
                task.getStatus()
        )) {

            Toast.makeText(
                    requireContext(),
                    "Task already completed",
                    Toast.LENGTH_SHORT
            ).show();

            restoreSwipedTask(task.getId());
            return;
        }

        task.setStatus(Task.STATUS_DONE);

        notifyProjectUpdated();
    }

    private void confirmTaskDeletion(Task task) {

        if (pendingDeleteDialog != null) {
            restoreSwipedTask(task.getId());
            return;
        }

        final String taskId = task.getId();
        final boolean[] deleted = {false};

        AlertDialog dialog =
                new AlertDialog.Builder(
                        requireContext()
                )
                        .setTitle("Delete Task")
                        .setMessage(
                                "Are you sure you want to delete this task?"
                        )
                        .setPositiveButton(
                                "Delete",
                                (d, which) -> {

                                    if (host == null
                                            || host.getProject() == null) {
                                        return;
                                    }

                                    List<Task> tasks =
                                            host.getProject()
                                                    .getTasks();

                                    for (int i = 0;
                                         i < tasks.size();
                                         i++) {

                                        if (taskId.equals(
                                                tasks.get(i).getId()
                                        )) {

                                            tasks.remove(i);
                                            deleted[0] = true;

                                            notifyProjectUpdated();
                                            break;
                                        }
                                    }
                                }
                        )
                        .setNegativeButton(
                                "Cancel",
                                null
                        )
                        .create();

        pendingDeleteDialog = dialog;

        dialog.setOnDismissListener(ignored -> {

            if (pendingDeleteDialog == dialog) {
                pendingDeleteDialog = null;
            }

            if (!deleted[0]) {
                restoreSwipedTask(taskId);
            }
        });

        dialog.show();
    }

    private void restoreSwipedTask(String taskId) {

        if (adapter == null) {
            return;
        }

        List<TaskListItem> items =
                adapter.getItems();

        for (int i = 0; i < items.size(); i++) {

            if (!(items.get(i) instanceof
                    TaskListItem.TaskItem)) {
                continue;
            }

            Task task =
                    ((TaskListItem.TaskItem)
                            items.get(i)).getTask();

            if (taskId.equals(task.getId())) {
                adapter.notifyItemChanged(i);
                return;
            }
        }

        applyFilters();
    }

    private Task findTaskById(String taskId) {

        if (host == null
                || host.getProject() == null
                || taskId == null) {
            return null;
        }

        for (Task task :
                host.getProject().getTasks()) {

            if (taskId.equals(task.getId())) {
                return task;
            }
        }

        return null;
    }

    private void notifyProjectUpdated() {

        if (host != null) {
            // ProjectActivity updates:
            // Overview, tab counts and TasksFragment.
            host.notifyProjectChanged();
        } else {
            applyFilters();
        }
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
                        applyFilters();
                    }

                    @Override
                    public void afterTextChanged(
                            Editable s
                    ) {
                    }
                }
        );
    }

    private void setupActions(View view) {

        btnReset.setOnClickListener(
                v -> resetFilters()
        );

        view.findViewById(
                R.id.btnTasksAddTask
        ).setOnClickListener(
                v -> openAddTaskDialog()
        );
    }

    private void resetFilters() {

        if (etSearch == null) {
            return;
        }

        etSearch.setText("");

        spStatus.setSelection(0);
        spPriority.setSelection(0);
        spDate.setSelection(0);
        spSort.setSelection(0);

        applyFilters();
    }

    /**
     * Used by OverviewFragment quick links.
     *
     * Example:
     * To do -> status "To do"
     * Overdue -> date "Overdue"
     */
    public void applyDirectFilters(
            String targetStatus,
            String targetDate
    ) {

        if (spStatus == null
                || spDate == null) {

            pendingDirectStatus = targetStatus;
            pendingDirectDate = targetDate;
            return;
        }

        if (targetStatus != null) {

            for (int i = 0;
                 i < statusChoices.length;
                 i++) {

                if (statusChoices[i]
                        .equalsIgnoreCase(
                                targetStatus
                        )) {

                    spStatus.setSelection(i);
                    break;
                }
            }
        }

        if (targetDate != null) {

            for (int i = 0;
                 i < dateChoices.length;
                 i++) {

                if (dateChoices[i]
                        .equalsIgnoreCase(
                                targetDate
                        )) {

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

        if (host == null
                || host.getProject() == null
                || adapter == null
                || etSearch == null
                || spStatus == null
                || spPriority == null
                || spDate == null
                || spSort == null) {
            return;
        }

        List<Task> all =
                host.getProject().getTasks();

        List<Task> filteredTasks =
                new ArrayList<>();

        String query =
                etSearch.getText()
                        .toString()
                        .trim()
                        .toLowerCase(
                                Locale.ROOT
                        );

        int statusPos =
                spStatus.getSelectedItemPosition();

        int priorityPos =
                spPriority.getSelectedItemPosition();

        int datePos =
                spDate.getSelectedItemPosition();

        int sortPos =
                spSort.getSelectedItemPosition();

        // Apply all search/filter conditions together.
        for (Task task : all) {

            if (!query.isEmpty()
                    && !task.getDescription()
                    .toLowerCase(Locale.ROOT)
                    .contains(query)) {
                continue;
            }

            if (statusPos == 1
                    && !Task.STATUS_TODO.equals(
                    task.getStatus())) {
                continue;
            }

            if (statusPos == 2
                    && !Task.STATUS_IN_PROGRESS.equals(
                    task.getStatus())) {
                continue;
            }

            if (statusPos == 3
                    && !Task.STATUS_DONE.equals(
                    task.getStatus())) {
                continue;
            }

            if (priorityPos == 1
                    && !Task.PRIORITY_HIGH.equals(
                    task.getPriority())) {
                continue;
            }

            if (priorityPos == 2
                    && !Task.PRIORITY_NORMAL.equals(
                    task.getPriority())) {
                continue;
            }

            if (priorityPos == 3
                    && !Task.PRIORITY_LOW.equals(
                    task.getPriority())) {
                continue;
            }

            if (datePos == 1
                    && !task.isDueToday()) {
                continue;
            }

            if (datePos == 2
                    && !task.isOverdue()) {
                continue;
            }

            if (datePos == 3
                    && task.getDueDateMillis() > 0L) {
                continue;
            }

            filteredTasks.add(task);
        }

        // Custom order keeps the ordering already
        // stored in the project's task collection.
        if (sortPos != SORT_CUSTOM) {

            filteredTasks.sort((first, second) -> {

                switch (sortPos) {

                    case 1:
                        // Due date descending.
                        // Undated tasks appear last.
                        long descendingFirst =
                                first.getDueDateMillis() == 0L
                                        ? Long.MIN_VALUE
                                        : first.getDueDateMillis();

                        long descendingSecond =
                                second.getDueDateMillis() == 0L
                                        ? Long.MIN_VALUE
                                        : second.getDueDateMillis();

                        return Long.compare(
                                descendingSecond,
                                descendingFirst
                        );

                    case 2:
                        return Integer.compare(
                                getPriorityRank(
                                        second.getPriority()
                                ),
                                getPriorityRank(
                                        first.getPriority()
                                )
                        );

                    case 3:
                        return first.getDescription()
                                .compareToIgnoreCase(
                                        second.getDescription()
                                );

                    default:
                        // Default: due date ascending.
                        // Undated tasks appear last.
                        long ascendingFirst =
                                first.getDueDateMillis() == 0L
                                        ? Long.MAX_VALUE
                                        : first.getDueDateMillis();

                        long ascendingSecond =
                                second.getDueDateMillis() == 0L
                                        ? Long.MAX_VALUE
                                        : second.getDueDateMillis();

                        return Long.compare(
                                ascendingFirst,
                                ascendingSecond
                        );
                }
            });
        }

        List<Task> overdue =
                new ArrayList<>();

        List<Task> inProgress =
                new ArrayList<>();

        List<Task> toDo =
                new ArrayList<>();

        List<Task> done =
                new ArrayList<>();

        for (Task task : filteredTasks) {

            switch (getTaskGroupId(task)) {

                case GROUP_OVERDUE:
                    overdue.add(task);
                    break;

                case GROUP_IN_PROGRESS:
                    inProgress.add(task);
                    break;

                case GROUP_DONE:
                    done.add(task);
                    break;

                default:
                    toDo.add(task);
                    break;
            }
        }

        // One RecyclerView with exactly two
        // row types: header and task.
        List<TaskListItem> flatList =
                new ArrayList<>();

        appendGroup(
                flatList,
                "Overdue",
                overdue
        );

        appendGroup(
                flatList,
                "In Progress",
                inProgress
        );

        appendGroup(
                flatList,
                "To Do",
                toDo
        );

        appendGroup(
                flatList,
                "Done",
                done
        );

        adapter.submitList(flatList);

        tvStatusCount.setText(
                String.format(
                        Locale.getDefault(),
                        "Showing %d of %d tasks",
                        filteredTasks.size(),
                        all.size()
                )
        );

        boolean hasMatches =
                !filteredTasks.isEmpty();

        rvTasks.setVisibility(
                hasMatches
                        ? View.VISIBLE
                        : View.GONE
        );

        layoutEmpty.setVisibility(
                hasMatches
                        ? View.GONE
                        : View.VISIBLE
        );

        if (!hasMatches) {

            if (all.isEmpty()) {

                tvEmptyMsg.setText(
                        "No tasks yet"
                );

                btnEmptyAction.setText(
                        "+ Add Task"
                );

                btnEmptyAction.setOnClickListener(
                        v -> openAddTaskDialog()
                );

            } else {

                tvEmptyMsg.setText(
                        "No tasks match these filters"
                );

                btnEmptyAction.setText(
                        "Reset Filters"
                );

                btnEmptyAction.setOnClickListener(
                        v -> resetFilters()
                );
            }
        }
    }

    /**
     * Adds a group only when it contains tasks.
     * Empty section headers are never displayed.
     */
    private void appendGroup(
            List<TaskListItem> output,
            String title,
            List<Task> tasks
    ) {

        if (tasks.isEmpty()) {
            return;
        }

        output.add(
                new TaskListItem.Header(
                        title,
                        tasks.size()
                )
        );

        for (Task task : tasks) {
            output.add(
                    new TaskListItem.TaskItem(
                            task
                    )
            );
        }
    }

    private int getPriorityRank(
            String priority
    ) {

        if (Task.PRIORITY_HIGH.equals(
                priority
        )) {
            return 3;
        }

        if (Task.PRIORITY_NORMAL.equals(
                priority
        )) {
            return 2;
        }

        return 1;
    }

    private void openAddTaskDialog() {

        if (!isAdded()
                || host == null
                || host.getProject() == null) {
            return;
        }

        TaskDialogHelper.showAddTaskDialog(
                requireContext(),
                host.getProject(),
                this::notifyProjectUpdated
        );
    }
}
