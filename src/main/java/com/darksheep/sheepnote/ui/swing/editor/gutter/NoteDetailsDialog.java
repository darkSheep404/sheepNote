package com.darksheep.sheepnote.ui.swing.editor.gutter;

import com.darksheep.sheepnote.data.NoteData;
import com.darksheep.sheepnote.ui.swing.editor.failtest.NoteDataHandler;
import com.darksheep.sheepnote.ui.swing.toolWindow.NoteListToolWindowFactory;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.components.JBLabel;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.SimpleDateFormat;

public class NoteDetailsDialog extends DialogWrapper {
    private final NoteData noteData;

    public NoteDetailsDialog(@Nullable Project project, NoteData noteData) {
        super(project);
        this.noteData = noteData;
        init();
        setTitle("Note Details");
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        JPanel panel = new JPanel(new BorderLayout());
        String createTime = dateFormat.format(noteData.createTime);
        String updateTime = dateFormat.format(noteData.updateTime);
        String details = String.format("Title: %s<br/>Create At: %s<br/> Update At:%s", noteData.noteTitle, createTime,updateTime);
        JBLabel label = new JBLabel("<html>" + details + "</html>");
        panel.add(label, BorderLayout.CENTER);
        return panel;
    }

    @Override
    protected Action[] createActions() {
        DialogWrapperAction locateAction = new DialogWrapperAction("Locate") {
            @Override
            protected void doAction(ActionEvent actionEvent) {
                Project currentProject = IdeFocusManager.getGlobalInstance().getLastFocusedFrame().getProject();
                ToolWindow toolWindow = ToolWindowManager.getInstance(currentProject)
                        .getToolWindow(NoteListToolWindowFactory.TOOL_WINDOW_ID);
                if (toolWindow != null && !toolWindow.isVisible()) {
                    toolWindow.show();
                }
                // 定位并选中对应的笔记
                NoteDataHandler.selectNoteInList(toolWindow,noteData);
                // Close the dialog when the button is clicked.
                close(DialogWrapper.OK_EXIT_CODE);
            }
        };

        // Set a default Icon for Locate button.
        locateAction.putValue(Action.SMALL_ICON, AllIcons.Actions.Find);

        return new Action[]{locateAction, getCancelAction()};
    }
}
