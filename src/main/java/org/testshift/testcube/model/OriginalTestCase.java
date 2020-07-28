package org.testshift.testcube.model;

import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.search.GlobalSearchScope;

public class OriginalTestCase extends TestCase {

    public OriginalTestCase(String filePath, String name, PsiJavaFile psiFile, AmplificationResult result) {
        super(filePath, name, psiFile, result);
    }

    public PsiJavaFile getFileInProjectSource() {
        return (PsiJavaFile) JavaPsiFacade.getInstance(result.project).findClass(result.testClass, GlobalSearchScope.everythingScope(result.project)).getContainingFile();
    }
}
