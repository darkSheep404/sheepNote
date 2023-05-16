package com.darksheep.sheepnote.editor.utils;

import com.darksheep.sheepnote.data.NoteData;
import com.darksheep.sheepnote.editor.failtest.NoteDataHandler;
import com.darksheep.sheepnote.editor.gutter.GutterAction;
import com.darksheep.sheepnote.editor.gutter.NoteGutterIconLineMarkerProvider;
import com.darksheep.sheepnote.toolWindow.NoteListToolWindowFactory;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorCustomElementRenderer;
import com.intellij.openapi.editor.EditorGutterAction;
import com.intellij.openapi.editor.Inlay;
import com.intellij.openapi.editor.InlayModel;
import com.intellij.openapi.editor.TextAnnotationGutterProvider;
import com.intellij.openapi.editor.VisualPosition;
import com.intellij.openapi.editor.colors.ColorKey;
import com.intellij.openapi.editor.colors.EditorFontType;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.ui.JBColor;
import com.intellij.util.Consumer;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import static com.darksheep.sheepnote.editor.gutter.NoteGutterIconRenderer.createGutterIconRenderer;

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

        editor.getGutter().registerTextAnnotation(getProvider(noteData), new EditorGutterAction() {
            @Override
            public void doAction(int lineNum) {
                VisualPosition visPosLineStart = new VisualPosition(noteData.noteLineNumber -1 , 0, false);

                // 将视觉位置转换为像素点坐标
                Point point = editor.visualPositionToXY(visPosLineStart);

                System.out.println(123456789);
                // 创建点击图标时显示的弹窗
                JDialog dialog = new JDialog();
                dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                dialog.setTitle("笔记信息");

                // 添加显示信息的面板
                JPanel panel = new JPanel(new GridLayout(3, 1));
                panel.add(new JLabel("标题: " + noteData.noteTitle));
                panel.add(new JLabel("创建时间: " + noteData.getCreateTime().toString()));
                // 添加 locate 按钮
                JButton locateButton = new JButton("Locate");
                locateButton.addActionListener(a -> {

                    // 查看和选中相应的笔记
                    ToolWindow toolWindow = ToolWindowManager.getInstance(editor.getProject())
                            .getToolWindow(NoteListToolWindowFactory.TOOL_WINDOW_ID);
                    if (toolWindow != null && !toolWindow.isVisible()) {
                        toolWindow.show();
                    }

                    // 添加定位并选中对应笔记的逻辑
                });

                locateButton.addActionListener(a -> {
                    // 查看工具窗口并选中相应的笔记
                    ToolWindow toolWindow = ToolWindowManager.getInstance(editor.getProject())
                            .getToolWindow(NoteListToolWindowFactory.TOOL_WINDOW_ID);
                    if (toolWindow != null && !toolWindow.isVisible()) {
                        toolWindow.show();
                    }

                    // 定位并选中对应的笔记
                    NoteDataHandler.selectNoteInList(toolWindow,noteData);
                });
                panel.add(locateButton);
                dialog.add(panel);

                dialog.pack();
                //设置为null为弹窗在屏幕正中间
                dialog.setLocationRelativeTo(editor.getContentComponent());
                dialog.setLocation(new Point(point.x, point.y - dialog.getHeight()));
                dialog.setVisible(true);
            }

            @Override
            public Cursor getCursor(int lineNum) {
                return null;
            }
        });
    }

    @NotNull
    private static TextAnnotationGutterProvider getProvider(NoteData noteData) {
        return new TextAnnotationGutterProvider() {

            @Override
            public @Nullable String getLineText(int line, Editor editor) {
                if (noteData.noteLineNumber - 1 == line)
                    return noteData.noteTitle;
                else return null;
            }

            @Nullable
            @Override
            public String getToolTip(int line, Editor editor) {
                return noteData.noteTitle;
            }

            @Override
            public EditorFontType getStyle(int line, Editor editor) {
                return EditorFontType.BOLD;
            }

            @Override
            public @Nullable ColorKey getColor(int line, Editor editor) {
                return null;
            }

            @Override
            public @Nullable Color getBgColor(int line, Editor editor) {
                return null;
            }

            @Override
            public List<AnAction> getPopupActions(int line, Editor editor) {
                List<AnAction> actions = new ArrayList<>();
                actions.add(new GutterAction(noteData));
                return actions;
            }

            @Override
            public void gutterClosed() {

            }

          /*  @Nullable
            @Override
            public Consumer<MouseEvent> createGutterRenderer(int line, Editor editor) {
                if (line == noteData.noteLineNumber - 1) {
                    return event -> clickListener.run();
                }
                return null;
            }

            @Nullable
            @Override
            public Icon getIcon(int line, Editor editor) {
                if (line == noteData.noteLineNumber - 1) {
                    return icon;
                }
                return null;
            }*/
        };
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
