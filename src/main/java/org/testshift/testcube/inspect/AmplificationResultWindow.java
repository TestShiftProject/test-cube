package org.testshift.testcube.inspect;

import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.Contract;
import org.testshift.testcube.misc.TestCubeNotifier;
import org.testshift.testcube.model.AmplificationResult;
import org.testshift.testcube.model.AmplifiedTestCase;
import org.testshift.testcube.model.TestCase;

import javax.swing.*;
import java.awt.*;

public class AmplificationResultWindow extends Component {

    private static final Logger logger = Logger.getInstance(AmplificationResultWindow.class);

    private JPanel amplificationResultPanel;

    private JTextArea header;

    private JPanel originalSide;
    private TestCaseEditorField originalTestCase;
    private JPanel originalVisualization;
    private JTextArea originalInformation;

    private JPanel amplifiedSide;
    private JPanel amplifiedVisualization;
    private JTextArea amplifiedInformation;
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
        this.amplificationResult = amplificationResult;
        this.currentAmplificationTestCaseIndex = 0;

        assert !amplificationResult.amplifiedTestCases.isEmpty();
        this.currentAmplificationTestCase = amplificationResult.amplifiedTestCases.get(currentAmplificationTestCaseIndex);

        displayOverallAmplificationReport();
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
    private void displayOverallAmplificationReport() {
        header.setText("Amplification of the test method " +
                amplificationResult.originalTestCase.name + " was " +
                "successful!\n" +
                "On the right you see the original test case. Below we show the overall coverage improvement the " +
                "amplification achieved.\n" +
                "On the left you can see the amplified test cases. Below the code of the test case we show how many " +
                "input modifications were applied and how many assertions were added. In addition we show where this " +
                "test achieves more coverage than the original test case.\n\n" +
                "Use 'Next' and 'Previous' explore the test cases!\n" +
                "If you find one that you would like to include in your existing test suite: 'Add To Test Suite' " +
                "automatically copies it over for you :)\n" +
                "Fell free to edit the test cases before adding them!");
    }

    private void showTestCaseInEditor(TestCase testCase, TestCaseEditorField editor) {
        editor.setNewDocumentAndFileType(JavaFileType.INSTANCE, PsiDocumentManager.getInstance(amplificationResult.project).getDocument(testCase.psiFile));
        moveCaretToTestCase(testCase, editor);
    }

    private void moveCaretToTestCase(TestCase testCase, TestCaseEditorField editor) {
        PsiMethod method = testCase.getTestMethod();
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

    @Contract("false, true -> fail")
    private void navigateTestCases(boolean forward, boolean removeCurrent) {
        if (!forward & removeCurrent) throw new IllegalArgumentException();

        if (removeCurrent) {
            amplificationResult.amplifiedTestCases.remove(currentAmplificationTestCase);
            currentAmplificationTestCaseIndex--;
            if (amplificationResult.amplifiedTestCases.isEmpty()) {
                TestCubeNotifier notifier = new TestCubeNotifier();
                notifier.notify(amplificationResult.project, "All amplified test cases were added or ignored. Thank " + "you for using Test Cube!", false);
                close();
                return;
            }
        }
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

        PsiMethod method = testToAdd.getTestMethod();
        WriteCommandAction.runWriteCommandAction(amplificationResult.project, () -> {
            if (method != null) {
                PsiMethod methodSave = (PsiMethod) method.copy();
                method.delete();

                PsiMethod originalMethod = amplificationResult.originalTestCase.getTestMethod();
                if (originalMethod != null) {
                    originalMethod.getContainingClass().addAfter(methodSave, originalMethod);
                }
                PsiDocumentManager.getInstance(amplificationResult.project).commitAllDocuments();

                navigateTestCases(true, true);
            }
        });

        // todo handle adding last amplified test case
    }

    public void ignoreTestCase() {
        navigateTestCases(true, true);
    }

    public void nextTestCase() {
        navigateTestCases(true, false);
    }

    public void previousTestCase() {
        navigateTestCases(false, false);
    }

    private void setAmplifiedInformation() {
        amplifiedInformation.setText(currentAmplificationTestCase.getDescription());
    }

    private void setOriginalInformation() {
        originalInformation.setText("Original test case: " + amplificationResult.originalTestCase.name + "\n\n");
        originalInformation.append(amplificationResult.amplifiedCoverage.toString());
    }

    public void close() {
        ToolWindow toolWindow = ToolWindowManager.getInstance(amplificationResult.project).getToolWindow("Test Cube");
        if (toolWindow != null) {
            toolWindow.getContentManager().removeContent(toolWindow.getContentManager().findContent(getDisplayName()), true);
            if (toolWindow.getContentManager().getContentCount() == 0) {
                toolWindow.hide();
            }
        }
    }

    private void createUIComponents() {
        // place custom component creation code here
    }

    public JPanel getContent() {
        return amplificationResultPanel;
    }

    public String getDisplayName() {
        return "Amplification of '" + amplificationResult.originalTestCase.name + "()'";
    }



}
