package org.testshift.testcube.amplify;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import eu.stamp_project.dspot.common.report.output.selector.branchcoverage.json.TestClassBranchCoverageJSON;
import org.jetbrains.annotations.NotNull;
import org.testshift.testcube.branches.AllCoveredDialog;
import org.testshift.testcube.branches.CFGWindow;
import org.testshift.testcube.branches.CodeToUml;
import org.testshift.testcube.branches.NoBranchDialog;
import org.testshift.testcube.icons.TestCubeIcons;
import org.testshift.testcube.misc.Util;

import java.util.Map;
import java.util.Set;

public class ShowCFGCoverageAction extends NotificationAction {
    private final Project project;
    private final PsiClass targetClass;
    private final PsiMethod targetMethod;
    private PsiClass testClass;
    private String testMethods;
    private String moduleRootPath;

    public ShowCFGCoverageAction(Project project, PsiClass targetClass, PsiMethod targetMethod, PsiClass testClass,
                                 String testMethods, String moduleRootPath) {
        super("Inspect Control Flow Graph with Coverage");
        this.project = project;
        this.targetClass = targetClass;
        this.targetMethod = targetMethod;
        this.testClass = testClass;
        this.testMethods = testMethods;
        this.moduleRootPath = moduleRootPath;
    }

    @Override
    public void update(AnActionEvent e) {
        // Set the availability based on whether a project is open
        Project project = e.getProject();
        e.getPresentation().setEnabledAndVisible(project != null);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event, @NotNull Notification notification) {
        TestClassBranchCoverageJSON coverageResult = (TestClassBranchCoverageJSON) Util.getBranchCoverageJSON(project,
                                                                                                              testClass.getQualifiedName());
        Set<String> initialCoveredLines = Util.getInitialCoveredLine(coverageResult);
        Set<Util.Branch> initialCoveredBranches = Util.getInitialCoveredBranch(coverageResult);

        String targetMethodText = targetMethod.getBody().getText();
        PsiFile containingFile = targetMethod.getContainingFile();
        Project project = containingFile.getProject();
        PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
        Document document = psiDocumentManager.getDocument(containingFile);
        int textOffset = targetMethod.getBody().getTextOffset();
        int lineNumber = document.getLineNumber(textOffset); // this lineNumber +1 = starlinenumber


        Map<String, Integer> result = CodeToUml.codeToUml(targetMethodText, lineNumber + 1);
        String source = result.keySet().toArray()[0].toString();
        int branchNum = result.get(source);

        if (branchNum == 0) {
            if(initialCoveredLines.isEmpty()) {
                NoBranchDialog dialog = new NoBranchDialog();
                dialog.pack();
                dialog.setVisible(true);
            }
            else{
                AllCoveredDialog dialog = new AllCoveredDialog();
                dialog.pack();;
                dialog.setVisible(true);
            }
        }

        String targetClassName = targetClass.getQualifiedName();
        String targetMethodName = targetMethod.getName();
        String testClassName = testClass.getQualifiedName();

        CFGWindow cfgWindow = new CFGWindow(project, targetClassName, targetMethodName, source, initialCoveredLines,
                                            initialCoveredBranches, moduleRootPath, testClassName, testMethods,
                                            branchNum);

        ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow("Test Cube");

        if (toolWindow != null) {
            ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
            Content content = contentFactory.createContent(cfgWindow.getContent(), cfgWindow.getDisplayName(), false);
            content.setCloseable(true);
            content.setIcon(TestCubeIcons.AMPLIFY_TEST);

            toolWindow.getContentManager().addContent(content);
            toolWindow.getContentManager().setSelectedContent(content);

            toolWindow.show();
        }
    }

}
