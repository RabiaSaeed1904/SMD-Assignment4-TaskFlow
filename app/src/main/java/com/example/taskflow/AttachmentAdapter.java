package com.example.taskflow;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class AttachmentAdapter extends RecyclerView.Adapter<AttachmentAdapter.ViewHolder> {

    private final List<AttachmentLink> links = new ArrayList<>();

    public interface OnAttachmentInteractListener {
        void onPreview(AttachmentLink link);
        void onDelete(AttachmentLink link, int position);
    }

    private final OnAttachmentInteractListener listener;

    public AttachmentAdapter(OnAttachmentInteractListener listener) {
        this.listener = listener;
    }

    public void setAttachments(List<AttachmentLink> newLinks) {
        this.links.clear();
        this.links.addAll(newLinks);
        notifyDataSetChanged();
    }

    public List<AttachmentLink> getAttachments() {
        return links;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_attachment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(links.get(position));
    }

    @Override
    public int getItemCount() {
        return links.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvName, tvDetails, btnPreview;
        View btnRemove;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivAttachmentIcon);
            tvName = itemView.findViewById(R.id.tvAttachmentName);
            tvDetails = itemView.findViewById(R.id.tvAttachmentDetails);
            btnPreview = itemView.findViewById(R.id.btnAttachmentPreview);
            btnRemove = itemView.findViewById(R.id.ivAttachmentRemove);

            btnPreview.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onPreview(links.get(pos));
                }
            });

            btnRemove.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onDelete(links.get(pos), pos);
                }
            });
        }

        void bind(AttachmentLink link) {
            CatalogItem item = link.getCatalogItem();
            tvName.setText(item.getDisplayName());

            if (item.getType() == CatalogItem.Type.IMAGE) {
                ivIcon.setImageResource(android.R.drawable.ic_menu_gallery);
                tvDetails.setText("Image • 2.4 MB • Linked");
            } else {
                ivIcon.setImageResource(android.R.drawable.ic_menu_agenda);
                tvDetails.setText("Document • 450 KB • Linked");
            }
        }
    }
}