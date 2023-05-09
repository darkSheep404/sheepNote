package com.darksheep.sheepnote.editor.failtest;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.VisualPosition;
import com.intellij.openapi.editor.ex.EditorGutterComponentEx;
import com.intellij.openapi.editor.impl.EditorImpl;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * 05
 * EditorAction（编辑器动作）：通过实现 AnAction，可以创建一个可以在编辑器中执行的自定义操作。通过监听文件编辑器打开的事件，你可以为特定行尾添加临时文本
 * 需要配合在右键菜单中新增才可以show text
 * 且文本不适用 删除
 */
public class CustomEditorAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) return;

        Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        if (editor == null) return;

        final int targetLineNumber = 19;

        // Ensure the line number is valid
        if (editor.getDocument().getLineCount() - 1 < targetLineNumber) return;

        int lineEndOffset = editor.getDocument().getLineEndOffset(targetLineNumber);
        VisualPosition lineEndVisualPosition = editor.offsetToVisualPosition(lineEndOffset);

        // Draw text
        EditorGutterComponentEx gutter = ((EditorImpl) editor).getGutterComponentEx();
        /**
         * 给出的方法不存在无法使用
         */
        //gutter.drawString(editor.textToVisualPosition(new VisualPosition(lineEndVisualPosition.getLine(), lineEndVisualPosition.getColumn() + 1)), "//HelloWorld", gutter.getBackground().darker());
        gutter.createToolTip();
    }
}
