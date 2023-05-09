package com.darksheep.sheepnote.editor;

import com.darksheep.sheepnote.data.NoteData;
import com.darksheep.sheepnote.toolWindow.NoteListToolWindowFactory;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;




//@Service
public final class NoteDataHandler {
    private Map<String, List<NoteData>> notesForFilePath = new HashMap<>();

    public NoteDataHandler(Project project) {
        ApplicationManager.getApplication().invokeLater(() -> {
            ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow(NoteListToolWindowFactory.TOOL_WINDOW_ID);
            if(toolWindow != null) {

                Content content = toolWindow.getContentManager().getContent(0);
                if (content != null) {
                    JSplitPane mainPanel = (JSplitPane)content.getComponent();
                    JPanel leftPanel = (JPanel) mainPanel.getLeftComponent();
                    JPanel noteListWrapperPanel = (JPanel)leftPanel.getComponent(2);

                    JScrollPane scrollPane = (JScrollPane)noteListWrapperPanel.getComponent(0);
                    JList<?> list = (JList<?>) scrollPane.getViewport().getView();
                    @SuppressWarnings("unchecked")
                    DefaultListModel<NoteData> noteListModel = (DefaultListModel<NoteData>) list.getModel();
                    int size = noteListModel.getSize();
                    for (int i = 0; i < size; i++) {
                        addToNotesForFilePath(noteListModel.getElementAt(i));
                    }
                }
            }
        });
    }

    public List<NoteData> getNotesForFile(String filePath) {
        return notesForFilePath.getOrDefault(filePath, Collections.emptyList());
    }

    private void addToNotesForFilePath(NoteData noteData) {
        List<NoteData> notes = notesForFilePath.computeIfAbsent(
                noteData.noteFilePath,
                k -> new ArrayList<>());
        notes.add(noteData);
    }
}
