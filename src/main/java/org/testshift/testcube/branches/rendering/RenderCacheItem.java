package org.testshift.testcube.branches.rendering;

import org.jetbrains.annotations.NotNull;

public class RenderCacheItem {
    private final RenderRequest renderRequest;
    private final RenderResult renderResult;
    private final String[] titles;

    private ImageItem[] imageItems;
    private Integer version;
    private int requestedPage;

    public RenderCacheItem(@NotNull RenderRequest renderRequest, RenderResult renderResult, int requestedPage, int version) {
        this.renderRequest = renderRequest;
        this.renderResult = renderResult;

        imageItems = renderResult.getImageItemsAsArray();
        this.titles = new String[imageItems.length];
        for (int i = 0; i < imageItems.length; i++) {
            ImageItem imageItem = imageItems[i];
            titles[i] = imageItem != null ? imageItem.getTitle() : null;
        }
        this.requestedPage = requestedPage;
        this.version = version;
    }

    public RenderResult getRenderResult() {
        return renderResult;
    }

    public ImageItem[] getImageItems() {
        return imageItems;
    }

    public int getRequestedPage() {
        return requestedPage;
    }

    public RenderRequest getRenderRequest() {
        return renderRequest;
    }
}
