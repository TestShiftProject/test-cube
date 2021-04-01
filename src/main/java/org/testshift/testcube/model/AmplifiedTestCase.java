package org.testshift.testcube.model;

import com.intellij.psi.PsiJavaFile;
import eu.stamp_project.dspot.selector.extendedcoverageselector.CoverageImprovement;
import eu.stamp_project.dspot.selector.extendedcoverageselector.ExtendedCoverage;

public class AmplifiedTestCase extends TestCase {

    public final HtmlCoverageImprovement coverageImprovement;
    public final int assertionsAdded;
    public final int inputAdded;
    public final ExtendedCoverage fullCoverage;

    public AmplifiedTestCase(String filePath, String name, PsiJavaFile psiFile, AmplificationResult result,
                             CoverageImprovement coverageImprovement, int assertionsAdded, int inputAdded,
                             ExtendedCoverage fullCoverage) {
        super(filePath, name, psiFile, result);
        this.coverageImprovement = new HtmlCoverageImprovement(coverageImprovement);
        this.assertionsAdded = assertionsAdded;
        this.inputAdded = inputAdded;
        this.fullCoverage = fullCoverage;
    }

    public String getDescription() {
        return "<b>Amplified test case</b> '" + name + "'<br><br>" + "Input modifications: " + inputAdded + "<br>" +
               "Assert statements added: " + assertionsAdded + "<br><br>" + coverageImprovement.toHtmlString(result);
    }
}
