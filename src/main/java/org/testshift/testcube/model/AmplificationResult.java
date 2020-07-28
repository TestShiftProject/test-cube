package org.testshift.testcube.model;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import eu.stamp_project.dspot.common.report.output.selector.coverage.json.TestClassJSON;
import org.testshift.testcube.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AmplificationResult {

    private static final Logger logger = Logger.getInstance(AmplificationResult.class);

    private AmplificationResult(Project project, String testClass, String testMethod) {
        this.project = project;
        this.testClass = testClass;
        this.testMethod = testMethod;
    }

    public Project project;
    public String testClass;
    public String testMethod;
    public OriginalTestCase originalTestCase;
    public List<AmplifiedTestCase> amplifiedTestCases = new ArrayList<>();

    public void removeAmplifiedTest(AmplifiedTestCase toRemove) {
        amplifiedTestCases.remove(toRemove);
    }

    public static AmplificationResult buildAmplificationResult(Project project, String testClass, String testMethod) {

        AmplificationResult result = new AmplificationResult(project, testClass, testMethod);
        TestClassJSON jsonResult = Util.getResultJSON(project, testClass);
        if (jsonResult == null) {
            logger.warn("Json result file not found!");
            jsonResult = new TestClassJSON("", 0, 0, 0, 0, 0);
        }

        String originalTestClassPath = Util.getOriginalTestClassPath(project, testClass);
        VirtualFile originalFile = LocalFileSystem.getInstance().findFileByPath(originalTestClassPath);
        if (originalFile != null) {
            PsiJavaFile psiFile = (PsiJavaFile) PsiManager.getInstance(project).findFile(originalFile);
            result.originalTestCase = new OriginalTestCase(originalTestClassPath, testMethod, psiFile);
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
                } else {
                    for (PsiMethod method : methods) {
                        result.amplifiedTestCases.add(new AmplifiedTestCase(amplifiedTestClassPath, method.getName(), psiFile));
                    }
                }
            }
        }

        return result;
    }

    public void copyAmplifiedTestToSuite(AmplifiedTestCase amplifiedTestCase) {
        // todo implement
    }
}
