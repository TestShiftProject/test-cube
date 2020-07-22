package org.testshift.testcube.inspect;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;
import org.testshift.testcube.Util;
import org.testshift.testcube.model.AmplificationResult;
import org.testshift.testcube.model.AmplifiedTest;
import org.testshift.testcube.model.OriginalTest;

public class InspectTestCubeResultsAction extends AnAction {

    private String testClass;

    public InspectTestCubeResultsAction() {
        super();
    }

    public InspectTestCubeResultsAction(String testClass) {
        super("Inspect Amplification Results");
        this.testClass = testClass;
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

        // TODO handle no amplified tests (no new class generated then)
        VirtualFile file = LocalFileSystem.getInstance().findFileByPath(Util.getAmplifiedTestClassPath(currentProject, testClass));
        if (file != null) {
            FileEditorManager.getInstance(currentProject).openFile(file, true);
        }

        AmplificationResult result = new AmplificationResult();
        result.originalTest = new OriginalTest(Util.getOriginalTestClassPath(currentProject, testClass));
        result.amplifiedTests.add(new AmplifiedTest(Util.getAmplifiedTestClassPath(currentProject, testClass)));

        AmplificationResultWindow amplificationResultWindow = new AmplificationResultWindow(result);

        ToolWindow toolWindow = ToolWindowManager.getInstance(currentProject).getToolWindow("Test Cube");
        if (toolWindow != null) {
            ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
            Content content = contentFactory.createContent(amplificationResultWindow.getContent(), "Amplification Result A", false);
            toolWindow.getContentManager().addContent(content);

            toolWindow.show();
        }
    }

}
