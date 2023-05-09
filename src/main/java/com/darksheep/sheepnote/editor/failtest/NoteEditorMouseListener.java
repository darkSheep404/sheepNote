package com.darksheep.sheepnote.editor.failtest;

import com.darksheep.sheepnote.data.NoteData;
import com.darksheep.sheepnote.toolWindow.NoteListToolWindowFactory;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.EditorFactoryEvent;
import com.intellij.openapi.editor.event.EditorFactoryListener;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class NoteEditorMouseListener implements EditorFactoryListener {

    public NoteEditorMouseListener() {
        super();
        System.out.println("123456");
    }

    @Override
    public void editorCreated(@NotNull EditorFactoryEvent event) {
        System.out.println(1234567);
        Editor editor = event.getEditor();
        VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(editor.getDocument());
        if (virtualFile == null) {
            return;
        }

        List<NoteData> noteDataList = NoteListToolWindowFactory.getNoteDataHandler().getNotesForFile(virtualFile.getPath());

        if (noteDataList.isEmpty()) {
            return;
        }

        createInlaysForNotes(noteDataList, editor);
    }
    private void createInlaysForNotes(List<NoteData> noteDataList, Editor editor) {
        // 使用行尾偏移量，而非行起始偏移量
        for (NoteData noteData : noteDataList) {
            Document document = editor.getDocument();
            int offset = document.getLineEndOffset(noteData.noteLineNumber - 1);
            addInlay(noteData.noteTitle, editor, offset);
        }
    }

    private void addInlay(String text, Editor editor, int lineNumber) {
        Document document = editor.getDocument();
        int offset = document.getLineEndOffset(lineNumber - 1);

        SimpleLinePainter renderer = createRendererForInlay(text);
        int width = renderer.calcWidthInPixels(null);
        int height = renderer.calcHeightInPixels(null);
       editor.getInlayModel()
               .addInlineElement(offset,true,renderer);
    }

    private SimpleLinePainter createRendererForInlay(String text) {
        return new SimpleLinePainter(text);
    }
}
