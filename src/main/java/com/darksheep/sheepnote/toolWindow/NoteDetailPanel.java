package com.darksheep.sheepnote.toolWindow;

import com.darksheep.sheepnote.data.NoteData;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.HyperlinkLabel;
import com.intellij.ui.ScrollPaneFactory;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;

public class NoteDetailPanel extends JPanel {
    private JLabel titleLabel;
    private JLabel creationTimeLabel;
    private JLabel updateTimeLabel;
    private HyperlinkLabel filePathLabel;
    private JTextArea codeTextArea;

    private NoteData noteData;

    public NoteDetailPanel() {
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 10, 2);

        // Title
        titleLabel = new JLabel();
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD));
        add(titleLabel, gbc);

        // Creation time
        creationTimeLabel = new JLabel();
        gbc.gridy = 1;
        add(creationTimeLabel, gbc);

        // Update time
        updateTimeLabel = new JLabel();
        gbc.gridx = 1; // Adjust the gridx value for update time
        add(updateTimeLabel, gbc);

        // File path + line number
        filePathLabel = new HyperlinkLabel();
        filePathLabel.addHyperlinkListener(e -> {
            Project currentProject = CommonDataKeys.PROJECT.getData(DataManager.getInstance().getDataContext(this));
            if (currentProject == null || noteData == null) {
                return;
            }
            int lineNumber = noteData.noteLineNumber;
            String filePath = noteData.noteFilePath;
            VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByPath(filePath);
            if (virtualFile != null) {
                FileEditorManager editorManager = FileEditorManager.getInstance(currentProject);
                OpenFileDescriptor descriptor = new OpenFileDescriptor(currentProject, virtualFile, lineNumber - 1, 0);
                editorManager.openEditor(descriptor, true);
            }
        });
        gbc.gridx = 0;
        gbc.gridy = 2;
        add(filePathLabel, gbc);

        // Customize GridBagConstraints for JTextArea
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.gridy = 4;
        // Reset insets for JTextArea
        gbc.insets = new Insets(0, 0, 0, 0);

        // Selected code
        codeTextArea = new JTextArea();
        codeTextArea.setEditable(false);
        codeTextArea.setWrapStyleWord(true);
        JScrollPane scrollPane = ScrollPaneFactory.createScrollPane(codeTextArea, true);
        add(scrollPane, gbc);
    }

    public void setNoteDetail(NoteData noteData) {
        this.noteData = noteData;

        titleLabel.setText(noteData.noteTitle);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        creationTimeLabel.setText("Create time: " + dateFormat.format(noteData.createTime));
        updateTimeLabel.setText("Update time: " + dateFormat.format(noteData.updateTime));

        filePathLabel.setHyperlinkText(noteData.noteFilePath + "#" + noteData.noteLineNumber);
        codeTextArea.setText(noteData.selectCode);
    }
}

