package org.testshift.testcube.branches.preview.image;

import com.intellij.ui.JBColor;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.scale.ScaleContext;
import com.intellij.util.ui.ImageUtil;
import com.intellij.util.ui.JBImageIcon;
import org.jetbrains.annotations.NotNull;
import org.testshift.testcube.branches.CFGPanel;
import org.testshift.testcube.branches.preview.image.links.MyJLabel;
import org.testshift.testcube.branches.preview.image.links.MyMouseAdapter;
import org.testshift.testcube.branches.rendering.ImageItem;
import org.testshift.testcube.branches.rendering.RenderRequest;
import org.testshift.testcube.branches.rendering.RenderResult;
import org.testshift.testcube.branches.rendering.Zoom;
import org.testshift.testcube.misc.Util;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Set;

public class ImageContainerPng extends JLabel{
    private RenderResult renderResult;
    private RenderRequest renderRequest;
    private ImageItem imageWithData;
    private Image originalImage;

    public ImageContainerPng(CFGPanel cfgPanel, JPanel parent, ImageItem imageWithData, int i,
                             RenderRequest renderRequest, RenderResult renderResult) {
        init(cfgPanel, parent, imageWithData, i, renderRequest, renderResult);
    }

    public void init(CFGPanel cfgPanel, JPanel parent, ImageItem imageWithData, int i,
                     RenderRequest renderRequest, RenderResult renderResult) {
        this.imageWithData = imageWithData;
//        this.project = project;
        this.renderResult = renderResult;
        this.renderRequest = renderRequest;
        setOpaque(true);
        setBackground(JBColor.WHITE);
        setup(cfgPanel, parent, renderRequest);
    }

    private void setup(CFGPanel cfgPanel, JPanel parent, RenderRequest renderRequest) {
//        if (project.isDisposed()) {
//            return;
//        }
        originalImage = this.imageWithData.getImage(cfgPanel, renderRequest, this.renderResult);
        if (originalImage != null) {
            setDiagram(parent, this.imageWithData, renderRequest, this);
        } else {
            setText("page not rendered, probably plugin error, please report it and try to hit reload");
        }
    }

    private void setDiagram(JPanel parent, @NotNull final ImageItem imageItem, RenderRequest renderRequest, final JLabel label) {
        long start = System.currentTimeMillis();
        Image scaledImage;
        if (originalImage != null) {
            ScaleContext ctx = ScaleContext.create(parent);
            scaledImage = ImageUtil.ensureHiDPI(originalImage, ctx);
            //        scaledImage = ImageLoader.scaleImage(scaledImage, ctx.getScale(JBUI.ScaleType.SYS_SCALE));

            label.setIcon(new JBImageIcon(scaledImage));


//            label.addMouseListener(new PopupHandler() {
//
//                @Override
//                public void invokePopup(Component comp, int x, int y) {
//                    ACTION_POPUP_MENU.getComponent().show(comp, x, y);
//                }
//            });

            //Removing all children from image label and creating transparent buttons for each item with url
            label.removeAll();
            initLinks( imageItem, renderRequest, renderResult, label);

//            LOG.debug("setDiagram done in ", System.currentTimeMillis() - start, "ms");
        } else {
            setText("page not rendered, probably plugin error, please report it and try to hit reload");
        }

    }

    public static void initLinks( @NotNull ImageItem imageItem, RenderRequest renderRequest, RenderResult renderResult, JComponent image) {
//        if (project.isDisposed()) {
//            return;
//        }
//        long start = System.currentTimeMillis();
//        LinkNavigator navigator = new LinkNavigator(renderRequest, renderResult, project);
//        boolean showUrlLinksBorder = false;
        Zoom zoom = renderRequest.getZoom();

        image.removeAll();

        for (ImageItem.LinkData linkData : imageItem.getLinks()) {

            Rectangle area = getRectangle(zoom, linkData);

            MyJLabel button = new MyJLabel(linkData, area/*, showUrlLinksBorder*/);

            //When user clicks on item, url is opened in default system browser
            if(linkData.getText().contains("True") || linkData.getText().contains("False")) {
                button.setIsbranch(true);
                button.addMouseListener(new MyMouseAdapter(linkData, renderRequest));
            }

            image.add(button);
        }
//        LOG.debug("initLinks done in ", System.currentTimeMillis() - start, "ms");
    }

    @NotNull
    private static Rectangle getRectangle(Zoom zoom, ImageItem.LinkData linkData) {
        Rectangle area = linkData.getClickArea();

        int tolerance = 1;
        double scale = zoom.getSystemScale();
        int x = (int) ((double) area.x / scale) - 2 * tolerance;
        int width = (int) ((area.width) / scale) + 4 * tolerance;

        int y = (int) (area.y / scale);
        int height = (int) ((area.height) / scale) + 5 * tolerance;

        area = new Rectangle(x, y, width, height);
        return area;
    }

    public void highlight(String text) {
        Component[] components = this.getComponents();
        for (Component component : components) {
            MyJLabel jLabel = (MyJLabel) component;
            if(jLabel.getLinkData().getText().equals(text)) {
                jLabel.highlight(text);
            }
            else if(jLabel.isHighlighted()){
                jLabel.highlight(text);
            }
        }
    }

    public void coverLines(Set<String> lines) {
        Component[] components = this.getComponents();
        for (Component component : components) {
            MyJLabel jLabel = (MyJLabel) component;
            if(!jLabel.isBranch()) {
                jLabel.coverLines(lines);
            }
        }
    }

    public void coverBranches(Set<Util.Branch> branches) {
        Component[] components = this.getComponents();
        for (Component component : components) {
            MyJLabel jLabel = (MyJLabel) component;
            if(jLabel.isBranch()) {
                jLabel.coverBranches(branches);
            }
        }
    }

    public void coverNewLines(Set<String> lines) {
        Component[] components = this.getComponents();
        for (Component component : components) {
            MyJLabel jLabel = (MyJLabel) component;
            if(!jLabel.isBranch() && !jLabel.isCovered()) {
                jLabel.coverNewLines(lines);
            }
        }
    }

    public void coverNewBranches(Set<Util.Branch> branches) {
        Component[] components = this.getComponents();
        for (Component component : components) {
            MyJLabel jLabel = (MyJLabel) component;
            if(jLabel.isBranch() && !jLabel.isCovered()) {
                jLabel.coverNewBranches(branches);
            }
        }
    }

    public void removeNewCover() {
        Component[] components = this.getComponents();
        for (Component component : components) {
            MyJLabel jLabel = (MyJLabel) component;
            if(jLabel.isNewCovered()) {
                jLabel.unNewCover();
            }
        }
    }
}
