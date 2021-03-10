package org.testshift.testcube.explore;

import com.intellij.ui.jcef.JBCefApp;
import com.intellij.ui.jcef.JBCefBrowser;

import javax.swing.*;

public class TestCaseExplorer {
    private JPanel panel;

    public TestCaseExplorer() {
        if (!JBCefApp.isSupported()) {
            // Fallback to an alternative browser-less solution
            return;
        }

        // Use JCEF
        panel.add(new JBCefBrowser("https://www.jetbrains.com").getComponent());
    }
}
