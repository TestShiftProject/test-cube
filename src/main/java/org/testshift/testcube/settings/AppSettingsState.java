package org.testshift.testcube.settings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.testshift.testcube.misc.Colors;


/**
 * Supports storing the application settings in a persistent way.
 * The State and Storage annotations define the name of the data and the file name where
 * these persistent application settings are stored.
 */
@State(name = "org.testshift.testcube.AppSettingsState", storages = {@Storage("TestCubePluginSettings.xml")})
public class AppSettingsState implements PersistentStateComponent<AppSettingsState> {

    public String javaJDKPath = "";
    public String mavenHome = "";
    public Colors highlightColor = Colors.BRIGHTER;

    // DSpot Config
//    public boolean generateAssertions = true;
//    public String selectorCriterion = "ExtendedCoverageSelector";
//    public static final String[] SELECTOR_CRITERION_OPTIONS = {"PitMutantScoreSelector", "JacocoCoverageSelector",
//            "TakeAllSelector", "ExtendedCoverageSelector"};
//    public String inputAmplificationDistributor = "RandomInputAmplDistributor";
//    public static final String[] INPUT_AMPLIFICATION_DISTRIBUTOR_OPTIONS = {"RandomInputAmplDistributor",
//    "TextualDistanceInputAmplDistributor", "SimpleInputAmplDistributor"};


    public static AppSettingsState getInstance() {
        return ApplicationManager.getApplication().getService(AppSettingsState.class);
    }

    @Nullable
    @Override
    public AppSettingsState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull AppSettingsState state) {
        XmlSerializerUtil.copyBean(state, this);
    }

}
