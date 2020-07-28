package org.testshift.testcube.model;

import com.intellij.psi.PsiJavaFile;

public class AmplifiedTestCase extends TestCase {

    public AmplifiedTestCase(String filePath, String name, PsiJavaFile psiFile, AmplificationResult result) {
        super(filePath, name, psiFile, result);
    }
}
