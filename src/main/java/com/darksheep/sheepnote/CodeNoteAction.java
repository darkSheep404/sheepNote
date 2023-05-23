package com.darksheep.sheepnote;

import com.darksheep.sheepnote.config.AddNoteEventListener;
import com.darksheep.sheepnote.config.NoteDataRepository;
import com.darksheep.sheepnote.data.NoteData;
import com.google.common.eventbus.EventBus;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import com.intellij.util.messages.MessageBus;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.Date;

public class CodeNoteAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project currentProject = IdeFocusManager.getGlobalInstance().getLastFocusedFrame().getProject();
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
        Project currentProject = IdeFocusManager.getGlobalInstance().getLastFocusedFrame().getProject();
        JPanel notePanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.insets = new Insets(2, 2, 10, 2);

        // Note title label
        JLabel titleLabel = new JLabel("Note Title");
        notePanel.add(titleLabel, gbc);

        // Note title input
        JTextField titleTextField = new JTextField();
        gbc.gridy = 1;
        notePanel.add(titleTextField, gbc);

        // Code label
        JLabel codeLabel = new JLabel("Code");
        gbc.gridy = 2;
        notePanel.add(codeLabel,gbc);

        // Code input
        JTextArea codeTextArea = new JTextArea();
        codeTextArea.setLineWrap(true);
        codeTextArea.setWrapStyleWord(true);
        JBScrollPane scrollPane = new JBScrollPane(codeTextArea);
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1;
        codeTextArea.setText(selectedText);
        codeTextArea.setFont(codeTextArea.getFont().deriveFont(16f));
        FontMetrics fontMetrics = titleTextField.getFontMetrics(titleTextField.getFont());
        int textWidth = fontMetrics.stringWidth(codeTextArea.getText()) + 20; // 附加一些额外的宽度以确保留有空间
        int newWidth = Math.max(textWidth, 300); // 限制对话框宽度至少为300像素
        Dimension newDimension = new Dimension(newWidth, 200);
        notePanel.setPreferredSize(newDimension);
        notePanel.add(scrollPane, gbc);

        /**
         * 笔记内容输入框:滚动面板
         */
      /* */


        // 给 DialogBuilder 添加确认按钮的监听器，用于保存笔记
        dialogBuilder.setOkOperation(() -> {
            // 获取用户输入的笔记标题
            String title = titleTextField.getText();
            // 获取用户输入的笔记内容
            String code = codeTextArea.getText();
            // 如果笔记标题或笔记内容为空，弹出提示
            if (StringUtil.isEmpty(title) || StringUtil.isEmpty(code)) {
                Messages.showMessageDialog("Please enter both title and note", "Error", Messages.getErrorIcon());
                return;
            }
            try{
                NoteData noteData = new NoteData(title,filePath,startLine, code);
                int id = NoteDataRepository.insert(noteData);
                noteData.id = id;
                noteData.createTime =new Date();
                noteData.updateTime = new Date();
                // 发布事件以刷新 UI
                MessageBus messageBus = currentProject.getMessageBus();
                messageBus.syncPublisher(AddNoteEventListener.ADD_NOTE_TOPIC).onAddNoteEvent(noteData);


            }
            catch (Exception e){
                e.printStackTrace();
            }
            // 关闭 Dialog
            dialogBuilder.getDialogWrapper().close(DialogWrapper.OK_EXIT_CODE);
        });
        dialogBuilder.setCancelOperation(()->{
            System.out.println(codeTextArea);
            dialogBuilder.getDialogWrapper().close(DialogWrapper.CANCEL_EXIT_CODE);
        });

        // 显示 Dialog
        return notePanel;
    }

}