package org.testshift.testcube.branches.preview.image.links;

import com.intellij.util.Alarm;
import org.testshift.testcube.branches.CFGPanel;
import org.testshift.testcube.branches.preview.image.ImageContainerPng;
//import org.testshift.testcube.branches.preview.image.ImageContainerSvg;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.List;

public class Highlighter {
    private Alarm myAlarm;

    public Highlighter() {
        myAlarm = new Alarm(Alarm.ThreadToUse.SWING_THREAD);
//        plantUmlSettings = PlantUmlSettings.getInstance();
    }

    public void highlightImages(JPanel panel, String text) {
////        if (editor == null || editor.getProject() == null || !previewPanel.isPreviewVisible()) {
////            return;
////        }
//        myAlarm.cancelAllRequests();
//        myAlarm.addRequest(() -> {
//            if (editor.isDisposed()) {
//                return;
//            }
            highlight(panel, text);
//        }, 10);
    }

    private void highlight(JPanel panel, String text) {
//        long start = System.currentTimeMillis();
//        JPanel imagesPanel = panel.getImagesPanel();
        Component[] components = panel.getComponents();
//
        if (components.length > 0) {
//            List<String> list;
////            if (plantUmlSettings.isHighlightInImages()) {
//                list = getListForHighlighting(editor);
////            } else {
//                list = Collections.emptyList();
////            }
//
            for (Component component : components) {
                if (component instanceof ImageContainerPng) {
                    ImageContainerPng imageContainer = (ImageContainerPng) component;
                    imageContainer.highlight(text);
                }
            }
        }
////        LOG.debug("highlightImages done in ", System.currentTimeMillis() - start, "ms");
    }
}
