package org.testshift.testcube.misc;

import com.google.gson.Gson;
import com.intellij.openapi.project.Project;
import eu.stamp_project.dspot.common.report.output.selector.extendedcoverage.json.TestClassJSON;

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

    public static String getDSpotOutputPath(Project project) {
        return project.getBasePath() + Config.OUTPUT_PATH_DSPOT;
    }

    public static String getTestCubeOutputPath(Project project) {
        return project.getBasePath() + Config.OUTPUT_PATH_TESTCUBE;
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
