package org.testshift.testcube.model;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.Nullable;
import org.testshift.testcube.inspect.AmplificationResultWindow;
import org.testshift.testcube.inspect.ResultWithCFGWindow;
import org.testshift.testcube.misc.Util;

import java.util.Arrays;
import java.util.Set;

public class GeneratedTestCase{
    private static final Logger logger = Logger.getInstance(ResultWithCFGWindow.class);
    public String filePath;
    private String name;
    private PsiMethod method;
    public PsiJavaFile psiFile;
    public Set<String> newCoveredLine;
    public Set<Util.Branch> newCovredBranch;
    public int assertionsAdded;
    public int inputAdded;

    public GeneratedTestCase(String filePath, String name, PsiMethod method, PsiJavaFile psiFile, int assertionsAdded
            , int inputAdded, Set<String> newCoveredLine, Set<Util.Branch> newCovredBranch) {
        this.filePath = filePath;
        this.name = name;
        this.method = method;
        this.psiFile = psiFile;
        this.assertionsAdded = assertionsAdded;
        this.inputAdded = inputAdded;
        this.newCoveredLine = newCoveredLine;
        this.newCovredBranch = newCovredBranch;
    }

    public PsiMethod getMethod() {
        return method;
    }

    public String getDescription() {
        return "<b>Generated test case</b> '" + name + "'<br><br>" + "Input modifications: " + inputAdded + "<br>" +
               "Assert statements added: " + assertionsAdded + "<br><br>" + "New branch covered: " + newCovredBranch.size() + "<br><br>"
                + "New line covered: " + newCoveredLine.size();
    }
}
