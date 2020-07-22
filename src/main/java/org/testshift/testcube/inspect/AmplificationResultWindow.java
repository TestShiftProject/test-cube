package org.testshift.testcube.inspect;

import org.testshift.testcube.model.AmplificationResult;

import javax.swing.*;
import java.awt.*;

public class AmplificationResultWindow extends Component {
    private JLabel header;
    private JEditorPane originalTestCase;
    private JEditorPane amplifiedTestCase;
    private JButton add;
    private JButton ignore;
    private JButton next;
    private JButton previous;
    private JButton close;
    private JPanel originalSide;
    private JPanel amplifiedSide;
    private JPanel originalVisualization;
    private JPanel amplifiedVisualization;
    private JLabel originalInformation;
    private JLabel amplifiedInformation;
    private JPanel buttons;
    private JPanel amplificationResultPanel;

    public AmplificationResult amplificationResult;

    public AmplificationResultWindow() {
    }

    public AmplificationResultWindow(AmplificationResult amplificationResult) {
        //close.addActionListener(e -> toolWindow.hide(null));
        this.amplificationResult = amplificationResult;
        originalInformation.setToolTipText(amplificationResult.originalTest.filePath);
        amplifiedInformation.setToolTipText(amplificationResult.amplifiedTests.get(0).filePath);
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }

    public JPanel getContent() {
        return amplificationResultPanel;
    }


}
