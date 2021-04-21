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
    public static Project project;

    public static ExplorationVisJSON constructGraph(AmplifiedTestCase amplifiedTestCase) {

        ExplorationVisJSON json = new ExplorationVisJSON();

        // get code lines of test case
        UnprocessedMethod testMethod = new UnprocessedMethod(amplifiedTestCase.getTestMethod(), -1);
        if (testMethod.psiMethod == null) {
            return json;
        }
        ProjectCoverageMap codeDiscovered = amplifiedTestCase.fullCoverage.getInstructionsProjectCoverageMap()
                                                                          .deepClone();
        ProjectCoverageMap addedCoverage = amplifiedTestCase.coverageImprovement.getInstructionImprovement();

        List<UnprocessedMethod> calledUnprocessedMethods = processCalledMethod(json, testMethod, 0, codeDiscovered,
                addedCoverage);

        for (int level = 1; level <= 3; level++) {
            calledUnprocessedMethods = processFrontier(json, calledUnprocessedMethods, level, codeDiscovered,
                    addedCoverage);
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

    private static List<UnprocessedMethod> processFrontier(ExplorationVisJSON json,
                                                           List<UnprocessedMethod> calledUnprocessedMethods,
                                                           int level, ProjectCoverageMap codeDiscovered,
                                                           ProjectCoverageMap addedCoverage) {

        List<UnprocessedMethod> methodsCalledThisLevel = new ArrayList<>();

        for (UnprocessedMethod unprocessedMethod : calledUnprocessedMethods) {
            methodsCalledThisLevel
                    .addAll(processCalledMethod(json, unprocessedMethod, level, codeDiscovered, addedCoverage));
        }

        return methodsCalledThisLevel;
    }

    /**
     *
     * @param json
     * @param methodToProcess
     * @param level depth of the current method in the call tree, 0 = test method
     * @param codeDiscovered
     * @param addedCoverage
     * @return
     */
    private static List<UnprocessedMethod> processCalledMethod(ExplorationVisJSON json,
                                                               UnprocessedMethod methodToProcess, int level,
                                                               ProjectCoverageMap codeDiscovered,
                                                               ProjectCoverageMap addedCoverage) {

        List<UnprocessedMethod> calledUnprocessedMethods = new ArrayList<>();

        if (methodToProcess == null) {
            return calledUnprocessedMethods;
        }

        ExplorationVisJSON.Node method = json.new Node().setId(getSigForId(methodToProcess))
                                                        .setNodeLevel(level)
                                                        .setAddCovered(false);
        if (level == 0) {
            method.setAddCovered(true);
        }
        json.addNode(method);

        MethodCoverage methodCoverage = new MethodCoverage(Collections.emptyList(), "");
        MethodCoverage addedMethodCoverage = new MethodCoverage(Collections.emptyList(), "");

        if (level > 0) {
            String className = methodToProcess.psiMethod.getContainingClass().getQualifiedName();
            ClassCoverageMap classDiscovered = codeDiscovered.getCoverageForClass(className);
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
            return calledUnprocessedMethods;
        }
        int methodStartLine = document.getLineNumber(methodToProcess.psiMethod.getTextOffset());

        PsiCodeBlock methodBody = methodToProcess.psiMethod.getBody();
        if (methodBody == null) {
            logger.debug("Body of Method " + methodToProcess.psiMethod.getName() + " is null.");
            return calledUnprocessedMethods;
        }
        int methodEndLine = document.getLineNumber(methodBody.getRBrace().getTextOffset()) - 1; // exclude brace
        String text = document.getText(new TextRange(document.getLineStartOffset(methodStartLine),
                document.getLineEndOffset(methodEndLine)));
        String[] lines = text.split("\n");

        // assumption: first line is method signature
        if (lines[0] == null) {
            method.setSignature(methodToProcess.psiMethod.getName());
        } else {
            method.setSignature(lines[0]);
        }

        for (int i = 0; i < lines.length - 1; i++) {
            ExplorationVisJSON.Line line = json.new Line().setCode(lines[i + 1]);
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
                    ExplorationVisJSON.Edge edge = json.new Edge().setSource(method.getId()).setTarget(getSigForId(methodToBeProcessed))
                                                                  .setSourceAnchor(method.lines.indexOf(line) + 1);
                    json.addEdge(edge);
                }
            }
        }


//        PsiCodeBlock body = methodToProcess.psiMethod.getBody();
//
//        if (body == null) {
//            logger.debug("Method " + methodToProcess.psiMethod.getName() + " with null body!");
//        } else {
//            PsiStatement[] statements = methodToProcess.psiMethod.getBody().getStatements();
//
//            // TODO let's compare statement length again method coverage line count!
//            logger.debug("statements.length from PSI " + statements.length);
//            if (level > 0 && methodCoverage != null) {
//                logger.debug("methodCoverage lines " + methodCoverage.lineCoverage.size());
//            }
//
//            for (int i = 0; i < statements.length; i++) {
//                PsiStatement statement = statements[i];
//
//                ExplorationVisJSON.Line line = json.new Line().setCode(statement.getText().replaceAll("\n", " "));
//
//                // find and add the called methods (if any)
//                Collection<PsiCall> callingChildrenStatements = PsiTreeUtil
//                        .findChildrenOfType(statement, PsiCall.class);
//                if (callingChildrenStatements.size() > 0) {
//                    PsiCall call = (PsiCall) callingChildrenStatements.toArray()[0];
//
//                }
//                method.addLine(line);
//            }
//        }


        // TODO remember that we already discovered this method
        // TODO what if a  method is called multiple times??
//        codeDiscovered.getCoverageForClass(methodToProcess.getContainingClass().getQualifiedName())
//                .methodCoverageMap.remove(methodToProcess.getName());

        return calledUnprocessedMethods;
    }

    private static String getSig(PsiMethod method) {
        return method.getSignature(JavaResolveResult.EMPTY.getSubstitutor()).toString();
    }

    private static String getSigForId(UnprocessedMethod method) {
        return method.psiMethod.getSignature(JavaResolveResult.EMPTY.getSubstitutor()).toString() + "_" + method.salt;
    }

}
