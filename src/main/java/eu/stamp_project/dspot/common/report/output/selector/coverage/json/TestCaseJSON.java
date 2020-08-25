package eu.stamp_project.dspot.common.report.output.selector.coverage.json;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 31/07/17
 */
public class TestCaseJSON {

    private final String name;
    private final int nbAssertionAdded;
    private final int nbInputAdded;
    private final int instructionCovered;
    private final int instructionTotal;

    public TestCaseJSON(String name, int nbAssertionAdded, int nbInputAdded, int instructionCovered, int instructionTotal) {
        this.name = name;
        this.nbAssertionAdded = nbAssertionAdded;
        this.nbInputAdded = nbInputAdded;
        this.instructionCovered = instructionCovered;
        this.instructionTotal = instructionTotal;
    }

    public String getName() {
        return name;
    }

    public int getNbAssertionAdded() {
        return nbAssertionAdded;
    }

    public int getNbInputAdded() {
        return nbInputAdded;
    }

    public int getInstructionCovered() {
        return instructionCovered;
    }

    public int getInstructionTotal() {
        return instructionTotal;
    }
}
