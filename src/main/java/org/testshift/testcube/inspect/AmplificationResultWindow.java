package org.testshift.testcube.inspect;

import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiMethod;
import org.testshift.testcube.Config;
import org.testshift.testcube.model.AmplificationResult;
import org.testshift.testcube.model.AmplifiedTestCase;
import org.testshift.testcube.model.TestCase;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

public class AmplificationResultWindow extends Component {

    private static final Logger logger = Logger.getInstance(AmplificationResultWindow.class);

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
    private AmplifiedTestCase currentAmplificationTestCase;

    public AmplificationResultWindow() {

    }

    public AmplificationResultWindow(AmplificationResult amplificationResult) {
        //close.addActionListener(e -> toolWindow.hide(null));
        this.amplificationResult = amplificationResult;
        this.currentAmplificationTestCaseIndex = 0;
        this.currentAmplificationTestCase = amplificationResult.amplifiedTestCases.get(currentAmplificationTestCaseIndex);
        originalInformation.setToolTipText(amplificationResult.originalTestCase.filePath);
        amplifiedInformation.setToolTipText(currentAmplificationTestCase.filePath);

        retrieveOverallAmplificationReport();
        showTestCaseInEditor(amplificationResult.originalTestCase, originalTestCase);
        setOriginalInformation();
        showTestCaseInEditor(currentAmplificationTestCase, amplifiedTestCase);
        setAmplifiedInformation();

        close.addActionListener(l -> close());
        add.addActionListener(l -> addTestCaseToTestSuite());
        ignore.addActionListener(l -> ignoreTestCase());
        next.addActionListener(l -> nextTestCase());
        previous.addActionListener(l -> previousTestCase());
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

    private void showTestCaseInEditor(TestCase testCase, TestCaseEditorField editor) {
        editor.setNewDocumentAndFileType(JavaFileType.INSTANCE, PsiDocumentManager.getInstance(amplificationResult.project).getDocument(testCase.psiFile));
        moveCaretToTestCase(testCase, editor);
    }

    private void moveCaretToTestCase(TestCase testCase, TestCaseEditorField editor) {
        PsiMethod method = testCase.getTestMethod(amplificationResult.testClass);
        if (method != null) {
            editor.setCaretPosition(method.getTextOffset());
            try {
                editor.getEditor().getScrollingModel().scrollVertically(
                        ((int) editor.getEditor().offsetToPoint2D(editor.getEditor().getCaretModel().getOffset()).getY()));
            } catch (NullPointerException ignored) {
                // first time we used the editor text field the editor is null
            }
        }
    }

    private void navigateTestCases(boolean forward) {
        if (forward) {
            if (currentAmplificationTestCaseIndex + 1 == amplificationResult.amplifiedTestCases.size()) {
                currentAmplificationTestCaseIndex = 0;
            } else {
                currentAmplificationTestCaseIndex++;
            }
        } else {
            if (currentAmplificationTestCaseIndex == 0) {
                currentAmplificationTestCaseIndex = amplificationResult.amplifiedTestCases.size() - 1;
            } else {
                currentAmplificationTestCaseIndex--;
            }
        }
        currentAmplificationTestCase = amplificationResult.amplifiedTestCases.get(currentAmplificationTestCaseIndex);
        moveCaretToTestCase(currentAmplificationTestCase, amplifiedTestCase);
        setAmplifiedInformation();
    }

    public void addTestCaseToTestSuite() {
        amplifiedInformation.setText("Added Test Case no. " + currentAmplificationTestCaseIndex);

        AmplifiedTestCase testToAdd = currentAmplificationTestCase;

        PsiMethod method = testToAdd.getTestMethod(amplificationResult.testClass);
        WriteCommandAction.runWriteCommandAction(amplificationResult.project, () -> {
            if (method != null) {
                PsiMethod methodSave = (PsiMethod) method.copy();
                method.delete();

                PsiClass psiClass = Arrays.stream(amplificationResult.originalTestCase.psiFile.getClasses())
                        .filter((PsiClass c) -> c.getQualifiedName().equals(amplificationResult.testClass))
                        .findFirst()
                        .get();
                PsiMethod originalMethod = amplificationResult.originalTestCase.getTestMethod(amplificationResult.testClass);
                psiClass.addAfter(methodSave, originalMethod);
                PsiDocumentManager.getInstance(amplificationResult.project).commitAllDocuments();

                amplificationResult.amplifiedTestCases.remove(testToAdd);
                navigateTestCases(true);
            }
        });

        // todo handle adding last amplified test case
    }

    public void ignoreTestCase() {
        AmplifiedTestCase testToRemove = currentAmplificationTestCase;
        navigateTestCases(true);
        amplificationResult.removeAmplifiedTest(testToRemove);
    }

    public void nextTestCase() {
        navigateTestCases(true);
    }

    public void previousTestCase() {
        navigateTestCases(false);
    }

    private void setAmplifiedInformation() {
        amplifiedInformation.setText("Navigated to Test Case no. " + currentAmplificationTestCaseIndex + " named: " + currentAmplificationTestCase.name);
    }

    private void setOriginalInformation() {
        originalInformation.setText("Original Test Case " + amplificationResult.originalTestCase.name);
    }

    public void close() {
        ToolWindow toolWindow = ToolWindowManager.getInstance(amplificationResult.project).getToolWindow("Test Cube");
        if (toolWindow != null) {
            toolWindow.getContentManager().removeContent(toolWindow.getContentManager().findContent(getDisplayName()), true);
        }
    }

    private void createUIComponents() {
        // place custom component creation code here
    }

    public JPanel getContent() {
        return amplificationResultPanel;
    }

    public String getDisplayName() {
        return "Amplification of " + amplificationResult.originalTestCase.name;
    }

}
