package org.testshift.testcube.explore;

import com.intellij.lang.LanguageImportStatements;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.testshift.testcube.explore.model.CodeLine;
import org.testshift.testcube.explore.model.TestCase;
import org.testshift.testcube.model.AmplifiedTestCase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class GraphConstructor {

    public static ExplorationVisJSON constructGraph(AmplifiedTestCase amplifiedTestCase) {

        ExplorationVisJSON json =  new ExplorationVisJSON();

        List<PsiMethod> calledUnprocessedMethods = new ArrayList<>();

        // get code lines of test case
        PsiMethod testMethod = amplifiedTestCase.getTestMethod();
        if (testMethod == null) {
            return json;
        }
        ExplorationVisJSON.Node testCase =
                json.new Node().setId("root").setSignature(testMethod.getSignature(PsiSubstitutor.EMPTY).toString());

        PsiStatement[] statements = testMethod.getBody().getStatements();
        for (int i = 0; i < statements.length; i++) {
            PsiStatement statement = statements[i];
            ExplorationVisJSON.Line line = json.new Line().setCode(statement.getText());

            // find and add the called methods (if any)
            Collection<PsiCall> callingChildrenStatements = PsiTreeUtil.findChildrenOfType(statement, PsiCall.class);
            if (callingChildrenStatements.size() > 0) {
                PsiMethod calledMethod = ((PsiCall) callingChildrenStatements.toArray()[0]).resolveMethod();
                if (calledMethod != null) {
                    line.setCallsMethod(true).setCalledMethod(getSig(calledMethod));
                    calledUnprocessedMethods.add(calledMethod);

                    // add edge to called method
                    ExplorationVisJSON.Edge edge = json.new Edge().setSource("root").setTarget(getSig(calledMethod))
                                                                  .setSourceAnchor(i + 1);
                }
            }
            testCase.addLine(line);
        }

        json.addNode(testCase);

        for (int level = 1; level <= 3; level++) {
            processFrontier(json,calledUnprocessedMethods,level);
        }

        // get each method:
        // check that it is in coverage data (print error if not)
        // get all code lines (already get method information too?)
        // check if any code line *is* add. covered
        // add code lines / method  to 'frontier'

        // go through frontier until level 3
        // check after end of every level if all covered methods are found

        // construct connections

        return json;
    }

    private static List<PsiMethod> processFrontier(ExplorationVisJSON json, List<PsiMethod> calledUnprocessedMethods, int level) {

        List<PsiMethod> methodsCalledThisLevel = new ArrayList<>();

        for (PsiMethod unprocessedMethod : calledUnprocessedMethods) {
            methodsCalledThisLevel.addAll(processCalledMethod(json,unprocessedMethod));
        }

        return methodsCalledThisLevel;
    }

    private static List<PsiMethod> processCalledMethod(ExplorationVisJSON json, PsiMethod methodToProcess) {

        List<PsiMethod> calledUnprocessedMethods = new ArrayList<>();

        if (methodToProcess == null) {
            return calledUnprocessedMethods;
        }
        ExplorationVisJSON.Node method =
                json.new Node().setId(getSig(methodToProcess)).setSignature(methodToProcess.getSignature(PsiSubstitutor.EMPTY).toString());

        PsiStatement[] statements = methodToProcess.getBody().getStatements();
        for (int i = 0; i < statements.length; i++) {
            PsiStatement statement = statements[i];
            ExplorationVisJSON.Line line = json.new Line().setCode(statement.getText());

            // find and add the called methods (if any)
            if (statement instanceof PsiCall) {
                PsiMethod calledMethod = ((PsiCall) statement).resolveMethod();
                line.setCallsMethod(true).setCalledMethod(getSig(calledMethod));
                calledUnprocessedMethods.add(calledMethod);

                // add edge to called method
                ExplorationVisJSON.Edge edge =
                        json.new Edge().setSource(getSig(methodToProcess)).setTarget(getSig(calledMethod)).setSourceAnchor(i + 1);
            }
            method.addLine(line);
        }

        json.addNode(method);

        return calledUnprocessedMethods;
    }

    private static String getSig(PsiMethod method) {
        return method.getSignature(PsiSubstitutor.EMPTY).toString();
    }

}
