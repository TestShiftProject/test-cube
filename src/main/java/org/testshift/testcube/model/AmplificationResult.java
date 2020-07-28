package org.testshift.testcube.model;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import eu.stamp_project.dspot.common.report.output.selector.coverage.json.TestClassJSON;
import org.testshift.testcube.misc.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AmplificationResult {

    private static final Logger logger = Logger.getInstance(AmplificationResult.class);

    private AmplificationResult(Project project, String testClass) {
        this.project = project;
        this.testClass = testClass;
    }

    public Project project;
    public String testClass;
    public OriginalTestCase originalTestCase;
    public List<AmplifiedTestCase> amplifiedTestCases = new ArrayList<>();

    public static AmplificationResult buildAmplificationResult(Project project, String testClass, String testMethod) {

        AmplificationResult result = new AmplificationResult(project, testClass);
        TestClassJSON jsonResult = Util.getResultJSON(project, testClass);
        if (jsonResult == null) {
            logger.warn("Json result file not found!");
            jsonResult = new TestClassJSON("", 0, 0, 0, 0, 0);
        }

        String originalTestClassPath = Util.getOriginalTestClassPath(project, testClass);
        VirtualFile originalFile = LocalFileSystem.getInstance().findFileByPath(originalTestClassPath);
        if (originalFile != null) {
            PsiJavaFile psiFile = (PsiJavaFile) PsiManager.getInstance(project).findFile(originalFile);
            result.originalTestCase = new OriginalTestCase(originalTestClassPath, testMethod, psiFile, result);
            result.originalTestCase.psiFile = result.originalTestCase.getFileInProjectSource();
        }

        String amplifiedTestClassPath = Util.getAmplifiedTestClassPath(project, testClass);
        VirtualFile file = LocalFileSystem.getInstance().findFileByPath(amplifiedTestClassPath);
        if (file != null) {
            PsiJavaFile psiFile = (PsiJavaFile) PsiManager.getInstance(project).findFile(file);
            if (psiFile != null) {
                PsiClass psiClass = Arrays.stream(psiFile.getClasses()).filter((PsiClass c) -> c.getQualifiedName().equals(testClass)).findFirst().get();
                PsiMethod[] methods = psiClass.getMethods();
                if (methods.length != jsonResult.getTestCases().size()) {
                    logger.warn("Count of methods found in amplified class: " + methods.length + " does not match with match with count of amplified methods reported: " + jsonResult.getTestCases().size());
                }
                for (PsiMethod method : methods) {
                    result.amplifiedTestCases.add(new AmplifiedTestCase(amplifiedTestClassPath, method.getName(), psiFile, result));
                }
            }
        }

        return result;
    }
}
