package org.testshift.testcube.explore;

import com.intellij.lang.LanguageImportStatements;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.externalSystem.service.project.manage.ProjectDataImportListener;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import eu.stamp_project.dspot.selector.extendedcoverageselector.ClassCoverageMap;
import eu.stamp_project.dspot.selector.extendedcoverageselector.MethodCoverage;
import eu.stamp_project.dspot.selector.extendedcoverageselector.ProjectCoverageMap;
import org.apache.log4j.Level;
import org.testshift.testcube.amplify.StartTestCubeAction;
import org.testshift.testcube.model.AmplifiedTestCase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
        ProjectCoverageMap codeDiscovered = amplifiedTestCase.fullCoverage.getInstructionsProjectCoverageMap().deepClone();
        ProjectCoverageMap addedCoverage = amplifiedTestCase.coverageImprovement.getInstructionImprovement();

        List<PsiMethod> calledUnprocessedMethods = processCalledMethod(json, testMethod, 0, codeDiscovered, addedCoverage);

        for (int level = 1; level <= 3; level++) {
            calledUnprocessedMethods = processFrontier(json, calledUnprocessedMethods, level, codeDiscovered, addedCoverage);
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

    private static List<PsiMethod> processFrontier(ExplorationVisJSON json, List<PsiMethod> calledUnprocessedMethods,
                                                   int level, ProjectCoverageMap codeDiscovered,
                                                   ProjectCoverageMap addedCoverage) {

        List<PsiMethod> methodsCalledThisLevel = new ArrayList<>();

        for (PsiMethod unprocessedMethod : calledUnprocessedMethods) {
            methodsCalledThisLevel.addAll(processCalledMethod(json, unprocessedMethod, level, codeDiscovered, addedCoverage
            ));
        }

        return methodsCalledThisLevel;
    }

    private static List<PsiMethod> processCalledMethod(ExplorationVisJSON json, PsiMethod methodToProcess, int level,
                                                       ProjectCoverageMap codeDiscovered,
                                                       ProjectCoverageMap addedCoverage) {

        List<PsiMethod> calledUnprocessedMethods = new ArrayList<>();

        if (methodToProcess == null) {
            return calledUnprocessedMethods;
        }
        ExplorationVisJSON.Node method = json.new Node()
                .setId(getSig(methodToProcess))
                .setSignature(getSig(methodToProcess))
                .setNodeLevel(level);

        MethodCoverage methodCoverage = new MethodCoverage(Collections.emptyList(), "");
        MethodCoverage addedMethodCoverage = new MethodCoverage(Collections.emptyList(), "");
        if (level > 0) {
            ClassCoverageMap classDiscovered =
                    codeDiscovered.getCoverageForClass(methodToProcess.getContainingClass().getQualifiedName());
            if (classDiscovered == null) {
                methodCoverage = null;
            } else {
                methodCoverage = classDiscovered.getCoverageForMethod(methodToProcess.getName());
            }

            ClassCoverageMap classAddedCoverage =
                    addedCoverage.getCoverageForClass(methodToProcess.getContainingClass().getQualifiedName());
            if (classAddedCoverage == null) {
                addedMethodCoverage = null;
            } else {
                addedMethodCoverage = classAddedCoverage.getCoverageForMethod(methodToProcess.getName());
            }
        }

        PsiCodeBlock body = methodToProcess.getBody();
        if (body == null) {
            logger.info("Method " + methodToProcess.getName() + " with null body!");
        } else {
            PsiStatement[] statements = methodToProcess.getBody().getStatements();

            // TODO let's compare statement length again method coverage line count!
            logger.error("statements.length from PSI " + statements.length);
            if (level > 0 && methodCoverage != null) {
                logger.error("methodCoverage lines " + methodCoverage.lineCoverage.size());
            }

            for (int i = 0; i < statements.length; i++) {
                PsiStatement statement = statements[i];
                ExplorationVisJSON.Line line = json.new Line()
                        .setCode(statement.getText().replaceAll("\n", " "));

                // TODO check if covered
                if (level > 0) {
                    if (methodCoverage != null && methodCoverage.lineCoverage.get(i) > 0) {
                        line.setCovered(true);
                    }
                    if (addedMethodCoverage != null && addedMethodCoverage.lineCoverage.get(i) > 0) {
                        line.setAddCovered(true);
                    }
                }

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

        // TODO remember that we already discovered this method
        // TODO what if a  method is called multiple times??
//        codeDiscovered.getCoverageForClass(methodToProcess.getContainingClass().getQualifiedName())
//                .methodCoverageMap.remove(methodToProcess.getName());

        json.addNode(method);

        return calledUnprocessedMethods;
    }

    private static String getSig(PsiMethod method) {
        return method.getSignature(JavaResolveResult.EMPTY.getSubstitutor()).getName();
    }

}
