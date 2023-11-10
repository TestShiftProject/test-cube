package org.testshift.testcube.branches.rendering;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.VirtualFile;
import net.sourceforge.plantuml.FileSystem;
import net.sourceforge.plantuml.OptionFlags;
import net.sourceforge.plantuml.SourceStringReader;
import net.sourceforge.plantuml.preproc.Defines;
import net.sourceforge.plantuml.security.SFile;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class PlantUmlRendererUtil {
    private static final PlantUmlNormalRenderer NORMAL_RENDERER = new PlantUmlNormalRenderer();
    public static final Pattern NEW_PAGE_PATTERN = Pattern.compile("\\n\\s*@?(?i)(newpage)(\\p{Blank}+[^\\n]+|\\p{Blank}*)(?=\\n)");

    public static RenderResult render(RenderRequest renderRequest, RenderCacheItem cachedItem) {
        //或许可以不要
//        prepareEnvironment(renderRequest.getSourceFilePath());

        String source = renderRequest.getSource();

        String[] sourceSplit = NEW_PAGE_PATTERN.split(source);
        RenderResult renderResult;
        renderResult = NORMAL_RENDERER.doRender(renderRequest, cachedItem, sourceSplit);


        return renderResult;
    }


    // comes from Utils
//    @NotNull
//    public static void prepareEnvironment(String sourceFilePath) {
////        OptionFlags.getInstance().setVerbose(LOG.isDebugEnabled());
//
////        long start = System.currentTimeMillis();
//        File baseDir = UIUtils.getParent(new File(sourceFilePath));
//        if (baseDir != null) {
//            setPlantUmlDir(baseDir);
//        } else {
//            resetPlantUmlDir();
//        }
//
//        saveAllDocuments(sourceFilePath);
//    }
    public static SourceStringReader newSourceStringReader(String source, RenderRequest renderRequest) {
//        File file = renderRequest.getSourceFile();
        List<String> configAsList;
        String encoding;

        encoding = "UTF-8";
        configAsList = new ArrayList<>();

        Defines defines;
//        if (file != null) {
//            defines = Defines.createWithFileName(file);
//        } else {
            defines = Defines.createEmpty();
//        }
        SourceStringReader sourceStringReader = new SourceStringReader(defines, source, encoding, configAsList);
        return sourceStringReader;
    }

    public static void setPlantUmlDir(@NotNull File baseDir) {
        FileSystem.getInstance().setCurrentDir(new SFile(baseDir.toURI()));

        String includedPaths = "";
        String separator = System.getProperty("path.separator");

        StringBuilder sb = new StringBuilder();
        sb.append(baseDir.getAbsolutePath());

        System.setProperty("plantuml.include.path", sb.toString());
    }

    public static void resetPlantUmlDir() {
        FileSystem.getInstance().reset();
        System.clearProperty("plantuml.include.path");
    }

    public static void saveAllDocuments(@Nullable String sourceFilePath) {
        try {
            FileDocumentManager documentManager = FileDocumentManager.getInstance();
            com.intellij.openapi.editor.Document[] unsavedDocuments = documentManager.getUnsavedDocuments();
            if (unsavedDocuments.length > 0 && !onlyCurrentlyDisplayed(documentManager, unsavedDocuments, sourceFilePath)) {
                ApplicationManager.getApplication().invokeAndWait(documentManager::saveAllDocuments);
            }
        } catch (Throwable e) {
            Throwable cause = e.getCause();
            if (cause instanceof InterruptedException) {
                throw new RuntimeException((InterruptedException) cause);
            }
            throw e;
        }
    }

    private static boolean onlyCurrentlyDisplayed(FileDocumentManager documentManager, com.intellij.openapi.editor.Document[] unsavedDocuments, @Nullable String sourceFile) {
        if (unsavedDocuments.length == 1 && sourceFile != null) {
            com.intellij.openapi.editor.Document unsavedDocument = unsavedDocuments[0];
            VirtualFile file = documentManager.getFile(unsavedDocument);
            if (file != null) {
                return file.getPath().equals(sourceFile);
            }
        }
        return false;

    }


}
