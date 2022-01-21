package org.testshift.testcube.settings;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class AskJavaPathDialogWrapper extends DialogWrapper {

    private final JBTextField pathField = new JBTextField();

    public AskJavaPathDialogWrapper() {
        super(true); // use current window as parent
        init();
        setTitle("Java 8 Path Not Set");
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        JPanel dialogPanel = //new JPanel(new BorderLayout());
                FormBuilder.createFormBuilder()
                           .addLabeledComponent(new JBLabel(
                                   "DSpot needs Java 8 to amplify your test cases. Please enter " +
                                   "the path to your JDK 8 home:"), pathField, 1, true)
                           .getPanel();

        return dialogPanel;
    }

    public boolean setJavaPathIfValid() {
        if (!pathField.getText().isEmpty()) {
            AppSettingsState.getInstance().javaJDKPath = pathField.getText();
            return true;
        } else {
            return false;
        }
    }
}
