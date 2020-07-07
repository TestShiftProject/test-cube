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

import java.io.*;

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
        String javaHome = "/Library/Java/JavaVirtualMachines/adoptopenjdk-8.jdk/Contents/Home";//System.getProperty("java.home");
        String javaBin = javaHome + File.separator + "bin" + File.separator + "java";

        String pluginPath = PathManager.getPluginsPath();
        String dSpotPath =  pluginPath + File.separator + "test-cube" + File.separator + "lib" + File.separator + "dspot-3.1.1-SNAPSHOT-jar-with-dependencies.jar";

        Task.Backgroundable dspotTask = new Task.Backgroundable(currentProject, "Running DSpot", true) {
            public void run(ProgressIndicator indicator) {
                ProcessBuilder pb = new ProcessBuilder(javaBin, "-jar", dSpotPath,
                        "--absolute-path-to-project-root", currentProject.getBasePath(),
                        "--test-criterion", "JacocoCoverageSelector",
                        "--test", testClass,
                        // TODO handlle null on testMethod
                        "--test-cases", testMethod,
                        "--output-directory", currentProject.getBasePath() + Config.OUTPUT_PATH,
                        "--with-comment");
                System.out.println(pb);
                try {
                    pb.redirectErrorStream(true);
                    Process p = pb.start();
                    InputStream is = p.getInputStream();
                    BufferedReader br = new BufferedReader(new InputStreamReader(is));
                    for (String line = br.readLine(); line != null; line = br.readLine()) {
                        System.out.println(line);
                    }
                    p.waitFor();
                    System.out.println(p.exitValue());
                } catch (InterruptedException | IOException e) {
                    e.printStackTrace();
                }
                AmplificationCompletedNotifier notifier = new AmplificationCompletedNotifier();
                notifier.notify(currentProject,
                        "Amplification completed, find the amplified test classes in target/output/...",
                        new InspectTestCubeResultsAction(testClass));
            }
        };
        BackgroundableProcessIndicator processIndicator = new BackgroundableProcessIndicator(dspotTask);
        ProgressManager.getInstance().runProcessWithProgressAsynchronously(dspotTask, processIndicator);
    }
}
