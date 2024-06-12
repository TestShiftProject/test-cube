package org.testshift.testcube.inspect;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiMethod;
import com.intellij.ui.JBColor;
import eu.stamp_project.dspot.common.report.output.selector.branchcoverage.json.TestCaseBranchCoverageJSON;
import eu.stamp_project.dspot.common.report.output.selector.branchcoverage.json.TestClassBranchCoverageJSON;
import org.jetbrains.annotations.Contract;
import org.testshift.testcube.branches.CFGPanel;
import org.testshift.testcube.branches.rendering.RenderCommand;
import org.testshift.testcube.misc.Colors;
import org.testshift.testcube.misc.TestCubeNotifier;
import org.testshift.testcube.misc.Util;
import org.testshift.testcube.model.AmplifiedTestCase;
import org.testshift.testcube.model.GeneratedTestCase;
import org.testshift.testcube.model.GenerationResult;
import org.testshift.testcube.model.TestCase;
import org.testshift.testcube.settings.AppSettingsState;

import java.util.List;

import javax.swing.*;
import java.awt.*;

public class ResultWithCFGWindow extends Component {
    private JPanel amplificationResultPanel;

    private TestCaseEditorField amplifiedTestCase;

    private JPanel buttons;
    private JButton add;
    private JButton ignore;
    private JButton next;
    private JButton previous;
    private JButton close;
    private CFGPanel cfgPanel;
    private JPanel testCasePanel;

    private int currentAmplificationTestCaseIndex;
    GeneratedTestCase currentTestCase;

    private String targetMethod;
    private GenerationResult generationResult;


    public ResultWithCFGWindow(CFGPanel cfgPanel, String targetMethod, GenerationResult generationResult){
//        this();
        this.amplificationResultPanel = new JPanel();

        this.cfgPanel = cfgPanel;
        this.targetMethod = targetMethod;
        this.currentAmplificationTestCaseIndex = 0;
        this.generationResult = generationResult;
        this.currentTestCase = generationResult.generatedTestCases.get(
                currentAmplificationTestCaseIndex);

        cfgPanel.render(RenderCommand.Reason.FILE_SWITCHED);
        cfgPanel.displayResult(RenderCommand.Reason.FILE_SWITCHED);
        cfgPanel.maintainInitialCover();
        cfgPanel.setNewCoveredLines(currentTestCase.newCoveredLine);
        cfgPanel.setNewCoveredBranches(currentTestCase.newCovredBranch);
        cfgPanel.maintainNewCover();
        cfgPanel.setLayout(new GridLayout());

        amplifiedTestCase = new TestCaseEditorField();
        amplifiedTestCase.createEditor();
        showTestCaseInEditor(currentTestCase, amplifiedTestCase);

        this.buttons = new JPanel();
        this.add = new JButton("Add Test To Test Suite");
        this.ignore = new JButton("Ignore Test Case");
        this.next = new JButton("Next Test Case");
        this.previous = new JButton("Previous Test Case");
        this.close = new JButton("Close Amplification Result");
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
        buttons.add(add);
        buttons.add(ignore);
        buttons.add(next);
        buttons.add(previous);
        buttons.add(close);

        close.addActionListener(l -> close());
        add.addActionListener(l -> addTestCaseToTestSuite());
        ignore.addActionListener(l -> ignoreTestCase());
        next.addActionListener(l -> nextTestCase());
        previous.addActionListener(l -> previousTestCase());
        this.testCasePanel = new JPanel();
        testCasePanel.setLayout(new BorderLayout());
        testCasePanel.add(amplifiedTestCase, BorderLayout.CENTER);
        testCasePanel.add(buttons, BorderLayout.SOUTH);
        amplificationResultPanel.setVisible(true);
        amplificationResultPanel.setLayout(new BorderLayout());
        amplificationResultPanel.add(testCasePanel, BorderLayout.NORTH);
        amplificationResultPanel.add(cfgPanel, BorderLayout.CENTER);
    }

    private void showTestCaseInEditor(GeneratedTestCase testCase, TestCaseEditorField editor) {
        editor.setText(testCase.getMethod().getText());
    }

