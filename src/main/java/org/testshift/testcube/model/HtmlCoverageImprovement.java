package org.testshift.testcube.model;

import eu.stamp_project.dspot.selector.extendedcoverageselector.CoverageImprovement;

public class HtmlCoverageImprovement extends CoverageImprovement {

    public HtmlCoverageImprovement(CoverageImprovement coverageImprovement) {
        super(coverageImprovement);
    }


    public String toHtmlString() {
        StringBuilder explanation = new StringBuilder("Coverage improved at<br>");
        this.instructionImprovement.classCoverageMaps.forEach((className, classCoverageMap) -> {
            explanation.append("<a href=class>").append(className).append("</a>:<br>");
            classCoverageMap.methodCoverageMap.forEach((methodName, methodCoverage) -> {
                explanation.append("<a href=method|")
                        .append(className).append("|")
                        .append(methodCoverage.methodDescriptor).append(">")
                        .append(methodName).append("</a><br>");
                int index = -1;
                for (Integer instructionImprovement : methodCoverage.lineCoverage) {
                    index++;
                    if (instructionImprovement <= 0) {
                        continue;
                    }
                    explanation.append("L. <a href=line|")
                            .append(className).append("|")
                            .append(methodName).append("|")
                            .append(methodCoverage.methodDescriptor).append(">")
                            .append(index + 1).append("</a> +")
                            .append(instructionImprovement).append(" instr.").append("<br>");
                }
            });
        });
        explanation.replace(explanation.length() - 1, explanation.length(), "");
        return explanation.toString();
    }
}
