package com.example.taskflow;

import java.io.Serializable;

public class CatalogItem implements Serializable {
    public enum Type { IMAGE, TEXT }

    private String id;
    private String displayName;
    private Type type;
    private String content;

    // Old Assignment 3 constructor compatibility
    public CatalogItem(String id, String displayName, Type type, String content, int size, String path) {
        this.id = id;
        this.displayName = displayName;
        this.type = type;
        this.content = content;
    }

    // New Assignment 4 constructor compatibility
    public CatalogItem(String id, String displayName, Type type, String content) {
        this.id = id;
        this.displayName = displayName;
        this.type = type;
        this.content = content;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public Type getType() { return type; }
    public String getContent() { return content; }
}