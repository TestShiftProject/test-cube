package org.testshift.testcube.branches.rendering;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

public class RenderResult {
    public static final String TITLE_ONLY = "TITLE ONLY";

    private RenderingType strategy;
    private final List<ImageItem> imageItems;
    private final int pages;
    private int rendered;
    private int updatedTitles;
    private int cached;
    private LinkedHashMap<File, Long> includedFiles = new LinkedHashMap<>();

    public RenderResult(RenderingType strategy, int totalPages) {
        this.strategy = strategy;
        if (totalPages == 0) {
            this.imageItems = Collections.emptyList();
        } else {
            this.imageItems = new ArrayList<ImageItem>(totalPages);
        }
        this.pages = totalPages;
    }

    @NotNull
    public ImageItem[] getImageItemsAsArray() {
        return getImageItems().toArray(new ImageItem[getImageItems().size()]);
    }
    public List<ImageItem> getImageItems() {
        return imageItems;
    }

    public RenderingType getStrategy() {
        return strategy;
    }

    public void addRenderedImage(ImageItem imageItem) {
        imageItems.add(imageItem);
        rendered++;
    }

    public void addUpdatedTitle(ImageItem imageItem) {
        imageItems.add(imageItem);
        updatedTitles++;
    }

    public boolean hasError() {
        if (strategy == RenderingType.NORMAL) {
            for (ImageItem imageItem : imageItems) {
                if (imageItem.hasError()) return true;
            }
        } else {
            //PartialRenderingException hack
            return imageItems.size() == 1 && imageItems.get(0).hasError();
        }
        return false;
    }

    public int getPages() {
        return pages;
    }
}
