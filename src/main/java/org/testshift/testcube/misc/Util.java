package org.testshift.testcube.misc;

import com.google.gson.Gson;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.util.ClassUtil;
import eu.stamp_project.dspot.common.report.output.selector.extendedcoverage.json.TestClassJSON;
import eu.stamp_project.prettifier.output.report.ReportJSON;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Objects;
import java.util.regex.Matcher;

public class Util {

    /**
     * Path to the file containing the final amplified test cases.
     */
    public static String getAmplifiedTestClassPath(Project currentProject, String testClass) {
        return getTestClassPath(currentProject, testClass, false, getDSpotOutputPath(currentProject));
    }

    /**
     * Path to the file containing the amplified test cases after amplification, but before prettification.
     */
    public static String getAmplifiedTestClassPathToPrettify(Project currentProject, String testClass) {
        return getTestClassPath(currentProject, testClass, false, getOutputSavePath(currentProject));
    }

    /**
     * Path to the file containing the original test case that was base for the amplification.
     */
    public static String getOriginalTestClassPath(Project currentProject, String testClass) {
        return getTestClassPath(currentProject, testClass, true, getDSpotOutputPath(currentProject));
    }

    private static String getTestClassPath(Project currentProject, String testClass, boolean original,
                                           String basePath) {
        return basePath + (original ? File.separator + "original" : "") +
               File.separator + testClass.replaceAll("\\.", Matcher.quoteReplacement(File.separator)) + ".java";
    }

    /**
     * Path to the output folder of DSpot (both DSpot itself (before saving for the prettifier) and the prettifier at
     * the end of the complete amplification project.
     */
    public static String getDSpotOutputPath(Project currentProject) {
        return currentProject.getBasePath() + Config.OUTPUT_PATH_DSPOT;
    }

    /**
     * Path where the output of DSpot is saved before running the prettifier (to avoid the prettifier overriding it).
     */
    public static String getOutputSavePath(Project currentProject) {
        return getTestCubeOutputPath(currentProject) + Config.OUTPUT_PATH_DSPOT;
    }

    /**
     * Path where TestCube outputs any intermediate files.
     */
    public static String getTestCubeOutputPath(Project currentProject) {
        return currentProject.getBasePath() + Config.OUTPUT_PATH_TESTCUBE;
    }

    /**
     * Load the JSON containing the results of the {@link eu.stamp_project.dspot.selector.ExtendedCoverageSelector}.
     *
     * @param project the current project which the amplification was performed on
     * @param testClass the fully qualified name of the test class which was amplified
     * @return the extended coverage result for that class
     */
    public static TestClassJSON getResultJSON(Project project, String testClass) {
        Gson gson = new Gson();
        try {
            ReportJSON prettifierReport = gson.fromJson(
                    new FileReader(getDSpotOutputPath(project) + File.separator + testClass + "prettifier_report.json"),
                    ReportJSON.class);
            return prettifierReport.extendedCoverageReport;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Checks if a {@link PsiMethod} matches the given simple name and method descriptor (showing the parameters &
     * return value, defined by Jacoco and {@link ClassUtil#getAsmMethodSignature(PsiMethod)}).
     *
     * @param psiMethod the Java method to check
     * @param name the simple name that the method should have
     * @param descriptor the method descriptor (as given by Jacoco)
     * @return true if the method matches name and descriptor, false otherwise
     */
    public static boolean matchMethodNameAndDescriptor(PsiMethod psiMethod, String name, String descriptor) {
        return psiMethod.getName().equals(name) && ClassUtil.getAsmMethodSignature(psiMethod).equals(descriptor);
    }

    /**
     * Sleeps and then refreshes the project directory, to aim to load all newly created files from disk so that they
     * can be picked up by the IDE for display.
     *
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
