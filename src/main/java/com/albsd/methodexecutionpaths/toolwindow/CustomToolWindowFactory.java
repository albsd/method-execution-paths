package com.albsd.methodexecutionpaths.toolwindow;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class CustomToolWindowFactory implements ToolWindowFactory {

    private static final String EXAMPLE_STR =
            """
            
            Usage examples:
            
            foo
            foo(int, String)
            Example.foo
            int foo()
            int foo(int, String)
            Example.foo(int, String)
            Example.int foo(int, String)
            Example.int foo()
            """;

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        JPanel panel = new JPanel();
        panel.add(new JLabel("Select the method from which you want to start.\nPress Alt + Enter (or Option + Enter or  Right Click + Show Context Actions) then select \"Track execution paths\".\n" + EXAMPLE_STR));

        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(panel, "", false);
        toolWindow.getContentManager().addContent(content);
    }

    public static void showSimpleToolWindow(Project project, String message) {
        ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow("Method Execution Paths");

        if (toolWindow == null) {
            return;
        }

        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel("Execution Paths:");
        JTextArea textArea = new JTextArea(10, 30);
        textArea.setText(message);
        textArea.setEditable(false);

        panel.add(label, BorderLayout.NORTH);
        panel.add(new JBScrollPane(textArea), BorderLayout.CENTER);

        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(panel, "", false);

        toolWindow.getContentManager().removeAllContents(true);
        toolWindow.getContentManager().addContent(content);

        toolWindow.show(null);
    }
}
