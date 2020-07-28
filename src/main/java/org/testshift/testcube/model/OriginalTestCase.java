package org.testshift.testcube.model;

import com.intellij.psi.PsiJavaFile;

public class OriginalTestCase extends TestCase {

    public OriginalTestCase(String filePath, String name, PsiJavaFile psiFile) {
        super(filePath, name, psiFile);
    }
}
