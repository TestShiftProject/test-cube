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


        Sdk projectSdk = ProjectRootManager.getInstance(currentProject).getProjectSdk();

        if (projectSdk != null) {
            AppSettingsState.getInstance().java8Path = projectSdk.getHomePath();
        }

        if (AppSettingsState.getInstance().java8Path.isEmpty()) {
            AskJavaPathDialogWrapper dialog = new AskJavaPathDialogWrapper();
            dialog.showAndGet();
            boolean pathValid = dialog.setJavaPathIfValid();
            // todo handle non valid
        }

        // check if Gradle or Maven
        boolean isGradle =
                ExternalSystemApiUtil.isExternalSystemAwareModule(new ProjectSystemId("GRADLE"),
                        ModuleManager.getInstance(currentProject).getModules()[0]);
        boolean isMaven =
                ExternalSystemApiUtil.isExternalSystemAwareModule(new ProjectSystemId("Maven"),
                        ModuleManager.getInstance(currentProject).getModules()[0]);

        String relativePathToClasses = "";
        String relativePathToTestClasses = "";
        String automaticBuilder = "";
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

        if (isMaven && AppSettingsState.getInstance().mavenHome.isEmpty()) {
            AskMavenHomeDialogWrapper dialog = new AskMavenHomeDialogWrapper();
            dialog.showAndGet();
            boolean mavenHomeValid = dialog.setMavenHomeIfValid();
            // todo handle non valid
        }

        String javaHome = AppSettingsState.getInstance().java8Path;
        String javaBin = javaHome + File.separator + "bin" + File.separator + "java";

        String pluginPath = PathManager.getPluginsPath();
        String dSpotPath =  pluginPath + File.separator + "test-cube" + File.separator + "lib" + File.separator + "dspot-3.1.1-SNAPSHOT-jar-with-dependencies.jar";

        String finalRelativePathToClasses = relativePathToClasses;
        String finalRelativePathToTestClasses = relativePathToTestClasses;
        String finalAutomaticBuilder = automaticBuilder;
        Task.Backgroundable dspotTask = new Task.Backgroundable(currentProject, "Amplifying test", true) {

            public void run(@NotNull ProgressIndicator indicator) {
                // clean output directory
                // todo close open amplification result windows or split output into different directories
                try {
                    File outputDirectory = new File(Util.getDSpotOutputPath(currentProject));
                    if (outputDirectory.exists()) {
                        FileUtils.cleanDirectory(outputDirectory);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                String targetModule = "";
                targetModule = moduleRootPath.replace(currentProject.getBasePath() + "/", "");

                List<String> dSpotStarter = new ArrayList<>(Arrays.asList(javaBin, "-jar", dSpotPath,
                        "--absolute-path-to-project-root", currentProject.getBasePath(),
                        "--relative-path-to-classes", finalRelativePathToClasses,
                        "--relative-path-to-test-classes", finalRelativePathToTestClasses,
                        "--test-criterion", "ExtendedCoverageSelector",
                        "--input-ampl-distributor", "RandomInputAmplDistributor",
                        "--test", testClass,
                        // TODO handlle null on testMethod
                        "--test-cases", testMethod,
                        "--output-directory", Util.getDSpotOutputPath(currentProject),
                        "--amplifiers", Config.AMPLIFIERS_ALL,
                        "--max-test-amplified", "25",
                        "--automatic-builder", finalAutomaticBuilder,
                        //"--generate-new-test-class",
                        //"--keep-original-test-methods",
                        "--verbose",
                        "--dev-friendly",
                        "--clean",
                        "--with-comment=None"));

//                if (!AppSettingsState.getInstance().generateAssertions) {
//                    dSpotStarter.add("--only-input-amplification");
//                }
                @NotNull Module[] modules = ModuleManager.getInstance(currentProject).getModules();
                if (modules.length > 1) {
                    dSpotStarter.add("--target-module");
                    dSpotStarter.add(targetModule);
                }

                ProcessBuilder pb = new ProcessBuilder(dSpotStarter);

                pb.environment().put("MAVEN_HOME", AppSettingsState.getInstance().mavenHome);

                File workdir = new File(Util.getTestCubeOutputPath(currentProject) + File.separator + "workdir");
                if (!workdir.exists()) {
                    if (!workdir.mkdirs()) {
                        logger.error("Could not create workdir output directory!");
                    }
                }
                File workdirTarget = new File(workdir.getPath() + File.separator + "target" + File.separator + "dspot");
                if (!workdirTarget.exists()) {
                    if (!workdirTarget.mkdirs()) {
                        logger.error("Could not create workdir/target/dspot output directory!");
                    }
                }
                pb.directory(workdir);

                pb.redirectErrorStream(true);
                try {
                    Process p = pb.start();

                    File outputDir = new File(Util.getTestCubeOutputPath(currentProject));
                    if (!outputDir.exists()) {
                        if (!outputDir.mkdirs()) {
                            logger.error("Could not create Test Cube output directory!");
                        }
                    }

                    File dSpotTerminalOutput = new File(Util.getTestCubeOutputPath(currentProject) + File.separator + "terminal_output_dspot.txt");

                    try (BufferedWriter writer = new BufferedWriter(new FileWriter(dSpotTerminalOutput))) {
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

                Util.sleepAndRefreshProject(currentProject);

                TestCubeNotifier notifier = new TestCubeNotifier();
                TestClassJSON result = Util.getResultJSON(currentProject, testClass);
                if (result == null || result.getTestCases() == null) {
                    notifier.notify(currentProject,
                            "No new test cases found. Please try amplifying another test case!",
                            new InspectDSpotTerminalOutputAction());
                } else {
                    int amplifiedTestCasesCount = result.getTestCases().size();

                    if (amplifiedTestCasesCount == 0) {
                        notifier.notify(currentProject, "Could find no new test cases through amplification.");
                    } else {
                        notifier.notify(currentProject,
                                "Test Cube found " + amplifiedTestCasesCount + " amplified test cases.",
                                new InspectTestCubeResultsAction(currentProject, testClass, testMethod), new InspectDSpotTerminalOutputAction());
                    }
                }
            }
        };

        BackgroundableProcessIndicator processIndicator = new BackgroundableProcessIndicator(dspotTask);
        ProgressManager.getInstance().runProcessWithProgressAsynchronously(dspotTask, processIndicator);
    }
}