    @Contract("false, true -> fail")
    private void navigateTestCases(boolean forward, boolean removeCurrent) {
        if (!forward & removeCurrent) throw new IllegalArgumentException();

        if (removeCurrent) {
            generationResult.generatedTestCases.remove(currentTestCase);
            currentAmplificationTestCaseIndex--;
            if (generationResult.generatedTestCases.isEmpty()) {
                TestCubeNotifier notifier = new TestCubeNotifier();
                notifier.notify(generationResult.project,
                                "All generated test cases were added or ignored. Thank you for using Test Cube!");
                close();
                return;
            }
        }
        if (forward) {
            if (currentAmplificationTestCaseIndex + 1 == generationResult.generatedTestCases.size()) {
                currentAmplificationTestCaseIndex = 0;
            } else {
                currentAmplificationTestCaseIndex++;
            }
        } else {
            if (currentAmplificationTestCaseIndex == 0) {
                currentAmplificationTestCaseIndex = generationResult.generatedTestCases.size() - 1;
            } else {
                currentAmplificationTestCaseIndex--;
            }
        }
        currentTestCase = generationResult.generatedTestCases.get(currentAmplificationTestCaseIndex);
        cfgPanel.setNewCoveredLines(currentTestCase.newCoveredLine);
        cfgPanel.setNewCoveredBranches(currentTestCase.newCovredBranch);
        cfgPanel.maintainNewCover();
        showTestCaseInEditor(currentTestCase, amplifiedTestCase);
//        setAmplifiedInformation();
        // deal with cfgPanel
    }

    public void close() {
        ToolWindow toolWindow = ToolWindowManager.getInstance(generationResult.project).getToolWindow("Test Cube");
        if (toolWindow != null) {
            toolWindow.getContentManager()
                      .removeContent(toolWindow.getContentManager().findContent(getDisplayName()), true);
            if (toolWindow.getContentManager().getContentCount() == 0) {
                toolWindow.hide();
            }
        }
    }

    public void addTestCaseToTestSuite() {
        GeneratedTestCase testToAdd = currentTestCase;

        PsiMethod method = testToAdd.getMethod();
        WriteCommandAction.runWriteCommandAction(generationResult.project, () -> {
            if (method != null) {
                PsiMethod methodSave = (PsiMethod) method.copy();
                method.delete();
                generationResult.getOriginalClass().add(methodSave);
                PsiDocumentManager.getInstance(generationResult.project).commitAllDocuments();

                navigateTestCases(true, true);
            }
        });
    }

    public void deleteAmplifiedTestCaseFromFile() {
        PsiMethod method = currentTestCase.getMethod();
        WriteCommandAction.runWriteCommandAction(generationResult.project, () -> {
            if (method != null) {
                method.delete();
                PsiDocumentManager.getInstance(generationResult.project).commitAllDocuments();
            }
        });
    }

    public void ignoreTestCase() {
        deleteAmplifiedTestCaseFromFile();
        navigateTestCases(true, true);
    }

    public void nextTestCase() {
        navigateTestCases(true, false);
    }

    public void previousTestCase() {
        navigateTestCases(false, false);
    }

//    private void setAmplifiedInformation() {
//        amplifiedInformation.setText(htmlStart() + currentTestCase.getDescription() + htmlEnd());
//    }

    private String htmlStart() {
        Color foreground = JBColor.foreground();
        Color link;
        if (AppSettingsState.getInstance().highlightColor.equals(Colors.DARKER)) {
            link = JBColor.green.darker();
        } else {
            link = JBColor.green.brighter();
        }
        return "<html><head><style>a {color:" + colorToRGBHtmlString(link) + ";}</style></head>" +
               "<body style=\"font-family:Sans-Serif;color:" + colorToRGBHtmlString(foreground) + ";\">";
    }

    private String colorToRGBHtmlString(Color color) {
        return "rgb(" + color.getRed() + "," + color.getGreen() + "," + color.getBlue() + ")";
    }

    private String htmlEnd() {
        return "</body></html>";
    }

    public JComponent getContent() {
        return amplificationResultPanel;
    }

    public String getDisplayName() {
        return "Test Generation for " + targetMethod+  "()'";
    }
}
