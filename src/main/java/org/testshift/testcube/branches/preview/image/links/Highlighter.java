package org.testshift.testcube.branches.preview.image.links;

import com.intellij.util.Alarm;
import org.testshift.testcube.branches.CFGPanel;
import org.testshift.testcube.branches.preview.image.ImageContainerPng;
import org.testshift.testcube.misc.Util;
//import org.testshift.testcube.branches.preview.image.ImageContainerSvg;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class Highlighter {
    private Alarm myAlarm;
//    private String highlightText;

    public Highlighter() {
        myAlarm = new Alarm(Alarm.ThreadToUse.SWING_THREAD);
//        String highlightText="";
//        plantUmlSettings = PlantUmlSettings.getInstance();
    }

    public void highlightImages(JPanel panel, String text) {
            highlight(panel, text);
    }

    private void highlight(JPanel panel, String text) {
        Component[] components = panel.getComponents();
//
        if (components.length > 0) {
            for (Component component : components) {
                if (component instanceof ImageContainerPng) {
                    ImageContainerPng imageContainer = (ImageContainerPng) component;
                    imageContainer.highlight(text);
                }
            }
        }
////        LOG.debug("highlightImages done in ", System.currentTimeMillis() - start, "ms");
    }

    public void coverInitialLinesAndBranches(JPanel panel, Set<String> lines, Set<Util.Branch> branches){
        Component[] components = panel.getComponents();

        if (components.length > 0) {
            for (Component component : components) {
                if (component instanceof ImageContainerPng) {
                    ImageContainerPng imageContainer = (ImageContainerPng) component;
                    imageContainer.coverLines(lines);
                    imageContainer.coverBranches(branches);
                }
            }
        }
    }

    public void coverNewLinesAndBranches(JPanel panel, Set<String> newCoveredLines,
                                         Set<Util.Branch> newCoveredBranches) {
        Component[] components = panel.getComponents();
        if (components.length > 0) {
            for (Component component : components) {
                if (component instanceof ImageContainerPng) {
                    ImageContainerPng imageContainer = (ImageContainerPng) component;
                    imageContainer.removeNewCover();
                    imageContainer.coverNewLines(newCoveredLines);
                    imageContainer.coverNewBranches(newCoveredBranches);
                }
            }
        }
    }

//    public String getHighlightText() {
//        return highlightText;
//    }
}
