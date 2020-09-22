package org.testshift.testcube.inspect;

import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.MarkupModel;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.ui.JBColor;
import eu.stamp_project.dspot.selector.extendedcoverageselector.ClassCoverageMap;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.testshift.testcube.misc.TestCubeNotifier;
import org.testshift.testcube.model.AmplificationResult;
import org.testshift.testcube.model.AmplifiedTestCase;
import org.testshift.testcube.model.TestCase;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import java.awt.*;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

public class AmplificationResultWindow extends Component {

    private static final Logger logger = Logger.getInstance(AmplificationResultWindow.class);

    private JPanel amplificationResultPanel;

    private JTextPane header;

    private JPanel originalSide;
    private TestCaseEditorField originalTestCase;
    private JPanel originalVisualization;
    private JTextPane originalInformation;

    private JPanel amplifiedSide;
    private JTextPane amplifiedInformation;
    private TestCaseEditorField amplifiedTestCase;

    private JPanel buttons;
    private JButton add;
    private JButton ignore;
    private JButton next;
    private JButton previous;
    private JButton close;
    private CoverageHighlightingEditorField amplifiedCoverageEditor;
    private JPanel amplifiedTCI;

    public AmplificationResult amplificationResult;
    private int currentAmplificationTestCaseIndex;
    private AmplifiedTestCase currentAmplificationTestCase;

    public AmplificationResultWindow() {
        originalInformation.setEditorKit(JEditorPane.createEditorKitForContentType("text/html"));
        originalInformation.addHyperlinkListener(new HyperlinkListener() {
            @Override
            public void hyperlinkUpdate(HyperlinkEvent e) {
                hyperlinkUpdateTestDescription(e);
            }
        });
        amplifiedInformation.setEditorKit(JEditorPane.createEditorKitForContentType("text/html"));
        amplifiedInformation.addHyperlinkListener(new HyperlinkListener() {
            @Override
            public void hyperlinkUpdate(HyperlinkEvent e) {
                hyperlinkUpdateTestDescription(e);
            }
        });
    }

    public AmplificationResultWindow(AmplificationResult amplificationResult) {
        this();
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

    public void hyperlinkUpdateTestDescription(HyperlinkEvent e) {
        if ( e.getEventType() == HyperlinkEvent.EventType.ACTIVATED ) {
            System.out.println("**** hyperlink ****");
            Element source = e.getSourceElement();
            String text = "";
            try {
                text = source.getDocument().getText(source.getStartOffset(),
                    source.getEndOffset() - source.getStartOffset());
                System.out.println(text);
            } catch (BadLocationException badLocationException) {
                badLocationException.printStackTrace();
            }
            if (e.getDescription().equals("class")) {
                showClassInAmplifiedCoverageEditor(text,
                        currentAmplificationTestCase.coverageImprovement.getInstructionImprovement().getCoverageForClass(text), null, 0);
            }
            if (e.getDescription().startsWith("method")) {
                String[] lineData = e.getDescription().split("\\|"); // {"method", <class name>}
                String classNameFQ = lineData[1];
                // todo scorlling does not really work
                // todo overloaded methods :/
                showClassInAmplifiedCoverageEditor(classNameFQ,
                        currentAmplificationTestCase.coverageImprovement.getInstructionImprovement().getCoverageForClass(classNameFQ), text, 0);
            }
            if (e.getDescription().startsWith("line")) {
                String[] lineData = e.getDescription().split("\\|"); // {"line", <class name>, <method name>}
                String classNameFQ = lineData[1];
                String methodName = lineData[2];
                int line = Integer.parseInt(text);
                showClassInAmplifiedCoverageEditor(classNameFQ,
                        currentAmplificationTestCase.coverageImprovement.getInstructionImprovement().getCoverageForClass(classNameFQ), methodName, line);
            }
        }
    }

    private void showClassInAmplifiedCoverageEditor(String classNameFQ, ClassCoverageMap coverageImprovement,
                                                    @Nullable String methodNameToScrollTo,
                                                    int methodLineToScrollTo) {
        PsiClass psiClass = JavaPsiFacade.getInstance(amplificationResult.project).findClass(classNameFQ,
                GlobalSearchScope.allScope(amplificationResult.project));
        if (psiClass != null) {
            amplifiedCoverageEditor.setNewDocumentAndFileType(JavaFileType.INSTANCE, PsiDocumentManager.getInstance(amplificationResult.project).getDocument(psiClass.getContainingFile()));
            TextAttributes coveredLine = new TextAttributes();
            coveredLine.setBackgroundColor(JBColor.green.darker());

            MarkupModel markupModel = amplifiedCoverageEditor.getEditor().getMarkupModel();
            coverageImprovement.methodCoverageMap.forEach((methodName, methodCoverage) -> {
                Optional<PsiMethod> method =
                Arrays.stream(psiClass.getMethods()).filter(psiMethod -> psiMethod.getName().equals(methodName)).findFirst();
                if (method.isPresent()) {
                    int methodLine =
                            amplifiedCoverageEditor.getEditor().offsetToLogicalPosition(method.get().getTextOffset()).line;
                    Map<Integer, Integer> coveredInstructions =
                            currentAmplificationTestCase.coverageImprovement.getInstructionImprovement().getCoverageForClass(classNameFQ).getCoverageForMethod(methodName).coveragePerLine();
                    for (Integer coverageLine : coveredInstructions.keySet()) {
                        markupModel.addLineHighlighter(methodLine + coverageLine, HighlighterLayer.ERROR, coveredLine);
                    }
                }
            });

            if (methodNameToScrollTo != null) {
                Optional<PsiMethod> method =
                        Arrays.stream(psiClass.getMethods()).filter(psiMethod -> psiMethod.getName().equals(methodNameToScrollTo)).findAny();
                if (method.isPresent()) {
                    try {
                        // todo take method line into account
                        amplifiedCoverageEditor.getEditor().getScrollingModel()
                                .scrollVertically((int) amplifiedCoverageEditor.getEditor().offsetToLogicalPosition(method.get().getTextOffset()).line);
                    } catch (NullPointerException ignored) {}
                }
            }
        }
    }

    /**
     * Writes the content of DSpot's report file into the header text of the window for this amplification result
     */
    private void displayOverallAmplificationReport() {
        header.setText("<html><body style=\"font-family:Sans-Serif;\">Amplification of the test method " +
                amplificationResult.originalTestCase.name + " was " +
                "successful!<br>" +
                "On the right you see the original test case. Below we show the overall coverage improvement the " +
                "amplification achieved.<br>" +
                "On the left you can see the amplified test cases. Below the code of the test case we show how many " +
                "input modifications were applied and how many assertions were added. In addition we show where this " +
                "test achieves more coverage than the original test case.<br><br>" +
                "Use 'Next' and 'Previous' explore the test cases!<br>" +
                "If you find one that you would like to include in your existing test suite: 'Add To Test Suite' " +
                "automatically copies it over for you :)<br>" +
                "Fell free to edit the test cases before adding them!</body></html>");
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
                editor.getEditor().getScrollingModel().scrollVertically((int) editor.getEditor().offsetToPoint2D(editor.getEditor().getCaretModel().getOffset()).getY());
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
        amplifiedInformation.setText("<html><body style=\"font-family:Sans-Serif;\">" + currentAmplificationTestCase.getDescription() + "</body></html>");
    }

    private void setOriginalInformation() {
        originalInformation.setText("<html><body style=\"font-family:Sans-Serif;\">Original test case: '" + amplificationResult.originalTestCase.name + "'<br><br>" +
                amplificationResult.amplifiedCoverage.toHtmlString() + "</body></html>");
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
