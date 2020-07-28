package org.testshift.testcube.model;

import com.intellij.psi.PsiJavaFile;

public abstract class TestCase {

    public TestCase(String filePath, String name, PsiJavaFile psiFile) {
        this.filePath = filePath;
        this.name = name;
        this.psiFile = psiFile;
    }

    public String filePath;
    public String name;
    public PsiJavaFile psiFile;
}
