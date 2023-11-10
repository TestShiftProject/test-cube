package org.testshift.testcube.branches.rendering;

public class FacadeImpl {
    public RenderResult render(RenderRequest renderRequest, RenderCacheItem cachedItem) {
            return PlantUmlRendererUtil.render(renderRequest, cachedItem);
    }
}
