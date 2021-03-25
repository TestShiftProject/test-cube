package org.testshift.testcube.explore.model;

import java.util.ArrayList;
import java.util.List;

public class Method {

    private final String signature;
    private List<CodeLine> lines;

    public Method(String signature) {
        this.signature = signature;
    }

    public void addLine(CodeLine line) {
        if (this.lines == null) {
            this.lines = new ArrayList<>();
        }
        this.lines.add(line);
    }

}
