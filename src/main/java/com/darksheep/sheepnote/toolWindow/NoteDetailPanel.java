package com.darksheep.sheepnote.toolWindow;

import com.darksheep.sheepnote.data.NoteData;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.ui.components.JBScrollPane;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class NoteDetailPanel extends JPanel {
    private JLabel titleLabel;
    private JLabel filePathLabel;
    private JLabel lineNumberLabel;
    private JTextArea codeTextArea;

    private NoteData noteData;

    public NoteDetailPanel() {
        Project currentProject = IdeFocusManager.getGlobalInstance().getLastFocusedFrame().getProject();
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(300, 400));

        // Note title label
        titleLabel = new JLabel("title");
        add(titleLabel, BorderLayout.NORTH);

       /* // File path and line number label
        filePathLabel = new JLabel("filePath");
        add(filePathLabel, BorderLayout.SOUTH);*/
        lineNumberLabel = new JLabel("lineNumber");
        add(lineNumberLabel, BorderLayout.SOUTH);
        lineNumberLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String filePath = noteData.noteFilePath;
                int lineNumber = noteData.noteLineNumber;
                VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByPath(filePath);
                if (virtualFile != null) {
                    FileEditorManager editorManager = FileEditorManager.getInstance(currentProject);
                    OpenFileDescriptor descriptor = new OpenFileDescriptor(currentProject, virtualFile, lineNumber - 1, 0);
                    editorManager.openEditor(descriptor, true);
                }
               /* if (e.getClickCount() == 1) {
                    // 单击事件处理
                } else if (e.getClickCount() == 2) {
                    // 双击事件处理
                }*/
            }
        });

        // Code text area
        codeTextArea = new JTextArea("selectCode");
        JScrollPane scrollPane = new JBScrollPane(codeTextArea);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void setNoteDetail(NoteData noteData) {
        this.noteData = noteData;
        titleLabel.setText(noteData.noteTitle);
        /*filePathLabel.setText(noteData.noteFilePath);*/
        lineNumberLabel.setText(noteData.noteFilePath+"Line: " + noteData.noteLineNumber);
        codeTextArea.setText(noteData.selectCode);
    }
}

