package org.testshift.testcube.branches.rendering;

import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.testshift.testcube.branches.CFGPanel;
//import org.testshift.testcube.branches.preview.image.ImageContainerSvg;
//import org.testshift.testcube.branches.preview.image.MyImageEditorImpl;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.List;

public class ImageItem {
    public static final String ERROR = "(Error)";
    private final int page;
    @Nullable
    private final String description;
    @NotNull
    private final RenderingType renderingType;
    @NotNull
    private List<LinkData> links;
    @Nullable
    private final String title;
    @Nullable
    private final String customFileName;
    @NotNull
    private final ImageFormat format;
    @Nullable
    private final String pageSource;
    @NotNull
    private final String documentSource;
    private final byte[] imageBytes;
    private Throwable exception;

    private final Map<CFGPanel, ImageItemComponent> componentMap = new HashMap<>();
    private BufferedImage bufferedImage;

    public ImageItem(//@Nullable File baseDir,
                     @NotNull ImageFormat format,
                     @NotNull String documentSource,
                     @Nullable String pageSource,
                     int page,
                     @Nullable String description,
                     @Nullable byte[] imageBytes,
                     @Nullable byte[] svgBytes,
                     @NotNull RenderingType renderingType,
                     @Nullable String title,
                     @Nullable String customFileName,
                     @Nullable Throwable exception) {
        this.format = format;
        this.pageSource = pageSource;
        this.documentSource = documentSource;
        this.page = page;
        this.description = description;
        this.renderingType = renderingType;
        this.title = title;
        this.customFileName = customFileName;
        this.imageBytes = imageBytes;
        this.exception = exception;

        this.links = this.parseLinks(svgBytes/*, baseDir*/);
    }

    public ImageItem(int page, ImageItem item, @NotNull ImageFormat format) {
        this.page = page;
        this.description = item.description;
        this.pageSource = item.pageSource;
        this.documentSource = item.documentSource;
        this.componentMap.putAll(item.componentMap);
        this.links = item.links;
        this.imageBytes = item.imageBytes;
        this.renderingType = item.renderingType;
        this.title = item.title;
        this.customFileName = item.customFileName;
        this.format = format;
        this.exception = item.exception;
    }

    public byte[] getImageBytes() {
        return imageBytes;
    }
    @Nullable
    public BufferedImage getImage(CFGPanel cfgPanel, RenderRequest renderRequest,
                                  RenderResult renderResult) {
        ImageItemComponent imageItemComponent = getImageItemComponent(cfgPanel);
        if (imageItemComponent.image == null && hasImageBytes()) {
            imageItemComponent = initImage(renderRequest, renderResult, cfgPanel);
        }
        if (imageItemComponent == null) {
            return null;
        }
        return imageItemComponent.getImage();
    }

    public ImageItemComponent initImage(RenderRequest renderRequest, RenderResult renderResult, CFGPanel cfgPanel) {
        ImageItemComponent imageItemComponent = getImageItemComponent(cfgPanel);

        if (imageItemComponent.isNull() && hasImageBytes()) {
                initPng(imageItemComponent);
            }
        return imageItemComponent;
    }

