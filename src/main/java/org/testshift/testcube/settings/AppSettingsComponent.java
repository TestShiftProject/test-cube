package org.testshift.testcube.settings;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;

/**
 * Supports creating and managing a JPanel for the Settings Dialog.
 */
public class AppSettingsComponent {
    private final JPanel myMainPanel;
    private final JBTextField java8Path = new JBTextField();
    private final JBTextField mavenHome = new JBTextField();
    private final JComboBox color = new ComboBox(new String[]{"Darker", "Brighter"});
    //private final JBCheckBox myIdeaUserStatus = new JBCheckBox("Do You Use IntelliJ IDEA? ");
//    private final JBCheckBox generateAssertions = new JBCheckBox("Generate assertions during amplification");
//    private final JBList<String> selectorCriterion = new JBList<>(AppSettingsState.SELECTOR_CRITERION_OPTIONS);
//    private final JBList<String> inputAmplificationDistributor = new JBList<>(AppSettingsState.INPUT_AMPLIFICATION_DISTRIBUTOR_OPTIONS);

    public AppSettingsComponent() {
        myMainPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel("Absolute path to your java 1.8 installation"), java8Path, 1, false)
                .addLabeledComponent(new JBLabel("Absolute path to your maven home (MAVEN_HOME)"), mavenHome)
                .addLabeledComponent(new JBLabel("Color Mode"), color)
                //.addComponent(myIdeaUserStatus, 1)
//                .addComponent(generateAssertions)
//                .addLabeledComponent(new JBLabel("Criterion for selecting amplified tests"),selectorCriterion)
//                .addLabeledComponent(new JBLabel("Value distribution for input amplification"),inputAmplificationDistributor)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
    }

    public JPanel getPanel() {
        return myMainPanel;
    }

    public JComponent getPreferredFocusedComponent() {
        return java8Path;
    }

    @NotNull
    public String getJava8Path() {
        return java8Path.getText();
    }

    public void setJava8Path(@NotNull String newText) {
        java8Path.setText(newText);
    }

    @NotNull
    public String getMavenHome() {
        return mavenHome.getText();
    }

    public void setMavenHome(@NotNull String newText) {
        mavenHome.setText(newText);
    }

    public String getColor(){return color.getSelectedItem().toString();}

    public void setColor(String newColor){color.setSelectedItem(newColor);}

//    public boolean getGenerateAssertions() {
//        return generateAssertions.isSelected();
//    }
//
//    public void setGenerateAssertions(boolean generateAssertions) {
//        this.generateAssertions.setSelected(generateAssertions);
//    }

//    public String getSelectorCriterion() {
//        return selectorCriterion.getSelectedValue();
//    }
//
//    public void setSelectorCriterion(String selectorCriterion) {
//        this.selectorCriterion.setSelectedValue(selectorCriterion, true);
//    }
//
//    public String getInputAmplificationDistributor() {
//        return inputAmplificationDistributor.getSelectedValue();
//    }
//
//    public void setInputAmplificationDistributor(String inputAmplificationDistributor) {
//        this.inputAmplificationDistributor.setSelectedValue(inputAmplificationDistributor, true);
//    }

}

