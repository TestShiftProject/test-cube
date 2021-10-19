package org.testshift.testcube.misc;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.project.Project;
import org.testshift.testcube.icons.TestCubeIcons;

public class TestCubeNotifier {

    public void notify(Project project, String content, AnAction... actions) {
        final Notification notification = NotificationGroupManager.getInstance()
                                                                  .getNotificationGroup("Test Cube")
                                                                  .createNotification(content,
                                                                                      NotificationType.INFORMATION);
        for (AnAction action : actions) {
            notification.addAction(action);
        }
        notification.setIcon(TestCubeIcons.AMPLIFY_TEST);
        notification.notify(project);
    }
}
