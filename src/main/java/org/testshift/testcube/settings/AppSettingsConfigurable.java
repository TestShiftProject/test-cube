package org.testshift.testcube.settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Provides controller functionality for application settings.
 */
public class AppSettingsConfigurable implements Configurable {
    private AppSettingsComponent mySettingsComponent;

    // A default constructor with no arguments is required because this implementation
    // is registered as an applicationConfigurable EP

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "Test Cube Settings";
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return mySettingsComponent.getPreferredFocusedComponent();
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        mySettingsComponent = new AppSettingsComponent();
        return mySettingsComponent.getPanel();
    }

    @Override
    public boolean isModified() {
        AppSettingsState settings = AppSettingsState.getInstance();
        boolean modified = !mySettingsComponent.getJava8Path().equals(settings.java8Path);
        modified |= !mySettingsComponent.getMavenHome().equals(settings.mavenHome);
//        modified |= mySettingsComponent.getGenerateAssertions() != settings.generateAssertions;
//        modified |= !mySettingsComponent.getSelectorCriterion().equals(settings.selectorCriterion);
//        modified |= !mySettingsComponent.getInputAmplificationDistributor().equals(settings
//        .inputAmplificationDistributor);
        return modified;
    }

    @Override
    public void apply() throws ConfigurationException {
        AppSettingsState settings = AppSettingsState.getInstance();
        settings.java8Path = mySettingsComponent.getJava8Path();
        settings.mavenHome = mySettingsComponent.getMavenHome();
//        settings.generateAssertions = mySettingsComponent.getGenerateAssertions();
//        settings.selectorCriterion = mySettingsComponent.getSelectorCriterion();
//        settings.inputAmplificationDistributor = mySettingsComponent.getInputAmplificationDistributor();
    }

    @Override
    public void reset() {
        AppSettingsState settings = AppSettingsState.getInstance();
        mySettingsComponent.setJava8Path(settings.java8Path);
        mySettingsComponent.setMavenHome(settings.mavenHome);
//        mySettingsComponent.setGenerateAssertions(settings.generateAssertions);
//        mySettingsComponent.setSelectorCriterion(settings.selectorCriterion);
//        mySettingsComponent.setInputAmplificationDistributor(settings.inputAmplificationDistributor);
    }

    @Override
    public void disposeUIResources() {
        mySettingsComponent = null;
    }

}
