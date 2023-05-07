package com.darksheep.sheepnote.toolWindow;

import com.darksheep.sheepnote.data.NoteData;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class NoteListPanel extends JPanel {
    private JList<NoteData> noteList;
    private DefaultListModel<NoteData> listModel;

    public NoteListPanel() {
        super(new BorderLayout());
        initUI();
    }

    private void initUI() {
        // 创建笔记列表控件
        noteList = new JList<>();
        listModel = new DefaultListModel<>();
        noteList.setModel(listModel);
        JScrollPane scrollPane = new JScrollPane(noteList);

        // 添加控件到面板中
        add(scrollPane, BorderLayout.CENTER);

        // 创建排序按钮和搜索框
        JPanel topPanel = new JPanel(new FlowLayout());
        JButton sortByCreateTimeBtn = new JButton("按创建时间排序");
        JButton sortByUpdateTimeBtn = new JButton("按修改时间排序");
        JTextField searchField = new JTextField(20);
        searchField.setToolTipText("输入笔记标题进行搜索");
        topPanel.add(sortByCreateTimeBtn);
        topPanel.add(sortByUpdateTimeBtn);
        topPanel.add(searchField);
        add(topPanel, BorderLayout.NORTH);

        // 给排序按钮和搜索框添加监听器
        sortByCreateTimeBtn.addActionListener(e -> {
            // TODO: 实现按创建时间排序功能
        });

        sortByUpdateTimeBtn.addActionListener(e -> {
            // TODO: 实现按修改时间排序功能
        });

        searchField.addActionListener(e -> {
            // TODO: 实现搜索功能
        });
    }

    public void updateNoteList(List<NoteData> noteDataList) {
        // 清空列表中的所有内容
        listModel.clear();

        // 将新的笔记列表添加到列表中
        for (NoteData noteData : noteDataList) {
            listModel.addElement(noteData);
        }
    }
}

