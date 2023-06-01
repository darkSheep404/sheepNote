package com.darksheep.sheepnote.toolWindow;

import com.darksheep.sheepnote.config.AddNoteEventListener;
import com.darksheep.sheepnote.config.NoteDataRepository;
import com.darksheep.sheepnote.data.NoteData;
import com.darksheep.sheepnote.editor.failtest.NoteDataHandler;
import com.darksheep.sheepnote.editor.utils.EditorHelper;
import com.darksheep.sheepnote.toolWindow.divider.CustomSplitPaneUI;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.components.JBList;
import com.intellij.ui.content.Content;
import com.intellij.util.messages.MessageBus;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class NoteListToolWindowFactory implements ToolWindowFactory {

    public static final String TOOL_WINDOW_ID = "sheepNote";
    private JTextField searchTextField = new JFormattedTextField();
    private JButton sortByCreateTimeButton = new JButton("按更新时间排序");
    private JButton sortByUpdateTimeButton = new JButton("按创建时间排序");
    private JButton deleteButton = new JButton("删除笔记");
    private JPanel noteListWrapperPanel = new JPanel();
    private JTextArea selectCodeTextArea = new JTextArea();

    private JBList<NoteData> noteList;
    private DefaultListModel<NoteData> noteListModel = new DefaultListModel<>();
    private NoteDetailPanel rightPanel;

    private  NoteListController noteListController;

    private static NoteDataHandler noteDataHandler;


    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {

        // 初始化排序按钮面板
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());
        buttonPanel.add(sortByCreateTimeButton);
        buttonPanel.add(sortByUpdateTimeButton);
        buttonPanel.add(deleteButton);

        searchTextField.setPreferredSize(new Dimension(0, 10));
        searchTextField.setMaximumSize(searchTextField.getPreferredSize());
        // 设置左侧面板内容
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(buttonPanel, BorderLayout.NORTH);
        leftPanel.add(searchTextField, BorderLayout.CENTER);
        leftPanel.add(noteListWrapperPanel, BorderLayout.SOUTH);
        //初始化右侧面板
        rightPanel = new NoteDetailPanel();

        //为笔记列表面板 绑定事件和数据
        prepareNoteListViewAndController(project, toolWindow);
        noteDataHandler = new NoteDataHandler(project);

        JSplitPane mainPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        mainPanel.setResizeWeight(0.5);
        mainPanel.setUI(new CustomSplitPaneUI());

        Content toolsWindowContent = toolWindow.getContentManager().getFactory().createContent(mainPanel, "", false);
        toolWindow.getContentManager().addContent(toolsWindowContent);
    }

    /**
     * 模糊搜索笔记列表中 noteTitle 与输入文本匹配的笔记
     */


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

    private void prepareNoteListViewAndController(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        List<NoteData> noteDataList = new ArrayList<>();
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

        // 初始化笔记列表面板
        noteListWrapperPanel.setLayout(new BorderLayout());
        noteListWrapperPanel.add(new JScrollPane(noteList));

        // 初始化笔记详情面板
        selectCodeTextArea.setEditable(false);

        // 设置默认选中的笔记
        rightPanel.setNoteDetail(noteListModel.get(0));

        // 初始化 NoteListController
        noteListController = new NoteListController(noteListModel, noteList, searchTextField, sortByCreateTimeButton, sortByUpdateTimeButton,deleteButton,rightPanel);
        noteListController.project=project;
        // NoteListController 订阅添加笔记事件（添加到列表的第一个位置）
        MessageBus messageBus = project.getMessageBus();
        messageBus.connect(toolWindow.getDisposable()).subscribe(AddNoteEventListener.ADD_NOTE_TOPIC, noteData -> {
            noteListModel.insertElementAt(noteData, 0);
            noteListController.addNewNoteToNoteList(noteData);
            noteList.updateUI();
            noteList.setSelectedIndex(0);
            noteList.ensureIndexIsVisible(0);
            Editor activeEditor = EditorHelper.getActiveEditor(project);
            EditorHelper.drawNoteAddNoteNumber(activeEditor,noteData);
        });
    }
    public static NoteDataHandler getNoteDataHandler() {
        return noteDataHandler;
    }

   /* public static List<NoteData> getNoteList(){
        return noteList;
    }*/

    public void selectNoteInList(NoteData noteData) {
        for (int i = 0; i < noteListModel.getSize(); i++) {
            NoteData currentNote = noteListModel.get(i);
            if (noteData.id == currentNote.id) {
                noteList.setSelectedIndex(i);
                noteList.ensureIndexIsVisible(i);
                break;
            }
        }
    }
}
