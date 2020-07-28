package org.testshift.testcube;

import com.google.gson.Gson;
import com.intellij.openapi.project.Project;
import eu.stamp_project.dspot.common.report.output.selector.coverage.json.TestClassJSON;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

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

    public static TestClassJSON getResultJSON(Project project, String testClass) {
        Gson gson = new Gson();
        try {
            return gson.fromJson(new FileReader(project.getBasePath() + Config.OUTPUT_PATH_DSPOT + File.separator + testClass + "_report.json"), TestClassJSON.class);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
