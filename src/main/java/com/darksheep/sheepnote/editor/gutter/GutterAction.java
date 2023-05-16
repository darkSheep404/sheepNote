package com.darksheep.sheepnote.editor.gutter;

import com.darksheep.sheepnote.data.NoteData;
import com.darksheep.sheepnote.toolWindow.NoteListToolWindowFactory;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class GutterAction extends AnAction {
        private NoteData noteData;

        public GutterAction(NoteData noteData) {
            this.noteData = noteData;
        }

    @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
        // 创建点击图标时显示的弹窗
        JDialog dialog = new JDialog();
        dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        dialog.setTitle("笔记信息");

        // 添加显示信息的面板
        JPanel panel = new JPanel(new GridLayout(3, 1));
        panel.add(new JLabel("标题: " + noteData.noteTitle));
        panel.add(new JLabel("创建时间: " + noteData.getCreateTime().toString()));
        Editor editor = e.getRequiredData(CommonDataKeys.EDITOR);
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

            //TODO 此处可以提供ToolWindow 直接获取对应的Factory 以后变量传递可以优化
            // 定位并选中对应的笔记
            NoteListToolWindowFactory noteListToolWindowFactory = (NoteListToolWindowFactory) toolWindow.getContentManager().getFactory();
            noteListToolWindowFactory.selectNoteInList(noteData);
        });
        panel.add(locateButton);
        dialog.add(panel);

        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }
}
