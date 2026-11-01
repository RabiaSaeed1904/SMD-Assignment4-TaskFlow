package com.example.taskflow;

import java.io.Serializable;
import java.util.Calendar;
import java.util.UUID;

public class Task implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String STATUS_TODO = "To do";
    public static final String STATUS_IN_PROGRESS = "In progress";
    public static final String STATUS_DONE = "Done";

    public static final String PRIORITY_HIGH = "High";
    public static final String PRIORITY_NORMAL = "Normal";
    public static final String PRIORITY_LOW = "Low";

    private final String id;
    private String description;
    private String status;
    private String priority;
    private long dueDateMillis; // 0L means no due date
    private final long creationTimeMillis;

    public Task(String description, String priority, String status, long dueDateMillis) {
        this.id = UUID.randomUUID().toString();
        this.description = description;
        this.priority = (priority != null) ? priority : PRIORITY_NORMAL;
        this.status = (status != null) ? status : STATUS_TODO;
        this.dueDateMillis = dueDateMillis;
        this.creationTimeMillis = System.currentTimeMillis();
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public long getDueDateMillis() {
        return dueDateMillis;
    }

    public void setDueDateMillis(long dueDateMillis) {
        this.dueDateMillis = dueDateMillis;
    }

    public long getCreationTimeMillis() {
        return creationTimeMillis;
    }

    public boolean isDone() {
        return STATUS_DONE.equals(status);
    }

    public boolean isDueToday() {
        if (dueDateMillis <= 0L) {
            return false;
        }
        Calendar todayStart = Calendar.getInstance();
        todayStart.set(Calendar.HOUR_OF_DAY, 0);
        todayStart.set(Calendar.MINUTE, 0);
        todayStart.set(Calendar.SECOND, 0);
        todayStart.set(Calendar.MILLISECOND, 0);

        Calendar tomorrowStart = (Calendar) todayStart.clone();
        tomorrowStart.add(Calendar.DAY_OF_MONTH, 1);

        return dueDateMillis >= todayStart.getTimeInMillis() && dueDateMillis < tomorrowStart.getTimeInMillis();
    }

    public boolean isOverdue() {
        if (isDone() || dueDateMillis <= 0L) {
            return false;
        }
        Calendar todayStart = Calendar.getInstance();
        todayStart.set(Calendar.HOUR_OF_DAY, 0);
        todayStart.set(Calendar.MINUTE, 0);
        todayStart.set(Calendar.SECOND, 0);
        todayStart.set(Calendar.MILLISECOND, 0);

        return dueDateMillis < todayStart.getTimeInMillis();
    }
}