package org.testshift.testcube.branches.rendering;

import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class PlantUmlNormalRenderer {
    protected static final FileFormatOption SVG = new FileFormatOption(FileFormat.SVG);

    public RenderResult doRender(RenderRequest renderRequest, RenderCacheItem cachedItem, String[] sourceSplit) {
        try {
//            long start = System.currentTimeMillis();
            DiagramFactory diagramFactory = DiagramFactory.create(renderRequest, renderRequest.getSource());

            int totalPages = diagramFactory.getTotalPages();

            if (totalPages == 0) {
                return new RenderResult(RenderingType.NORMAL, 0);
            }

            //image/error is not rendered when page >= totalPages
            int renderRequestPage = renderRequest.getPage();
            if (renderRequestPage >= totalPages) {
                renderRequestPage = -1;
            }

//            FileFormatOption formatOption = new FileFormatOption(Format.from(renderRequest.getFormat()));
            FileFormatOption formatOption = new FileFormatOption(FileFormat.PNG);

            boolean containsIncludedNewPage = sourceSplit.length != totalPages;


            RenderResult renderResult = new RenderResult(RenderingType.NORMAL, totalPages);
            for (int page = 0; page < totalPages; page++) {
                boolean pageRequested = renderRequestPage == -1 || renderRequestPage == page;
                normalRendering(renderRequest, sourceSplit, renderRequest.getSource(), diagramFactory, renderResult, formatOption, containsIncludedNewPage, page, pageRequested);
            }
            return renderResult;
        } catch (RuntimeException e) {
            throw e;
        } catch (Throwable e) {
            return new RenderResult(RenderingType.NORMAL, 0);
        }
    }

    protected boolean cachedPageCountChanged(RenderCacheItem cachedItem, int pagesCount) {
        return cachedItem != null && pagesCount != cachedItem.getImageItems().length;
    }

    private void normalRendering(RenderRequest renderRequest, String[] sourceSplit, String documentSource, DiagramFactory factory, RenderResult renderResult, FileFormatOption formatOption, boolean containsIncludedNewPage, int page, boolean pageRequested) {
        String pageSource = pageSource(sourceSplit, containsIncludedNewPage, page);
        if (pageRequested) {
            ImageItem imageItem = factory.generateImageItem(renderRequest, documentSource, pageSource, formatOption, page, page, RenderingType.NORMAL);
            renderResult.addRenderedImage(imageItem);
        } else {
//            logger.debug("page ", page, "  title only");
            ImageItem imageItem = new ImageItem(/*renderRequest.getBaseDir(),*/ renderRequest.getFormat(),
                    documentSource, pageSource, page, RenderResult.TITLE_ONLY, null, null, RenderingType.NORMAL, factory.getTitle(page), factory.getFilename(page), null);
            renderResult.addUpdatedTitle(imageItem);
        }
    }


    @Nullable
    private String pageSource(String[] sourceSplit, boolean containsIncludedNewPage, int i) {
        String pageSource = null;
        if (!containsIncludedNewPage) {
            pageSource = sourceSplit[i];
        }
        return pageSource;
    }
}
