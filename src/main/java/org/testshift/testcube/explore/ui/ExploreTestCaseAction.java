package org.testshift.testcube.explore.ui;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonWriter;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.mongodb.util.JSON;
import org.jetbrains.annotations.NotNull;
import org.joni.ast.StringNode;
import org.testshift.testcube.explore.GraphConstructor;
import org.testshift.testcube.icons.TestCubeIcons;
import org.testshift.testcube.model.AmplifiedTestCase;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class ExploreTestCaseAction extends AnAction {

    private final AmplifiedTestCase testCaseToExplore;
    private final Project project;

    public ExploreTestCaseAction() {
        super("Explore Test Case!");
        this.testCaseToExplore = null;
        this.project = null;
    }

    public ExploreTestCaseAction(AmplifiedTestCase testCaseToExplore, Project project) {
        this.testCaseToExplore = testCaseToExplore;
        this.project = project;
    }

    @Override
    public void update(AnActionEvent e) {
        // Set the availability based on whether a project is open
        Project project = e.getProject();
        e.getPresentation().setEnabledAndVisible(project != null);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        GraphConstructor.project = e.getProject();
        String json = gson.toJson(GraphConstructor.constructGraph(testCaseToExplore));
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream("/Users/caro/Dev/test-cube/target/explorerJson.json"), "utf-8"))) {
            writer.write(json);
        } catch (IOException unsupportedEncodingException) {
            unsupportedEncodingException.printStackTrace();
        }

        TestCaseExplorer explorerWindow = new TestCaseExplorer();

        ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow("Test Cube Explorer");
        if (toolWindow != null) {
            ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
            Content content = contentFactory.createContent(explorerWindow.getContent(), explorerWindow.getDisplayName()
                    , false);
            content.setCloseable(true);
            content.setIcon(TestCubeIcons.AMPLIFY_TEST);
            toolWindow.getContentManager().addContent(content);
            toolWindow.getContentManager().setSelectedContent(content);

            toolWindow.show();
        }
    }
}
