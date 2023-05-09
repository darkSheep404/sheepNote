package com.darksheep.sheepnote.editor.failtest;

import com.darksheep.sheepnote.data.NoteData;
import com.darksheep.sheepnote.editor.failtest.SimpleLinePainter;
import com.darksheep.sheepnote.toolWindow.NoteListToolWindowFactory;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.changes.shelf.ShelvedChangesViewManager;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CustomNewFileListener implements ShelvedChangesViewManager.PostStartupActivity.DumbAware {
    @Override
    public void runActivity(@NotNull Project project) {
        if (!project.isDisposed()) {
            ApplicationManager.getApplication().getMessageBus().connect(project)
                    .subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER,
                            new FileEditorManagerListener() {
                                @Override
                                public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
                                    System.out.println("openFile"+file.getName()+file.getPath());
                                    FileEditor[] editors = source.getEditors(file);
                                    for (FileEditor fileEditor : editors) {
                                        if (fileEditor instanceof TextEditor) {
                                            Editor editor = ((TextEditor) fileEditor).getEditor();
                                            List<NoteData> noteDataList = NoteListToolWindowFactory.getNoteDataHandler().getNotesForFile(file.getPath());

                                            if (!noteDataList.isEmpty()) {
                                                createInlaysForNotes(noteDataList, editor);
                                            }
                                        }
                                    }
                                }
                            });
        }
    }
    private void createInlaysForNotes(List<NoteData> noteDataList, Editor editor) {
        // 使用行尾偏移量，而非行起始偏移量
        for (NoteData noteData : noteDataList) {
            Document document = editor.getDocument();
            int offset = document.getLineEndOffset(noteData.noteLineNumber - 1); // 修复这一行
            addInlay(noteData.noteTitle, editor, noteData.noteLineNumber - 1);
        }
    }

    private void addInlay(String text, Editor editor, int lineNumber) {
        Document document = editor.getDocument();
        //第一行33个字符 getLineEndOffset(0) 返回33 第二行1字符 getLineEndOffset(1)返回34
        int offset = document.getLineEndOffset(lineNumber-1);
        SimpleLinePainter renderer = createRendererForInlay(text);
        editor.getInlayModel()
                .addInlineElement(offset, true, renderer);
    }

    private SimpleLinePainter createRendererForInlay(String text) {
        return new SimpleLinePainter(text);
    }
}
