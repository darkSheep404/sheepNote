package com.darksheep.sheepnote.toolWindow;

import com.darksheep.sheepnote.config.NoteDataRepository;
import com.darksheep.sheepnote.data.NoteData;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.components.JBList;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class NoteListToolWindowFactory implements ToolWindowFactory {
    private JPanel noteListPanel = new JPanel();
    private JTextField searchTextField = new JFormattedTextField();
    private JPanel buttonPanel =  new JPanel();
    private JButton sortByCreateTimeButton = new JButton("按更新时间排序");
    private JButton sortByUpdateTimeButton = new JButton("按创建时间排序");
    private JPanel noteListWrapperPanel = new JPanel();
    private JPanel noteDetailPanel = new JPanel();
    private JLabel noteTitleLabel = new JLabel();
    private JLabel noteFilePathLabel = new JLabel();
    private JLabel noteLineNumberLabel = new JLabel();
    private JTextArea selectCodeTextArea = new JTextArea();

    private JBList<NoteData> noteList;
    private DefaultListModel<NoteData> noteListModel = new DefaultListModel<>();

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        List<NoteData> noteDataList = new ArrayList<>() ;
        try {
            noteDataList = NoteDataRepository.getAllNoteData();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 初始化笔记列表
        for (NoteData noteData : noteDataList) {
            noteListModel.addElement(noteData);
        }

        noteList = new JBList<>(noteListModel);
        noteList.setCellRenderer(new NoteListRenderer());
        noteList.addListSelectionListener(e -> {
            NoteData selectedNote = noteList.getSelectedValue();
            if (selectedNote != null) {
                noteTitleLabel.setText(selectedNote.noteTitle);
                noteFilePathLabel.setText(selectedNote.noteFilePath);
                noteLineNumberLabel.setText("Line " + selectedNote.noteLineNumber);
                selectCodeTextArea.setText(selectedNote.selectCode);
            }
        });

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
            //TODO 排序暂未实现
            /*Collections.sort(Arrays.asList(noteListModel.toArray()), new NoteDataComparator(NoteDataComparator.SortField.CREATE_TIME));*/
            noteList.repaint();
        });

        sortByUpdateTimeButton.addActionListener(e -> {
           /* Collections.sort(Arrays.asList(noteListModel.toArray()), new NoteDataComparator(NoteDataComparator.SortField.UPDATE_TIME));*/
            noteList.repaint();
        });

        // 初始化笔记列表面板
        noteListWrapperPanel.setLayout(new BorderLayout());
        noteListWrapperPanel.add(new JScrollPane(noteList));

        // 初始化笔记详情面板
        selectCodeTextArea.setEditable(false);

        // 初始化整个笔记列表窗口
        JPanel content = new JPanel(new BorderLayout());

        //左上第一部分 排序按钮
        JPanel buttonPanel = new JPanel();

        buttonPanel.setLayout(new FlowLayout());
        buttonPanel.add(sortByCreateTimeButton);
        buttonPanel.add(sortByCreateTimeButton);
        content.add(buttonPanel,BorderLayout.NORTH);

        //左上第二部分 搜索框
        content.add(searchTextField,BorderLayout.CENTER);
        //左侧第三部分 笔记列表
        content.add(noteListWrapperPanel, BorderLayout.SOUTH);
       /* content.add(noteDetailPanel, BorderLayout.CENTER);*/
        Content content01 = toolWindow.getContentManager().getFactory().createContent(content,"", false);
       /* toolWindow.getContentManager().addContent(
                ContentFactory.SERVICE.getInstance().createContent(content, "", false));*/
        toolWindow.getContentManager().addContent(content01);
    }

    /**
     * 模糊搜索笔记列表中 noteTitle 与输入文本匹配的笔记
     */
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

    private static class NoteListRenderer extends JLabel implements ListCellRenderer<NoteData> {
        private static final Border SELECTED_BORDER = BorderFactory.createLineBorder(Color.GRAY,0);
        @Override
        public Component getListCellRendererComponent(JList<? extends NoteData> list, NoteData note, int index,boolean isSelected, boolean cellHasFocus) {
            setText(note.noteTitle + " - " + note.noteFilePath);
            setBorder(cellHasFocus ? SELECTED_BORDER : BorderFactory.createEmptyBorder(1, 1, 1, 1));
            setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
            setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
            setOpaque(true);
            return this;
        }
    }

    private static class NoteDataComparator implements Comparator<NoteData> {
        enum SortField {
            CREATE_TIME,
            UPDATE_TIME
        }

        private final SortField sortField;

        public NoteDataComparator(SortField sortField) {
            this.sortField = sortField;
        }

        @Override
        public int compare(NoteData o1, NoteData o2) {
            switch (sortField) {
                case CREATE_TIME:
                    return o2.createTime.compareTo(o1.createTime);
                case UPDATE_TIME:
                    return o2.updateTime.compareTo(o1.updateTime);
                default:
                    return 0;
            }
        }
    }
}
