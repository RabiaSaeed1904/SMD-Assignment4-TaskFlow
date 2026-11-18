
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

import java.util.Objects;

public class ProjectAdapter
        extends ListAdapter<Project, ProjectAdapter.ProjectViewHolder> {

    public interface OnProjectClickListener {
        void onProjectClick(Project project);
    }

    private final OnProjectClickListener listener;

    private static final DiffUtil.ItemCallback<Project> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Project>() {

                @Override
                public boolean areItemsTheSame(
                        @NonNull Project oldItem,
                        @NonNull Project newItem
                ) {
                    // Compare stable project IDs, not names or positions.
                    return Objects.equals(
                            oldItem.getId(),
                            newItem.getId()
                    );
                }

                @Override
                public boolean areContentsTheSame(
                        @NonNull Project oldItem,
                        @NonNull Project newItem
                ) {
                    // Compare every field displayed on a project card.
                    return Objects.equals(
                            oldItem.getName(),
                            newItem.getName()
                    )
                            && Objects.equals(
                            oldItem.getTag(),
                            newItem.getTag()
                    )
                            && Objects.equals(
                            oldItem.getDescription(),
                            newItem.getDescription()
                    )
                            && oldItem.getCompletedTaskCount()
                            == newItem.getCompletedTaskCount()
                            && oldItem.getTasks().size()
                            == newItem.getTasks().size()
                            && oldItem.getProgressPercentage()
                            == newItem.getProgressPercentage()
                            && oldItem.getAttachments().size()
                            == newItem.getAttachments().size()
                            && oldItem.getCommentCount()
                            == newItem.getCommentCount();
                }
            };

    public ProjectAdapter(OnProjectClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProjectViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(
                parent.getContext()
        ).inflate(
                R.layout.item_project,
                parent,
                false
        );

        return new ProjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ProjectViewHolder holder,
            int position
    ) {
        holder.bind(getItem(position));
    }

    class ProjectViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvTag;
        private final TextView tvName;
        private final TextView tvDesc;
        private final TextView tvTaskProgress;
        private final TextView tvPercentage;
        private final TextView tvAttachments;
        private final TextView tvComments;
        private final ProgressBar progressBar;

        public ProjectViewHolder(@NonNull View itemView) {
            super(itemView);

            tvTag = itemView.findViewById(
                    R.id.tvProjectTag
            );

            tvName = itemView.findViewById(
                    R.id.tvProjectName
            );

            tvDesc = itemView.findViewById(
                    R.id.tvProjectDescription
            );

            tvTaskProgress = itemView.findViewById(
                    R.id.tvProjectTaskProgress
            );

            tvPercentage = itemView.findViewById(
                    R.id.tvProjectPercentage
            );

            progressBar = itemView.findViewById(
                    R.id.pbProjectProgress
            );

            tvAttachments = itemView.findViewById(
                    R.id.tvProjectAttachmentsCount
            );

            tvComments = itemView.findViewById(
                    R.id.tvProjectCommentsCount
            );

            itemView.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();

                if (position == RecyclerView.NO_POSITION) {
                    return;
                }

                if (listener != null) {
                    listener.onProjectClick(
                            getItem(position)
                    );
                }
            });
        }

        public void bind(@NonNull Project project) {

            // Project tag and name
            tvTag.setText(project.getTag());
            tvName.setText(project.getName());

            // Project description: maximum two lines
            tvDesc.setText(project.getDescription());
            tvDesc.setMaxLines(2);
            tvDesc.setEllipsize(
                    android.text.TextUtils.TruncateAt.END
            );

            // Task completion counts
            int completed = project.getCompletedTaskCount();
            int total = project.getTasks().size();

            tvTaskProgress.setText(
                    completed + "/" + total + " tasks"
            );

            // Progress percentage
            int percentage = project.getProgressPercentage();

            tvPercentage.setText(
                    percentage + "%"
            );

            progressBar.setMax(100);
            progressBar.setProgress(percentage);

            // Attachment count
            int attachmentCount =
                    project.getAttachments().size();

            tvAttachments.setText(
                    attachmentCount + " Attachments"
            );

            // Actual project comment count
            int commentCount =
                    project.getCommentCount();

            tvComments.setText(
                    commentCount + " Comments"
            );
        }
    }
}
