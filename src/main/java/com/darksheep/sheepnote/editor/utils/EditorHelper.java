package com.darksheep.sheepnote.editor.utils;

import com.darksheep.sheepnote.data.NoteData;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorCustomElementRenderer;
import com.intellij.openapi.editor.Inlay;
import com.intellij.openapi.editor.InlayModel;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.JBColor;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.List;



public class EditorHelper {


    /**
     * 删除笔记时 删除对应的渲染文本
     * @param noteData
     * @param project
     */
    public static void removeTextRenderInEditor(NoteData noteData, Project project) {
        Editor activeEditor = getActiveEditor(project);
        VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(activeEditor.getDocument());
        if (virtualFile == null) {
            return;
        }
        if(StringUtils.equals(virtualFile.getPath(),noteData.noteFilePath)){
            InlayModel inlayModel = activeEditor.getInlayModel();
            List<Inlay<?>> afterLineEndElements = inlayModel.getAfterLineEndElementsForLogicalLine(noteData.noteLineNumber - 1);
            for (Inlay<?> afterLineEndElement : afterLineEndElements) {
                //判定是否是本插件建立的渲染
                if(afterLineEndElement.getRenderer() instanceof CustomTextRenderer){
                    CustomTextRenderer  renderer = (CustomTextRenderer) afterLineEndElement.getRenderer();
                    if(StringUtils.equals(renderer.text,noteData.noteTitle)){
                        // afterLineEndElement.dispose();
                        //不推荐直接调用自身的dispose() 方法 尝试改为 调用
                        Disposer.dispose(afterLineEndElement);
                }
                }
            }
        }
    }

    public static Editor getActiveEditor(Project project) {
        // 获取当前的 FileEditorManager 实例
        FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);

        // 获取当前选择的文件编辑器
        FileEditor fileEditor = fileEditorManager.getSelectedEditor();
        if (fileEditor instanceof TextEditor) {
            // 如果选择的文件编辑器是一个文本编辑器，那么我们可以从它那里获取活跃的 Editor 实例
            return ((TextEditor) fileEditor).getEditor();
        } else {
            // 如果没有选择任何编辑器或选择的编辑器不是文本编辑器，返回空
            return null;
        }
    }

    public static void drawNoteAddNoteNumber(Editor editor,NoteData noteData){
        if(editor.getDocument().getLineCount()< noteData.noteLineNumber)
            return;
        int lineEndOffset = editor.getDocument().getLineEndOffset(noteData.noteLineNumber -1);
        EditorCustomElementRenderer renderer = new CustomTextRenderer(noteData.noteTitle);
        editor.getInlayModel().addAfterLineEndElement(lineEndOffset, true, renderer);
        //editor.getMarkupModel().addGutterIconRenderer(noteData.noteLineNumber - 1, createGutterIconRenderer(noteData, editor));
    }




    static class CustomTextRenderer implements EditorCustomElementRenderer {

        public String text;

        public CustomTextRenderer(String text) {
            this.text = text;
        }

        @Override
        public int calcWidthInPixels(@NotNull Inlay inlay) {
            return 80;
        }

        @Override
        public void paint(@NotNull Inlay inlay, @NotNull Graphics g, @NotNull Rectangle targetRegion, @NotNull TextAttributes textAttributes) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setColor(JBColor.yellow);
            Font font = new Font("Microsoft YaHei", Font.PLAIN, g.getFont().getSize());
            FontMetrics fontMetrics = g2d.getFontMetrics(font);
            g2d.setFont(font);
            g2d.drawString("//"+text, targetRegion.x, targetRegion.y + fontMetrics.getAscent());
        }
    }


}
