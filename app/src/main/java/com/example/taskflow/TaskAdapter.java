package com.example.taskflow;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskAdapter extends BaseAdapter {

    public interface OnTaskStatusChangedListener {
        void onStatusChanged();
    }

    private final Context context;
    private final List<Task> tasks;
    private final LayoutInflater inflater;
    private final OnTaskStatusChangedListener listener;

    public TaskAdapter(Context context, List<Task> tasks, OnTaskStatusChangedListener listener) {
        this.context = context;
        this.tasks = tasks;
        this.listener = listener;
        this.inflater = LayoutInflater.from(context);
    }

    @Override public int getCount() { return tasks.size(); }
    @Override public Task getItem(int position) { return tasks.get(position); }
    @Override public long getItemId(int position) { return position; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_task, parent, false);
        }

        CheckBox cbCompleted = convertView.findViewById(R.id.cbTaskCompleted);
        TextView tvDesc = convertView.findViewById(R.id.tvTaskDescription);
        TextView tvMeta = convertView.findViewById(R.id.tvTaskMetadata);

        Task task = getItem(position);

        tvDesc.setText(task.getDescription());

        // Remove listener temporarily to prevent firing during recycle
        cbCompleted.setOnCheckedChangeListener(null);
        cbCompleted.setChecked(task.isDone());

        // Apply strike-through for completed tasks
        if (task.isDone()) {
            tvDesc.setPaintFlags(tvDesc.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            tvDesc.setTextColor(0xFF91A1BB);
        } else {
            tvDesc.setPaintFlags(tvDesc.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            tvDesc.setTextColor(0xFF202C40);
        }

        // Format metadata string
        StringBuilder meta = new StringBuilder();
        meta.append(task.getStatus()).append(" • ").append(task.getPriority()).append(" • ");

        if (task.getDueDateMillis() <= 0L) {
            meta.append("No due date");
        } else if (task.isDueToday()) {
            meta.append("Due today");
        } else {
            String dateStr = new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(new Date(task.getDueDateMillis()));
            meta.append(dateStr);
        }

        if (task.isOverdue()) {
            meta.append(" (OVERDUE)");
            tvMeta.setTextColor(0xFFE67C7C);
        } else {
            tvMeta.setTextColor(0xFF91A1BB);
        }

        tvMeta.setText(meta.toString());

        cbCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
            task.setStatus(isChecked ? Task.STATUS_DONE : Task.STATUS_TODO);
            if (listener != null) listener.onStatusChanged();
        });

        return convertView;
    }
}