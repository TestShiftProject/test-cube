package org.testshift.testcube;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.testshift.testcube.icons.TestCubeIcons;
import org.testshift.testcube.settings.AppSettingsState;
import org.testshift.testcube.settings.AskJavaPathDialogWrapper;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StartTestCubeAction extends AnAction {

    private String testClass;
    private String testMethod;

    public StartTestCubeAction() {
    }

    public StartTestCubeAction(@Nullable @Nls(capitalization = Nls.Capitalization.Title) String text, @NotNull String testClass, @Nullable("null means whole class") String testMethod) {
        super(text, "Improves the selected test case by applying amplification operators", TestCubeIcons.AMPLIFY_TEST);
        this.testClass = testClass;
        this.testMethod = testMethod;
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

        if (AppSettingsState.getInstance().java8Path.isEmpty()) {
            AskJavaPathDialogWrapper dialog = new AskJavaPathDialogWrapper();
            dialog.showAndGet();
            boolean pathValid = dialog.setJavaPathIfValid();
            // todo handle non valid
        }



        String javaHome = AppSettingsState.getInstance().java8Path; //"/Library/Java/JavaVirtualMachines/adoptopenjdk-8.jdk/Contents/Home";//System.getProperty("java.home");
        String javaBin = javaHome + File.separator + "bin" + File.separator + "java";

        String pluginPath = PathManager.getPluginsPath();
        String dSpotPath =  pluginPath + File.separator + "test-cube" + File.separator + "lib" + File.separator + "dspot-3.1.1-SNAPSHOT-jar-with-dependencies.jar";

        Task.Backgroundable dspotTask = new Task.Backgroundable(currentProject, "Running DSpot", true) {

            public void run(ProgressIndicator indicator) {
                List<String> dSpotStarter = new ArrayList<>(Arrays.asList(javaBin, "-jar", dSpotPath,
                        "--absolute-path-to-project-root", currentProject.getBasePath(),
                        "--test-criterion", AppSettingsState.getInstance().selectorCriterion,
                        "--input-ampl-distributor", AppSettingsState.getInstance().inputAmplificationDistributor,
                        "--test", testClass,
                        // TODO handlle null on testMethod
                        "--test-cases", testMethod,
                        "--output-directory", currentProject.getBasePath() + Config.OUTPUT_PATH_DSPOT,
                        "--amplifiers", Config.AMPLIFIERS_ALL,
                        //"--generate-new-test-class",
                        //"--keep-original-test-methods",
                        "--with-comment"));

                if (!AppSettingsState.getInstance().generateAssertions) {
                    dSpotStarter.add("--only-input-amplification");
                }

                ProcessBuilder pb = new ProcessBuilder(dSpotStarter);

                pb.redirectErrorStream(true);
                try {
                    Process p = pb.start();

                    File outputDir = new File(currentProject.getBasePath() + Config.OUTPUT_PATH_TESTCUBE);
                    if (!outputDir.exists()) {
                        if (!outputDir.mkdirs()) {
                            System.out.println("Could not create Test Cube output directory!");
                        }
                    }

                    String dSpotOutputPath = currentProject.getBasePath() + Config.OUTPUT_PATH_TESTCUBE + File.separator + "terminal_output_dspot.txt";
                    File output = new File(dSpotOutputPath);

                    try (BufferedWriter writer = new BufferedWriter(new FileWriter(output))) {
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
                    System.out.println(e.getMessage());
                    //e.printStackTrace();
                }

                AmplificationCompletedNotifier notifier = new AmplificationCompletedNotifier();
                notifier.notify(currentProject,
                        "Amplification completed, find the amplified test classes in " + Util.getAmplifiedTestClassPath(currentProject, testClass),
                        new InspectTestCubeResultsAction(testClass));
            }
        };
        BackgroundableProcessIndicator processIndicator = new BackgroundableProcessIndicator(dspotTask);
        ProgressManager.getInstance().runProcessWithProgressAsynchronously(dspotTask, processIndicator);
    }
}
