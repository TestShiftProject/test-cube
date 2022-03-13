package org.testshift.testcube.branches.rendering;

import net.sourceforge.plantuml.BlockUml;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.Log;
import net.sourceforge.plantuml.SourceStringReader;
import net.sourceforge.plantuml.core.DiagramDescription;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DiagramFactory {
    private final List<MyBlock> myBlocks;
    private final int totalPages;

    public DiagramFactory(List<MyBlock> myBlocks, int totalPages) {
        this.myBlocks = myBlocks;
        this.totalPages = totalPages;

        if (myBlocks.size() > 1) {
//            LOG.debug("more than 1 block ", this);
            //happens when the source is incorrectly extracted and contains multiple diagrams
        }
    }

    public static DiagramFactory create(RenderRequest renderRequest, String documentSource) {
        SourceStringReader reader = PlantUmlRendererUtil.newSourceStringReader(documentSource, renderRequest);
        return create(reader, renderRequest);
    }

    public static DiagramFactory create(SourceStringReader reader, @Nullable RenderRequest renderRequest) {
        long start1 = System.currentTimeMillis();
        int totalPages = 0;
        List<MyBlock> myBlocks = new ArrayList<>();

        for (BlockUml blockUml : reader.getBlocks()) {
            checkCancel();
//            long start = System.currentTimeMillis();

            MyBlock myBlockInfo = new MyBlock(blockUml);
            if (renderRequest != null) {
                myBlockInfo.zoomDiagram(renderRequest);
            }
            myBlocks.add(myBlockInfo);
            totalPages = totalPages + myBlockInfo.getNbImages();
//            LOG.debug("myBlockInfo done in  ", System.currentTimeMillis() - start, " ms");

            break;
        }


        DiagramFactory diagramFactory = new DiagramFactory(myBlocks, totalPages);
//        LOG.debug("diagramFactory done in ", System.currentTimeMillis() - start1, "ms");
        return diagramFactory;
    }

    public static void checkCancel() {
        if (Thread.currentThread().isInterrupted()) {
            throw new RuntimeException();
        }
    }

    public int getTotalPages() {
        return totalPages;
    }

    @NotNull
    public ImageItem generateImageItem(RenderRequest renderRequest, String documentSource,
                                       @Nullable String pageSource, FileFormatOption formatOption, int page,
                                       int logPage, RenderingType renderingType) {
        checkCancel();
//        long start = System.currentTimeMillis();

        ImageFormat format = renderRequest.getFormat();
        ByteArrayOutputStream imageStream = new ByteArrayOutputStream();

        DiagramDescription diagramDescription = outputImage(imageStream, page, formatOption);

        byte[] bytes = imageStream.toByteArray();
        boolean png = isPng(bytes);
        boolean wrongResultFormat = format == ImageFormat.SVG && png;

//        LOG.debug("generated ", formatOption.getFileFormat(), " for page ", logPage, " in ", System.currentTimeMillis() - start, "ms, png=", png,", wrongResultFormat=",wrongResultFormat);

        byte[] svgBytes = new byte[0];
        if (!wrongResultFormat) {
            if (format == ImageFormat.SVG) {
                svgBytes = bytes;
            } else if (format == ImageFormat.PNG && renderRequest.isRenderUrlLinks()) { //todo  do not do that if exporting
                svgBytes = generateSvgLinks(page);
            }
        }
//        debugInfo(documentSource, svgBytes);

        Objects.requireNonNull(diagramDescription);
        String description = diagramDescription.getDescription();
        if (description != null && description.contains("entities")) {
            description = "ok";
        }

        ImageFormat resultFormat = format;
        if (wrongResultFormat) {
            resultFormat = ImageFormat.PNG;
        }
        return new ImageItem(/*renderRequest.getBaseDir(),*/ resultFormat, documentSource, pageSource, page, description
                , bytes, svgBytes, renderingType, getTitle(page), getFilename(page), null);
    }

    public DiagramDescription outputImage(OutputStream imageStream, int numImage, FileFormatOption formatOption) {
        try {
            for (MyBlock myBlock : myBlocks) {
                final int nbInSystem = myBlock.getNbImages();
                if (numImage < nbInSystem) {
                    myBlock.getDiagram().exportDiagram(imageStream, numImage, formatOption);
                    return myBlock.getDiagram().getDescription();
                }
                numImage = numImage - nbInSystem;
            }
        } catch (UnsupportedOperationException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Log.error("numImage is too big = " + numImage);
        return null;
    }

    public static boolean isPng(byte[] bytes) {
        boolean isPng = false;
        if (bytes.length > 4) {
            isPng = "‰PNG".equals(new String(bytes, 0, 4));
        }
        return isPng;
    }

    protected byte[] generateSvgLinks(int i) {
        long start = System.currentTimeMillis();
        ByteArrayOutputStream svgStream = new ByteArrayOutputStream();
        outputImage(svgStream, i, PlantUmlNormalRenderer.SVG);
        byte[] svgBytes = svgStream.toByteArray();
        boolean png = isPng(svgBytes);
//        LOG.debug("generated ", PlantUmlNormalRenderer.SVG.getFileFormat(), " for page ", i, " in ", System.currentTimeMillis() - start, "ms, png=", png);
        if (png) {
//            LOG.debug("generated png instead of svg, no links possible");
            return new byte[0];
        }
        return svgBytes;
    }

    public String getTitle(int numImage) {
        for (MyBlock myBlock : myBlocks) {
            final int nbInSystem = myBlock.getNbImages();
            if (numImage < nbInSystem) {
                return myBlock.getTitles().getTitle(numImage);
            }
            numImage = numImage - nbInSystem;
        }

        Log.error("numImage is too big = " + numImage);
        return null;
    }

    public String getFilename(int numImage) {
        for (MyBlock myBlock : myBlocks) {
            final int nbInSystem = myBlock.getNbImages();
            if (numImage < nbInSystem) {
                return myBlock.getFilename();
            }
            numImage = numImage - nbInSystem;
        }

        Log.error("numImage is too big = " + numImage);
        return null;
    }
}
