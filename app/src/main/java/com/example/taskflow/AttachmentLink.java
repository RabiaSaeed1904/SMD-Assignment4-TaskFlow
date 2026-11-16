package com.example.taskflow;

import java.io.Serializable;

public class AttachmentLink implements Serializable {
    private CatalogItem catalogItem;

    public AttachmentLink(CatalogItem catalogItem) {
        this.catalogItem = catalogItem;
    }

    public CatalogItem getCatalogItem() {
        return catalogItem;
    }
}