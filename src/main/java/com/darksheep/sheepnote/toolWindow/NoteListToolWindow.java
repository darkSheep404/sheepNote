package com.darksheep.sheepnote.toolWindow;

import com.darksheep.sheepnote.data.NoteData;
import com.intellij.openapi.wm.ToolWindow;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

import static com.intellij.structuralsearch.plugin.ui.UIUtil.setContent;

/**
 * UI相关布局代码
 */
public class NoteListToolWindow {
   /*  extends ToolWindow

    private JList<String> noteList;
    private JTextField searchField;

    public NoteListToolWindow() {
        super("DarkSheep Note List");

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // 左侧
        JPanel leftPanel = new JPanel(new BorderLayout());

        // 第一部分：排序按钮
        JPanel sortPanel = new JPanel();
        JButton sortCreateTimeButton = new JButton("Sort by create time");
        JButton sortUpdateTimeButton = new JButton("Sort by update time");
        sortPanel.add(sortCreateTimeButton);
        sortPanel.add(sortUpdateTimeButton);
        leftPanel.add(sortPanel, BorderLayout.NORTH);

        // 第二部分：搜索框
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchField = new JTextField();
        searchField.setColumns(20);
        searchPanel.add(searchField, BorderLayout.CENTER);
        leftPanel.add(searchPanel, BorderLayout.CENTER);

        // 第三部分：笔记列表
        noteList = new JList<>();
        JScrollPane noteListScrollPane = new JScrollPane(noteList);
        leftPanel.add(noteListScrollPane, BorderLayout.SOUTH);

        contentPanel.add(leftPanel, BorderLayout.WEST);

        // 右侧
        JPanel rightPanel = new JPanel(new BorderLayout());
        JLabel noteTitleLabel = new JLabel();
        JLabel noteFilePathLabel = new JLabel();
        JLabel selectCodeLabel = new JLabel();
        rightPanel.add(noteTitleLabel, BorderLayout.NORTH);
        rightPanel.add(noteFilePathLabel, BorderLayout.CENTER);
        rightPanel.add(selectCodeLabel, BorderLayout.SOUTH);

        contentPanel.add(rightPanel, BorderLayout.CENTER);

        setContent(contentPanel);
    }

    public void setNoteList(List<NoteData> noteDataList) {
        DefaultListModel<String> model = new DefaultListModel<>();
        for (NoteData noteData : noteDataList) {
            String noteTitle = noteData.noteTitle;
            String noteFilePath = noteData.noteFilePath;
            int noteLineNumber = noteData.noteLineNumber;
            String fileName = noteFilePath.substring(noteFilePath.lastIndexOf("/") + 1);
            model.addElement(noteTitle + " " + fileName + ":" + noteLineNumber);
        }
        noteList.setModel(model);
    }*/
}

