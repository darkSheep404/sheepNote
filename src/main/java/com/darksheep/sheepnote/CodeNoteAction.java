package com.darksheep.sheepnote;

import com.darksheep.sheepnote.config.NoteDataRepository;
import com.darksheep.sheepnote.data.NoteData;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class CodeNoteAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        // 获取当前编辑器对象
        Editor editor = e.getRequiredData(CommonDataKeys.EDITOR);
        // 获取选中的代码
        String selectedText = editor.getSelectionModel().getSelectedText();
        // 如果没有选中代码，弹出提示
        if (StringUtil.isEmpty(selectedText)) {
            Messages.showMessageDialog("请先选中要做笔记的代码！", "提示", Messages.getInformationIcon());
            return;
        }

        /**
         *
         */
        SelectionModel selectionModel = editor.getSelectionModel();
        Document document = editor.getDocument();
        int startOffset = selectionModel.getSelectionStart();
        int startLine = document.getLineNumber(startOffset) + 1;
        VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(document);
        String filePath = virtualFile != null ? virtualFile.getPath() : "";

        /**
         * Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
         * if (editor == null) {
         *     return;
         * }
         *
         * SelectionModel selectionModel = editor.getSelectionModel();
         * Document document = editor.getDocument();
         * int startOffset = selectionModel.getSelectionStart();
         * int endOffset = selectionModel.getSelectionEnd();
         * int startLine = document.getLineNumber(startOffset) + 1;
         * int endLine = document.getLineNumber(endOffset) + 1;
         * VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(document);
         * String filePath = virtualFile != null ? virtualFile.getPath() : "";
         *
         * String selectedText = selectionModel.getSelectedText();
         * if (selectedText != null && !selectedText.isEmpty()) {
         *     // 调用上面的方法显示笔记对话框，并传入选中的代码和文件信息
         *     showNoteDialog(selectedText, filePath, startLine, endLine);
         * }
         */


        // 创建一个 DialogBuilder 对象
        DialogBuilder dialogBuilder = new DialogBuilder(e.getProject());
        // 设置 Dialog 的标题
        dialogBuilder.setTitle("代码笔记");
        // 设置 Dialog 的内容
        dialogBuilder.setCenterPanel(createNotePanel(selectedText, dialogBuilder,startLine,filePath));
        // 添加确认按钮
        dialogBuilder.addOkAction().setText("保存");
        // 添加取消按钮
        dialogBuilder.addCancelAction();
        // 显示 Dialog
        dialogBuilder.show();
        //获取文件路径
        // var file_key = ((ArrayBackedFMap) ((DocumentImpl) ((EditorImpl) editor).myDocument).value).getKeys()[0]
        //((ArrayBackedFMap) ((DocumentImpl) ((EditorImpl) editor).myDocument).value).getKey(file_key)
    }

    /**
     * 创建笔记面板
     *
     * @param selectedText  选中的代码
     * @param dialogBuilder DialogBuilder 对象
     * @return 笔记面板
     */
    private JPanel createNotePanel(String selectedText, DialogBuilder dialogBuilder,int startLine,String filePath) {
        // 创建一个笔记面板，使用 BorderLayout 布局
        JPanel notePanel = new JPanel(new BorderLayout());

        // 添加一个标签，提示用户输入笔记标题
        notePanel.add(new JLabel("笔记标题："), BorderLayout.NORTH);
        // 创建一个文本框，用于输入笔记标题
        JTextField titleTextField = new JTextField("测试");
        // 把文本框添加到笔记面板中
        notePanel.add(titleTextField, BorderLayout.SOUTH);

        /**
         * 笔记内容输入框:滚动面板
         */
        // 创建一个滚动面板
        JBScrollPane scrollPane = new JBScrollPane();
        // 创建一个文本域
        JBTextArea noteTextArea = new JBTextArea();
        // 设置文本域的字体大小
        noteTextArea.setFont(noteTextArea.getFont().deriveFont(16f));
        // 设置文本域的默认内容为选中的文本
        noteTextArea.setText(selectedText);
        // 把文本域添加到滚动面板中
        scrollPane.setViewportView(noteTextArea);
        // 把滚动面板添加到笔记面板中
        notePanel.add(scrollPane, BorderLayout.CENTER);


        // 给 DialogBuilder 添加确认按钮的监听器，用于保存笔记
        dialogBuilder.setOkOperation(() -> {
            // 获取用户输入的笔记标题
            String title = titleTextField.getText();
            // 获取用户输入的笔记内容
            String note = noteTextArea.getText();
            // 如果笔记标题或笔记内容为空，弹出提示
            if (StringUtil.isEmpty(title) || StringUtil.isEmpty(note)) {
                Messages.showMessageDialog("Please enter both title and note", "Error", Messages.getErrorIcon());
                return;
            }
            try{
                NoteData noteData = new NoteData(title, note,startLine, filePath);
                NoteDataRepository.insert(noteData);

            }
            catch (Exception e){
                e.printStackTrace();
            }
            // 关闭 Dialog
            dialogBuilder.getDialogWrapper().close(DialogWrapper.OK_EXIT_CODE);
        });
        dialogBuilder.setCancelOperation(()->{
            System.out.println(noteTextArea);
            dialogBuilder.getDialogWrapper().close(DialogWrapper.CANCEL_EXIT_CODE);
        });

        // 显示 Dialog
        /**
         *  2023.05.03修正: 解决弹出弹窗后没有输入页面的问题
         *  dialogBuilder.show();
         *  在 createNotePanel 方法中，DialogBuilder 的 show() 方法在返回 notePanel 前被调用，因此对话框会在调用 show() 方法时弹出并阻塞线程，导致用户无法在 notePanel 上进行输入
         *  为了解决这个问题，可以在 createNotePanel 方法中先返回 notePanel，再在外部的调用方法中调用 DialogBuilder 的 show() 方法，使得对话框可以在 notePanel 调用后弹出
         *  --promot : 分析一下以上代码会存在什么问题 如何修复
         *  -- 无效promot : 仅粘贴本方法代码 询问 运行时 没有显示可以输入的文本框 分析一下可能的原因和改正方式
          */
        return notePanel;
    }

}