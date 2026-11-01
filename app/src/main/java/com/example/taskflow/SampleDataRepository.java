package com.example.taskflow;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class SampleDataRepository {

    private static ArrayList<CatalogItem> catalog;

    /**
     * In-memory local catalog of eligible files.
     * Includes PNG/JPG images and text documents with real preview content.
     */
    public static synchronized List<CatalogItem> getLocalCatalog() {
        if (catalog == null) {
            catalog = new ArrayList<>();

            // 1. Image Catalog Item
            catalog.add(new CatalogItem(
                    "cat_fig_1",
                    "sports-projectv2.fig",
                    CatalogItem.Type.IMAGE,
                    "2.4 MB",
                    android.R.drawable.ic_menu_gallery,
                    null
            ));

            // 2. Text/Document Catalog Item
            catalog.add(new CatalogItem(
                    "cat_doc_1",
                    "briefing from client.pdf",
                    CatalogItem.Type.TEXT,
                    "450 KB",
                    0,
                    "CLIENT BRIEF & REQUIREMENTS:\n\n"
                            + "1. Project Scope: Sports management and tracking application.\n"
                            + "2. Design Colors: Blue (#3099F3) and Red accents.\n"
                            + "3. Responsive cards showing pricing and features.\n"
                            + "4. Clean typography with distinct category badges."
            ));

            // 3. Image Catalog Item
            catalog.add(new CatalogItem(
                    "cat_img_1",
                    "mount asset.png",
                    CatalogItem.Type.IMAGE,
                    "1.1 MB",
                    android.R.drawable.ic_menu_report_image,
                    null
            ));

            // 4. Text/Document Catalog Item
            catalog.add(new CatalogItem(
                    "cat_txt_1",
                    "api_endpoints_v1.txt",
                    CatalogItem.Type.TEXT,
                    "14 KB",
                    0,
                    "BACKEND API SPECIFICATIONS:\n\n"
                            + "• POST /api/v1/auth/login\n"
                            + "• GET  /api/v1/projects\n"
                            + "• POST /api/v1/tasks/create\n"
                            + "• PUT  /api/v1/tasks/{id}/status"
            ));
        }
        return catalog;
    }

    /**
     * Seeds the 5 initial projects displayed in the Assignment 3 workspace screen.
     */
    public static ArrayList<Project> getInitialProjects() {
        ArrayList<Project> list = new ArrayList<>();
        List<CatalogItem> cat = getLocalCatalog();

        // Calculate reference calendar dates
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        long todayMillis = calendar.getTimeInMillis();

        calendar.add(Calendar.DAY_OF_MONTH, -5);
        long fiveDaysAgoMillis = calendar.getTimeInMillis(); // Causes Overdue if unfinished

        calendar.add(Calendar.DAY_OF_MONTH, 10);
        long fiveDaysLaterMillis = calendar.getTimeInMillis();

        // =========================================================================
        // 1. Service Page Website (User Interface)
        // 4/12 tasks (33%), 3 Attachments, 3 Comments
        // Breakdown: 5 To do, 3 In progress, 4 Done (Overdue: 2)
        // =========================================================================
        Project p1 = new Project(
                "Service Page Website",
                "User Interface",
                "Make a page display about services for the company website with blue and red colors and provide some price information on each product card.",
                3
        );

        // 3 Attachments
        p1.getAttachments().add(new AttachmentLink(cat.get(0))); // sports-projectv2.fig
        p1.getAttachments().add(new AttachmentLink(cat.get(1))); // briefing from client.pdf
        p1.getAttachments().add(new AttachmentLink(cat.get(2))); // mount asset.png

        // 12 Tasks:
        // To do (5 total, 2 are overdue):
        p1.getTasks().add(new Task("Build header layout", Task.PRIORITY_HIGH, Task.STATUS_TODO, todayMillis));
        p1.getTasks().add(new Task("Add form validation", Task.PRIORITY_HIGH, Task.STATUS_TODO, todayMillis));
        p1.getTasks().add(new Task("Create low fidelity service page website", Task.PRIORITY_NORMAL, Task.STATUS_TODO, fiveDaysAgoMillis)); // Overdue 1
        p1.getTasks().add(new Task("Audit external dependencies", Task.PRIORITY_HIGH, Task.STATUS_TODO, fiveDaysAgoMillis));              // Overdue 2
        p1.getTasks().add(new Task("Optimize card vector illustrations", Task.PRIORITY_LOW, Task.STATUS_TODO, 0L));                         // No due date

        // In progress (3 total):
        p1.getTasks().add(new Task("Configure pricing table responsive grid", Task.PRIORITY_NORMAL, Task.STATUS_IN_PROGRESS, fiveDaysLaterMillis));
        p1.getTasks().add(new Task("Review typography and contrast ratios", Task.PRIORITY_NORMAL, Task.STATUS_IN_PROGRESS, fiveDaysLaterMillis));
        p1.getTasks().add(new Task("Setup production bundling script", Task.PRIORITY_NORMAL, Task.STATUS_IN_PROGRESS, fiveDaysLaterMillis));

        // Done (4 total):
        p1.getTasks().add(new Task("Create brand guideline for website design", Task.PRIORITY_NORMAL, Task.STATUS_DONE, fiveDaysAgoMillis));
        p1.getTasks().add(new Task("Draft hero copy and CTA text", Task.PRIORITY_NORMAL, Task.STATUS_DONE, fiveDaysAgoMillis));
        p1.getTasks().add(new Task("Setup routing for contact details", Task.PRIORITY_LOW, Task.STATUS_DONE, fiveDaysAgoMillis));
        p1.getTasks().add(new Task("Integrate analytics trigger", Task.PRIORITY_NORMAL, Task.STATUS_DONE, fiveDaysAgoMillis));

        list.add(p1);

        // =========================================================================
        // 2. Login API (Backend)
        // 3/10 tasks (30%), 2 Attachments, 2 Comments
        // =========================================================================
        Project p2 = new Project(
                "Login API",
                "Backend",
                "Build authentication endpoints and JWT token refresh handlers.",
                2
        );
        p2.getAttachments().add(new AttachmentLink(cat.get(1)));
        p2.getAttachments().add(new AttachmentLink(cat.get(3)));
        for (int i = 1; i <= 3; i++) {
            p2.getTasks().add(new Task("Completed security routine " + i, Task.PRIORITY_NORMAL, Task.STATUS_DONE, todayMillis));
        }
        for (int i = 1; i <= 7; i++) {
            p2.getTasks().add(new Task("Pending auth endpoint " + i, Task.PRIORITY_NORMAL, Task.STATUS_TODO, 0L));
        }
        list.add(p2);

        // =========================================================================
        // 3. Mobile Dashboard (Frontend)
        // 2/8 tasks (25%), 1 Attachment, 1 Comment
        // =========================================================================
        Project p3 = new Project(
                "Mobile Dashboard",
                "Frontend",
                "Responsive overview screens and telemetry summary cards.",
                1
        );
        p3.getAttachments().add(new AttachmentLink(cat.get(0)));
        for (int i = 1; i <= 2; i++) {
            p3.getTasks().add(new Task("Dashboard widget layout " + i, Task.PRIORITY_NORMAL, Task.STATUS_DONE, todayMillis));
        }
        for (int i = 1; i <= 6; i++) {
            p3.getTasks().add(new Task("Telemetry component " + i, Task.PRIORITY_NORMAL, Task.STATUS_TODO, 0L));
        }
        list.add(p3);

        // =========================================================================
        // 4. Release Checklist (QA)
        // 4/4 tasks (100%), 1 Attachment, 2 Comments
        // =========================================================================
        Project p4 = new Project(
                "Release Checklist",
                "QA",
                "Pre-production and smoke testing checklist for staging build.",
                2
        );
        p4.getAttachments().add(new AttachmentLink(cat.get(1)));
        for (int i = 1; i <= 4; i++) {
            p4.getTasks().add(new Task("Regression check item " + i, Task.PRIORITY_HIGH, Task.STATUS_DONE, todayMillis));
        }
        list.add(p4);

        // =========================================================================
        // 5. Design Assets (Design)
        // 0/0 tasks (0%), 0 Attachments, 0 Comments
        // =========================================================================
        Project p5 = new Project(
                "Design Assets",
                "Design",
                "Vector icons and typography for launching marketing pages.",
                0
        );
        list.add(p5);

        return list;
    }
}