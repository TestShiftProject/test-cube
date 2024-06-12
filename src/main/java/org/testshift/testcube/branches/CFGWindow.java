package org.testshift.testcube.branches;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.openapi.Disposable;
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
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import eu.stamp_project.dspot.common.report.output.selector.branchcoverage.json.TestClassBranchCoverageJSON;
import eu.stamp_project.dspot.common.report.output.selector.extendedcoverage.json.TestClassJSON;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.testshift.testcube.amplify.InspectDSpotTerminalOutputAction;
import org.testshift.testcube.amplify.StartTestCubeAction;
import org.testshift.testcube.branches.rendering.ImageFormat;
import org.testshift.testcube.branches.rendering.RenderCommand;
import org.testshift.testcube.inspect.InspectResultWithCFGAction;
import org.testshift.testcube.inspect.InspectTestCubeResultsAction;
import org.testshift.testcube.misc.Config;
import org.testshift.testcube.misc.TestCubeNotifier;
import org.testshift.testcube.misc.Util;
import org.testshift.testcube.settings.AppSettingsState;
import org.testshift.testcube.settings.AskJavaPathDialogWrapper;
import org.testshift.testcube.settings.AskMavenHomeDialogWrapper;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class CFGWindow extends JPanel implements Disposable {
    private static final Logger logger = Logger.getInstance(CFGWindow.class);

    private Project project;
    private String targetClass;
    private String targetMethod;
    private static JPanel contentPanel;
    private CFGPanel cfgPanel;
    private JPanel buttonPanel;
    private JButton finish;
    private JButton close;
    private String moduleRootPath;
    private String testClass;
    private String testMethod;
    private Set<String> initialCoveredLines;
    private Set<Util.Branch> initialCoveredBranches;

    private int branchNum;

    public CFGWindow(Project project, String targetClass, String targetMethod, String source,
                     Set<String> initialCoveredLines,
                     Set<Util.Branch> initialCoverdBranches,
                     String moduleRootPath, String testClass, String testMethod, int branchNum){
        this.project = project;
        this.targetClass = targetClass;
        this.targetMethod = targetMethod;
        this.moduleRootPath = moduleRootPath;
        this.testClass = testClass;
        this.testMethod = testMethod;
        this.initialCoveredLines = initialCoveredLines;
        this.initialCoveredBranches = initialCoverdBranches;
        this.branchNum = branchNum;

        this.contentPanel = new JPanel();
        this.buttonPanel = new JPanel();
        this.finish = new JButton("Ok");
        this.close = new JButton("Close");
//        this.cfgPanel = cfgPanel;
//        this.buttonPanel = buttonPanel;
        ImageFormat imageFormat = ImageFormat.PNG;
        int page = 0;
        int version = 0;
        RenderCommand.Reason reason = RenderCommand.Reason.FILE_SWITCHED;
        this.cfgPanel = new CFGPanel(/*sourceFilePath,*/ source, imageFormat, page, version, initialCoveredLines, initialCoveredBranches);
        cfgPanel.render(reason);
        cfgPanel.displayResult(reason);
        cfgPanel.maintainInitialCover();
        cfgPanel.setLayout(new GridLayout());
        finish.addActionListener(l->finishSelection(project));
        close.addActionListener(l->cancel());
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.add(finish);
        buttonPanel.add(close);
        contentPanel.setVisible(true);
        contentPanel.setLayout(new BorderLayout());
        contentPanel.add(cfgPanel, BorderLayout.CENTER);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);
//        contentPanel.setVisible(true);
    }

    private void cancel() {
        dispose();
    }


    public static JPanel getContent() {
        return contentPanel;
    }

    public String getDisplayName(){
        return "Control Flow Graph of "+ targetMethod;
    }

    private void finishSelection(Project project) {
        if(branchNum>0) {
            cfgPanel.recordHilight();
            String selectedBranch = cfgPanel.getHilightText();
            if (!selectedBranch.equals("")) {
                try {
                    runDSpot(project, selectedBranch);
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
//                TestCubeNotifier notifier = new TestCubeNotifier();
//                notifier.notify(project,
//                                "Test Cube found " + 1 + " amplified test cases.",
//
//                                new InspectResultWithCFGAction(project, testClass, testMethod,
//                                                               new CFGPanel(cfgPanel), targetMethod),
//                                new InspectDSpotTerminalOutputAction());
            }
            else {
                NoSelectionDialog dialog = new NoSelectionDialog();
                dialog.pack();;
                dialog.setVisible(true);
            }
        }
        else{
            String selectedBranch = "noBranch";
            try {
                runDSpot(project, selectedBranch);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }

        dispose();
    }

    public void runDSpot(Project currentProject, String branch) throws IOException, InterruptedException {

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
        boolean isGradle = ExternalSystemApiUtil.isExternalSystemAwareModule(new ProjectSystemId("GRADLE"),
                                                                             ModuleManager.getInstance(currentProject)
                                                                                          .getModules()[0]);
        boolean isMaven = ExternalSystemApiUtil.isExternalSystemAwareModule(new ProjectSystemId("Maven"),
                                                                            ModuleManager.getInstance(currentProject)
                                                                                         .getModules()[0]);

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
        String dSpotPath = pluginPath + File.separator + "test-cube" + File.separator + "lib" + File.separator +
                           "dspot-3.1.1-SNAPSHOT-jar-with-dependencies.jar";

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
                if (isMaven) {
                    targetModule = moduleRootPath.replace(currentProject.getBasePath() + "/", "");
                }

                // @formatter:off
                List<String> dSpotStarter = new ArrayList<>(Arrays.asList(javaBin, "-jar", dSpotPath,
                                                                          "--absolute-path-to-project-root", currentProject.getBasePath(),
                                                                          "--relative-path-to-classes", finalRelativePathToClasses,
                                                                          "--relative-path-to-test-classes", finalRelativePathToTestClasses,
                                                                          "--test-criterion", "BranchCoverageSelector",
                                                                          "--input-ampl-distributor", "RandomInputAmplDistributor",
                                                                          "--test", testClass,
                                                                          // TODO handlle null on testMethod
                                                                          "--test-cases", testMethod,
                                                                          "--target-class", targetClass,
                                                                          "--target-method", targetMethod,
                                                                          "--target-branch", branch,
                                                                          "--output-directory", Util.getDSpotOutputPath(currentProject),
                                                                          "--amplifiers", "TargetMethodAdderOnExistingObjectsAmplifier",
                                                                          "--max-test-amplified", "25",
                                                                          "--automatic-builder", finalAutomaticBuilder,
                                                                          //"--generate-new-test-class",
                                                                          //"--keep-original-test-methods",
                                                                          "--verbose",
                                                                          "--dev-friendly",
                                                                          "--clean",
                                                                          "--with-comment=None"));
                // @formatter:on

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

                    File dSpotTerminalOutput = new File(
                            Util.getTestCubeOutputPath(currentProject) + File.separator + "terminal_output_dspot.txt");

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
//                TestClassJSON result = Util.getResultJSON(currentProject, testClass);
                TestClassBranchCoverageJSON coverageResult =
                        (TestClassBranchCoverageJSON) Util.getBranchCoverageJSON(project, testClass);
                if (coverageResult.getTestCases() == null || coverageResult == null) {
                    notifier.notify(currentProject, "No new test cases found.",
                                    new InspectDSpotTerminalOutputAction());
                } else {
                    int amplifiedTestCasesCount = coverageResult.getTestCases().size();

                    if (amplifiedTestCasesCount == 0) {
                        notifier.notify(currentProject, "Could find no new test cases through amplification.");
                    } else {
                        notifier.notify(currentProject,
                                        "Test Cube found " + amplifiedTestCasesCount + " amplified test cases.",
                                        // TODO: create a new Action for CFG
                                        new InspectResultWithCFGAction(currentProject, testClass, testMethod,
                                                                       new CFGPanel(cfgPanel), targetMethod),
                                        new InspectDSpotTerminalOutputAction());
                    }
                }
            }
        };

        BackgroundableProcessIndicator processIndicator = new BackgroundableProcessIndicator(dspotTask);
        ProgressManager.getInstance().runProcessWithProgressAsynchronously(dspotTask, processIndicator);
    }

    private void close(Project project) {
        ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow("Test Cube");
        if (toolWindow != null) {
            toolWindow.getContentManager()
                      .removeContent(toolWindow.getContentManager().findContent(getDisplayName()), true);
            if (toolWindow.getContentManager().getContentCount() == 0) {
                toolWindow.hide();
            }
        }
    }

    @Override
    public void dispose() {
        buttonPanel.removeAll();
        cfgPanel.dispose();
        for(Component component: contentPanel.getComponents()){
            if (component instanceof Disposable) {
                Disposer.dispose((Disposable) component);
            }
        }
        close(project);
    }
}
