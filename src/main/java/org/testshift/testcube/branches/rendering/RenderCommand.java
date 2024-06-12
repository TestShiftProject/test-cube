package org.testshift.testcube.branches.rendering;

public class RenderCommand {
    public enum Reason {
        INCLUDES,
        FILE_SWITCHED,
        REFRESH,
        CARET,
        MANUAL_UPDATE, /* no function*/
        SOURCE_PAGE_ZOOM
    }

    protected String sourceFilePath;

}
