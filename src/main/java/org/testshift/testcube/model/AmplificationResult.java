package org.testshift.testcube.model;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiMethod;
import eu.stamp_project.dspot.common.report.output.selector.extendedcoverage.json.TestCaseJSON;
import eu.stamp_project.dspot.common.report.output.selector.extendedcoverage.json.TestClassJSON;
import eu.stamp_project.dspot.selector.extendedcoverageselector.CoverageImprovement;
import eu.stamp_project.dspot.selector.extendedcoverageselector.ExtendedCoverage;
import org.testshift.testcube.misc.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * This class holds the whole result of the amplification performed by DSpot.
 */
public class AmplificationResult {

    private static final Logger logger = Logger.getInstance(AmplificationResult.class);

    private AmplificationResult(Project project, String testClass) {
        this.project = project;
        this.testClass = testClass;
    }

    public Project project;
    public String testClass;
    public ExtendedCoverage initialCoverage;
    public HtmlCoverageImprovement amplifiedCoverageHTML;
    public CoverageImprovement amplifiedCoverage;
    public OriginalTestCase originalTestCase;
    public List<AmplifiedTestCase> amplifiedTestCases = new ArrayList<>();

    /**
     * Builds the amplification result based on
     * @param project the current project in which the amplification was performed
     * @param testClass the fully qualified name of the original/amplified test class
     * @param testMethod the simple name of the original test method
     * @return the complete result of the amplification
     */
    public static AmplificationResult buildAmplificationResult(Project project, String testClass, String testMethod) {

        AmplificationResult result = new AmplificationResult(project, testClass);
        TestClassJSON jsonResult = Util.getResultJSON(project, testClass);
        if (jsonResult == null) {
            logger.warn("Json result file not found!");
            return result;
        }

        String originalTestClassPath = Util.getOriginalTestClassPath(project, testClass);
        VirtualFile originalFile = LocalFileSystem.getInstance().findFileByPath(originalTestClassPath);
        if (originalFile != null) {
            PsiJavaFile psiFile = (PsiJavaFile) PsiManager.getInstance(project).findFile(originalFile);
            result.originalTestCase = new OriginalTestCase(originalTestClassPath, testMethod, psiFile, result);
            result.originalTestCase.psiFile = result.originalTestCase.getFileInProjectSource();
        }

        result.initialCoverage = jsonResult.getInitialCoverage();
        result.amplifiedCoverage = new CoverageImprovement(
                jsonResult.getAmplifiedCoverage().getInstructionImprovement());
        result.amplifiedCoverageHTML = new HtmlCoverageImprovement(jsonResult.getAmplifiedCoverage());

        String amplifiedTestClassPath = Util.getAmplifiedTestClassPath(project, testClass);
        VirtualFile file = LocalFileSystem.getInstance().findFileByPath(amplifiedTestClassPath);
        if (file != null) {
            PsiJavaFile psiFile = (PsiJavaFile) PsiManager.getInstance(project).findFile(file);
            if (psiFile != null) {

                PsiClass psiClass = Arrays.stream(psiFile.getClasses())
                                          .filter((PsiClass c) -> c.getQualifiedName().equals(testClass))
                                          .findFirst()
                                          .get();
                PsiMethod[] methods = psiClass.getMethods();

                if (methods.length != jsonResult.getTestCases().size()) {
                    logger.warn("Count of methods found in amplified class: " + methods.length + " does not match " +
                                "with match with count of amplified methods reported: " +
                                jsonResult.getTestCases().size());
                }

                for (PsiMethod method : methods) {
                    Optional<TestCaseJSON> testCaseJSON = jsonResult.getTestCases()
                                                                    .stream()
                                                                    .filter(tcj -> tcj.getName()
                                                                                      .equals(method.getName()))
                                                                    .findAny();

                    if (testCaseJSON.isPresent()) {
                        result.amplifiedTestCases.add(
                                new AmplifiedTestCase(amplifiedTestClassPath, method.getName(), psiFile, result,
                                                      testCaseJSON.get().getCoverageImprovement(),
                                                      testCaseJSON.get().getNbAssertionAdded(),
                                                      testCaseJSON.get().getNbInputAdded()));
                    } else {
                        logger.warn("Found no matching json result for test case " + method.getName());
//                        if (terminationCounter > 0) {
//                            Util.sleepAndRefreshProject(project);
//                            return buildAmplificationResult(project, testClass, testMethod, terminationCounter - 1);
//                        }
                    }
                }
            }
        }

        return result;
    }
}
