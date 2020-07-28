package org.testshift.testcube.model;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.Nullable;
import org.testshift.testcube.inspect.AmplificationResultWindow;

import java.util.Arrays;

public abstract class TestCase {

    private static final Logger logger = Logger.getInstance(AmplificationResultWindow.class);

    public TestCase(String filePath, String name, PsiJavaFile psiFile) {
        this.filePath = filePath;
        this.name = name;
        this.psiFile = psiFile;
    }

    public String filePath;
    public String name;
    public PsiJavaFile psiFile;

    @Nullable
    public PsiMethod getTestMethod(String testClass) {
        PsiClass psiClass = Arrays.stream(psiFile.getClasses()).filter((PsiClass c) -> c.getQualifiedName().equals(testClass)).findFirst().get();
        PsiMethod[] methods = psiClass.findMethodsByName(name, false);
        if (methods.length != 1) {
            logger.error("More than one method named '" + name + "' found in class '" + psiFile.getName() + "'");
            return null;
        } else {
            return methods[0];
        }
    }
}
