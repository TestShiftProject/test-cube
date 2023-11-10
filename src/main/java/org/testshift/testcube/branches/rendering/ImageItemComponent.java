package org.testshift.testcube.branches.rendering;

import org.jetbrains.annotations.Nullable;
//import org.testshift.testcube.branches.preview.image.MyImageEditorImpl;

import java.awt.image.BufferedImage;

public class ImageItemComponent {
    @Nullable
    public volatile BufferedImage image;

    @Nullable
    public BufferedImage getImage() {
        return image;
    }

    public void setImage(@Nullable BufferedImage image) {
        this.image = image;
    }

    boolean isNull() {
        return image == null;
    }
}

