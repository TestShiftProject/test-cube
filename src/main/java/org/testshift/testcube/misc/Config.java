package org.testshift.testcube.misc;

import java.io.File;

public class Config {
    static final String OUTPUT_PATH_DSPOT = File.separator + "target" + File.separator + "dspot" + File.separator + "output";
    static final String OUTPUT_PATH_TESTCUBE = File.separator + "test-cube";
    public static final String AMPLIFIERS_FAST_LITERAL = "FastLiteralAmplifier";
    public static final String AMPLIFIERS_ALL_SLOW_LITERAL = "MethodDuplicationAmplifier,MethodRemove,FastLiteralAmplifier,MethodAdderOnExistingObjectsAmplifier,ReturnValueAmplifier,StringLiteralAmplifier,NumberLiteralAmplifier,BooleanLiteralAmplifier,CharLiteralAmplifier,AllLiteralAmplifiers,NullifierAmplifier,ArrayAmplifier";
    public static final String AMPLIFIERS_ALL = "MethodDuplicationAmplifier,MethodRemove,FastLiteralAmplifier,MethodAdderOnExistingObjectsAmplifier,ReturnValueAmplifier,NullifierAmplifier,ArrayAmplifier";
}
