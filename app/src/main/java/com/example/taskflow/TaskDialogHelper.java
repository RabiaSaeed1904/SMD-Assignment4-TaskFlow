package com.example.taskflow;

import android.content.Context;
import android.widget.Toast;

public class TaskDialogHelper {
    public interface OnTaskAddedListener {
        void onAdded();
    }
    public static void showAddTaskDialog(Context context, Project project, OnTaskAddedListener listener) {
        // Temporarily display a toast. Full implementation coming in Commit 7.
        Toast.makeText(context, "Add Task dialog will be implemented in the next step.", Toast.LENGTH_SHORT).show();
    }
}