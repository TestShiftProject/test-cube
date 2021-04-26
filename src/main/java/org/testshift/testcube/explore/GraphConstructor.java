package org.testshift.testcube.explore;

import com.intellij.ide.actions.searcheverywhere.statistics.SearchEverywhereUsageTriggerCollector;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.rd.util.string.StingUtilKt;
import eu.stamp_project.dspot.selector.extendedcoverageselector.ClassCoverageMap;
import eu.stamp_project.dspot.selector.extendedcoverageselector.MethodCoverage;
import eu.stamp_project.dspot.selector.extendedcoverageselector.ProjectCoverageMap;
import org.testshift.testcube.model.AmplifiedTestCase;

import java.util.*;

public class GraphConstructor {

    private static final Logger logger = Logger.getInstance(GraphConstructor.class);
    private static final Random random = new Random();

    private static final int maxTreeDepth = 5;

    public static Project project;

    public static ExplorationVisJSON constructGraph(AmplifiedTestCase amplifiedTestCase) {

        ExplorationVisJSON json = new ExplorationVisJSON();

        // get code lines of test case
        UnprocessedMethod testMethod = new UnprocessedMethod(amplifiedTestCase.getTestMethod(), -1);
        if (testMethod.psiMethod == null) {
            return json;
        }
        ProjectCoverageMap covered = amplifiedTestCase.fullCoverage.getInstructionsProjectCoverageMap()
                                                                          .deepClone();
        ProjectCoverageMap addedCoverage = amplifiedTestCase.coverageImprovement.getInstructionImprovement();

        List<UnprocessedMethod> calledUnprocessedMethods = processCalledMethod(json, testMethod, 0, covered,
                addedCoverage);

        for (int level = 1; level <= maxTreeDepth; level++) {
            calledUnprocessedMethods = processFrontier(json, calledUnprocessedMethods, level, covered,
                    addedCoverage);
        }

        removeEdgesToRemainingMethods(json,calledUnprocessedMethods);

        return json;
    }

    private static List<UnprocessedMethod> processFrontier(ExplorationVisJSON json,
                                                           List<UnprocessedMethod> calledUnprocessedMethods,
                                                           int level, ProjectCoverageMap covered,
                                                           ProjectCoverageMap addedCoverage) {

        List<UnprocessedMethod> methodsCalledThisLevel = new ArrayList<>();

        for (UnprocessedMethod unprocessedMethod : calledUnprocessedMethods) {
            methodsCalledThisLevel
                    .addAll(processCalledMethod(json, unprocessedMethod, level, covered, addedCoverage));
        }

        return methodsCalledThisLevel;
    }

