package org.testshift.testcube.branches;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class WriteObjectDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JPanel ContentPane;

    public WriteObjectDialog() {
        setContentPane(contentPane);
        setLocationRelativeTo(null);
        setModal(false);


        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });
    }

    private void onOK() {
        // add your code here
        dispose();
    }

    public static void main(String[] args) {
        WriteObjectDialog dialog = new WriteObjectDialog();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
}
