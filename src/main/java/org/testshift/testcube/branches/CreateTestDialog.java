package org.testshift.testcube.branches;

import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiUtilBase;
import com.intellij.testIntegration.LanguageTestCreators;
import com.intellij.testIntegration.TestCreator;

import javax.swing.*;
import java.awt.event.*;

public class CreateTestDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JLabel Content;
    private Editor editor;
    private PsiFile file;

    public CreateTestDialog(Editor editor, PsiFile file) {
        setContentPane(contentPane);
        setLocationRelativeTo(null);
        setModal(false);
        this.editor = editor;
        this.file = file;

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK(e);
            }
        });
    }

    private void onOK(ActionEvent e) {
        // add your code here
        dispose();
        for (TestCreator creator : LanguageTestCreators.INSTANCE.allForLanguage(file.getLanguage())) {
            if (!creator.isAvailable(file.getProject(), editor, file)) continue;
            creator.createTest(file.getProject(), editor,file);
        }
    }
}
