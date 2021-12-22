package org.testshift.testcube.amplify;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
//import org.plantuml.idea.external.PlantUmlFacade;
//import org.plantuml.idea.plantuml.ImageFormat;
//import org.plantuml.idea.preview.Zoom;
//import org.plantuml.idea.rendering.RenderCacheItem;
//import org.plantuml.idea.rendering.RenderCommand;
//import org.plantuml.idea.rendering.RenderRequest;
//import org.plantuml.idea.rendering.RenderResult;
//import org.plantuml.idea.settings.PlantUmlSettings;
import org.testshift.testcube.branches.CFGPanel;
import org.testshift.testcube.branches.NoBranchDialog;
import org.testshift.testcube.branches.rendering.*;
import org.testshift.testcube.icons.TestCubeIcons;
//import org.plantuml.idea.adapter.FacadeImpl;

import java.awt.*;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;


public class ShowCFGAction extends AnAction {
    private static final Logger logger = Logger.getInstance(ShowCFGAction.class);
    private final String targetClass;
    private final String targetMethod;

    public ShowCFGAction(@Nullable @Nls(capitalization = Nls.Capitalization.Title) String text,
                         String targetClass,
                         String targetMethod) {
        super(text, "generate test cases for the selected method", TestCubeIcons.AMPLIFY_TEST);
        this.targetClass = targetClass;
        this.targetMethod = targetMethod;
    }

    @Override
    public void update(AnActionEvent e) {
        // Set the availability based on whether a project is open
        Project project = e.getProject();
        e.getPresentation().setEnabledAndVisible(project != null);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        // TODO:transform code to uml, return uml and branch number
        int branchNum = 1;
        if(branchNum==0){
            NoBranchDialog dialog = new NoBranchDialog();
            dialog.pack();
            dialog.setVisible(true);
        } else{

            ImageFormat imageFormat = ImageFormat.PNG;
            int page = 0;
            int version=0;
            String sourceFilePath = "F:\\mytestcube\\test-cube\\src\\main\\resources\\test.puml";

            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new FileReader(sourceFilePath));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            StringBuilder stringBuilder = new StringBuilder();
            String line = null;
            String ls = System.getProperty("line.separator");
            while (true) {
                try {
                    if (!((line = reader.readLine()) != null)) break;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                stringBuilder.append(line);
                stringBuilder.append(ls);
            }
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            String source = stringBuilder.toString();

            RenderCommand.Reason reason = RenderCommand.Reason.FILE_SWITCHED;

            CFGPanel cfgPanel = new CFGPanel(sourceFilePath, source, imageFormat, page, version);

            cfgPanel.render(reason);
            cfgPanel.displayResult(reason);

            cfgPanel.setLayout(new GridLayout());
            ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
            Content content = contentFactory.createContent(cfgPanel, "CFG", false);
            content.setCloseable(true);
            content.setIcon(TestCubeIcons.AMPLIFY_TEST);
            ToolWindow toolWindow = ToolWindowManager.getInstance(event.getProject()).getToolWindow("Test Cube");
            toolWindow.getContentManager().addContent(content);
            toolWindow.getContentManager().setSelectedContent(content);

            toolWindow.show();
        }
    }
}
