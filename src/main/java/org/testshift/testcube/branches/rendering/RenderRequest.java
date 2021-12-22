package org.testshift.testcube.branches.rendering;

import org.jetbrains.annotations.NotNull;

import java.io.File;

public class RenderRequest {
    private final String sourceFilePath;
    @NotNull
    private final String source;
    @NotNull
    private final ImageFormat format;
    private final int page;
    @NotNull
    private Zoom zoom;
    private Integer version;
    private boolean renderUrlLinks;
    private RenderCommand.Reason reason;
    protected boolean useSettings = false;
    private boolean disableSvgZoom;

    public RenderRequest(String sourceFilePath,
                         @NotNull String source,
                         @NotNull ImageFormat format,
                         int page,
                         @NotNull
                                 Zoom zoom,
                         Integer version,
                         boolean renderUrlLinks,
                         RenderCommand.Reason reason) {
        this.sourceFilePath = sourceFilePath;
        this.source = source;
        this.format = format;
        this.page = page;
        this.zoom = zoom;
        this.version = version;
        this.renderUrlLinks = renderUrlLinks;
        this.reason = reason;
    }

    public String getSourceFilePath() {
        return sourceFilePath;
    }

    public File getSourceFile() {
        return new File(sourceFilePath);
    }

    @NotNull
    public String getSource() {
        return source;
    }

    @NotNull
    public Zoom getZoom() {
        return zoom;
    }
    @NotNull
    public ImageFormat getFormat() {
        return format;
    }

    public boolean isDisableSvgZoom() {
        return disableSvgZoom;
    }

    public boolean isUseSettings() {
        return useSettings;
    }

    public int getPage() {
        return page;
    }

    public File getBaseDir() {
        return UIUtils.getParent(new File(sourceFilePath));
    }

    public boolean isRenderUrlLinks() {
        return renderUrlLinks;
    }

    public RenderCommand.Reason getReason() {
        return reason;
    }

    public void setZoom(Zoom zoom){
        this.zoom = zoom;
    }

    public Integer getVersion() {
        return version;
    }
}
