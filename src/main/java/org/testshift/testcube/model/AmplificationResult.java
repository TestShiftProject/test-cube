package org.testshift.testcube.model;

import com.intellij.openapi.project.Project;

import java.util.ArrayList;
import java.util.List;

public class AmplificationResult {

    public AmplificationResult(Project project, String testClass, String testMethod, OriginalTest originalTest, List<AmplifiedTest> amplifiedTests) {
        this.project = project;
        this.testClass = testClass;
        this.testMethod = testMethod;
        this.originalTest = originalTest;
        this.amplifiedTests = amplifiedTests;
    }

    public Project project;
    public String testClass;
    public String testMethod;
    public OriginalTest originalTest;
    public List<AmplifiedTest> amplifiedTests = new ArrayList<>();

    public void removeAmplifiedTest(AmplifiedTest toRemove) {
        amplifiedTests.remove(toRemove);
    }
}
