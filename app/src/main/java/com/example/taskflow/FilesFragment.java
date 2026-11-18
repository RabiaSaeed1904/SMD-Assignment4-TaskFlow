
package com.example.taskflow;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FilesFragment extends Fragment {

    private static final String STATE_QUERY = "files_query";
    private static final String STATE_FILTER = "files_filter";

    private static final int FILTER_ALL = 0;
    private static final int FILTER_IMAGES = 1;
    private static final int FILTER_TEXT = 2;

    private ProjectHost host;

    private EditText etSearch;
    private TextView chipAll;
    private TextView chipImages;
    private TextView chipText;
    private TextView tvShowingCount;
    private TextView tvEmptyMsg;

    private RecyclerView rvFiles;
    private View layoutEmpty;

    private Button btnEmptyAction;
    private Button btnAdd;

    private AttachmentAdapter adapter;

    private int selectedFilter = FILTER_ALL;

    private AlertDialog deletionDialog;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (!(context instanceof ProjectHost)) {
            throw new IllegalStateException(
                    "FilesFragment requires ProjectHost"
            );
        }

        host = (ProjectHost) context;
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(
                R.layout.fragment_files,
                container,
                false
        );

        initializeViews(view);
        setupRecyclerView();
        setupSearchAndFilters();
        setupAddButton();

        if (savedInstanceState != null) {
            selectedFilter = savedInstanceState.getInt(
                    STATE_FILTER,
                    FILTER_ALL
            );

            etSearch.setText(
                    savedInstanceState.getString(
                            STATE_QUERY,
                            ""
                    )
            );
        }

        refreshList();
        return view;
    }

    private void initializeViews(View view) {
        etSearch = view.findViewById(R.id.etSearchFiles);
        chipAll = view.findViewById(R.id.chipFileAll);
        chipImages = view.findViewById(R.id.chipFileImages);
        chipText = view.findViewById(R.id.chipFileText);
        tvShowingCount = view.findViewById(
                R.id.tvFilesShowingCount
        );
        rvFiles = view.findViewById(R.id.rvFiles);
        layoutEmpty = view.findViewById(
                R.id.layoutFilesEmpty
        );
        tvEmptyMsg = view.findViewById(
                R.id.tvFilesEmptyMessage
        );
        btnEmptyAction = view.findViewById(
                R.id.btnFilesEmptyAction
        );
        btnAdd = view.findViewById(R.id.btnFilesAdd);
    }

    private void setupRecyclerView() {
        adapter = new AttachmentAdapter(
                new AttachmentAdapter.OnAttachmentInteractListener() {

                    @Override
                    public void onPreview(AttachmentLink link) {
                        if (link != null
                                && link.getCatalogItem() != null) {
                            showPreviewDialog(
                                    link.getCatalogItem()
                            );
                        }
                    }

                    @Override
                    public void onDelete(
                            AttachmentLink link,
                            int position
                    ) {
                        confirmTypedDeletion(link);
                    }
                }
        );

        rvFiles.setLayoutManager(
                new LinearLayoutManager(requireContext())
        );

        rvFiles.setAdapter(adapter);
        setupSwipeToDelete();
    }

    private void setupSwipeToDelete() {
        ItemTouchHelper.SimpleCallback callback =
                new ItemTouchHelper.SimpleCallback(
                        0,
                        ItemTouchHelper.RIGHT
                ) {

                    @Override
                    public boolean onMove(
                            @NonNull RecyclerView recyclerView,
                            @NonNull RecyclerView.ViewHolder viewHolder,
                            @NonNull RecyclerView.ViewHolder target
                    ) {
                        return false;
                    }

                    @Override
                    public int getSwipeDirs(
                            @NonNull RecyclerView recyclerView,
                            @NonNull RecyclerView.ViewHolder viewHolder
                    ) {
                        if (deletionDialog != null) {
                            return 0;
                        }
                        return ItemTouchHelper.RIGHT;
                    }

                    @Override
                    public void onSwiped(
                            @NonNull RecyclerView.ViewHolder viewHolder,
                            int direction
                    ) {
                        if (adapter == null) {
                            return;
                        }

                        int position =
                                viewHolder.getBindingAdapterPosition();

                        List<AttachmentLink> visible =
                                adapter.getAttachments();

                        if (position == RecyclerView.NO_POSITION
                                || position < 0
                                || position >= visible.size()) {
                            refreshList();
                            return;
                        }

                        confirmTypedDeletion(
                                visible.get(position)
                        );
                    }

                    @Override
                    public void onChildDraw(
                            @NonNull Canvas canvas,
                            @NonNull RecyclerView recyclerView,
                            @NonNull RecyclerView.ViewHolder viewHolder,
                            float dX,
                            float dY,
                            int actionState,
                            boolean isCurrentlyActive
                    ) {
                        if (dX > 0f
                                && actionState ==
                                ItemTouchHelper.ACTION_STATE_SWIPE) {

                            View row = viewHolder.itemView;

                            ColorDrawable background =
                                    new ColorDrawable(
                                            Color.parseColor("#E74C3C")
                                    );

                            background.setBounds(
                                    row.getLeft(),
                                    row.getTop(),
                                    row.getLeft() + Math.round(dX),
                                    row.getBottom()
                            );

                            background.draw(canvas);
                        }

                        super.onChildDraw(
                                canvas,
                                recyclerView,
                                viewHolder,
                                dX,
                                dY,
                                actionState,
                                isCurrentlyActive
                        );
                    }
                };

        new ItemTouchHelper(callback)
                .attachToRecyclerView(rvFiles);
    }

    private void setupSearchAndFilters() {
        etSearch.addTextChangedListener(
                new TextWatcher() {

                    @Override
                    public void beforeTextChanged(
                            CharSequence s,
                            int start,
                            int count,
                            int after
                    ) {
                    }

                    @Override
                    public void onTextChanged(
                            CharSequence s,
                            int start,
                            int before,
                            int count
                    ) {
                        refreshList();
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                }
        );

        chipAll.setOnClickListener(
                v -> selectFilter(FILTER_ALL)
        );

        chipImages.setOnClickListener(
                v -> selectFilter(FILTER_IMAGES)
        );

        chipText.setOnClickListener(
                v -> selectFilter(FILTER_TEXT)
        );
    }

    private void selectFilter(int filter) {
        selectedFilter = filter;
        refreshList();
    }

    private void updateChipStyles() {
        styleChip(
                chipAll,
                selectedFilter == FILTER_ALL
        );

        styleChip(
                chipImages,
                selectedFilter == FILTER_IMAGES
        );

        styleChip(
                chipText,
                selectedFilter == FILTER_TEXT
        );
    }

    private void styleChip(
            TextView chip,
            boolean selected
    ) {
        chip.setBackgroundResource(
                selected
                        ? R.drawable.bg_chip_selected
                        : R.drawable.bg_chip_unselected
        );

        chip.setTextColor(
                selected
                        ? Color.WHITE
                        : Color.parseColor("#91A1BB")
        );

        chip.setTypeface(
                null,
                selected
                        ? Typeface.BOLD
                        : Typeface.NORMAL
        );
    }

    private void setupAddButton() {
        btnAdd.setOnClickListener(
                v -> showAddAttachmentDialog()
        );

        btnEmptyAction.setOnClickListener(
                v -> showAddAttachmentDialog()
        );
    }

    public void refreshList() {
        if (host == null
                || host.getProject() == null
                || adapter == null
                || etSearch == null
                || rvFiles == null) {
            return;
        }

        List<AttachmentLink> allLinks =
                host.getProject().getAttachments();

        List<AttachmentLink> filtered =
                new ArrayList<>();

        int imageCount = 0;
        int textCount = 0;

        String query = etSearch.getText()
                .toString()
                .trim()
                .toLowerCase(Locale.ROOT);

        for (AttachmentLink link : allLinks) {
            if (link == null) {
                continue;
            }

            CatalogItem item = link.getCatalogItem();

            if (item == null) {
                continue;
            }

            if (item.getType() == CatalogItem.Type.IMAGE) {
                imageCount++;
            }

            if (item.getType() == CatalogItem.Type.TEXT) {
                textCount++;
            }

            if (selectedFilter == FILTER_IMAGES
                    && item.getType()
                    != CatalogItem.Type.IMAGE) {
                continue;
            }

            if (selectedFilter == FILTER_TEXT
                    && item.getType()
                    != CatalogItem.Type.TEXT) {
                continue;
            }

            if (!item.getDisplayName()
                    .toLowerCase(Locale.ROOT)
                    .contains(query)) {
                continue;
            }

            filtered.add(link);
        }

        chipAll.setText(
                String.format(
                        Locale.getDefault(),
                        "All (%d)",
                        allLinks.size()
                )
        );

        chipImages.setText(
                String.format(
                        Locale.getDefault(),
                        "Images (%d)",
                        imageCount
                )
        );

        chipText.setText(
                String.format(
                        Locale.getDefault(),
                        "Text (%d)",
                        textCount
                )
        );

        updateChipStyles();

        tvShowingCount.setText(
                String.format(
                        Locale.getDefault(),
                        "Showing %d of %d attachments",
                        filtered.size(),
                        allLinks.size()
                )
        );

        adapter.setAttachments(filtered);

        boolean hasMatches = !filtered.isEmpty();

        rvFiles.setVisibility(
                hasMatches ? View.VISIBLE : View.GONE
        );

        layoutEmpty.setVisibility(
                hasMatches ? View.GONE : View.VISIBLE
        );

        if (!hasMatches) {
            if (allLinks.isEmpty()) {
                tvEmptyMsg.setText(
                        "No attachments yet"
                );

                btnEmptyAction.setText(
                        "+ Add Attachment"
                );

                btnEmptyAction.setOnClickListener(
                        v -> showAddAttachmentDialog()
                );

            } else {
                tvEmptyMsg.setText(
                        "No attachments match your search or type"
                );

                btnEmptyAction.setText(
                        "Reset Filters"
                );

                btnEmptyAction.setOnClickListener(
                        v -> resetFilters()
                );
            }
        }
    }

    private void resetFilters() {
        selectedFilter = FILTER_ALL;
        etSearch.setText("");
        refreshList();
    }

    private boolean sameAttachment(
            AttachmentLink first,
            AttachmentLink second
    ) {
        if (first == second) {
            return true;
        }

        if (first == null || second == null) {
            return false;
        }

        String firstId = first.getId();
        String secondId = second.getId();

        return firstId != null
                && firstId.equals(secondId);
    }

    private void restoreSwipedRow(
            AttachmentLink link
    ) {
        if (adapter == null) {
            return;
        }

        List<AttachmentLink> visible =
                adapter.getAttachments();

        for (int i = 0; i < visible.size(); i++) {
            if (sameAttachment(visible.get(i), link)) {
                adapter.notifyItemChanged(i);
                return;
            }
        }

        refreshList();
    }

    private void confirmTypedDeletion(
            AttachmentLink link
    ) {
        if (link == null
                || link.getCatalogItem() == null
                || !isAdded()) {
            return;
        }

        if (deletionDialog != null) {
            restoreSwipedRow(link);
            return;
        }

        View dialogView =
                LayoutInflater.from(requireContext())
                        .inflate(
                                R.layout.dialog_delete_attachment,
                                null
                        );

        EditText etConfirm =
                dialogView.findViewById(
                        R.id.etDeleteConfirm
                );

        TextView tvPrompt =
                dialogView.findViewById(
                        R.id.tvDeletePrompt
                );

        String targetName =
                link.getCatalogItem().getDisplayName();

        tvPrompt.setText(
                "Type \"" + targetName
                        + "\" to confirm removing this attachment."
        );

        AlertDialog dialog =
                new AlertDialog.Builder(requireContext())
                        .setTitle("Remove Attachment")
                        .setView(dialogView)
                        .setNegativeButton("Cancel", null)
                        .setPositiveButton("Remove", null)
                        .create();

        final boolean[] deleted = {false};

        deletionDialog = dialog;

        dialog.setOnShowListener(
                ignored -> {
                    Button removeButton =
                            dialog.getButton(
                                    AlertDialog.BUTTON_POSITIVE
                            );

                    removeButton.setOnClickListener(v -> {
                        String typedName =
                                etConfirm.getText().toString();

                        if (!targetName.equals(typedName)) {
                            Toast.makeText(
                                    requireContext(),
                                    "Name did not match. Attachment kept.",
                                    Toast.LENGTH_SHORT
                            ).show();

                            dialog.dismiss();
                            return;
                        }

                        Project project =
                                host == null
                                        ? null
                                        : host.getProject();

                        if (project != null) {
                            List<AttachmentLink> links =
                                    project.getAttachments();

                            for (int i = 0; i < links.size(); i++) {
                                if (sameAttachment(
                                        links.get(i),
                                        link
                                )) {
                                    links.remove(i);
                                    deleted[0] = true;
                                    host.notifyProjectChanged();
                                    break;
                                }
                            }
                        }

                        dialog.dismiss();
                    });
                }
        );

        dialog.setOnDismissListener(
                ignored -> {
                    if (deletionDialog == dialog) {
                        deletionDialog = null;
                    }

                    if (!deleted[0]) {
                        restoreSwipedRow(link);
                    }
                }
        );

        dialog.show();
    }

    private void showPreviewDialog(
            CatalogItem item
    ) {
        if (item == null || !isAdded()) {
            return;
        }

        AlertDialog.Builder builder =
                new AlertDialog.Builder(requireContext())
                        .setTitle(item.getDisplayName())
                        .setPositiveButton("Close", null);

        if (item.getType()
                == CatalogItem.Type.IMAGE) {

            int imageResource =
                    item.getImageDrawableRes();

            if (imageResource == 0) {
                builder.setMessage(
                        "Image preview unavailable."
                );
            } else {
                ImageView image =
                        new ImageView(requireContext());

                image.setImageResource(imageResource);

                image.setScaleType(
                        ImageView.ScaleType.FIT_CENTER
                );

                image.setAdjustViewBounds(true);

                int padding = dpToPx(16);

                image.setPadding(
                        padding,
                        padding,
                        padding,
                        padding
                );

                image.setMinimumHeight(dpToPx(220));

                builder.setView(image);
            }

        } else {
            String content = item.getTextContent();

            TextView text =
                    new TextView(requireContext());

            text.setText(
                    content == null || content.isEmpty()
                            ? "No document content available."
                            : content
            );

            text.setTextColor(
                    Color.parseColor("#202C40")
            );

            text.setTextSize(15f);
            text.setTextIsSelectable(true);

            int padding = dpToPx(16);

            text.setPadding(
                    padding,
                    padding,
                    padding,
                    padding
            );

            ScrollView scroll =
                    new ScrollView(requireContext());

            scroll.addView(text);
            builder.setView(scroll);
        }

        builder.show();
    }

    /**
     * Allows adding multiple catalog attachments
     * to the current project in one operation.
     *
     * Existing attachments are excluded.
     * Cancel makes no changes.
     */
    private void showAddAttachmentDialog() {
        if (!isAdded()
                || host == null
                || host.getProject() == null) {
            return;
        }

        List<CatalogItem> available =
                new ArrayList<>();

        List<AttachmentLink> linked =
                host.getProject().getAttachments();

        for (CatalogItem candidate :
                SampleDataRepository.getLocalCatalog()) {

            boolean alreadyLinked = false;

            for (AttachmentLink link : linked) {
                if (link == null) {
                    continue;
                }

                CatalogItem item =
                        link.getCatalogItem();

                if (item != null
                        && candidate.getId().equals(item.getId())) {
                    alreadyLinked = true;
                    break;
                }
            }

            if (!alreadyLinked) {
                available.add(candidate);
            }
        }

        if (available.isEmpty()) {
            Toast.makeText(
                    requireContext(),
                    "All catalog files are already attached",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        String[] names =
                new String[available.size()];

        boolean[] checked =
                new boolean[available.size()];

        for (int i = 0; i < available.size(); i++) {
            names[i] =
                    available.get(i).getDisplayName();
        }

        AlertDialog dialog =
                new AlertDialog.Builder(requireContext())
                        .setTitle("Select Attachments")
                        .setMultiChoiceItems(
                                names,
                                checked,
                                (d, index, isChecked) -> {
                                    checked[index] = isChecked;
                                }
                        )
                        .setNegativeButton(
                                "Cancel",
                                null
                        )
                        .setPositiveButton(
                                "Add Selected",
                                null
                        )
                        .create();

        dialog.setOnShowListener(
                ignored -> {
                    Button addButton =
                            dialog.getButton(
                                    AlertDialog.BUTTON_POSITIVE
                            );

                    addButton.setOnClickListener(v -> {
                        Project project =
                                host == null
                                        ? null
                                        : host.getProject();

                        if (project == null || !isAdded()) {
                            dialog.dismiss();
                            return;
                        }

                        List<AttachmentLink> currentLinks =
                                project.getAttachments();

                        int addedCount = 0;

                        for (int i = 0; i < available.size(); i++) {
                            if (!checked[i]) {
                                continue;
                            }

                            CatalogItem candidate =
                                    available.get(i);

                            boolean duplicate = false;

                            // Recheck against the live project list
                            // before adding each attachment.
                            for (AttachmentLink link : currentLinks) {
                                if (link == null
                                        || link.getCatalogItem() == null) {
                                    continue;
                                }

                                if (candidate.getId().equals(
                                        link.getCatalogItem().getId()
                                )) {
                                    duplicate = true;
                                    break;
                                }
                            }

                            if (!duplicate) {
                                currentLinks.add(
                                        new AttachmentLink(candidate)
                                );
                                addedCount++;
                            }
                        }

                        if (addedCount > 0) {
                            host.notifyProjectChanged();

                            Toast.makeText(
                                    requireContext(),
                                    addedCount
                                            + " attachment(s) added",
                                    Toast.LENGTH_SHORT
                            ).show();

                            dialog.dismiss();
                        } else {
                            Toast.makeText(
                                    requireContext(),
                                    "Select at least one available attachment",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    });
                }
        );

        dialog.show();
    }

    private int dpToPx(int dp) {
        return Math.round(
                dp * getResources()
                        .getDisplayMetrics()
                        .density
        );
    }

    @Override
    public void onSaveInstanceState(
            @NonNull Bundle outState
    ) {
        super.onSaveInstanceState(outState);

        outState.putInt(
                STATE_FILTER,
                selectedFilter
        );

        if (etSearch != null) {
            outState.putString(
                    STATE_QUERY,
                    etSearch.getText().toString()
            );
        }
    }

    @Override
    public void onDestroyView() {
        if (deletionDialog != null) {
            AlertDialog dialog = deletionDialog;
            deletionDialog = null;
            dialog.dismiss();
        }

        if (rvFiles != null) {
            rvFiles.setAdapter(null);
        }

        adapter = null;
        rvFiles = null;
        etSearch = null;

        super.onDestroyView();
    }

    @Override
    public void onDetach() {
        host = null;
        super.onDetach();
    }
}
