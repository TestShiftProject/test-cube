package org.testshift.testcube.explore;

import com.intellij.designer.LightFillLayout;
import com.intellij.ui.jcef.JBCefApp;
import com.intellij.ui.jcef.JBCefBrowser;

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
        JBCefBrowser browser = new JBCefBrowser("https://www.jetbrains.com");
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
