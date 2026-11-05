package com.example.taskflow;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TaskDialogHelper {

    public interface OnTaskAddedListener {
        void onAdded();
    }

    public static void showAddTaskDialog(Context context, Project project, OnTaskAddedListener listener) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_task, null);

        EditText etDesc = dialogView.findViewById(R.id.etTaskDescription);
        TextView tvCounter = dialogView.findViewById(R.id.tvTaskCharCounter);
        Spinner spPriority = dialogView.findViewById(R.id.spinnerDialogPriority);
        Spinner spStatus = dialogView.findViewById(R.id.spinnerDialogStatus);
        Button btnPickDate = dialogView.findViewById(R.id.btnPickDueDate);
        Button btnClearDate = dialogView.findViewById(R.id.btnClearDueDate);

        final long[] selectedDueDate = {0L};

        etDesc.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                tvCounter.setText(s.length() + "/500");
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Updated to use the custom item_spinner layout
        ArrayAdapter<String> pAdapter = new ArrayAdapter<>(context, R.layout.item_spinner,
                new String[]{Task.PRIORITY_NORMAL, Task.PRIORITY_HIGH, Task.PRIORITY_LOW});
        spPriority.setAdapter(pAdapter);

        // Updated to use the custom item_spinner layout
        ArrayAdapter<String> sAdapter = new ArrayAdapter<>(context, R.layout.item_spinner,
                new String[]{Task.STATUS_TODO, Task.STATUS_IN_PROGRESS, Task.STATUS_DONE});
        spStatus.setAdapter(sAdapter);

        btnPickDate.setOnClickListener(v -> {
            Calendar now = Calendar.getInstance();
            DatePickerDialog dpd = new DatePickerDialog(context, (view, year, month, dayOfMonth) -> {
                Calendar chosen = Calendar.getInstance();
                chosen.set(year, month, dayOfMonth, 0, 0, 0);
                selectedDueDate[0] = chosen.getTimeInMillis();
                btnPickDate.setText(new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(new Date(selectedDueDate[0])));
                btnClearDate.setVisibility(View.VISIBLE);
            }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
            dpd.show();
        });

        btnClearDate.setOnClickListener(v -> {
            selectedDueDate[0] = 0L;
            btnPickDate.setText("Select Due Date");
            btnClearDate.setVisibility(View.GONE);
        });

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle("Add Task")
                .setView(dialogView)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Add", null)
                .create();

        dialog.setOnShowListener(d -> {
            Button addBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            addBtn.setOnClickListener(v -> {
                String desc = etDesc.getText().toString().trim();
                if (desc.isEmpty() || desc.length() > 500) {
                    etDesc.setError("Task description must be 1 to 500 characters");
                    etDesc.requestFocus();
                    return;
                }

                String priority = (String) spPriority.getSelectedItem();
                String status = (String) spStatus.getSelectedItem();

                Task newTask = new Task(desc, priority, status, selectedDueDate[0]);
                project.getTasks().add(newTask);

                if (listener != null) listener.onAdded();
                dialog.dismiss();
                Toast.makeText(context, "Task created", Toast.LENGTH_SHORT).show();
            });
        });

        dialog.show();
    }
}