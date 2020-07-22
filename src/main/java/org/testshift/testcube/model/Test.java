package org.testshift.testcube.model;

public abstract class Test {

    public Test(String filePath) {
        this.filePath = filePath;
    }

    public String filePath;
}
