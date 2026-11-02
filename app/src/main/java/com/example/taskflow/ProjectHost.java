package com.example.taskflow;

/**
 * Interface used by Fragments to communicate with ProjectActivity.
 */
public interface ProjectHost {
    Project getProject();
    void notifyProjectChanged();
    void navigateToTasks(String statusFilter, String dateFilter);
    void navigateToTab(int tabIndex);
}