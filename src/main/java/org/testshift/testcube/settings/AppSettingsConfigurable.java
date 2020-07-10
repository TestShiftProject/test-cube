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
        //modified |= mySettingsComponent.getIdeaUserStatus() != settings.ideaStatus;
        return modified;
    }

    @Override
    public void apply() throws ConfigurationException {
        AppSettingsState settings = AppSettingsState.getInstance();
        settings.java8Path = mySettingsComponent.getJava8Path();
        //settings.ideaStatus = mySettingsComponent.getIdeaUserStatus();
    }

    @Override
    public void reset() {
        AppSettingsState settings = AppSettingsState.getInstance();
        mySettingsComponent.setJava8Path(settings.java8Path);
        //mySettingsComponent.setIdeaUserStatus(settings.ideaStatus);
    }

    @Override
    public void disposeUIResources() {
        mySettingsComponent = null;
    }

}
