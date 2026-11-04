package com.example.taskflow;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

public class AttachmentAdapter extends BaseAdapter {

    public interface OnAttachmentActionListener {
        void onPreview(AttachmentLink link);
        void onRemove(AttachmentLink link);
    }

    private final Context context;
    private final List<AttachmentLink> items;
    private final LayoutInflater inflater;
    private final OnAttachmentActionListener listener;

    public AttachmentAdapter(Context context, List<AttachmentLink> items, OnAttachmentActionListener listener) {
        this.context = context;
        this.items = items;
        this.listener = listener;
        this.inflater = LayoutInflater.from(context);
    }

    @Override public int getCount() { return items.size(); }
    @Override public AttachmentLink getItem(int position) { return items.get(position); }
    @Override public long getItemId(int position) { return position; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_attachment, parent, false);
        }

        TextView tvIcon = convertView.findViewById(R.id.tvAttachmentIcon);
        TextView tvName = convertView.findViewById(R.id.tvAttachmentName);
        TextView tvMeta = convertView.findViewById(R.id.tvAttachmentMeta);
        Button btnPreview = convertView.findViewById(R.id.btnAttachmentPreview);
        TextView btnRemove = convertView.findViewById(R.id.btnAttachmentRemove);

        AttachmentLink link = getItem(position);

        tvName.setText(link.getDisplayName());
        tvMeta.setText((link.getType() == CatalogItem.Type.IMAGE ? "Image" : "Text") + " • " + link.getSampleSize());
        tvIcon.setText(link.getType() == CatalogItem.Type.IMAGE ? "🖼️" : "📄");

        btnPreview.setOnClickListener(v -> {
            if (listener != null) listener.onPreview(link);
        });

        btnRemove.setOnClickListener(v -> {
            if (listener != null) listener.onRemove(link);
        });

        return convertView;
    }
}