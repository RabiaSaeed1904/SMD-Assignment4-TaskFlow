package com.example.taskflow;

import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<TaskListItem> items = new ArrayList<>();

    public interface OnTaskInteractListener {
        void onTaskChecked(Task task, boolean isChecked);
    }

    private final OnTaskInteractListener listener;

    public TaskAdapter(OnTaskInteractListener listener) {
        this.listener = listener;
    }

    public void submitList(List<TaskListItem> newItems) {
        this.items.clear();
        this.items.addAll(newItems);
        notifyDataSetChanged();
    }

    public List<TaskListItem> getItems() {
        return items;
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TaskListItem.TYPE_HEADER) {
            return new HeaderViewHolder(inflater.inflate(R.layout.item_task_header, parent, false));
        } else {
            return new TaskViewHolder(inflater.inflate(R.layout.item_task, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TaskListItem.TYPE_HEADER) {
            ((HeaderViewHolder) holder).bind(((TaskListItem.Header) items.get(position)));
        } else {
            ((TaskViewHolder) holder).bind(((TaskListItem.TaskItem) items.get(position)).getTask());
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvCount;

        HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvHeaderTitle);
            tvCount = itemView.findViewById(R.id.tvHeaderCount);
        }

        void bind(TaskListItem.Header header) {
            tvTitle.setText(header.getTitle());
            tvCount.setText(String.valueOf(header.getCount()));

            String colorHex = "#202C40";
            if (header.getTitle().equals("Overdue")) colorHex = "#E74C3C";
            else if (header.getTitle().equals("In Progress")) colorHex = "#2ECC71";
            else if (header.getTitle().equals("To Do")) colorHex = "#3498DB";
            else if (header.getTitle().equals("Done")) colorHex = "#27AE60";

            tvTitle.setTextColor(Color.parseColor(colorHex));
            tvCount.setTextColor(Color.parseColor(colorHex));
        }
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {
        CheckBox cbDone;
        TextView tvDesc, tvDetails;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            cbDone = itemView.findViewById(R.id.cbTaskDone);
            tvDesc = itemView.findViewById(R.id.tvTaskDesc);
            tvDetails = itemView.findViewById(R.id.tvTaskDetails);

            cbDone.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onTaskChecked(((TaskListItem.TaskItem) items.get(pos)).getTask(), cbDone.isChecked());
                }
            });
        }

        void bind(Task task) {
            cbDone.setChecked(Task.STATUS_DONE.equals(task.getStatus()));
            tvDesc.setText(task.getDescription());

            if (cbDone.isChecked()) {
                tvDesc.setPaintFlags(tvDesc.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                tvDesc.setTextColor(Color.parseColor("#91A1BB"));
            } else {
                tvDesc.setPaintFlags(tvDesc.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                tvDesc.setTextColor(Color.parseColor("#202C40"));
            }

            StringBuilder details = new StringBuilder();
            details.append(task.getStatus()).append(" • ").append(task.getPriority());

            if (task.getDueDateMillis() > 0) {
                if (task.isDueToday()) {
                    details.append(" • Due today");
                } else {
                    String dateStr = new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(new Date(task.getDueDateMillis()));
                    details.append(" • ").append(dateStr);
                    if (task.isOverdue()) {
                        details.append(" (OVERDUE)");
                    }
                }
            } else {
                details.append(" • No due date");
            }
            tvDetails.setText(details.toString());
        }
    }
}