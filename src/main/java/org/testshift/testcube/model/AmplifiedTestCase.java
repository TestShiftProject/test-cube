package org.testshift.testcube.model;

import com.intellij.psi.PsiJavaFile;
import eu.stamp_project.dspot.selector.extendedcoverageselector.CoverageImprovement;

public class AmplifiedTestCase extends TestCase {

    public HtmlCoverageImprovement coverageImprovement;
    public int assertionsAdded;
    public int inputAdded;

    public AmplifiedTestCase(String filePath, String name, PsiJavaFile psiFile, AmplificationResult result,
                             CoverageImprovement coverageImprovement, int assertionsAdded, int inputAdded) {
        super(filePath, name, psiFile, result);
        this.coverageImprovement = new HtmlCoverageImprovement(coverageImprovement);
        this.assertionsAdded = assertionsAdded;
        this.inputAdded = inputAdded;
    }

    public String getDescription() {
        return "<b>Amplified test case</b> '" + name + "'<br><br>" + "Input modifications: " + inputAdded + "<br>" +
               "Assert statements added: " + assertionsAdded + "<br><br>" + coverageImprovement.toHtmlString(result);
    }
}
