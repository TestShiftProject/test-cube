package org.testshift.testcube.explore;

import com.intellij.psi.PsiMethod;

public class UnprocessedMethod {
    public PsiMethod psiMethod;
    public int salt;

    public UnprocessedMethod(PsiMethod psiMethod, int salt) {
        this.psiMethod = psiMethod;
        this.salt = salt;
    }
}
