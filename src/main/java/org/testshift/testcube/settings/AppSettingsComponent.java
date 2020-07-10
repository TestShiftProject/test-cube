package org.testshift.testcube.settings;

import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Supports creating and managing a JPanel for the Settings Dialog.
 */
public class AppSettingsComponent {
    private final JPanel myMainPanel;
    private final JBTextField java8Path = new JBTextField();
    //private final JBCheckBox myIdeaUserStatus = new JBCheckBox("Do You Use IntelliJ IDEA? ");

    public AppSettingsComponent() {
        myMainPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel("Enter absolute path to your java 1.8 installation: "), java8Path, 1, false)
                //.addComponent(myIdeaUserStatus, 1)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
    }

    public JPanel getPanel() {
        return myMainPanel;
    }

    public JComponent getPreferredFocusedComponent() {
        return java8Path;
    }

    @NotNull
    public String getJava8Path() {
        return java8Path.getText();
    }

    public void setJava8Path(@NotNull String newText) {
        java8Path.setText(newText);
    }

//    public boolean getIdeaUserStatus() {
//        return myIdeaUserStatus.isSelected();
//    }
//
//    public void setIdeaUserStatus(boolean newStatus) {
//        myIdeaUserStatus.setSelected(newStatus);
//    }

}

