package org.testshift.testcube.amplify;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.testshift.testcube.misc.Util;

import java.io.File;

public class InspectDSpotTerminalOutputAction extends NotificationAction  {

    public InspectDSpotTerminalOutputAction() {
        super("Inspect DSpot terminal output");
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
        VirtualFile file = LocalFileSystem.getInstance().findFileByPath(Util.getTestCubeOutputPath(e.getProject()) + File.separator + "terminal_output_dspot.txt");
        if (file != null) {
            FileEditorManager.getInstance(e.getProject()).openFile(file, true);
            //notification.expire();
        }
    }
}
