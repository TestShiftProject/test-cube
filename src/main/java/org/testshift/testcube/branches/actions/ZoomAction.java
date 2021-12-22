package org.testshift.testcube.branches.actions;

import com.intellij.openapi.project.DumbAwareAction;

public abstract class ZoomAction extends DumbAwareAction {
    protected static int DEFAULT_ZOOM = 100;
    public static int MAX_ZOOM = 500;
    protected static int MIN_ZOOM = 20;
    protected static int ZOOM_STEP = 20;

}
