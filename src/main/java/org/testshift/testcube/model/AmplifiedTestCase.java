package org.testshift.testcube.model;

import com.intellij.psi.PsiJavaFile;
import eu.stamp_project.dspot.selector.extendedcoverageselector.CoverageImprovement;

public class AmplifiedTestCase extends TestCase {

    public CoverageImprovement coverageImprovement;
    public int assertionsAdded;
    public int inputAdded;

    public AmplifiedTestCase(String filePath, String name, PsiJavaFile psiFile, AmplificationResult result,
                             CoverageImprovement coverageImprovement, int assertionsAdded, int inputAdded) {
        super(filePath, name, psiFile, result);
        this.coverageImprovement = coverageImprovement;
        this.assertionsAdded = assertionsAdded;
        this.inputAdded = inputAdded;
    }

    public String getDescription() {
       return "Amplified test case '" + name + "'\n\n" +
               "Input modifications: " + inputAdded + "\n" +
               "Assert statements added: " + assertionsAdded + "\n\n"
               + coverageImprovement;
    }
}
