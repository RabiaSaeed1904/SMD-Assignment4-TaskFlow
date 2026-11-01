package com.example.taskflow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;

public class Project implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String id;
    private String name;
    private String tag;
    private String description;
    private int commentCount;
    private final ArrayList<Task> tasks;
    private final ArrayList<AttachmentLink> attachments;

    public Project(String name, String tag, String description, int commentCount) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.tag = tag;
        this.description = description;
        this.commentCount = commentCount;
        this.tasks = new ArrayList<>();
        this.attachments = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public ArrayList<Task> getTasks() {
        return tasks;
    }

    public ArrayList<AttachmentLink> getAttachments() {
        return attachments;
    }

    public int getCompletedTaskCount() {
        int count = 0;
        for (Task t : tasks) {
            if (t.isDone()) {
                count++;
            }
        }
        return count;
    }

    public int getProgressPercentage() {
        if (tasks.isEmpty()) {
            return 0;
        }
        // Integer division removes fractional part: e.g., 4/12 becomes 33%
        return (getCompletedTaskCount() * 100) / tasks.size();
    }

    public int getTodoCount() {
        int count = 0;
        for (Task t : tasks) {
            if (Task.STATUS_TODO.equals(t.getStatus())) {
                count++;
            }
        }
        return count;
    }

    public int getInProgressCount() {
        int count = 0;
        for (Task t : tasks) {
            if (Task.STATUS_IN_PROGRESS.equals(t.getStatus())) {
                count++;
            }
        }
        return count;
    }

    public int getOverdueCount() {
        int count = 0;
        for (Task t : tasks) {
            if (t.isOverdue()) {
                count++;
            }
        }
        return count;
    }
}