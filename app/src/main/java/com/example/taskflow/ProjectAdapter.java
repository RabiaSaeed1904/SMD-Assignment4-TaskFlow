package com.example.taskflow;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

public class ProjectAdapter extends ListAdapter<Project, ProjectAdapter.ProjectViewHolder> {

    public interface OnProjectClickListener {
        void onProjectClick(Project project);
    }

    private final OnProjectClickListener listener;

    public ProjectAdapter(OnProjectClickListener listener) {
        super(new DiffUtil.ItemCallback<Project>() {
            @Override
            public boolean areItemsTheSame(@NonNull Project oldItem, @NonNull Project newItem) {
                return oldItem.getId().equals(newItem.getId());
            }

            @Override
            public boolean areContentsTheSame(@NonNull Project oldItem, @NonNull Project newItem) {
                return oldItem.getName().equals(newItem.getName()) &&
                        oldItem.getTag().equals(newItem.getTag()) &&
                        oldItem.getDescription().equals(newItem.getDescription()) &&
                        oldItem.getProgressPercentage() == newItem.getProgressPercentage() &&
                        oldItem.getCompletedTaskCount() == newItem.getCompletedTaskCount() &&
                        oldItem.getTasks().size() == newItem.getTasks().size() &&
                        oldItem.getAttachments().size() == newItem.getAttachments().size();
            }
        });
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_project, parent, false);
        return new ProjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class ProjectViewHolder extends RecyclerView.ViewHolder {
        TextView tvTag, tvName, tvDesc, tvTaskProgress, tvPercentage, tvAttachments, tvComments;
        ProgressBar progressBar;

        public ProjectViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTag = itemView.findViewById(R.id.tvProjectTag);
            tvName = itemView.findViewById(R.id.tvProjectName);
            tvDesc = itemView.findViewById(R.id.tvProjectDescription);
            tvTaskProgress = itemView.findViewById(R.id.tvProjectTaskProgress);
            tvPercentage = itemView.findViewById(R.id.tvProjectPercentage);
            progressBar = itemView.findViewById(R.id.pbProjectProgress);
            tvAttachments = itemView.findViewById(R.id.tvProjectAttachmentsCount);
            tvComments = itemView.findViewById(R.id.tvProjectCommentsCount);

            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onProjectClick(getItem(pos));
                }
            });
        }

        public void bind(Project project) {
            tvTag.setText(project.getTag());
            tvName.setText(project.getName());
            tvDesc.setText(project.getDescription());

            int done = project.getCompletedTaskCount();
            int total = project.getTasks().size();
            tvTaskProgress.setText(done + "/" + total + " tasks");

            int pct = project.getProgressPercentage();
            tvPercentage.setText(pct + "%");
            progressBar.setProgress(pct);

            tvAttachments.setText(project.getAttachments().size() + " Attachments");

            if (tvComments != null) {
                tvComments.setText("2 Comments");
            }
        }
    }
}