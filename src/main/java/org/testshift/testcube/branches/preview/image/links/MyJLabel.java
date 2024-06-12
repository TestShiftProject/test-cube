package org.testshift.testcube.branches.preview.image.links;

import com.intellij.ui.ColoredSideBorder;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.testshift.testcube.branches.rendering.ImageItem;
import org.testshift.testcube.misc.Util;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.List;
import java.util.Set;

public class MyJLabel extends JLabel {
    private static final ColoredSideBorder BORDER = new ColoredSideBorder(Color.RED, Color.RED, Color.RED, Color.RED,
                                                                          2);
    private static final CompoundBorder COVERBORDER = getCoverBorder();
    private static final CompoundBorder HIGHLIGHT = getCompoundBorder();
    private static final CompoundBorder NEWCOVERBORDER = getNewCoverBorder();

    private final ImageItem.LinkData linkData;
    private Rectangle area;
//    private final boolean showUrlLinksBorder;
    private boolean highlighted;
    private boolean covered;
    private boolean newCovered;
    private boolean isbranch;

    public MyJLabel(ImageItem.LinkData linkData, Rectangle area/*, boolean showUrlLinksBorder*/) {
        this.linkData = linkData;
        this.area = area;
//        this.showUrlLinksBorder = showUrlLinksBorder;
        this.highlighted=false;
        this.covered = false;
        this.newCovered = false;
        this.isbranch = false;
//        if (showUrlLinksBorder) {
//            setBorder(BORDER);
//        }

        setLocation(area.getLocation());
        setSize(area.getSize());

        setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    public ImageItem.LinkData getLinkData() {
        return linkData;
    }

    @NotNull
    private static CompoundBorder getCompoundBorder() {
        LineBorder lineBorder1 = new LineBorder(Color.RED, 1);
        LineBorder lineBorder2 = new LineBorder(Color.RED, 1);
        LineBorder lineBorder3 = new LineBorder(Color.RED, 1);
        CompoundBorder border = new CompoundBorder(lineBorder1, new CompoundBorder(lineBorder2, lineBorder3));
        return border;
    }

    @NotNull
    private static CompoundBorder getCoverBorder() {
        LineBorder lineBorder1 = new LineBorder(new Color(0,153,0), 1);
        LineBorder lineBorder2 = new LineBorder(new Color(0,153,0), 1); //Dark GREEN
        LineBorder lineBorder3 = new LineBorder(new Color(0,153,0), 1);
        CompoundBorder border = new CompoundBorder(lineBorder1, new CompoundBorder(lineBorder2, lineBorder3));
        return border;
    }

    @NotNull
    private static CompoundBorder getNewCoverBorder() {
        LineBorder lineBorder1 = new LineBorder(new Color(0,255,51), 1);
        LineBorder lineBorder2 = new LineBorder(new Color(0,255,51), 1); //Light GREEN
        LineBorder lineBorder3 = new LineBorder(new Color(0,255,51), 1);
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
            if (this.linkData.getText().equals(text) && !highlighted && !covered && !newCovered) {
                highlight();
                highlighted = true;
            }
            else if(highlighted){
                unhighlight();
                highlighted = false;
            }
    }

    private void unhighlight() {
        setBorder(null);
        setLocation(area.getLocation());
        setSize(area.getSize());
    }

    private void highlight() {
        setBorder(HIGHLIGHT);
        Rectangle rectangle = new Rectangle(area.x - 2, area.y - 2, area.width + 4, area.height + 4);
        setLocation(rectangle.getLocation());
        setSize(rectangle.getSize());
    }

    public void coverLines(Set<String> lines){
        boolean contain = containsLine(linkData.getText(), lines);
        if (contain) {
            if (!covered) {
                cover();
                covered = true;
            }
        }
    }

    public void coverBranches(Set<Util.Branch> branchs){
        boolean contain = containsBranch(linkData.getText(), branchs);
        if (contain) {
            if (!covered) {
                cover();
                covered = true;
            }
        }
    }


    public void coverNewLines(Set<String> lines) {
        boolean contain = containsLine(linkData.getText(), lines);
        if (contain) {
            newCover();
            newCovered = true;
        }
    }

    public void coverNewBranches(Set<Util.Branch> branchs){
        boolean contain = containsBranch(linkData.getText(), branchs);
        if (contain) {
            newCover();
            newCovered = true;
        }
    }

    private void cover(){
        setBorder(COVERBORDER);
        setLocation(area.getLocation());
        setSize(area.getSize());
    }

    private void newCover(){
        setBorder(NEWCOVERBORDER);
        setLocation(area.getLocation());
        setSize(area.getSize());
    }

    public void unNewCover(){
        setBorder(null);
        setLocation(area.getLocation());
        setSize(area.getSize());
        newCovered = false;
    }

    private boolean containsLine(String text, Set<String> lines) {
        for (String line : lines) {
            if (line.length() == 0) {
                continue;
            }
            if (text.contains(line)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsBranch(String text, Set<Util.Branch> branchs) {
        for (Util.Branch branch : branchs) {
            if (text.contains(branch.getLine()) && text.contains(branch.getSymbol())) {
                return true;
            }
        }
        return false;
    }

    public boolean isHighlighted(){
        return highlighted;
    }

    public boolean isBranch() {
        return isbranch;
    }

    public boolean isCovered() {
        return covered;
    }

    public boolean isNewCovered() {
        return newCovered;
    }

    public void setIsbranch(boolean isbranch) {
        this.isbranch = isbranch;
    }

    public String getLinkDataText(){
        return linkData.getText();
    }
}