    /**
     * it seems that BufferedImage can be shared in multiple components
     */
    private synchronized void initPng(ImageItemComponent imageItemComponent) {
        try {
            if (bufferedImage == null) {
                bufferedImage = getBufferedImage(getImageBytes());
            }
            imageItemComponent.image = bufferedImage;
            if (imageItemComponent.image == null) {
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //come from Utils
    @Nullable
    public static BufferedImage getBufferedImage(@NotNull byte[] imageBytes) throws IOException {
        ByteArrayInputStream input = new ByteArrayInputStream(imageBytes);
        return ImageIO.read(input);
    }


    @Nullable
    public String getTitle() {
        return title;
    }
    private List<LinkData> parseLinks(byte[] svgData/*, File baseDir*/) {
        if (svgData == null || svgData.length == 0) {
            return Collections.emptyList();
        }
        try {
            long start = System.currentTimeMillis();
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            //http://stackoverflow.com/a/155874/685796
            factory.setValidating(false);
            factory.setNamespaceAware(true);
            factory.setFeature("http://xml.org/sax/features/namespaces", false);
            factory.setFeature("http://xml.org/sax/features/validation", false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new ByteArrayInputStream(svgData));

            ArrayList<LinkData> linkData = new ArrayList<>();
            linkData.addAll(parseHyperLinks(document));
            linkData.addAll(parseText(document));

//            LOG.debug("parseLinks done in ", System.currentTimeMillis() - start, "ms");
            return linkData;
        } catch (Throwable e) {
//            logger.warn(e);
            return Collections.emptyList();
        }
    }
    public class LinkData {
        private final String text;
        private final Rectangle clickArea;
        private final boolean link;

        public LinkData(String text, Rectangle clickArea, boolean link) {
            this.text = text;
            this.clickArea = clickArea;
            this.link = link;
        }
        public Rectangle getClickArea() {
            return clickArea;
        }

        public String getText() {
            return text;
        }

        public boolean isLink() {
            return link;
        }
    }

    @NotNull
    private List<LinkData> parseHyperLinks(Document document) throws XPathExpressionException, URISyntaxException {
        String xpathExpression = "//a";

        XPathFactory xpf = XPathFactory.newInstance();
        XPath xpath = xpf.newXPath();
        XPathExpression expression = xpath.compile(xpathExpression);

        NodeList svgPaths = (NodeList) expression.evaluate(document, XPathConstants.NODESET);

        List<LinkData> urls = new ArrayList<LinkData>();
        for (int i = 0; i < svgPaths.getLength(); i++) {
            urls.addAll(createLink(svgPaths.item(i)));
        }
        return urls;
    }

    private Collection<? extends LinkData> parseText(Document document) throws XPathExpressionException {
        String xpathExpression = "//text";

        XPathFactory xpf = XPathFactory.newInstance();
        XPath xpath = xpf.newXPath();
        XPathExpression expression = xpath.compile(xpathExpression);

        NodeList nodes = (NodeList) expression.evaluate(document, XPathConstants.NODESET);

        List<LinkData> urls = new ArrayList<LinkData>();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getParentNode() != null && node.getParentNode().getNodeName().equals("a")) {
                continue;
            }
            String textContent = node.getTextContent();
            if (StringUtils.isEmpty(textContent)) {
                continue;
            }
            LinkData e = textNodeToUrlData(textContent, node, false);
            if (e != null) {
                urls.add(e);
            }
        }
        return urls;
    }

    /**
     * <a href="ddd" target="_top" title="ddd" xlink:actuate="onRequest" xlink:href="ddd" xlink:show="new"
     * xlink:title="ddd" xlink:type="simple">
     * <text fill="#0000FF" font-family="sans-serif" font-size="13" lengthAdjust="spacingAndGlyphs"
     * text-decoration="underline" textLength="21" x="1235.5" y="3587.8857">ddd
     * </text>
     * </a>
     */
    private List<LinkData> createLink(Node linkNode) throws URISyntaxException {
        List<LinkData> urls = new ArrayList<LinkData>();

        String nodeValue = linkNode.getAttributes().getNamedItem("xlink:href").getNodeValue();

        for (int i = 0; i < linkNode.getChildNodes().getLength(); i++) {
            Node child = linkNode.getChildNodes().item(i);
            if (child.getNodeName().equals("text")) {
                LinkData e = textNodeToUrlData(nodeValue, child, true);
                if (e != null) {
                    urls.add(e);
                }
            }
        }

        return urls;
    }

    private LinkData textNodeToUrlData(String text, Node child, boolean link) {
        NamedNodeMap nodeAttributes = child.getAttributes();
        int x = (int) Float.parseFloat(nodeAttributes.getNamedItem("x").getNodeValue());
        int y = (int) Float.parseFloat(nodeAttributes.getNamedItem("y").getNodeValue());
        Node textLength1 = nodeAttributes.getNamedItem("textLength");
        if (textLength1 == null) {
            return null;
        }
        int textLength = (int) Float.parseFloat(textLength1.getNodeValue());
        int height = (int) Float.parseFloat(nodeAttributes.getNamedItem("font-size").getNodeValue());

        Rectangle rect = new Rectangle(
                x,
                y - height,
                textLength,
                height
        );

        return new LinkData(text, rect, link);
    }

    public boolean hasError() {
        String description = getDescription();
        if (description == null || description.isEmpty() || ImageItem.ERROR.equals(description)) {
            return true;
        }
        return false;
    }

    @NotNull
    public List<LinkData> getLinks() {
        return links;
    }

    public String getDescription() {
        return description;
    }

    public boolean hasImageBytes() {
        return imageBytes != null && imageBytes.length > 0;
    }


    private synchronized ImageItemComponent getImageItemComponent(CFGPanel cfgPanel) {
        ImageItemComponent imageItemComponent = componentMap.get(cfgPanel);
        if (imageItemComponent == null) {
            imageItemComponent = new ImageItemComponent();
            if (cfgPanel != null) {
                componentMap.put(cfgPanel, imageItemComponent);
            }
        }
        return imageItemComponent;
    }

}
