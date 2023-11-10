package org.testshift.testcube.branches.preview.image.links;

import org.testshift.testcube.branches.CFGPanel;
import org.testshift.testcube.branches.rendering.ImageItem;
import org.testshift.testcube.branches.rendering.RenderRequest;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

public class MyMouseAdapter extends MouseAdapter {
//    private static final Logger LOG = Logger.getInstance(MyMouseAdapter.class);

//    private final LinkNavigator navigator;
    private final ImageItem.LinkData linkData;
    private final RenderRequest renderRequest;

    public MyMouseAdapter(ImageItem.LinkData linkData, RenderRequest renderRequest) {
//        this.navigator = navigator;
        this.linkData = linkData;
        this.renderRequest = renderRequest;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        long start = System.currentTimeMillis();
        String text = linkData.getText();
        Component button = e.getComponent();
        Component label = button.getParent();
        Component panel = label.getParent();
        Component cfgPanel = panel.getParent();
//        try {
//            if (linkData.isLink()) {
//                if (isWebReferenceUrl(text)) {
//                    BrowserUtil.browse(text);
//                } else {
//                    if (navigator.openFile(new File(renderRequest.getBaseDir(), text))) return;
//                    navigator.findNextSourceAndNavigate(text);
//                }
//            } else {
                if(panel instanceof JPanel) {
                    new Highlighter().highlightImages((JPanel) panel, text);
                }
//            }
//        } catch (
//                Exception ex) {
//            LOG.warn(ex);
//        }
//        LOG.debug("mousePressed ", (System.currentTimeMillis() - start), "ms");
    }
}
