package com.darksheep.sheepnote.editor;

import com.darksheep.sheepnote.config.NoteDataRepository;
import com.darksheep.sheepnote.data.NoteData;
import com.darksheep.sheepnote.editor.utils.EditorHelper;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 在打开该文件时使用 EditorCustomElementRenderer，为编辑器添加自定义渲染文本
 * <applicationListeners>
 *     <listener class="com.example.CustomEditorFileOpenedListener"/>
 * </applicationListeners>
 */
public class CustomEditorFileOpenedListener implements FileEditorManagerListener {
    @Override
    public void fileOpened(@NotNull FileEditorManager manager, @NotNull VirtualFile file) {
        Editor editor = manager.getSelectedTextEditor();

        if (editor == null) {
            return;
        }
        VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(editor.getDocument());
        if (virtualFile == null) {
            return;
        }
        /**
         * 原写法会报空指针
         * List<NoteData> noteDataList = NoteListToolWindowFactory.getNoteDataHandler().getNotesForFile(virtualFile.getPath());
         * 因为
         * 加载 NoteListToolWindowFactory 获取到的NoteDataHandler() 为空
         * NoteDataHandler()在createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) 中赋值
         * 需要借助Project project 获取ToolsWindow进而获取数据
         */

        List<NoteData> noteDataList = null;
        try {
            noteDataList = NoteDataRepository.getAllNoteData();
            noteDataList = noteDataList.stream().filter(noteData -> StringUtils.equals(virtualFile.getPath(),noteData.getNoteFilePath()) ).collect(Collectors.toList());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        for (NoteData noteData : noteDataList) {
            EditorHelper.drawNoteAddNoteNumber(editor,noteData);
        }
    }

    // You can leave fileClosed and selectionChanged methods empty or implement functionality as needed.
    @Override
    public void fileClosed(@NotNull FileEditorManager manager, @NotNull VirtualFile file) {
    }

    @Override
    public void selectionChanged(@NotNull FileEditorManagerEvent event) {
    }
}
