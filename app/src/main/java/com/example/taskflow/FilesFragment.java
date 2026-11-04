package com.example.taskflow;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FilesFragment extends Fragment {

    private ProjectHost host;

    private EditText etSearch;
    private TextView chipAll, chipImages, chipText;
    private TextView tvFilesCount;
    private ListView lvAttachments;
    private View layoutEmpty;
    private TextView tvEmptyMsg;
    private Button btnEmptyAction;

    private final List<AttachmentLink> filteredList = new ArrayList<>();
    private AttachmentAdapter adapter;
    private String selectedTypeFilter = "All"; // "All", "Images", "Text"

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof ProjectHost) host = (ProjectHost) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_files, container, false);
        initViews(view);
        setupChips();
        setupList();
        setupSearch();
        setupAddButton(view);
        applyFilters();
        return view;
    }

    private void initViews(View v) {
        etSearch = v.findViewById(R.id.etSearchFiles);
        chipAll = v.findViewById(R.id.chipAll);
        chipImages = v.findViewById(R.id.chipImages);
        chipText = v.findViewById(R.id.chipText);
        tvFilesCount = v.findViewById(R.id.tvFilesCount);
        lvAttachments = v.findViewById(R.id.lvAttachments);
        layoutEmpty = v.findViewById(R.id.layoutFilesEmpty);
        tvEmptyMsg = v.findViewById(R.id.tvFilesEmptyMessage);
        btnEmptyAction = v.findViewById(R.id.btnFilesEmptyAction);
    }

    private void setupChips() {
        chipAll.setOnClickListener(v -> selectChip("All"));
        chipImages.setOnClickListener(v -> selectChip("Images"));
        chipText.setOnClickListener(v -> selectChip("Text"));
    }

    private void selectChip(String filter) {
        selectedTypeFilter = filter;
        chipAll.setTextColor("All".equals(filter) ? 0xFF3099F3 : 0xFF58677D);
        chipImages.setTextColor("Images".equals(filter) ? 0xFF3099F3 : 0xFF58677D);
        chipText.setTextColor("Text".equals(filter) ? 0xFF3099F3 : 0xFF58677D);
        applyFilters();
    }

    private void setupList() {
        adapter = new AttachmentAdapter(requireContext(), filteredList, new AttachmentAdapter.OnAttachmentActionListener() {
            @Override
            public void onPreview(AttachmentLink link) {
                showPreviewDialog(link);
            }

            @Override
            public void onRemove(AttachmentLink link) {
                showDeleteConfirmationDialog(link);
            }
        });
        lvAttachments.setAdapter(adapter);
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { applyFilters(); }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void setupAddButton(View v) {
        v.findViewById(R.id.btnFilesAdd).setOnClickListener(view -> openAddAttachmentDialog());
    }

    public void refreshList() {
        applyFilters();
    }

    private void applyFilters() {
        if (host == null || host.getProject() == null) return;
        List<AttachmentLink> all = host.getProject().getAttachments();
        filteredList.clear();

        String query = etSearch.getText().toString().trim().toLowerCase(Locale.ROOT);

        int imageCount = 0;
        int textCount = 0;
        for (AttachmentLink link : all) {
            if (link.getType() == CatalogItem.Type.IMAGE) imageCount++;
            else textCount++;
        }

        chipAll.setText("All (" + all.size() + ")");
        chipImages.setText("Images (" + imageCount + ")");
        chipText.setText("Text (" + textCount + ")");

        for (AttachmentLink link : all) {
            if (!query.isEmpty() && !link.getDisplayName().toLowerCase(Locale.ROOT).contains(query)) {
                continue;
            }
            if ("Images".equals(selectedTypeFilter) && link.getType() != CatalogItem.Type.IMAGE) continue;
            if ("Text".equals(selectedTypeFilter) && link.getType() != CatalogItem.Type.TEXT) continue;

            filteredList.add(link);
        }

        adapter.notifyDataSetChanged();
        tvFilesCount.setText("Showing " + filteredList.size() + " of " + all.size() + " attachments");

        if (filteredList.isEmpty()) {
            lvAttachments.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
            if (all.isEmpty()) {
                tvEmptyMsg.setText("No attachments yet");
                btnEmptyAction.setText("+ Add Attachment");
                btnEmptyAction.setOnClickListener(v -> openAddAttachmentDialog());
            } else {
                tvEmptyMsg.setText("No attachments match your search or type");
                btnEmptyAction.setText("Reset");
                btnEmptyAction.setOnClickListener(v -> {
                    etSearch.setText("");
                    selectChip("All");
                });
            }
        } else {
            lvAttachments.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);
        }
    }

    private void showPreviewDialog(AttachmentLink link) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(link.getDisplayName());

        if (link.getType() == CatalogItem.Type.IMAGE) {
            ImageView iv = new ImageView(requireContext());
            iv.setAdjustViewBounds(true);
            iv.setPadding(20, 20, 20, 20);
            iv.setImageResource(link.getImageDrawableRes() != 0 ? link.getImageDrawableRes() : android.R.drawable.ic_menu_gallery);
            builder.setView(iv);
        } else {
            TextView tv = new TextView(requireContext());
            tv.setPadding(30, 30, 30, 30);
            tv.setText(link.getTextContent() != null ? link.getTextContent() : "No document content preview available.");
            tv.setTextColor(0xFF202C40);
            tv.setTextSize(14f);
            builder.setView(tv);
        }

        builder.setPositiveButton("Close", null).show();
    }

    private void showDeleteConfirmationDialog(AttachmentLink link) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_delete_attachment, null);
        TextView tvPrompt = dialogView.findViewById(R.id.tvDeletePrompt);
        EditText etNameInput = dialogView.findViewById(R.id.etConfirmAttachmentName);

        tvPrompt.setText("Type \"" + link.getDisplayName() + "\" to confirm deletion:");

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Confirm Deletion")
                .setView(dialogView)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", null)
                .create();

        dialog.setOnShowListener(d -> {
            Button delBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            delBtn.setOnClickListener(v -> {
                String typed = etNameInput.getText().toString().trim();
                if (!typed.equals(link.getDisplayName())) {
                    etNameInput.setError("Name does not match");
                    etNameInput.requestFocus();
                    return;
                }

                host.getProject().getAttachments().remove(link);
                host.notifyProjectChanged();
                dialog.dismiss();
                Toast.makeText(requireContext(), "Attachment removed", Toast.LENGTH_SHORT).show();
            });
        });

        dialog.show();
    }

    private void openAddAttachmentDialog() {
        List<CatalogItem> allCatalog = SampleDataRepository.getLocalCatalog();
        List<CatalogItem> unlinked = new ArrayList<>();

        for (CatalogItem cat : allCatalog) {
            boolean linked = false;
            for (AttachmentLink existing : host.getProject().getAttachments()) {
                if (existing.getCatalogId().equals(cat.getId())) {
                    linked = true;
                    break;
                }
            }
            if (!linked) unlinked.add(cat);
        }

        if (unlinked.isEmpty()) {
            Toast.makeText(requireContext(), "All catalog items already linked", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] displayNames = new String[unlinked.size()];
        for (int i = 0; i < unlinked.size(); i++) {
            displayNames[i] = unlinked.get(i).getDisplayName() + " (" + unlinked.get(i).getType() + ")";
        }

        final int[] selectedIndex = {-1};

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Add Attachment from Catalog")
                .setSingleChoiceItems(displayNames, -1, (d, which) -> {
                    selectedIndex[0] = which;
                    Button btn = ((AlertDialog) d).getButton(AlertDialog.BUTTON_POSITIVE);
                    if (btn != null) btn.setEnabled(true);
                })
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Add", (d, which) -> {
                    if (selectedIndex[0] >= 0) {
                        CatalogItem chosen = unlinked.get(selectedIndex[0]);
                        host.getProject().getAttachments().add(new AttachmentLink(chosen));
                        host.notifyProjectChanged();
                    }
                })
                .create();

        dialog.setOnShowListener(d -> {
            Button btn = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            if (btn != null) btn.setEnabled(false);
        });

        dialog.show();
    }
}