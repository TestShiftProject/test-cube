package org.testshift.testcube.model;

import com.intellij.openapi.editor.Document;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.search.GlobalSearchScope;
import eu.stamp_project.dspot.selector.extendedcoverageselector.CoverageImprovement;
import org.testshift.testcube.misc.Util;

import java.util.Arrays;
import java.util.Optional;

public class HtmlCoverageImprovement extends CoverageImprovement {

    public HtmlCoverageImprovement(CoverageImprovement coverageImprovement) {
        super(coverageImprovement);
    }


    public String toHtmlString(AmplificationResult amplificationResult) {

        StringBuilder explanation = new StringBuilder("This test case improves the coverage in these " +
                                                      "classes/methods/lines: <br>(Click on the green links to see " +
                                                      "these lines within the class)<br>");
        this.instructionImprovement.classCoverageMaps.forEach((className, classCoverageMap) -> {
            explanation.append("<a href=class>").append(className).append("</a>:<br>");
            classCoverageMap.methodCoverageMap.forEach((methodName, methodCoverage) -> {
                explanation.append("<a href=method|")
                        .append(className).append("|")
                        .append(methodCoverage.methodDescriptor).append(">")
                        .append(methodName).append("</a><br>");

                boolean wrotelines = false;

                PsiClass psiClass = JavaPsiFacade.getInstance(amplificationResult.project)
                                                 .findClass(className, GlobalSearchScope.allScope(amplificationResult.project));
                if (psiClass != null) {
                    Document document = PsiDocumentManager
                            .getInstance(amplificationResult.project).getDocument(psiClass.getContainingFile());
                    Optional<PsiMethod> method = Arrays.stream(psiClass.getMethods()).filter(psiMethod -> Util
                            .matchMethodNameAndDescriptor(psiMethod, methodName, methodCoverage.methodDescriptor))
                                                       .findAny();
                    if (method.isPresent() && document != null) {
                        int methodLine = document.getLineNumber(method.get().getTextOffset());
                        int index = -1;
                        for (Integer instructionImprovement : methodCoverage.lineCoverage) {
                            index++;
                            if (instructionImprovement <= 0) {
                                continue;
                            }
                            explanation.append("L. <a href=line|")
                                       .append(className).append("|")
                                       .append(methodName).append("|")
                                       .append(methodCoverage.methodDescriptor).append("|")
                                       .append("fileLine").append(">")
                                       .append(methodLine + index + 2).append("</a> +")
                                       .append(instructionImprovement).append(" instr.").append("<br>");
                        }
                        wrotelines = true;
                    }
                }

                if (!wrotelines) {
                    int index = -1;
                    for (Integer instructionImprovement : methodCoverage.lineCoverage) {
                        index++;
                        if (instructionImprovement <= 0) {
                            continue;
                        }
                        explanation.append("L. <a href=line|")
                                   .append(className).append("|")
                                   .append(methodName).append("|")
                                   .append(methodCoverage.methodDescriptor).append("|")
                                   .append("methodLine").append(">")
                                   .append(index + 1).append("</a> +")
                                   .append(instructionImprovement).append(" instr.").append("<br>");
                    }
                }
            });
        });
        explanation.replace(explanation.length() - 1, explanation.length(), "");
        return explanation.toString();
    }
}
