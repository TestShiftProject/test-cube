package org.testshift.testcube.inspect;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import org.jetbrains.annotations.NotNull;

public class TestCubeToolWindowFactory implements ToolWindowFactory {

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
//        TestCubeToolWindow window = new TestCubeToolWindow(toolWindow);
//        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
//        Content content = contentFactory.createContent(window.getContent(), "Test Cube Content", false);
//        toolWindow.getContentManager().addContent(content);
    }
}
