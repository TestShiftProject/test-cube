package org.testshift.testcube.branches.rendering;

import com.intellij.ui.scale.ScaleContext;
import com.intellij.ui.scale.ScaleType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class Zoom {
    private final int unscaledZoom;
    private final int scaledZoom;
    private boolean displaySvg;
    private double systemScale;

    public Zoom(@Nullable JComponent context, int unscaledZoom, boolean displaySvg) {
        this.unscaledZoom = unscaledZoom;
        systemScale = getSystemScale(context);
        scaledZoom = (int) (unscaledZoom * systemScale);
        displaySvg = displaySvg;
    }


    public Zoom(int unscaledZoom, boolean displaySvg) {
        this.unscaledZoom = unscaledZoom;
        systemScale = 1;
        scaledZoom = unscaledZoom;
        displaySvg = displaySvg;
    }
    public int getScaledZoom() {
        return scaledZoom;
    }

    public int getUnscaledZoom() {
        return unscaledZoom;
    }

    public double getSystemScale() {
        return systemScale;
    }

    private double getSystemScale(JComponent context) {
        try {
            return ScaleContext.create(context).getScale(ScaleType.SYS_SCALE);
        } catch (Throwable e) {
//            LOG.debug(e);
            return 1;
        }
    }
    public Double getDoubleScaledZoom() {
        return (double) scaledZoom / 100;
    }

    @NotNull
    public Zoom refresh(JComponent parent, boolean displaySvg) {
        return new Zoom(parent, unscaledZoom, displaySvg);
    }
}
