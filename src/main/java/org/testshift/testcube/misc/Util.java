package org.testshift.testcube.misc;

import com.google.gson.Gson;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.util.ClassUtil;
import eu.stamp_project.dspot.common.report.output.selector.extendedcoverage.json.TestClassJSON;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Objects;
import java.util.regex.Matcher;

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
               File.separator + testClass.replaceAll("\\.", Matcher.quoteReplacement(File.separator)) + ".java";
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

    public static boolean matchMethodNameAndDescriptor(PsiMethod psiMethod, String name, String descriptor) {
        return psiMethod.getName().equals(name) && ClassUtil.getAsmMethodSignature(psiMethod).equals(descriptor);
    }

    public static void sleepAndRefreshProject(Project project) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Objects.requireNonNull(ProjectUtil.guessProjectDir(project)).refresh(false, true);
    }
}
