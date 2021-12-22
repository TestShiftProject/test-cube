package org.testshift.testcube.branches.preview.image.links;

import com.intellij.ui.ColoredSideBorder;
import org.jetbrains.annotations.NotNull;
import org.testshift.testcube.branches.rendering.ImageItem;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.List;

public class MyJLabel extends JLabel {
    private static final ColoredSideBorder BORDER = new ColoredSideBorder(Color.RED, Color.RED, Color.RED, Color.RED, 1);
    private static final CompoundBorder HIGHLIGHT = getCompoundBorder();

    private final ImageItem.LinkData linkData;
    private Rectangle area;
    private final boolean showUrlLinksBorder;
    private boolean highlighted;

    public MyJLabel(ImageItem.LinkData linkData, Rectangle area, boolean showUrlLinksBorder) {
        this.linkData = linkData;
        this.area = area;
        this.showUrlLinksBorder = showUrlLinksBorder;
        this.highlighted=false;
        if (showUrlLinksBorder) {
            setBorder(BORDER);
        }

        setLocation(area.getLocation());
        setSize(area.getSize());

        setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    public ImageItem.LinkData getLinkData() {
        return linkData;
    }

    @NotNull
    private static CompoundBorder getCompoundBorder() {
        LineBorder lineBorder1 = new LineBorder(Color.BLACK, 1);
        LineBorder lineBorder2 = new LineBorder(Color.GREEN, 1);
        LineBorder lineBorder3 = new LineBorder(Color.BLACK, 1);
        CompoundBorder border = new CompoundBorder(lineBorder1, new CompoundBorder(lineBorder2, lineBorder3));
        return border;
    }

//    public void updatePosition(Rectangle area) {
//        this.area = area;
//
//        if (highlighted) {
//            highlight();
//        } else {
//            unhighlight();
//        }
//    }

    public void highlight(String text) {
            if (!highlighted) {
                highlight();
                highlighted = true;
            }
            else{
                unhighlight();
                highlighted =false;
            }
    }

    private void unhighlight() {
        //BORDER可以直接不要
        setBorder(showUrlLinksBorder ? BORDER : null);
        setLocation(area.getLocation());
        setSize(area.getSize());
    }

    private void highlight() {
//        setBackground(Color.RED);
        setBorder(HIGHLIGHT);
        Rectangle rectangle = new Rectangle(area.x - 2, area.y - 2, area.width + 4, area.height + 4);
        setLocation(rectangle.getLocation());
        setSize(rectangle.getSize());
    }

    public boolean isHighlighted(){
        return highlighted;
    }


    public String getLinkDataText(){
        return linkData.getText();
    }
}
