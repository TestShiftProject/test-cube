package org.testshift.testcube.explore.model;

public class CodeLine {

    private String code;
    private boolean covered;
    private boolean additionallyCovered;
    private int addCovInstr;
    private boolean leadsToAddCoverage;
    private int leadsToAddCovInstr;
    private String calls;

    public CodeLine() { }

    public CodeLine(String code, boolean covered, boolean additionallyCovered, int addCovInstr, boolean leadsToAddCoverage, int leadsToAddCovInstr, String calls) {
        this.code = code;
        this.covered = covered;
        this.additionallyCovered = additionallyCovered;
        this.addCovInstr = addCovInstr;
        this.leadsToAddCoverage = leadsToAddCoverage;
        this.leadsToAddCovInstr = leadsToAddCovInstr;
        this.calls = calls;
    }

    public CodeLine setCode(String code) {
        this.code = code;
        return this;
    }

    public CodeLine setCovered(boolean covered) {
        this.covered = covered;
        return this;
    }

    public CodeLine setAdditionallyCovered(boolean additionallyCovered) {
        this.additionallyCovered = additionallyCovered;
        return this;
    }

    public CodeLine setAddCovInstr(int addCovInstr) {
        this.addCovInstr = addCovInstr;
        return this;
    }

    public CodeLine setLeadsToAddCoverage(boolean leadsToAddCoverage) {
        this.leadsToAddCoverage = leadsToAddCoverage;
        return this;
    }

    public CodeLine setLeadsToAddCovInstr(int leadsToAddCovInstr) {
        this.leadsToAddCovInstr = leadsToAddCovInstr;
        return this;
    }

    public CodeLine setCalls(String calls) {
        this.calls = calls;
        return this;
    }
}
