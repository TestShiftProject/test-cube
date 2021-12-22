package org.testshift.testcube.branches;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.colorpicker.ButtonPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.Alarm;

import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import org.jetbrains.annotations.NotNull;
import org.testshift.testcube.branches.actions.ZoomAction;
import org.testshift.testcube.branches.preview.image.ImageContainerPng;
//import org.testshift.testcube.branches.preview.image.ImageContainerSvg;
import org.testshift.testcube.branches.preview.image.links.Highlighter;
import org.testshift.testcube.branches.preview.image.links.MyJLabel;
import org.testshift.testcube.branches.rendering.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class CFGPanel extends JPanel implements Disposable{
//    private Project project;
    private JPanel imagesPanel;
    private JScrollPane scrollPane;
    private Zoom zoom;
    private RenderResult renderResult;
    private RenderCacheItem displayedItem;
    private int lastValidVerticalScrollValue;
    private int lastValidHorizontalScrollValue;
    private Alarm zoomAlarm = new Alarm(Alarm.ThreadToUse.SWING_THREAD);
    public Alarm backgroundZoomAlarm;
    private Highlighter highlighter;
    private int selectedPage = -1;
//    private RenderRequest renderRequest;
    private String sourceFilePath;
    private String source;
    private ImageFormat imageFormat;
    private int page;
    private int version;
    private List<String> hilightText;
    private JPanel buttonPanel;
    private JButton finish;

    public CFGPanel(String sourceFilePath, String source, ImageFormat imageFormat, int page, int version){
        this.source = source;
        this.sourceFilePath = sourceFilePath;
        this.imageFormat = imageFormat;
        this.page = page;
        this.version = version;
        zoom = new Zoom(this,100, false);
        hilightText = new ArrayList<>();
        setupUI();
//        LowMemoryWatcher.register(new Runnable() {
//            @Override
//            public void run() {
////                renderCache.clear();
//                if (displayedItem != null && !CFGPanel.isVisible()) {
//                    displayedItem = null;
//                    imagesPanel.removeAll();
//                    imagesPanel.add(new JLabel("Low memory detected, cache and images cleared. Go to PlantUML plugin settings and set lower cache size, or increase IDE heap size (-Xmx)."));
//                    imagesPanel.revalidate();
//                    imagesPanel.repaint();
//                }
//            }
//        }, this);
        highlighter = new Highlighter();
        backgroundZoomAlarm = new Alarm(Alarm.ThreadToUse.POOLED_THREAD, this);
    }

    private void setupUI() {
        //好像没用
        createToolbar();

        imagesPanel = new JPanel();
        imagesPanel.setLayout(new BoxLayout(imagesPanel, BoxLayout.Y_AXIS));

        scrollPane = new JBScrollPane(imagesPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);
        scrollPane.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {
            @Override
            public void adjustmentValueChanged(AdjustmentEvent adjustmentEvent) {
                if (!adjustmentEvent.getValueIsAdjusting()) {
                    RenderCacheItem displayedItem = getDisplayedItem();
                    if (displayedItem != null && !displayedItem.getRenderResult().hasError()) {
                        lastValidVerticalScrollValue = adjustmentEvent.getValue();
                    }
                }
            }
        });
        scrollPane.getHorizontalScrollBar().addAdjustmentListener(new AdjustmentListener() {
            @Override
            public void adjustmentValueChanged(AdjustmentEvent adjustmentEvent) {
                if (!adjustmentEvent.getValueIsAdjusting()) {
                    RenderCacheItem displayedItem = getDisplayedItem();
                    if (displayedItem != null && !displayedItem.getRenderResult().hasError()) {
                        lastValidHorizontalScrollValue = adjustmentEvent.getValue();
                    }
                }

            }
        });
        //Usage Panel
//        imagesPanel.add(new Usage("Usage:\n"));

        add(scrollPane, BorderLayout.CENTER);

        addScrollBarListeners(imagesPanel);
    }

    private void finish() {
    //get startTestcubeAction
//        this.dispose();
    }

    protected void createToolbar() {
    }

    public RenderCacheItem getDisplayedItem() {
        return displayedItem;
    }

    @Deprecated
    @Override
    public boolean isVisible() {
        return super.isVisible();
    }

    private void addScrollBarListeners(JComponent panel) {
        MouseWheelListener l = new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (e.isControlDown()) {
                    changeZoom(Math.max(zoom.getUnscaledZoom() - e.getWheelRotation() * 10, 1), e.getPoint());
                } else {
                    e.setSource(scrollPane);
                    scrollPane.dispatchEvent(e);
                }
                e.consume();
            }
        };
        MouseMotionListener l1 = new MouseMotionListener() {
            private int x, y;

            @Override
            public void mouseDragged(MouseEvent e) {
                JScrollBar h = scrollPane.getHorizontalScrollBar();
                JScrollBar v = scrollPane.getVerticalScrollBar();

                int dx = x - e.getXOnScreen();
                int dy = y - e.getYOnScreen();

                h.setValue(h.getValue() + dx);
                v.setValue(v.getValue() + dy);

                x = e.getXOnScreen();
                y = e.getYOnScreen();
                e.consume();
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                x = e.getXOnScreen();
                y = e.getYOnScreen();
            }
        };
        panel.addMouseWheelListener(l);
        panel.addMouseMotionListener(l1);
        for (Component component : getAllComponents(panel)) {
            component.addMouseWheelListener(l);
            component.addMouseMotionListener(l1);
        }
    }


    public void changeZoom(int unscaledZoom, Point point) {
        unscaledZoom = Math.min(ZoomAction.MAX_ZOOM, unscaledZoom);
        int oldUnscaled = zoom.getUnscaledZoom();
        //do always, so that changed OS scaling takes effect
        zoom = new Zoom(unscaledZoom, false);

        if (oldUnscaled == unscaledZoom) {
            return;
        }

        zoom = zoom.refresh(this, false);
        version = version+1;
        RenderCommand.Reason reason = RenderCommand.Reason.SOURCE_PAGE_ZOOM;

        this.recordHilight();
        this.render(reason);
        this.displayResult(reason);
        this.maintainHighlight();
//        renderRequest.disableSvgZoom();
            //show the zoom ratio
//        WindowManager.getInstance().getStatusBar(project).setInfo("Zoomed changed to " + unscaledZoom + "%");
    }

    public static java.util.List<Component> getAllComponents(final Container c) {
        Component[] comps = c.getComponents();
        List<Component> compList = new ArrayList<Component>();
        for (Component comp : comps) {
            compList.add(comp);
            if (comp instanceof Container) {
                compList.addAll(getAllComponents((Container) comp));
            }
        }
        return compList;
    }

    public void render(RenderCommand.Reason reason){
        RenderCacheItem cachedItem = null;
        RenderRequest renderRequest = new RenderRequest(sourceFilePath, source, imageFormat, page, zoom, version,
                                                        true, reason);
        FacadeImpl plantUmlFacade = new FacadeImpl();
        RenderResult render = plantUmlFacade.render(renderRequest, cachedItem);
        this.renderResult = render;
    }

    public void displayResult(RenderCommand.Reason reason){;
        RenderRequest renderRequest = new RenderRequest(sourceFilePath, source, imageFormat, page, zoom, version,
                                                        true, reason);
        RenderCacheItem newRenderCacheItem = new RenderCacheItem(renderRequest, this.renderResult, page, version);
        displayResult(newRenderCacheItem);
    }

    public void displayResult(RenderCacheItem newItem) {
        boolean updateStatus = displayImages(newItem, false);
    }

    private boolean displayImages(RenderCacheItem cacheItem, boolean force) {
        RenderCacheItem displayedItem = this.displayedItem;
        //must be before revalidate
        int lastValidVerticalScrollValue = this.lastValidVerticalScrollValue;
        int lastValidHorizontalScrollValue = this.lastValidHorizontalScrollValue;


        this.displayedItem = cacheItem;

        ImageItem[] imageItems = cacheItem.getImageItems();
        RenderResult renderResult = cacheItem.getRenderResult();
        int requestedPage = cacheItem.getRequestedPage();

        removeAllImages();
        displayImage(cacheItem, requestedPage, imageItems[requestedPage]);

        imagesPanel.revalidate();
        imagesPanel.repaint();

        return true;
    }

    public void recordHilight(){
        hilightText.clear();
        for(Component component: imagesPanel.getComponents())
        {
            if(component instanceof ImageContainerPng){
                for( Component label : ((ImageContainerPng) component).getComponents()){
                    if(label instanceof MyJLabel){
                        if(((MyJLabel) label).isHighlighted()){
                            hilightText.add(((MyJLabel) label).getLinkDataText());
                        }
                    }
                }
            }
        }
    }

    private void maintainHighlight(){
        for(String text: hilightText){
            new Highlighter().highlightImages(imagesPanel, text);
        }
    }

    private void removeAllImages() {
        Component[] children = imagesPanel.getComponents();
        imagesPanel.removeAll();
        for (Component component : children) {
            if (component instanceof Disposable) {
                Disposer.dispose((Disposable) component);
            }
        }
    }

    private void displayImage(RenderCacheItem cacheItem, int pageNumber, ImageItem imageWithData) {
        JComponent component = createImageContainer(cacheItem, pageNumber, imageWithData);

        imagesPanel.add(component);
        imagesPanel.add(separator());
        buttonPanel = new JPanel();
        finish = new JButton("Finish");
        finish.addActionListener(l->finish());
        buttonPanel.add(finish, BorderLayout.SOUTH);
        imagesPanel.add(buttonPanel);
    }

    @NotNull
    private JComponent createImageContainer(RenderCacheItem cacheItem, int pageNumber, ImageItem imageWithData) {
        if (imageWithData == null) {
            throw new RuntimeException("trying to display null image. selectedPage=" + selectedPage + ", nullPage=" + pageNumber + ", cacheItem=" + cacheItem);
        }
        JComponent component = null;

        component = new ImageContainerPng(this, imagesPanel, imageWithData, pageNumber, cacheItem.getRenderRequest(), cacheItem.getRenderResult());
        addScrollBarListeners(component);
        return component;
    }

    private JSeparator separator() {
        JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
        Dimension size = new Dimension(separator.getPreferredSize().width, 10);
        separator.setVisible(true);
        separator.setMaximumSize(size);
        separator.setPreferredSize(size);
        return separator;
    }

    public JPanel getImagesPanel() {
        return imagesPanel;
    }

    @Override
    public void dispose() {
//        logger.debug("dispose");
        removeAllImages();
    }
}
