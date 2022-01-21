package org.testshift.testcube.amplify;

import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.externalSystem.model.ProjectSystemId;
import com.intellij.openapi.externalSystem.util.ExternalSystemApiUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ProjectRootManager;
import org.jetbrains.annotations.NotNull;
import org.testshift.testcube.misc.Config;
import org.testshift.testcube.misc.Util;
import org.testshift.testcube.settings.AppSettingsState;
import org.testshift.testcube.settings.AskJavaPathDialogWrapper;
import org.testshift.testcube.settings.AskMavenHomeDialogWrapper;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class houses all configuration data needed to start DSpot.
 * (See subclass {@link PrettifierStartConfiguration} for the data needed for the DSpot prettifier.)
 *
 * Also hosts the methods that determine the configuration values from the user / IDE input.
 */
public class DSpotStartConfiguration {

    private static final Logger logger = Logger.getInstance(DSpotStartConfiguration.class);

    public final Project currentProject;

    public String relativePathToClasses;
    public String relativePathToTestClasses;
    public String automaticBuilder;
    public String targetModule = "";
    public String javaBinPath;
    public String dspotJarPath;

    public boolean isGradle;
    public boolean isMaven;

    public DSpotStartConfiguration(Project currentProject, String moduleRootPath) {
        this.currentProject = currentProject;
        setJavaJDKPathAccordingToProject();
        determineBuilderAndSetCorrespondingOptions();
        findTargetModule(moduleRootPath);
        setJavaBinPath();
        setDSpotJarPath();
    }

    /**
     * @return Whether the saved log should be appended to the existing one or cleared & restarted.
     */
    public boolean appendToLog() {
        return false;
    }

    /**
     * This method uses the SDK which is configured for the provided project to set the path to the Java executable
     * which will be used to run DSpot.
     * If no SDK is configured, is asks the user with a dialog until they provide a valid
     * ({@link AskJavaPathDialogWrapper#setJavaPathIfValid()}) path.
     */
    private void setJavaJDKPathAccordingToProject() {
        Sdk projectSdk = ProjectRootManager.getInstance(currentProject).getProjectSdk();
        if (projectSdk != null) {
            AppSettingsState.getInstance().javaJDKPath = projectSdk.getHomePath();
        }
        boolean pathInvalid = AppSettingsState.getInstance().javaJDKPath.isEmpty();
        while (pathInvalid) {
            AskJavaPathDialogWrapper dialog = new AskJavaPathDialogWrapper();
            dialog.showAndGet();
            pathInvalid = dialog.setJavaPathIfValid();
        }
    }

    private void determineBuilderAndSetCorrespondingOptions() {
        // check if Gradle or Maven
        isGradle = ExternalSystemApiUtil.isExternalSystemAwareModule(new ProjectSystemId("GRADLE"),
                                                                             ModuleManager.getInstance(currentProject)
                                                                                          .getModules()[0]);
        isMaven = ExternalSystemApiUtil.isExternalSystemAwareModule(new ProjectSystemId("Maven"),
                                                                            ModuleManager.getInstance(currentProject)
                                                                                         .getModules()[0]);

        // Determine builder-specific configuration variables
        if (isMaven) {
            relativePathToClasses = "target" + File.separator + "classes" + File.separator;
            relativePathToTestClasses = "target" + File.separator + "test-classes" + File.separator;
            automaticBuilder = "Maven";
        } else {
            relativePathToClasses = "bin" + File.separator + "main" + File.separator;
            relativePathToTestClasses = "bin" + File.separator + "test" + File.separator;
            automaticBuilder = "Gradle";
            if (!isGradle) {
                logger.info("Neither Gradle nor Maven");
            }
        }

        boolean mavenHomeValid = isMaven && AppSettingsState.getInstance().mavenHome.isEmpty();
        while (mavenHomeValid) {
            AskMavenHomeDialogWrapper dialog = new AskMavenHomeDialogWrapper();
            dialog.showAndGet();
            mavenHomeValid = dialog.setMavenHomeIfValid();
        }
    }

    private void findTargetModule(String moduleRootPath) {
        if (isMaven) {
            targetModule = moduleRootPath.replace(currentProject.getBasePath() + "/", "");
        }
    }

    private void setJavaBinPath() {
        String javaHome = AppSettingsState.getInstance().javaJDKPath;
        javaBinPath = javaHome + File.separator + "bin" + File.separator + "java";
    }

    protected void setDSpotJarPath() {
        String pluginPath = PathManager.getPluginsPath();
        dspotJarPath = pluginPath + File.separator + "test-cube" + File.separator + "lib" + File.separator +
                           "dspot-3.2.1-SNAPSHOT-jar-with-dependencies.jar";
    }

    /**
     * @return the path to the output directory of this dspot run. Will be cleaned before the run.
     */
    public String getOutputDirectoryToClean() {
        return Util.getDSpotOutputPath(currentProject);
    }

    // @formatter:off
    public List<String> getCommandLineOptions(String testClass, String testMethod) {
         List<String> commandLine = new ArrayList<>(Arrays.asList(
                javaBinPath, "-jar", dspotJarPath,
                "--absolute-path-to-project-root", currentProject.getBasePath(),
                "--relative-path-to-classes", relativePathToClasses,
                "--relative-path-to-test-classes", relativePathToTestClasses,
                "--test-criterion", "ExtendedCoverageSelector",
                "--input-ampl-distributor", "RandomInputAmplDistributor",
                "--test", testClass,
                // TODO handlle null on testMethod
                "--test-cases", testMethod,
                "--output-directory", Util.getDSpotOutputPath(currentProject),
                "--amplifiers", Config.AMPLIFIERS_ALL,
                "--max-test-amplified", "25",
                "--automatic-builder", automaticBuilder,
                //"--generate-new-test-class",
                //"--keep-original-test-methods",
                "--verbose",
                "--dev-friendly",
                "--clean",
                "--with-comment", "None"));

         addTargetModuleIfNeeded(commandLine);
         return commandLine;
    }
    // @formatter:on

    protected void addTargetModuleIfNeeded(List<String> commandLineOptions) {
        @NotNull Module[] modules = ModuleManager.getInstance(currentProject).getModules();
        if (modules.length > 1) {
            commandLineOptions.add("--target-module");
            commandLineOptions.add(targetModule);
        }
    }
}
