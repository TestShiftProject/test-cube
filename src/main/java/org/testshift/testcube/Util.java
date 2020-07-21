package org.testshift.testcube;

import com.intellij.openapi.project.Project;

import java.io.File;

public class Util {

    public static String getAmplifiedTestClassPath(Project currentProject, String testClass) {
        return currentProject.getBasePath() +
                Config.OUTPUT_PATH_DSPOT + File.separator +
                testClass.replaceAll("\\.", File.separator) + ".java";
    }
}
