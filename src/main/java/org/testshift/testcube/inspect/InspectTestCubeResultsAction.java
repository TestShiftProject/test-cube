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
import org.testshift.testcube.icons.TestCubeIcons;
import org.testshift.testcube.model.AmplificationResult;

/**
 * This action is triggered by the user when they want to inspect the result of the amplification.
 * It collects the {@link AmplificationResult} data and opens the {@link AmplificationResultWindow} tool window.
 */
public class InspectTestCubeResultsAction extends NotificationAction {

    private final Project project;
    private final String testClass;
    private final String testMethod;

    public InspectTestCubeResultsAction(Project project, String testClass, String testMethod) {
        super("Inspect amplification results");
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

        AmplificationResult amplificationResult = AmplificationResult.buildAmplificationResult(project, testClass,
                                                                                               testMethod);

        AmplificationResultWindow amplificationResultWindow = new AmplificationResultWindow(amplificationResult);

        ToolWindow toolWindow = ToolWindowManager.getInstance(amplificationResult.project).getToolWindow("Test Cube");
        if (toolWindow != null) {
            ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
            Content content = contentFactory.createContent(amplificationResultWindow.getContent(),
                                                           amplificationResultWindow.getDisplayName(), false);
            content.setCloseable(true);
            content.setIcon(TestCubeIcons.AMPLIFY_TEST);
            toolWindow.getContentManager().addContent(content);
            toolWindow.getContentManager().setSelectedContent(content);

            toolWindow.show();
            amplificationResultWindow.addHighlights();
        }
        //notification.expire();
    }

}
