package org.testshift.testcube.explore;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.util.BusyObject;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.ex.ToolWindowManagerEx;
import com.intellij.ui.jcef.JBCefApp;
import org.jetbrains.annotations.NotNull;

public class ExploreTestCaseAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        ToolWindow toolWindow = ToolWindowManager.getInstance(e.getProject()).getToolWindow("Test Cube Explorer");
        if (toolWindow != null) {
//            ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
//            Content content = contentFactory.createContent(amplificationResultWindow.getContent(), amplificationResultWindow.getDisplayName(), false);
//            content.setCloseable(true);
//            content.setIcon(TestCubeIcons.AMPLIFY_TEST);
//            toolWindow.getContentManager().addContent(content);
//            toolWindow.getContentManager().setSelectedContent(content);
//
//            toolWindow.show();
//            amplificationResultWindow.addHighlights();
            if (!JBCefApp.isSupported()) {
                // Fallback to an alternative browser-less solution
                return;
            }

// Use JCEF
        }
    }
}