    /**
     * Handle one called method: adding it's lines to the json, handle coverage, detect method calls and add
     * corresponding edges
     * @param json the {@link ExplorationVisJSON} to edit
     * @param methodToProcess the {@link UnprocessedMethod} to handle
     * @param level depth of the current method in the call tree, 0 = test method
     * @param covered what the test suite covers
     * @param addedCoverage what the new test case covers in addition to the original test suite coverage
     * @return the frontier of methods called by this method
     */
    private static List<UnprocessedMethod> processCalledMethod(ExplorationVisJSON json,
                                                               UnprocessedMethod methodToProcess, int level,
                                                               ProjectCoverageMap covered,
                                                               ProjectCoverageMap addedCoverage) {

        List<UnprocessedMethod> calledUnprocessedMethods = new ArrayList<>();

        if (methodToProcess == null) {
            return calledUnprocessedMethods;
        }

        ExplorationVisJSON.Node method = json.new Node().setId(getSigForId(methodToProcess))
                                                        .setNodeLevel(level)
                                                        .setClassName(methodToProcess.psiMethod.getContainingClass().getQualifiedName())
                                                        .setAddCovered(false);
        if (level == 0) {
            method.setAddCovered(true);
            method.setId("root");
        }
        json.addNode(method);

        // check coverage
        MethodCoverage methodCoverage = new MethodCoverage(Collections.emptyList(), "");
        MethodCoverage addedMethodCoverage = new MethodCoverage(Collections.emptyList(), "");

        if (level > 0) {
            String className = methodToProcess.psiMethod.getContainingClass().getQualifiedName();
            ClassCoverageMap classDiscovered = covered.getCoverageForClass(className);
            if (classDiscovered == null) {
                methodCoverage = null;
            } else {
                methodCoverage = classDiscovered.getCoverageForMethod(methodToProcess.psiMethod.getName());
            }

            ClassCoverageMap classAddedCoverage = addedCoverage.getCoverageForClass(className);
            if (classAddedCoverage == null) {
                addedMethodCoverage = null;
            } else {
                addedMethodCoverage = classAddedCoverage.getCoverageForMethod(methodToProcess.psiMethod.getName());
            }
        }

        // retrieve all lines from method
        Document document = PsiDocumentManager.getInstance(project)
                                              .getDocument(methodToProcess.psiMethod.getContainingFile());
        if (document == null) {
            logger.debug("No document for method " + methodToProcess.psiMethod.getName() + " found.");
            method.setSignature(methodToProcess.psiMethod.getName());
            return calledUnprocessedMethods;
        }
        int methodStartLine = document.getLineNumber(methodToProcess.psiMethod.getTextOffset());

        PsiCodeBlock methodBody = methodToProcess.psiMethod.getBody();
        if (methodBody == null) {
            logger.debug("Body of Method " + methodToProcess.psiMethod.getName() + " is null.");
            method.setSignature(methodToProcess.psiMethod.getName());
            return calledUnprocessedMethods;
        }
        int methodEndLine = document.getLineNumber(methodBody.getRBrace().getTextOffset()) - 1; // exclude brace
        String text = document.getText(new TextRange(document.getLineStartOffset(methodStartLine),
                document.getLineEndOffset(methodEndLine)));
        String[] lines = text.split("\n");

        // assumption: first line is method signature
        int indentSpaces = 0;
        if (lines.length == 0 || lines[0] == null) {
            method.setSignature(methodToProcess.psiMethod.getName());
        } else {
            String ltrim = lines[0].replaceAll("^\\s+","");
            indentSpaces = lines[0].length() - ltrim.length();
            method.setSignature(lines[0].substring(indentSpaces));
        }

        for (int i = 0; i < lines.length - 1; i++) {
            ExplorationVisJSON.Line line = json.new Line().setCode(lines[i + 1].substring(indentSpaces));
            method.lines.add(line);

            if (level > 0) {
                if (methodCoverage != null && methodCoverage.lineCoverage.size() > i && methodCoverage.lineCoverage.get(i) > 0) {
                    line.setCovered(true);
                }
                if (addedMethodCoverage != null && addedMethodCoverage.lineCoverage.size() > i && addedMethodCoverage.lineCoverage.get(i) > 0) {
                    line.setAddCovered(true);
                    method.setAddCovered(true);
                    // TODO we also need to set this on all the methods on the path "to" this method
                }
            }
        }

        // find all calls within the method
        Collection<PsiCall> callingChildrenStatements = PsiTreeUtil
                .findChildrenOfType(methodToProcess.psiMethod, PsiCall.class);

        for (PsiCall call : callingChildrenStatements) {
            PsiMethod calledMethod;
            if (call instanceof PsiConstructorCall) {
                calledMethod = ((PsiConstructorCall) call).resolveConstructor();
            } else {
                calledMethod = call.resolveMethod();
            }

            if (calledMethod != null) {
                // determine line of the call
                int callLine = document.getLineNumber(call.getTextOffset()) - methodStartLine - 1;
                if (method.lines.size() <= callLine) {
                    logger.debug("callLine: " + callLine + " while method has " + method.lines.size() + " lines!");
                } else {
                    ExplorationVisJSON.Line line = method.lines.get(callLine);

                    line.setCallsMethod(true);

                    int calledMethodSalt = random.nextInt();
                    UnprocessedMethod methodToBeProcessed = new UnprocessedMethod(calledMethod, calledMethodSalt);
                    calledUnprocessedMethods.add(methodToBeProcessed);

                    // add edge to called method
                    ExplorationVisJSON.Edge edge = json.new Edge().setSource(method.getId())
                                                                  .setTarget(getSigForId(methodToBeProcessed))
                                                                  .setSourceAnchor(method.lines.indexOf(line) + 1);
                    json.addEdge(edge);
                }
            }
        }

        return calledUnprocessedMethods;
    }

    private static void removeEdgesToRemainingMethods(ExplorationVisJSON json,
                                                      List<UnprocessedMethod> unprocessedMethods) {
        unprocessedMethods.forEach(unprocessedMethod -> {
            json.removeEdgeWithTarget(getSigForId(unprocessedMethod));
        });
    }

    private static String getSig(PsiMethod method) {
        return method.getSignature(JavaResolveResult.EMPTY.getSubstitutor()).toString();
    }

    private static String getSigForId(UnprocessedMethod method) {
        return method.psiMethod.getSignature(JavaResolveResult.EMPTY.getSubstitutor()).toString() + "_" + method.salt;
    }

}
