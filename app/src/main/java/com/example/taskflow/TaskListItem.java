package com.example.taskflow;

public abstract class TaskListItem {
    public static final int TYPE_HEADER = 0;
    public static final int TYPE_TASK = 1;

    public abstract int getType();

    public static class Header extends TaskListItem {
        private String title;
        private int count;

        public Header(String title, int count) {
            this.title = title;
            this.count = count;
        }

        public String getTitle() { return title; }
        public int getCount() { return count; }

        @Override
        public int getType() { return TYPE_HEADER; }
    }

    public static class TaskItem extends TaskListItem {
        private Task task;

        public TaskItem(Task task) {
            this.task = task;
        }

        public Task getTask() { return task; }

        @Override
        public int getType() { return TYPE_TASK; }
    }
}