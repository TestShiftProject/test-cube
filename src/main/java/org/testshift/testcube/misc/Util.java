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
        return getTestClassPath(currentProject, testClass, false, getDSpotOutputPath(currentProject));
    }

    public static String getAmplifiedTestClassPathToPrettify(Project currentProject, String testClass) {
        return getTestClassPath(currentProject, testClass, false, getOutputSavePath(currentProject));
    }

    public static String getOriginalTestClassPath(Project currentProject, String testClass) {
        return getTestClassPath(currentProject, testClass, true, getDSpotOutputPath(currentProject));
    }

    private static String getTestClassPath(Project currentProject, String testClass, boolean original,
                                           String basePath) {
        return basePath + (original ? File.separator + "original" : "") +
               File.separator + testClass.replaceAll("\\.", Matcher.quoteReplacement(File.separator)) + ".java";
    }

    public static String getTargetFolder(Project project) {
        return project.getBasePath() + File.separator + "target";
    }

    public static String getDSpotOutputPath(Project project) {
        return project.getBasePath() + Config.OUTPUT_PATH_DSPOT;
    }

    public static String getOutputSavePath(Project project) {
        return getTestCubeOutputPath(project) + Config.OUTPUT_PATH_DSPOT;
    }

    public static String getPrettifierOutputPath(Project project) {
        return project.getBasePath() + Config.OUTPUT_PATH_PRETTIFIER;
    }

    public static String getTestCubeOutputPath(Project project) {
        return project.getBasePath() + Config.OUTPUT_PATH_TESTCUBE;
    }


    public static TestClassJSON getResultJSON(Project project, String testClass) {
        Gson gson = new Gson();
        try {
            return gson.fromJson(new FileReader(getOutputSavePath(project) + File.separator + testClass + "_report.json"),
                                 TestClassJSON.class);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean matchMethodNameAndDescriptor(PsiMethod psiMethod, String name, String descriptor) {
        return psiMethod.getName().equals(name) && ClassUtil.getAsmMethodSignature(psiMethod).equals(descriptor);
    }

    /**
     * Sleeps and then refreshes the project directory, to aim to load all newly created files from disk so that they
     * can be picked up by the IDE for display.
     * @param project the project whose root directory should be refreshed
     */
    public static void sleepAndRefreshProject(Project project) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Objects.requireNonNull(ProjectUtil.guessProjectDir(project)).refresh(false, true);
    }
}
