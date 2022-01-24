package org.testshift.testcube.amplify;

import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.project.Project;
import org.testshift.testcube.misc.Util;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PrettifierStartConfiguration extends DSpotStartConfiguration {

    /**
     * Fully qualified name of the test class to be prettified.
     */
    public String testClass;

    public PrettifierStartConfiguration(Project currentProject, String moduleRootPath) {
        super(currentProject, moduleRootPath);
    }

    @Override
    public boolean appendToLog() {
        return true;
    }

    @Override
    protected void setDSpotJarPath() {
        String pluginPath = PathManager.getPluginsPath();
        dspotJarPath = pluginPath + File.separator + "test-cube" + File.separator + "lib" + File.separator +
                       "dspot-prettifier-3.2.1-SNAPSHOT-jar-with-dependencies.jar";
    }

    @Override
    public String getOutputDirectoryToClean() {
        return Util.getDSpotOutputPath(currentProject);
    }

    // @formatter:off
    @Override
    public List<String> getCommandLineOptions(String testClass, String testMethod) {
        List<String> commandLine = new ArrayList<>(Arrays.asList(
                javaBinPath, "-jar",
                dspotJarPath,
                "--absolute-path-to-project-root", currentProject.getBasePath(),
                "--relative-path-to-classes", relativePathToClasses,
                "--relative-path-to-test-classes", relativePathToTestClasses,
                "--path-to-amplified-test-class", Util.getAmplifiedTestClassPathToPrettify(currentProject, testClass),
                "--test", testClass,
                "--output-directory", Util.getDSpotOutputPath(currentProject),
                "--path-to-dspot-reports", Util.getOutputSavePath(currentProject),
                "--automatic-builder", automaticBuilder,
                "--verbose",
                "--with-comment", "None",
                "--remove-redundant-casts",
                "--generate-descriptions",
                "--rename-local-variables", "SimpleVariableRenamer",
                "--rename-test-methods", "ImprovedCoverageTestRenamer",
                "--apply-extended-coverage-minimizer",
                "--apply-general-minimizer",
                "--filter-dev-friendly",
                "--prioritize-most-coverage"));

        addTargetModuleIfNeeded(commandLine);
        return commandLine;
    }
    // @formatter:on
}
