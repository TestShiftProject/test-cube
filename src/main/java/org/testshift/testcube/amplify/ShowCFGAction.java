package org.testshift.testcube.amplify;

import com.intellij.codeInsight.navigation.GotoTargetHandler;
import com.intellij.icons.AllIcons;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.lang.LangBundle;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
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
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiUtilBase;
import com.intellij.testIntegration.LanguageTestCreators;
import com.intellij.testIntegration.TestCreator;
import com.intellij.testIntegration.TestFinderHelper;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.util.ObjectUtils;
import eu.stamp_project.dspot.common.report.output.selector.branchcoverage.json.TestClassBranchCoverageJSON;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import eu.stamp_project.dspot.common.report.output.selector.TestClassJSON;
import org.testshift.testcube.branches.*;
import org.testshift.testcube.branches.rendering.*;
import org.testshift.testcube.icons.TestCubeIcons;
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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;


public class ShowCFGAction extends AnAction {
    private static final Logger logger = Logger.getInstance(ShowCFGAction.class);
    private final PsiClass targetClass;
    private final PsiMethod targetMethod;
    private PsiClass testClass;
    private String testMethodsName;
    private Project project;
    private String moduleRootPath;

    public ShowCFGAction(@Nullable /*@Nls(capitalization = Nls.Capitalization.Title)*/ String text,
                         PsiClass targetClass, PsiMethod targetMethod, String moduleRootPath) {
        super(text, "generate test cases for the selected method", TestCubeIcons.AMPLIFY_TEST);
        this.moduleRootPath = moduleRootPath;
        this.targetClass = targetClass;
        this.targetMethod = targetMethod;
    }

    @Override
    public void update(AnActionEvent e) {
        // Set the availability based on whether a project is open
        Project project = e.getProject();
        e.getPresentation().setEnabledAndVisible(project != null);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        this.project = event.getProject();
        //find testclass
        Collection<PsiElement> testClasses = ReadAction.compute(() -> TestFinderHelper.findTestsForClass(targetClass));
        final List<PsiElement> candidates = Collections.synchronizedList(new ArrayList<>());
        candidates.addAll(testClasses);
        if(candidates.size()>0) {
            this.testClass = (PsiClass) candidates.get(0);
        }

        if(this.testClass==null){
            Editor editor = event.getData(CommonDataKeys.EDITOR);
            if (editor == null || project == null) return;
            PsiFile file = PsiUtilBase.getPsiFileInEditor(editor, project);
            if (file == null) return;
            CreateTestDialog createTestDialog = new CreateTestDialog(editor, file);
            createTestDialog.pack();
            createTestDialog.setVisible(true);

            return;
        }

//        try {
//            Thread.currentThread().sleep(5000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        // find testMethods;
        //TODO: make use of all testMethods
        PsiMethod[] testMethods = this.testClass.getMethods();

        if(testMethods.length ==0 || isEmptyMethods(testMethods)){
            WriteObjectDialog writeObjectDialog = new WriteObjectDialog();
            writeObjectDialog.pack();
            writeObjectDialog.setVisible(true);
            return;
        }

        this.testMethodsName = getMethodsString(testMethods);


        try {
            runDSpotForCoverage(this.project);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
//        TestCubeNotifier notifier = new TestCubeNotifier();
//        notifier.notify(project, "Initial Coverage computing finished",
//                        new ShowCFGCoverageAction(project, targetClass, targetMethod, testClass, testMethodsName,
//                                                  moduleRootPath));
    }

    private String getMethodsString(PsiMethod[] testMethods) {
        String methods = "";
        for(PsiMethod method: testMethods){
            methods = methods + method.getName() +",";
        }
        methods = methods.substring(0,methods.length()-1);
        return methods;
    }

    private boolean isEmptyMethods(PsiMethod[] testMethods) {
        boolean empty = true;
        for(PsiMethod method: testMethods){
            if(!method.getBody().isEmpty()){
                empty = false;
                break;
            }
        }
        return empty;
    }

    private void runDSpotForCoverage(Project currentProject) throws IOException, InterruptedException {

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
        String relativePathToSourceCode = "";
        String relativePathToTestCode = "";
        String automaticBuilder = "";
        if (isMaven) {
            relativePathToClasses = "target" + File.separator + "classes" + File.separator;
            relativePathToTestClasses = "target" + File.separator + "test-classes" + File.separator;
            relativePathToSourceCode = "src" + File.separator + "main" + File.separator + "java" + File.separator;
            relativePathToTestCode = "src" + File.separator + "test" + File.separator + "java" + File.separator;
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
//        String finalRelativePathToSourceCode = relativePathToSourceCode;
//        String finalRelativePathToTestCode = relativePathToTestCode;
        String finalAutomaticBuilder = automaticBuilder;

        String testClassName = testClass.getQualifiedName();
        String targetClassName = targetClass.getQualifiedName();
        String targetMethodName = targetMethod.getName();
        Task.Backgroundable dspotTask = new Task.Backgroundable(currentProject, "Computing coverage", true) {
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
//                                                                          "--relative-path-to-source-code", finalRelativePathToSourceCode,
//                                                                          "--relative-path-to-test-code", finalRelativePathToTestCode,
                                                                          "--test-criterion", "BranchCoverageSelector",
                                                                          "--amplifiers", "TargetMethodAdderOnExistingObjectsAmplifier",
//                                                                          "--input-ampl-distributor", "RandomInputAmplDistributor",
                                                                          "--iteration", "1",
                                                                          "--test", testClassName,
                                                                          // TODO handlle null on testMethod
                                                                          "--test-cases", testMethodsName,
                                                                          "--target-class", targetClassName,
                                                                          "--target-method", targetMethodName,
                                                                          "--output-directory", Util.getDSpotOutputPath(currentProject),
//                                                                          "--max-test-amplified", "25",
                                                                          "--automatic-builder", finalAutomaticBuilder,
                                                                          //"--generate-new-test-class",
                                                                          //"--keep-original-test-methods",
                                                                          "--verbose",
                                                                          "--dev-friendly",
                                                                          "--clean",
                                                                          "--with-comment=All"));
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
                notifier.notify(currentProject, "Initial Coverage computing finished",
                                new ShowCFGCoverageAction(project, targetClass, targetMethod, testClass,
                                                          testMethodsName,
                                                          moduleRootPath));
            }
        };

        BackgroundableProcessIndicator processIndicator = new BackgroundableProcessIndicator(dspotTask);
        ProgressManager.getInstance().runProcessWithProgressAsynchronously(dspotTask, processIndicator);
    }
}
