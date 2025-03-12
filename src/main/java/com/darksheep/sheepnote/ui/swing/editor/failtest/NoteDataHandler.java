package com.darksheep.sheepnote.ui.swing.editor.failtest;

import com.darksheep.sheepnote.config.NoteDataRepository;
import com.darksheep.sheepnote.data.NoteData;
import com.darksheep.sheepnote.ui.swing.toolWindow.NoteListToolWindowFactory;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;

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
                    List<NoteData> noteList = NoteDataRepository.getAllNoteData();
                    int size = noteList.size();
                    for (int i = 0; i < size; i++) {
                        addToNotesForFilePath(noteList.get(i));
                    }
                }
            }
        });
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
}
