package org.testshift.testcube;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

public class InspectTestCubeResultsAction extends AnAction {

    private String testClass;

    public InspectTestCubeResultsAction() {
        super();
    }
    public InspectTestCubeResultsAction(String testClass) {
        super("Inspect amplified test case");
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
//        ToolWindow testCubeWindow = ToolWindowManager.getInstance(currentProject).registerToolWindow(
//                RegisterToolWindowTask.closable("Test Cube", AllIcons.Actions.Colors));

        // TODO handle no amplified tests (no new class generated then)
        VirtualFile file = LocalFileSystem.getInstance().findFileByPath(Util.getAmplifiedTestClassPath(currentProject, testClass));
        if (file != null) {
            FileEditorManager.getInstance(currentProject).openFile(file, true);
        }
    }

}
