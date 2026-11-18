
package com.example.taskflow;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class AttachmentLink implements Serializable {

    private static final long serialVersionUID = 1L;

    // Unique identity for this attachment link
    private final String id;

    // Reference to the original catalog item
    private final String catalogId;

    // Attachment information
    private final String displayName;
    private final CatalogItem.Type type;
    private final String sampleSize;
    private final int imageDrawableRes;
    private final String textContent;

    // Time when the attachment was linked
    private final long linkedTimestamp;

    // Retained for Assignment 4 adapter compatibility
    private final CatalogItem catalogItem;

    public AttachmentLink(CatalogItem item) {

        Objects.requireNonNull(
                item,
                "Catalog item cannot be null"
        );

        this.id = UUID.randomUUID().toString();

        this.catalogId = item.getId();
        this.displayName = item.getDisplayName();
        this.type = item.getType();
        this.sampleSize = item.getSampleSize();
        this.imageDrawableRes = item.getImageDrawableRes();
        this.textContent = item.getTextContent();

        this.linkedTimestamp = System.currentTimeMillis();

        this.catalogItem = item;
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

    public CatalogItem getCatalogItem() {

        if (catalogItem != null) {
            return catalogItem;
        }

        // Compatibility with an older serialized
        // AttachmentLink containing the individual
        // catalog fields but not catalogItem.
        return new CatalogItem(
                catalogId,
                displayName,
                type,
                sampleSize,
                imageDrawableRes,
                textContent
        );
    }
}
