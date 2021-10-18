package org.testshift.testcube.settings;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class AskMavenHomeDialogWrapper extends DialogWrapper {

    private final JBTextField pathField = new JBTextField();

    public AskMavenHomeDialogWrapper() {
        super(true); // use current window as parent
        init();
        setTitle("Maven Home Not Set");
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        JPanel dialogPanel = //new JPanel(new BorderLayout());
                FormBuilder.createFormBuilder()
                           .addLabeledComponent(new JBLabel(
                                   "DSpot needs Maven to amplify your test cases. Please enter " +
                                   "the path to your Maven Home:"), pathField, 1, true)
                           .getPanel();

        return dialogPanel;
    }

    public boolean setMavenHomeIfValid() {
        if (!pathField.getText().isEmpty()) {
            AppSettingsState.getInstance().mavenHome = pathField.getText();
            return true;
        } else {
            return false;
        }
    }
}
