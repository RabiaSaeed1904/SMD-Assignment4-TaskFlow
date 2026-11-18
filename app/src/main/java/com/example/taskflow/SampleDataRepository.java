
package com.example.taskflow;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public final class SampleDataRepository {

    private static List<CatalogItem> catalog;

    private SampleDataRepository() {
        // Prevent instantiation.
    }

    /**
     * Local catalog of attachments.
     *
     * Contains actual PNG image resource references
     * and text document content.
     */
    public static synchronized List<CatalogItem> getLocalCatalog() {

        if (catalog == null) {

            List<CatalogItem> items = new ArrayList<>();

            // ------------------------------------
            // Image 1: Sports Dashboard
            // ------------------------------------

            items.add(new CatalogItem(
                    "cat_fig_1",
                    "sports-projectv2.png",
                    CatalogItem.Type.IMAGE,
                    "2.4 MB",
                    R.drawable.sports_dashboard_preview,
                    null
            ));

            // ------------------------------------
            // Text Document 1: Client Briefing
            // ------------------------------------

            items.add(new CatalogItem(
                    "cat_doc_1",
                    "briefing from client.pdf",
                    CatalogItem.Type.TEXT,
                    "450 KB",
                    0,
                    "CLIENT BRIEF & REQUIREMENTS:\n\n"
                            + "1. Project Scope: Sports management "
                            + "and tracking application.\n"
                            + "2. Design Colors: Blue (#3099F3) "
                            + "and Red accents.\n"
                            + "3. Responsive cards showing pricing "
                            + "and features.\n"
                            + "4. Clean typography with distinct "
                            + "category badges."
            ));

            // ------------------------------------
            // Image 2: Mountain Asset
            // ------------------------------------

            items.add(new CatalogItem(
                    "cat_img_1",
                    "mount asset.png",
                    CatalogItem.Type.IMAGE,
                    "1.1 MB",
                    R.drawable.mountain_asset_preview,
                    null
            ));

            // ------------------------------------
            // Text Document 2: API Endpoints
            // ------------------------------------

            items.add(new CatalogItem(
                    "cat_txt_1",
                    "api_endpoints_v1.txt",
                    CatalogItem.Type.TEXT,
                    "14 KB",
                    0,
                    "BACKEND API SPECIFICATIONS:\n\n"
                            + "POST /api/v1/auth/login\n"
                            + "GET  /api/v1/projects\n"
                            + "POST /api/v1/tasks/create\n"
                            + "PUT  /api/v1/tasks/{id}/status"
            ));

            catalog = Collections.unmodifiableList(items);
        }

        return catalog;
    }

    /**
     * Creates the five initial Assignment 3 projects.
     *
     * Project, Task and AttachmentLink constructors
     * assign their own unique record IDs.
     */
    public static ArrayList<Project> getInitialProjects() {

        ArrayList<Project> projects = new ArrayList<>();

        List<CatalogItem> items = getLocalCatalog();

        // ------------------------------------
        // Calculate dates using local time
        // ------------------------------------

        Calendar calendar = Calendar.getInstance();

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        long todayMillis = calendar.getTimeInMillis();

        Calendar past = (Calendar) calendar.clone();
        past.add(Calendar.DAY_OF_MONTH, -5);

        long fiveDaysAgoMillis = past.getTimeInMillis();

        Calendar future = (Calendar) calendar.clone();
        future.add(Calendar.DAY_OF_MONTH, 5);

        long fiveDaysLaterMillis = future.getTimeInMillis();

        // ====================================
        // PROJECT 1: SERVICE PAGE WEBSITE
        //
        // Tasks: 12
        // Completed: 4
        // Progress: 33%
        // Attachments: 3
        // Comments: 3
        // ====================================

        Project p1 = new Project(
                "Service Page Website",
                "User Interface",
                "Make a page display about services for the "
                        + "company website with blue and red "
                        + "colors and provide some price "
                        + "information on each product card.",
                3
        );

        // Three initial attachments

        p1.getAttachments().add(
                new AttachmentLink(items.get(0))
        );

        p1.getAttachments().add(
                new AttachmentLink(items.get(1))
        );

        p1.getAttachments().add(
                new AttachmentLink(items.get(2))
        );

        // ------------------------------------
        // To do: 5 tasks
        // Two tasks are overdue
        // ------------------------------------

        p1.getTasks().add(new Task(
                "Build header layout",
                Task.PRIORITY_HIGH,
                Task.STATUS_TODO,
                todayMillis
        ));

        p1.getTasks().add(new Task(
                "Add form validation",
                Task.PRIORITY_HIGH,
                Task.STATUS_TODO,
                todayMillis
        ));

        p1.getTasks().add(new Task(
                "Create low fidelity service page website",
                Task.PRIORITY_NORMAL,
                Task.STATUS_TODO,
                fiveDaysAgoMillis
        ));

        p1.getTasks().add(new Task(
                "Audit external dependencies",
                Task.PRIORITY_HIGH,
                Task.STATUS_TODO,
                fiveDaysAgoMillis
        ));

        p1.getTasks().add(new Task(
                "Optimize card vector illustrations",
                Task.PRIORITY_LOW,
                Task.STATUS_TODO,
                0L
        ));

        // ------------------------------------
        // In progress: 3 tasks
        // ------------------------------------

        p1.getTasks().add(new Task(
                "Configure pricing table responsive grid",
                Task.PRIORITY_NORMAL,
                Task.STATUS_IN_PROGRESS,
                fiveDaysLaterMillis
        ));

        p1.getTasks().add(new Task(
                "Review typography and contrast ratios",
                Task.PRIORITY_NORMAL,
                Task.STATUS_IN_PROGRESS,
                fiveDaysLaterMillis
        ));

        p1.getTasks().add(new Task(
                "Setup production bundling script",
                Task.PRIORITY_NORMAL,
                Task.STATUS_IN_PROGRESS,
                fiveDaysLaterMillis
        ));

        // ------------------------------------
        // Done: 4 tasks
        // ------------------------------------

        p1.getTasks().add(new Task(
                "Create brand guideline for website design",
                Task.PRIORITY_NORMAL,
                Task.STATUS_DONE,
                fiveDaysAgoMillis
        ));

        p1.getTasks().add(new Task(
                "Draft hero copy and CTA text",
                Task.PRIORITY_NORMAL,
                Task.STATUS_DONE,
                fiveDaysAgoMillis
        ));

        p1.getTasks().add(new Task(
                "Setup routing for contact details",
                Task.PRIORITY_LOW,
                Task.STATUS_DONE,
                fiveDaysAgoMillis
        ));

        p1.getTasks().add(new Task(
                "Integrate analytics trigger",
                Task.PRIORITY_NORMAL,
                Task.STATUS_DONE,
                fiveDaysAgoMillis
        ));

        projects.add(p1);

        // ====================================
        // PROJECT 2: LOGIN API
        //
        // Tasks: 10
        // Completed: 3
        // Progress: 30%
        // Attachments: 2
        // Comments: 2
        // ====================================

        Project p2 = new Project(
                "Login API",
                "Backend",
                "Build authentication endpoints and "
                        + "JWT token refresh handlers.",
                2
        );

        p2.getAttachments().add(
                new AttachmentLink(items.get(1))
        );

        p2.getAttachments().add(
                new AttachmentLink(items.get(3))
        );

        for (int i = 1; i <= 3; i++) {

            p2.getTasks().add(new Task(
                    "Completed security routine " + i,
                    Task.PRIORITY_NORMAL,
                    Task.STATUS_DONE,
                    todayMillis
            ));
        }

        for (int i = 1; i <= 7; i++) {

            p2.getTasks().add(new Task(
                    "Pending auth endpoint " + i,
                    Task.PRIORITY_NORMAL,
                    Task.STATUS_TODO,
                    0L
            ));
        }

        projects.add(p2);

        // ====================================
        // PROJECT 3: MOBILE DASHBOARD
        //
        // Tasks: 8
        // Completed: 2
        // Progress: 25%
        // Attachments: 1
        // Comments: 1
        // ====================================

        Project p3 = new Project(
                "Mobile Dashboard",
                "Frontend",
                "Responsive overview screens and "
                        + "telemetry summary cards.",
                1
        );

        p3.getAttachments().add(
                new AttachmentLink(items.get(0))
        );

        for (int i = 1; i <= 2; i++) {

            p3.getTasks().add(new Task(
                    "Dashboard widget layout " + i,
                    Task.PRIORITY_NORMAL,
                    Task.STATUS_DONE,
                    todayMillis
            ));
        }

        for (int i = 1; i <= 6; i++) {

            p3.getTasks().add(new Task(
                    "Telemetry component " + i,
                    Task.PRIORITY_NORMAL,
                    Task.STATUS_TODO,
                    0L
            ));
        }

        projects.add(p3);

        // ====================================
        // PROJECT 4: RELEASE CHECKLIST
        //
        // Tasks: 4
        // Completed: 4
        // Progress: 100%
        // Attachments: 1
        // Comments: 2
        // ====================================

        Project p4 = new Project(
                "Release Checklist",
                "QA",
                "Pre-production and smoke testing "
                        + "checklist for staging build.",
                2
        );

        p4.getAttachments().add(
                new AttachmentLink(items.get(1))
        );

        for (int i = 1; i <= 4; i++) {

            p4.getTasks().add(new Task(
                    "Regression check item " + i,
                    Task.PRIORITY_HIGH,
                    Task.STATUS_DONE,
                    todayMillis
            ));
        }

        projects.add(p4);

        // ====================================
        // PROJECT 5: DESIGN ASSETS
        //
        // Tasks: 0
        // Completed: 0
        // Progress: 0%
        // Attachments: 0
        // Comments: 0
        // ====================================

        Project p5 = new Project(
                "Design Assets",
                "Design",
                "Vector icons and typography for "
                        + "launching marketing pages.",
                0
        );

        projects.add(p5);

        return projects;
    }
}
