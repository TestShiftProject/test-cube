package org.testshift.testcube.inspect;

import com.intellij.openapi.wm.ToolWindow;

import javax.swing.*;

public class TestCubeToolWindow {
    private JPanel basePanel;
    public JTabbedPane tabbedPane;
    private AmplificationResultWindow amplificaionResultWindow;

    public TestCubeToolWindow(ToolWindow toolWindow) {
        //close.addActionListener(e -> toolWindow.hide(null));
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }

    public JPanel getContent() {
        return basePanel;
    }
}
