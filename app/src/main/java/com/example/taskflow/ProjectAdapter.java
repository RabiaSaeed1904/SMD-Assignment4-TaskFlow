package com.example.taskflow;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

public class ProjectAdapter extends BaseAdapter {

    private final Context context;
    private final List<Project> projects;
    private final LayoutInflater inflater;

    public ProjectAdapter(Context context, List<Project> projects) {
        this.context = context;
        this.projects = projects;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return projects.size();
    }

    @Override
    public Project getItem(int position) {
        return projects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position; // Using position for standard BaseAdapter, but models use UUIDs
    }

    static class ViewHolder {
        TextView tvTag;
        TextView tvName;
        TextView tvDescription;
        TextView tvTaskFraction;
        TextView tvPercentage;
        ProgressBar pbProgress;
        TextView tvAttachments;
        TextView tvComments;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_project, parent, false);
            holder = new ViewHolder();
            holder.tvTag = convertView.findViewById(R.id.tvItemTag);
            holder.tvName = convertView.findViewById(R.id.tvItemName);
            holder.tvDescription = convertView.findViewById(R.id.tvItemDescription);
            holder.tvTaskFraction = convertView.findViewById(R.id.tvItemTaskFraction);
            holder.tvPercentage = convertView.findViewById(R.id.tvItemPercentage);
            holder.pbProgress = convertView.findViewById(R.id.pbItemProgress);
            holder.tvAttachments = convertView.findViewById(R.id.tvItemAttachments);
            holder.tvComments = convertView.findViewById(R.id.tvItemComments);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Project p = getItem(position);
        holder.tvTag.setText(p.getTag() != null ? p.getTag() : "Project");
        holder.tvName.setText(p.getName() != null ? p.getName() : "Untitled");
        holder.tvDescription.setText(p.getDescription() != null ? p.getDescription() : "");

        int total = p.getTasks().size();
        int done = p.getCompletedTaskCount();
        int percent = p.getProgressPercentage();

        holder.tvTaskFraction.setText(done + "/" + total + " tasks");
        holder.tvPercentage.setText(percent + "%");
        holder.pbProgress.setProgress(percent);

        int files = p.getAttachments().size();
        holder.tvAttachments.setText("📎 " + files + (files == 1 ? " Attachment" : " Attachments"));
        holder.tvComments.setText("💬 " + p.getCommentCount() + (p.getCommentCount() == 1 ? " Comment" : " Comments"));

        return convertView;
    }
}