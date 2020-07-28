package org.testshift.testcube.amplify;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.project.Project;
import org.testshift.testcube.icons.TestCubeIcons;

public class AmplificationCompletedNotifier {
    private final NotificationGroup NOTIFICATION_GROUP = new NotificationGroup("Test Cube",
            NotificationDisplayType.STICKY_BALLOON, true);

    public Notification notify(Project project, String content, AnAction action) {
        final Notification notification = NOTIFICATION_GROUP.createNotification(content, NotificationType.INFORMATION);
        if (action != null) {
            notification.addAction(action);
        }
        notification.setIcon(TestCubeIcons.AMPLIFY_TEST);
        notification.notify(project);
        return notification;

    }
}
