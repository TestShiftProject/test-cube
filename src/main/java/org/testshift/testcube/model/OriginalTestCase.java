package org.testshift.testcube.model;

import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.search.GlobalSearchScope;

public class OriginalTestCase extends TestCase {

    public OriginalTestCase(String filePath, String name, PsiJavaFile psiFile) {
        super(filePath, name, psiFile);
    }

    public PsiJavaFile getFileInProjectSource(Project project, String testClass) {
        //return (PsiJavaFile) FilenameIndex.getFilesByName(project, testClass, GlobalSearchScope.projectScope(project))[0];
        return (PsiJavaFile) JavaPsiFacade.getInstance(project).findClass(testClass, GlobalSearchScope.everythingScope(project)).getContainingFile();
    }
}
