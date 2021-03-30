package org.testshift.testcube.explore;

import com.intellij.lang.LanguageImportStatements;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.testshift.testcube.amplify.StartTestCubeAction;
import org.testshift.testcube.model.AmplifiedTestCase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class GraphConstructor {

    private static final Logger logger = Logger.getInstance(GraphConstructor.class);

    public static ExplorationVisJSON constructGraph(AmplifiedTestCase amplifiedTestCase) {

        ExplorationVisJSON json =  new ExplorationVisJSON();

        // get code lines of test case
        PsiMethod testMethod = amplifiedTestCase.getTestMethod();
        if (testMethod == null) {
            return json;
        }

        List<PsiMethod> calledUnprocessedMethods = processCalledMethod(json, testMethod, 0);

        for (int level = 1; level <= 3; level++) {
            calledUnprocessedMethods = processFrontier(json, calledUnprocessedMethods, level);
        }

        // TODO compare with coverage information & stop if all in coverage are found

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
            methodsCalledThisLevel.addAll(processCalledMethod(json, unprocessedMethod, level));
        }

        return methodsCalledThisLevel;
    }

    private static List<PsiMethod> processCalledMethod(ExplorationVisJSON json, PsiMethod methodToProcess, int level) {

        List<PsiMethod> calledUnprocessedMethods = new ArrayList<>();

        if (methodToProcess == null) {
            return calledUnprocessedMethods;
        }
        ExplorationVisJSON.Node method = json.new Node()
                .setId(getSig(methodToProcess))
                .setSignature(methodToProcess.getSignature(PsiSubstitutor.EMPTY).toString())
                .setNodeLevel(level);

        PsiCodeBlock body = methodToProcess.getBody();
        if (body == null) {
            logger.info("Method " + methodToProcess.getName() + " with null body!");
        } else {
            PsiStatement[] statements = methodToProcess.getBody().getStatements();
            for (int i = 0; i < statements.length; i++) {
                PsiStatement statement = statements[i];
                ExplorationVisJSON.Line line = json.new Line()
                        .setCode(statement.getText().replaceAll("\n", " "));

                // find and add the called methods (if any)
                Collection<PsiCall> callingChildrenStatements = PsiTreeUtil.findChildrenOfType(statement, PsiCall.class);
                if (callingChildrenStatements.size() > 0) {
                    PsiCall call = (PsiCall) callingChildrenStatements.toArray()[0];
                    PsiMethod calledMethod;
                    if (call instanceof PsiConstructorCall)  {
                        calledMethod = ((PsiConstructorCall) call).resolveConstructor();
                    } else {
                        calledMethod = call.resolveMethod();
                    }
                    if (calledMethod != null) {
                        line.setCallsMethod(true).setCalledMethod(getSig(calledMethod));
                        calledUnprocessedMethods.add(calledMethod);

                        // add edge to called method
                        ExplorationVisJSON.Edge edge =
                                json.new Edge().setSource(method.getId()).setTarget(getSig(calledMethod))
                                                                      .setSourceAnchor(i + 1);
                        json.addEdge(edge);
                    }
                }
                method.addLine(line);
            }
        }

        json.addNode(method);

        return calledUnprocessedMethods;
    }

    private static String getSig(PsiMethod method) {
        return method.getSignature(PsiSubstitutor.EMPTY).toString();
    }

}
