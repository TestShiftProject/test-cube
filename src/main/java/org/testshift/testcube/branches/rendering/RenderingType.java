package org.testshift.testcube.branches.rendering;

public enum RenderingType {
    PARTIAL,
    NORMAL,
    REMOTE;

    public boolean renderingTypeChanged(RenderCacheItem cachedItem) {
        return cachedItem != null && cachedItem.getRenderResult().getStrategy() != this;
    }
}
