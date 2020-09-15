package org.testshift.testcube.misc;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.project.Project;
import org.testshift.testcube.icons.TestCubeIcons;

public class TestCubeNotifier {
    private final NotificationGroup NOTIFICATION_GROUP_STICKY = new NotificationGroup("Test Cube",
            NotificationDisplayType.STICKY_BALLOON, true);

    private final NotificationGroup NOTIFICATION_GROUP = new NotificationGroup("Test Cube",
            NotificationDisplayType.BALLOON, true);

    public void notify(Project project, String content, boolean sticky, AnAction... actions) {
        final Notification notification =
                (sticky ? NOTIFICATION_GROUP_STICKY : NOTIFICATION_GROUP).createNotification(content,
                NotificationType.INFORMATION);
        for (AnAction action : actions) {
            notification.addAction(action);
        }
        notification.setIcon(TestCubeIcons.AMPLIFY_TEST);
        notification.notify(project);
    }
}
