package org.testshift.testcube.branches.rendering;

import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public class UIUtils {
    @Nullable
    public static File getParent(File file) {
        if (file.exists()) {
            File parentFile = file.getParentFile();
            if (parentFile != null && parentFile.isDirectory()) {
                return parentFile.getAbsoluteFile();
            }
        }
        return null;
    }

}
