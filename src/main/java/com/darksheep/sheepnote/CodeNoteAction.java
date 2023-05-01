package com.darksheep.sheepnote;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
        // 创建一个 DialogBuilder 对象
        DialogBuilder dialogBuilder = new DialogBuilder(e.getProject());
        // 设置 Dialog 的标题
        dialogBuilder.setTitle("代码笔记");
        // 设置 Dialog 的内容
        dialogBuilder.setCenterPanel(createNotePanel(selectedText, dialogBuilder));
        // 添加确认按钮
        dialogBuilder.addOkAction().setText("保存");
        // 添加取消按钮
        dialogBuilder.addCancelAction();
        // 显示 Dialog
        dialogBuilder.show();
    }

    /**
     * 创建笔记面板
     *
     * @param selectedText  选中的代码
     * @param dialogBuilder DialogBuilder 对象
     * @return 笔记面板
     */
    private JPanel createNotePanel(String selectedText, DialogBuilder dialogBuilder) {
        JPanel notePanel = new JPanel(new BorderLayout());
        // 创建一个滚动面板
        JBScrollPane scrollPane = new JBScrollPane();
        // 创建一个文本域
        JBTextArea noteTextArea = new JBTextArea();
        // 设置文本域的字体大小
        noteTextArea.setFont(noteTextArea.getFont().deriveFont(16f));
        // 设置文本域的默认内容
        noteTextArea.setText(selectedText);
        // 把文本域添加到滚动面板中
        scrollPane.setViewportView(noteTextArea);
        // 把滚动面板添加到笔记面板中
        notePanel.add(scrollPane, BorderLayout.CENTER);

        // 添加一个标签，提示用户输入笔记标题
        notePanel.add(new JLabel("笔记标题："), BorderLayout.NORTH);
        // 创建一个文本框，用于输入笔记标题
        JTextField titleTextField = new JTextField();
        // 把文本框添加到笔记面板中
        notePanel.add(titleTextField, BorderLayout.SOUTH);

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
            // 在控制台输出笔记内容
            System.out.println("Note Title: " + title);
            System.out.println("Note Content: " + note);
            // 关闭 Dialog
            dialogBuilder.getDialogWrapper().close(DialogWrapper.OK_EXIT_CODE);
        });

        // 显示 Dialog
        dialogBuilder.show();
        return notePanel;
    }

}