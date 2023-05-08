package com.darksheep.sheepnote.toolWindow;

import com.darksheep.sheepnote.data.NoteData;
import com.intellij.ui.components.JBList;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

// NoteListController.java
public class NoteListController {
    private final DefaultListModel<NoteData> noteListModel;
    private final JBList<NoteData> noteList;
    private final JTextField searchTextField;
    private final JButton sortByCreateTimeButton;
    private final JButton sortByUpdateTimeButton;

    private NoteDetailPanel rightPanel;
    private List<NoteData> noteDataList;

    public NoteListController(DefaultListModel<NoteData> noteListModel,
                              JBList<NoteData> noteList,
                              JTextField searchTextField,
                              JButton sortByCreateTimeButton,
                              JButton sortByUpdateTimeButton,NoteDetailPanel rightPanel) {
        this.noteListModel = noteListModel;
        this.noteList = noteList;
        this.searchTextField = searchTextField;
        this.sortByCreateTimeButton = sortByCreateTimeButton;
        this.sortByUpdateTimeButton = sortByUpdateTimeButton;
        this.rightPanel = rightPanel;
        this.noteDataList = new ArrayList<>();
        for (int i = 0; i < noteListModel.getSize(); i++) {
            noteDataList.add(noteListModel.getElementAt(i));
        }
        initComponents();
    }

    public void addNewNoteToNoteList(NoteData noteData){
        noteDataList.add(noteData);
    }

    private void initComponents() {
        // 初始化搜索框
        searchTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                searchNoteList();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                searchNoteList();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                searchNoteList();
            }
        });

        // 初始化排序按钮
        sortByCreateTimeButton.addActionListener(e -> {
            noteDataList.sort(Comparator.comparing(NoteData::getCreateTime).reversed());
            updateNoteListModel();
            noteList.repaint();
        });

        sortByUpdateTimeButton.addActionListener(e -> {
            noteDataList.sort(Comparator.comparing(NoteData::getUpdateTime).reversed());
            updateNoteListModel();
            noteList.repaint();
        });

        noteList.addListSelectionListener(e -> {
            NoteData selectedNote = noteList.getSelectedValue();
            if (selectedNote != null) {
                rightPanel.setNoteDetail(selectedNote);
            }
        });
    }

    private void searchNoteList() {
        String searchText = searchTextField.getText().trim().toLowerCase();
        if (searchText.isEmpty()) {
            noteList.clearSelection();
            noteList.repaint();
            return;
        }

        for (int i = 0; i < noteListModel.size(); i++) {
            NoteData note = noteListModel.get(i);
            if (note.noteTitle.toLowerCase().contains(searchText)) {
                noteList.setSelectedIndex(i);
                noteList.ensureIndexIsVisible(i);
                noteList.repaint();
                return;
            }
        }

        noteList.clearSelection();
        noteList.repaint();
    }
    private void updateNoteListModel() {
        noteListModel.clear();
        for (NoteData noteData : noteDataList) {
            noteListModel.addElement(noteData);
        }
    }
}
