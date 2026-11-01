package com.example.taskflow;

import java.io.Serializable;

public class CatalogItem implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Type {
        IMAGE,
        TEXT
    }

    private final String id;
    private final String displayName;
    private final Type type;
    private final String sampleSize;
    private final int imageDrawableRes;
    private final String textContent;

    public CatalogItem(String id, String displayName, Type type, String sampleSize, int imageDrawableRes, String textContent) {
        this.id = id;
        this.displayName = displayName;
        this.type = type;
        this.sampleSize = sampleSize;
        this.imageDrawableRes = imageDrawableRes;
        this.textContent = textContent;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Type getType() {
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
}