package org.testshift.testcube.inspect;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;
import org.testshift.testcube.model.AmplificationResult;

public class InspectTestCubeResultsAction extends NotificationAction {

    private final Project project;
    private final String testClass;
    private final String testMethod;

    public InspectTestCubeResultsAction(Project project, String testClass, String testMethod) {
        super("Inspect Amplification Results");
        this.project = project;
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
    public void actionPerformed(@NotNull AnActionEvent event, @NotNull Notification notification) {

        AmplificationResult amplificationResult = AmplificationResult.buildAmplificationResult(project, testClass, testMethod);

        AmplificationResultWindow amplificationResultWindow = new AmplificationResultWindow(amplificationResult);

        ToolWindow toolWindow = ToolWindowManager.getInstance(amplificationResult.project).getToolWindow("Test Cube");
        if (toolWindow != null) {
            ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
            Content content = contentFactory.createContent(amplificationResultWindow.getContent(), amplificationResultWindow.getDisplayName(), false);
            content.setCloseable(true);
            toolWindow.getContentManager().addContent(content);
            toolWindow.getContentManager().setSelectedContent(content);

            toolWindow.show();
        }
        notification.expire();
    }

}
