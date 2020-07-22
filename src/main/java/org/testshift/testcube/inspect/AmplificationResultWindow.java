package org.testshift.testcube.inspect;

import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.ui.content.Content;
import org.testshift.testcube.Config;
import org.testshift.testcube.model.AmplificationResult;
import org.testshift.testcube.model.AmplifiedTest;
import org.testshift.testcube.model.Test;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

public class AmplificationResultWindow extends Component {

    private JPanel amplificationResultPanel;

    private JLabel header;

    private JPanel originalSide;
    private TestCaseEditorField originalTestCase;
    private JPanel originalVisualization;
    private JLabel originalInformation;

    private JPanel amplifiedSide;
    private JPanel amplifiedVisualization;
    private JLabel amplifiedInformation;
    private TestCaseEditorField amplifiedTestCase;

    private JPanel buttons;
    private JButton add;
    private JButton ignore;
    private JButton next;
    private JButton previous;
    private JButton close;

    public AmplificationResult amplificationResult;
    private int currentAmplificationTestCaseIndex;
    private AmplifiedTest currentAmplificationTestCase;

    public AmplificationResultWindow() {

    }

    public AmplificationResultWindow(AmplificationResult amplificationResult) {
        //close.addActionListener(e -> toolWindow.hide(null));
        this.amplificationResult = amplificationResult;
        this.currentAmplificationTestCaseIndex = 0;
        this.currentAmplificationTestCase = amplificationResult.amplifiedTests.get(currentAmplificationTestCaseIndex);
        originalInformation.setToolTipText(amplificationResult.originalTest.filePath);
        amplifiedInformation.setToolTipText(currentAmplificationTestCase.filePath);

        retrieveOverallAmplificationReport();
        updateRender();

        close.addActionListener(l -> this.getParent().remove(this));
        add.addActionListener(l -> addTestCaseToTestSuite());
        ignore.addActionListener(l -> ignoreTestCase());
        next.addActionListener(l -> nextTestCase());
        previous.addActionListener(l -> previousTestCase());
    }

    private void updateRender() {
        showTestCaseInEditor(amplificationResult.originalTest, originalTestCase);
        showTestCaseInEditor(currentAmplificationTestCase, amplifiedTestCase);
    }

    /**
     * Writes the content of DSpot's report file into the header text of the window for this amplification result
     */
    private void retrieveOverallAmplificationReport() {
        try {
            header.setText(String.join("\n", Files.readAllLines(Paths.get(amplificationResult.project.getBasePath() + Config.OUTPUT_PATH_DSPOT + File.separator + "report.txt"))));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showTestCaseInEditor(Test testCase, TestCaseEditorField editor) {
        //editor = new TestCaseEditorField(JavaLanguage.INSTANCE, amplificationResult.project, "value");
        VirtualFile file = LocalFileSystem.getInstance().findFileByPath(testCase.filePath);
        if (file != null) {
            PsiJavaFile psiFile = (PsiJavaFile) PsiManager.getInstance(amplificationResult.project).findFile(file);
            if (psiFile != null) {
                editor.setNewDocumentAndFileType(JavaFileType.INSTANCE, PsiDocumentManager.getInstance(amplificationResult.project).getDocument(psiFile));
                PsiClass psiClass = Arrays.stream(psiFile.getClasses()).filter((PsiClass c) -> c.getQualifiedName().equals(amplificationResult.testClass)).findFirst().get();
                PsiMethod[] methods = psiClass.findMethodsByName(amplificationResult.testMethod, false);
                if (methods.length == 1) {
                    editor.setCaretPosition(methods[0].getTextOffset());
                } else {
                    System.out.println("more than one method found!");
                }
            }
        }
    }

    private void navigateTestCases(boolean forward) {
        if (forward) {
            if (currentAmplificationTestCaseIndex + 1 == amplificationResult.amplifiedTests.size()) {
                currentAmplificationTestCaseIndex = 0;
            } else {
                currentAmplificationTestCaseIndex++;
            }
        } else {
            if (currentAmplificationTestCaseIndex == 0) {
                currentAmplificationTestCaseIndex = amplificationResult.amplifiedTests.size() - 1;
            } else {
                currentAmplificationTestCaseIndex--;
            }
        }
        currentAmplificationTestCase = amplificationResult.amplifiedTests.get(currentAmplificationTestCaseIndex);
        updateRender();
    }

    public void addTestCaseToTestSuite() {
        amplifiedInformation.setText("Added Test Case no. " + currentAmplificationTestCaseIndex);

        // todo copy over current test case to project
        AmplifiedTest testToAdd = currentAmplificationTestCase;
        navigateTestCases(true);

        // todo handle adding last amplified test case
    }

    public void ignoreTestCase() {
        AmplifiedTest testToRemove = currentAmplificationTestCase;
        navigateTestCases(true);
        amplificationResult.removeAmplifiedTest(testToRemove);
    }

    public void nextTestCase() {
        amplifiedInformation.setText("Navigated to Test Case no. " + currentAmplificationTestCaseIndex);
        navigateTestCases(true);
    }

    public void previousTestCase() {
        amplifiedInformation.setText("Navigated to Test Case no. " + currentAmplificationTestCaseIndex);
        navigateTestCases(false);
    }

    public void close() {
        ((Content) getContent()).release();
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }

    public JPanel getContent() {
        return amplificationResultPanel;
    }


}
