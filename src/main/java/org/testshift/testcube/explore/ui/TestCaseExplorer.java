package org.testshift.testcube.explore.ui;

import com.intellij.designer.LightFillLayout;
import com.intellij.execution.Executor;
import com.intellij.execution.RunManager;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.runners.ExecutionEnvironmentBuilder;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.jcef.JBCefApp;
import com.intellij.ui.jcef.JBCefBrowser;
import com.intellij.xdebugger.XDebuggerManager;
import com.intellij.xdebugger.XDebuggerUtil;
import com.intellij.xdebugger.breakpoints.XBreakpointManager;
import com.intellij.xdebugger.breakpoints.XBreakpointProperties;
import com.intellij.xdebugger.breakpoints.XLineBreakpointType;

import javax.swing.*;
import java.awt.*;

public class TestCaseExplorer {
    private JPanel panel;
    private JScrollPane scroll;
    private JPanel sp;

    public TestCaseExplorer() {
        if (!JBCefApp.isSupported()) {
            // Fallback to an alternative browser-less solution
            return;
        }

        // Use JCEF
        JBCefBrowser browser = new JBCefBrowser("http://localhost:3000/");
        Component component = browser.getComponent();
        sp.setLayout(new BorderLayout());
        sp.add(component);

    }


    public JPanel getContent() {
        return panel;
    }

    public String getDisplayName() {
        return "Exploring!!!";
    }


}
