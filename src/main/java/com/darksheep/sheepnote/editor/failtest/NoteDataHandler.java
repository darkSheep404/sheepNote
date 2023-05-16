package com.darksheep.sheepnote.editor.failtest;

import com.darksheep.sheepnote.data.NoteData;
import com.darksheep.sheepnote.toolWindow.NoteListToolWindowFactory;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.components.JBList;
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

    /**
     * 初始化时 根据UI层级 获取到scrollPane持有的笔记列表
     * 并把笔记列表 根据 笔记路径 作为key分类存入Map<String, List<NoteData>> notesForFilePath
     * 方便启动渲染时 直接获取到当前文件路径对应的笔记列表
     * 已经废弃 未使用 而且这样获取到的列表 不包含 新添加的笔记 记完笔记重新打开会丢失
     * @param project
     */
    @Deprecated
    public NoteDataHandler(Project project) {
        ApplicationManager.getApplication().invokeLater(() -> {
            ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow(NoteListToolWindowFactory.TOOL_WINDOW_ID);
            if(toolWindow != null) {

                Content content = toolWindow.getContentManager().getContent(0);
                if (content != null) {
                    DefaultListModel<NoteData> noteListModel = getNoteDataDefaultListModel(content);
                    int size = noteListModel.getSize();
                    for (int i = 0; i < size; i++) {
                        addToNotesForFilePath(noteListModel.getElementAt(i));
                    }
                }
            }
        });
    }

    public static DefaultListModel<NoteData> getNoteDataDefaultListModel(Content content) {
        JSplitPane mainPanel = (JSplitPane) content.getComponent();
        JPanel leftPanel = (JPanel) mainPanel.getLeftComponent();
        JPanel noteListWrapperPanel = (JPanel)leftPanel.getComponent(2);

        JScrollPane scrollPane = (JScrollPane)noteListWrapperPanel.getComponent(0);
        JList<?> list = (JList<?>) scrollPane.getViewport().getView();
        @SuppressWarnings("unchecked")
        DefaultListModel<NoteData> noteListModel = (DefaultListModel<NoteData>) list.getModel();
        return noteListModel;
    }

    public  static void selectNoteInList(ToolWindow toolWindow,NoteData noteData){
        Content content = toolWindow.getContentManager().getContent(0);
        JSplitPane mainPanel = (JSplitPane) content.getComponent();
        JPanel leftPanel = (JPanel) mainPanel.getLeftComponent();
        JPanel noteListWrapperPanel = (JPanel)leftPanel.getComponent(2);
        JScrollPane scrollPane = (JScrollPane)noteListWrapperPanel.getComponent(0);
        JList<?> list = (JList<?>) scrollPane.getViewport().getView();
        @SuppressWarnings("unchecked")
        DefaultListModel<NoteData> noteListModel = (DefaultListModel<NoteData>) list.getModel();
        int index = 0;

        for (int i = 0; i < noteListModel.size(); i++) {
            NoteData currentNote = noteListModel.get(i);
            if(noteData.id == currentNote.id ){
                index = i;
                break;
            }
        }

        JBList<NoteData> noteList = (JBList<NoteData>) scrollPane.getViewport().getView();
        noteList.updateUI();
        noteList.setSelectedIndex(index);
        noteList.ensureIndexIsVisible(index);

    }

    @Deprecated
    public List<NoteData> getNotesForFile(String filePath) {
        return notesForFilePath.getOrDefault(filePath, Collections.emptyList());
    }
    @Deprecated
    private void addToNotesForFilePath(NoteData noteData) {
        List<NoteData> notes = notesForFilePath.computeIfAbsent(
                noteData.noteFilePath,
                k -> new ArrayList<>());
        notes.add(noteData);
    }
    /*public void selectNoteInList(NoteData noteData,DefaultListModel<NoteData> noteListModel) {
        for (int i = 0; i < noteListModel.getSize(); i++) {
            NoteData currentNote = noteListModel.get(i);
            if (noteData.id == currentNote.id) {
                noteList.setSelectedIndex(i);
                noteList.ensureIndexIsVisible(i);
                break;
            }
        }
    }*/
}
