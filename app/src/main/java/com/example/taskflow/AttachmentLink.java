package com.example.taskflow;

import java.io.Serializable;
import java.util.UUID;

public class AttachmentLink implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String id;
    private final String catalogId;
    private final String displayName;
    private final CatalogItem.Type type;
    private final String sampleSize;
    private final int imageDrawableRes;
    private final String textContent;
    private final long linkedTimestamp;

    public AttachmentLink(CatalogItem item) {
        this.id = UUID.randomUUID().toString();
        this.catalogId = item.getId();
        this.displayName = item.getDisplayName();
        this.type = item.getType();
        this.sampleSize = item.getSampleSize();
        this.imageDrawableRes = item.getImageDrawableRes();
        this.textContent = item.getTextContent();
        this.linkedTimestamp = System.currentTimeMillis();
    }

    public String getId() {
        return id;
    }

    public String getCatalogId() {
        return catalogId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public CatalogItem.Type getType() {
        return type;
    }

    public String getSampleSize() {
        return sampleSize;
    }

    public int getImageDrawableRes() {
        return imageDrawableRes;
    }

    public String getTextContent() {
        return textContent;
    }

    public long getLinkedTimestamp() {
        return linkedTimestamp;
    }
}