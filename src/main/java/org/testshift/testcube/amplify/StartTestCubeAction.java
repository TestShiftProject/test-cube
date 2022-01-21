package org.testshift.testcube.amplify;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.externalSystem.model.ProjectSystemId;
import com.intellij.openapi.externalSystem.util.ExternalSystemApiUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ProjectRootManager;
import eu.stamp_project.dspot.common.report.output.selector.extendedcoverage.json.TestClassJSON;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.testshift.testcube.icons.TestCubeIcons;
import org.testshift.testcube.inspect.InspectTestCubeResultsAction;
import org.testshift.testcube.misc.Config;
import org.testshift.testcube.misc.TestCubeNotifier;
import org.testshift.testcube.misc.Util;
import org.testshift.testcube.settings.AppSettingsState;
import org.testshift.testcube.settings.AskJavaPathDialogWrapper;
import org.testshift.testcube.settings.AskMavenHomeDialogWrapper;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This action is responsible for running DSpot on a particular test method of the currently active project.
 * The test class and method are passed in the initialization, i.e. when a new action is created for the gutter icon
 * next to the test method.
 */
public class StartTestCubeAction extends AnAction {

    private static final Logger logger = Logger.getInstance(StartTestCubeAction.class);

    private String testClass;
    private String testMethod;
    private String moduleRootPath;

    public StartTestCubeAction() {
    }

    public StartTestCubeAction(@Nullable @Nls(capitalization = Nls.Capitalization.Title) String text,
                               @NotNull String testClass, @Nullable("null means whole class") String testMethod,
                               String moduleRootPath) {
        super(text, "Improves the selected test case by applying amplification operators", TestCubeIcons.AMPLIFY_TEST);
        this.testClass = testClass;
        this.testMethod = testMethod;
        this.moduleRootPath = moduleRootPath;
    }

    @Override
    public void update(AnActionEvent e) {
        // Set the availability based on whether a project is open
        Project project = e.getProject();
        e.getPresentation().setEnabledAndVisible(project != null);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {

        Project currentProject = event.getProject();
        try {
            runDSpot(currentProject);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void runDSpot(Project currentProject) throws IOException, InterruptedException {

        IdeaPluginDescriptor testCubePlugin = PluginManagerCore.getPlugin(PluginId.getId("org.testshift.testcube"));
        if (testCubePlugin != null) {
            testCubePlugin.getPluginClassLoader();
        }

        DSpotStartConfiguration configuration = new DSpotStartConfiguration(currentProject, moduleRootPath);
        PrettifierStartConfiguration prettifierConfiguration = new PrettifierStartConfiguration(currentProject,
                                                                                                moduleRootPath);


        Task.Backgroundable dspotTask = new Task.Backgroundable(currentProject, "Amplifying test", true) {

            public void run(@NotNull ProgressIndicator indicator) {
                // run amplification
                spawnDSpotProcess(configuration, currentProject);
                // prettify generated test cases
                spawnDSpotProcess(prettifierConfiguration, currentProject);
                // popup about completion
                notifyDSpotFinished(currentProject);
            }
        };

        BackgroundableProcessIndicator processIndicator = new BackgroundableProcessIndicator(dspotTask);
        ProgressManager.getInstance().runProcessWithProgressAsynchronously(dspotTask, processIndicator);
    }

    /**
     * Sets up and starts the subprocess that runs DSpot or the prettifier.
     * @param configuration the {@link DSpotStartConfiguration} to use, pass a
     * {@link PrettifierStartConfiguration} to run the prettifier.
     * @param currentProject the currently active project.
     */
    private void spawnDSpotProcess(DSpotStartConfiguration configuration, Project currentProject) {
        List<String> dSpotStarter = configuration.getCommandLineOptions(testClass, testMethod);

        ProcessBuilder processBuilder = prepareEnvironmentForSubprocess(dSpotStarter, currentProject, configuration);
        try {
            Process p = processBuilder.start();

            File dSpotTerminalOutput = new File(
                    Util.getTestCubeOutputPath(currentProject) + File.separator + "terminal_output_dspot.txt");

            // write the output to the console and the file simultaneously, while the project is running
            try (BufferedWriter writer =
                         new BufferedWriter(new FileWriter(dSpotTerminalOutput, configuration.appendToLog()))) {
                InputStream is = p.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                for (String line = br.readLine(); line != null; line = br.readLine()) {
                    System.out.println(line);
                    writer.write(line);
                    writer.newLine();
                }
            }
            p.waitFor();
            System.out.println(p.exitValue());

        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }

        // try to avoid the newly generated files not being found by IntelliJ
        Util.sleepAndRefreshProject(currentProject);
    }

    /**
     * Prepares the environment variables and directories to start the DSpot process.
     */
    private ProcessBuilder prepareEnvironmentForSubprocess(List<String> dSpotStarter, Project currentProject,
                                                           DSpotStartConfiguration configuration) {
        ProcessBuilder pb = new ProcessBuilder(dSpotStarter);

        // clean output directory
        // todo close open amplification result windows or split output into different directories
        try {
            File outputDirectory = new File(configuration.getOutputDirectoryToClean());
            if (outputDirectory.exists()) {
                FileUtils.cleanDirectory(outputDirectory);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Create the temporary directories to save the output in
        File workdir = new File(Util.getTestCubeOutputPath(currentProject) + File.separator + "workdir");
        if (!workdir.exists()) {
            if (!workdir.mkdirs()) {
                logger.error("Could not create workdir output directory!");
            }
        }
//        File workdirTarget = new File(workdir.getPath() + File.separator + "target" + File.separator + "dspot");
//        if (!workdirTarget.exists()) {
//            if (!workdirTarget.mkdirs()) {
//                logger.error("Could not create workdir/target/dspot output directory!");
//            }
//        }
        pb.directory(workdir);

        pb.redirectErrorStream(true);
        pb.environment().put("MAVEN_HOME", AppSettingsState.getInstance().mavenHome);

        // TODO check: is this subsumed by creating the workdir?
//        File outputDir = new File(Util.getTestCubeOutputPath(currentProject));
//        if (!outputDir.exists()) {
//            if (!outputDir.mkdirs()) {
//                logger.error("Could not create Test Cube output directory!");
//            }
//        }

        return pb;
    }

    /**
     * Prettifies the amplified test cases by running the dspot-prettifier over them.
     */
    private void prettifyAmplifiedTests(Project currentProject) {
        PrettifierStartConfiguration configuration = new PrettifierStartConfiguration(currentProject, moduleRootPath);

    }

    /**
     * Creates a notification balloon reporting that DSpot has completed.
     * Offers actions to inspect the test cases or inspect the terminal output.
     * @param currentProject
     */
    private void notifyDSpotFinished(Project currentProject) {
        TestCubeNotifier notifier = new TestCubeNotifier();
        TestClassJSON result = Util.getResultJSON(currentProject, testClass);
        if (result == null || result.getTestCases() == null) {
            notifier.notify(currentProject, "No new test cases found. Please try amplifying another test case!",
                            new InspectDSpotTerminalOutputAction());
        } else {
            int amplifiedTestCasesCount = result.getTestCases().size();

            if (amplifiedTestCasesCount == 0) {
                notifier.notify(currentProject, "Could find no new test cases through amplification.");
            } else {
                notifier.notify(currentProject,
                                "Test Cube found " + amplifiedTestCasesCount + " amplified test cases.",
                                new InspectTestCubeResultsAction(currentProject, testClass, testMethod),
                                new InspectDSpotTerminalOutputAction());
            }
        }
    }
}
