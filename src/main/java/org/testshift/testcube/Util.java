package org.testshift.testcube;

import com.intellij.openapi.project.Project;

import java.io.File;

public class Util {

    public static String getAmplifiedTestClassPath(Project currentProject, String testClass) {
        return getTestClassPath(currentProject, testClass, false);
    }

    public static String getOriginalTestClassPath(Project currentProject, String testClass) {
        return getTestClassPath(currentProject, testClass, true);
    }

    private static String getTestClassPath(Project currentProject, String testClass, boolean original) {
        return currentProject.getBasePath() +
                Config.OUTPUT_PATH_DSPOT +
                (original ? File.separator + "original" : "") +
                File.separator + testClass.replaceAll("\\.", File.separator) + ".java";
    }
}
