package com.example.taskflow;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
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

    private ProjectHost host;

    private EditText etSearch;
    private TextView chipAll, chipImages, chipText;
    private TextView tvShowingCount;
    private RecyclerView rvFiles;
    private View layoutEmpty;
    private TextView tvEmptyMsg;
    private Button btnEmptyAction, btnAdd;

    private AttachmentAdapter adapter;
    private int selectedFilter = 0;

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
        setupList();
        setupSearchAndChips();
        setupAddButton();
        refreshUI();
        return view;
    }

    private void initViews(View v) {
        etSearch = v.findViewById(R.id.etSearchFiles);
        chipAll = v.findViewById(R.id.chipFileAll);
        chipImages = v.findViewById(R.id.chipFileImages);
        chipText = v.findViewById(R.id.chipFileText);
        tvShowingCount = v.findViewById(R.id.tvFilesShowingCount);
        rvFiles = v.findViewById(R.id.rvFiles);
        layoutEmpty = v.findViewById(R.id.layoutFilesEmpty);
        tvEmptyMsg = v.findViewById(R.id.tvFilesEmptyMessage);
        btnEmptyAction = v.findViewById(R.id.btnFilesEmptyAction);
        btnAdd = v.findViewById(R.id.btnFilesAdd);
    }

    private void setupList() {
        adapter = new AttachmentAdapter(new AttachmentAdapter.OnAttachmentInteractListener() {
            @Override
            public void onPreview(AttachmentLink link) {
                showPreviewDialog(link.getCatalogItem());
            }

            @Override
            public void onDelete(AttachmentLink link, int position) {
                confirmTypedDeletion(link, position);
            }
        });
        rvFiles.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvFiles.setAdapter(adapter);

        ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int pos = viewHolder.getAdapterPosition();
                AttachmentLink link = adapter.getAttachments().get(pos);
                confirmTypedDeletion(link, pos);
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                View itemView = viewHolder.itemView;
                ColorDrawable background = new ColorDrawable();

                if (dX > 0) {
                    background.setColor(Color.parseColor("#E74C3C"));
                    background.setBounds(itemView.getLeft(), itemView.getTop(), itemView.getLeft() + (int) dX, itemView.getBottom());
                } else {
                    background.setBounds(0, 0, 0, 0);
                }

                background.draw(c);
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };
        new ItemTouchHelper(callback).attachToRecyclerView(rvFiles);
    }

    private void setupSearchAndChips() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { refreshUI(); }
            @Override public void afterTextChanged(Editable s) {}
        });

        View.OnClickListener chipListener = v -> {
            selectedFilter = (v.getId() == R.id.chipFileAll) ? 0 : (v.getId() == R.id.chipFileImages) ? 1 : 2;
            refreshUI();
        };

        chipAll.setOnClickListener(chipListener);
        chipImages.setOnClickListener(chipListener);
        chipText.setOnClickListener(chipListener);
    }

    private void setupAddButton() {
        View.OnClickListener addListener = v -> showAddAttachmentDialog();
        btnAdd.setOnClickListener(addListener);
        btnEmptyAction.setOnClickListener(addListener);
    }

    public void refreshUI() {
        if (host == null || host.getProject() == null) return;
        List<AttachmentLink> allLinks = host.getProject().getAttachments();

        int countAll = allLinks.size();
        int countImages = 0;
        int countText = 0;
        for (AttachmentLink a : allLinks) {
            if (a.getCatalogItem().getType() == CatalogItem.Type.IMAGE) countImages++;
            else countText++;
        }

        chipAll.setText("All (" + countAll + ")");
        chipImages.setText("Images (" + countImages + ")");
        chipText.setText("Text (" + countText + ")");

        String query = etSearch.getText().toString().trim().toLowerCase(Locale.ROOT);
        List<AttachmentLink> filtered = new ArrayList<>();

        for (AttachmentLink a : allLinks) {
            if (selectedFilter == 1 && a.getCatalogItem().getType() != CatalogItem.Type.IMAGE) continue;
            if (selectedFilter == 2 && a.getCatalogItem().getType() != CatalogItem.Type.TEXT) continue;
            if (!query.isEmpty() && !a.getCatalogItem().getDisplayName().toLowerCase(Locale.ROOT).contains(query)) continue;
            filtered.add(a);
        }

        adapter.setAttachments(filtered);
        tvShowingCount.setText("Showing " + filtered.size() + " of " + countAll + " attachments");

        if (filtered.isEmpty()) {
            rvFiles.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
            if (allLinks.isEmpty()) {
                tvEmptyMsg.setText("No attachments yet");
                btnEmptyAction.setText("+ Add Attachment");
            } else {
                tvEmptyMsg.setText("No attachments match search");
                btnEmptyAction.setText("Reset Filters");
                btnEmptyAction.setOnClickListener(v -> {
                    etSearch.setText("");
                    selectedFilter = 0;
                    refreshUI();
                });
            }
        } else {
            rvFiles.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);
            btnEmptyAction.setOnClickListener(v -> showAddAttachmentDialog());
        }
    }

    private void confirmTypedDeletion(AttachmentLink link, int adapterPos) {
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_delete_attachment, null);
        EditText etConfirm = view.findViewById(R.id.etDeleteConfirm);
        TextView tvPrompt = view.findViewById(R.id.tvDeletePrompt);

        String targetName = link.getCatalogItem().getDisplayName();
        tvPrompt.setText(getString(R.string.prompt_delete_attachment) + " '" + targetName + "':");

        new AlertDialog.Builder(requireContext())
                .setTitle("Remove Attachment")
                .setView(view)
                .setPositiveButton("Remove", (dialog, which) -> {
                    String typed = etConfirm.getText().toString().trim();
                    if (typed.equals(targetName)) {
                        host.getProject().getAttachments().remove(link);
                        if (host != null) host.notifyProjectChanged();
                        refreshUI();
                    } else {
                        Toast.makeText(requireContext(), "Name did not match. Attachment kept.", Toast.LENGTH_SHORT).show();
                        adapter.notifyItemChanged(adapterPos);
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> adapter.notifyItemChanged(adapterPos))
                .setOnCancelListener(dialog -> adapter.notifyItemChanged(adapterPos))
                .show();
    }

    private void showPreviewDialog(CatalogItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(item.getDisplayName());

        if (item.getType() == CatalogItem.Type.IMAGE) {
            ImageView iv = new ImageView(requireContext());
            iv.setImageResource(android.R.drawable.ic_menu_gallery);
            iv.setPadding(32, 32, 32, 32);
            builder.setView(iv);
        } else {
            TextView tv = new TextView(requireContext());
            tv.setText(item.getContent());
            tv.setPadding(48, 48, 48, 48);
            tv.setTextSize(16f);
            tv.setTextColor(getResources().getColor(android.R.color.black));
            builder.setView(tv);
        }

        builder.setPositiveButton("Close", null).show();
    }

    private void showAddAttachmentDialog() {
        List<CatalogItem> catalog = SampleDataRepository.getLocalCatalog();
        List<CatalogItem> eligible = new ArrayList<>();

        for (CatalogItem c : catalog) {
            boolean alreadyLinked = false;
            for (AttachmentLink a : host.getProject().getAttachments()) {
                if (a.getCatalogItem().getId().equals(c.getId())) {
                    alreadyLinked = true;
                    break;
                }
            }
            if (!alreadyLinked) eligible.add(c);
        }

        if (eligible.isEmpty()) {
            Toast.makeText(requireContext(), "All catalog files are already attached", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] names = new String[eligible.size()];
        for(int i=0; i<eligible.size(); i++) names[i] = eligible.get(i).getDisplayName();

        final CatalogItem[] selected = {null};

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Select Attachment")
                .setSingleChoiceItems(names, -1, (d, which) -> {
                    selected[0] = eligible.get(which);
                    ((AlertDialog)d).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                })
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Add", (d, which) -> {
                    if (selected[0] != null) {
                        host.getProject().getAttachments().add(new AttachmentLink(selected[0]));
                        if (host != null) host.notifyProjectChanged();
                        refreshUI();
                    }
                })
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false));
        dialog.show();
    }
}