package com.darksheep.sheepnote.toolWindow;

import com.darksheep.sheepnote.data.NoteData;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.highlighter.EditorHighlighter;
import com.intellij.openapi.editor.highlighter.EditorHighlighterFactory;
import com.intellij.openapi.editor.impl.EditorImpl;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeRegistry;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.HyperlinkLabel;
import com.intellij.ui.ScrollPaneFactory;
import org.apache.commons.io.FilenameUtils;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;

import static java.awt.GridBagConstraints.REMAINDER;

public class NoteDetailPanel extends JPanel{
    private JLabel titleLabel;
    private JLabel creationTimeLabel;
    private JLabel updateTimeLabel;
    private HyperlinkLabel filePathLabel;

    private Editor codeEditor;

    private Editor editorBase;

    private NoteData noteData;

    private Project currentProject;

    private GridBagConstraints gbc = new GridBagConstraints();

    public NoteDetailPanel() {
        currentProject = CommonDataKeys.PROJECT.getData(DataManager.getInstance().getDataContext(this));
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
        gbc.gridwidth = REMAINDER;
        add(filePathLabel, gbc);

        // Customize GridBagConstraints for JTextArea
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.gridy = 4;
        gbc.gridwidth = REMAINDER;
        // Reset insets for JTextArea
        gbc.insets = new Insets(0, 0, 0, 0);

        // Selected code
        codeEditor = createCodeEditor();
        JScrollPane scrollPane = ScrollPaneFactory.createScrollPane(codeEditor.getComponent(), true);
        add(scrollPane, gbc);
    }

    private Editor createCodeEditor() {
        EditorFactory editorFactory = EditorFactory.getInstance();
        Editor editor_base = editorFactory.createEditor(createDocument(), currentProject);
        EditorImpl editor = (EditorImpl) editor_base; // Create the Editor
        editor.getSettings().setFoldingOutlineShown(false);
        editor.getSettings().setLineMarkerAreaShown(false);
        editor.getSettings().setLineNumbersShown(true);
        editor.getSettings().setIndentGuidesShown(false);

       /* ProjectManager.getInstance().addProjectManagerListener(new ProjectManagerListener() {
            @Override
            public void projectClosed(@NotNull Project project) {
                //会导致非新窗口 打开项目时 绿色
               *//* if(codeEditor!=null){
                    EditorFactory.getInstance().releaseEditor(codeEditor);
                    codeEditor = null;
                }*//*
                ProjectManagerListener.super.projectClosed(project);
            }

            @Override
            public void projectOpened(@NotNull Project project) {
                currentProject = project;
                ProjectManagerListener.super.projectOpened(project);
            }
        });*/
      return editor;
    }

    private Document createDocument() {
        Document document = EditorFactory.getInstance().createDocument(""); // Create an empty document
        document.setReadOnly(false);
        return document;
    }
    private void setEditorContent(String content) {
       /* if(codeEditor == null){
            remove(scrollPane);

            codeEditor = createCodeEditor();
            scrollPane = ScrollPaneFactory.createScrollPane(codeEditor.getComponent(), true);
            add(scrollPane, gbc);
        }*/
        if (codeEditor != null) {
            /**
             * 设置 文本必须在ApplicationManager.getApplication().runWriteAction中设置 否则会报错
             * ava.lang.Throwable: Assertion failed: Write access is allowed inside write-action only (see com.intellij.openapi.application.Application.runWriteAction())
             * 	at com.intellij.openapi.diagnostic.Logger.assertTrue(Logger.java:218)
             * 	at com.intellij.openapi.application.impl.ApplicationImpl.assertWriteAccessAllowed
             */
            ApplicationManager.getApplication().runWriteAction(() -> {
                codeEditor.getDocument().setReadOnly(false);
                codeEditor.getDocument().setText(noteData.selectCode);

                String fileExtension = FilenameUtils.getExtension(noteData.noteFilePath);
                FileType fileType = FileTypeRegistry.getInstance().getFileTypeByExtension(fileExtension);
                SyntaxHighlighter syntaxHighlighter = SyntaxHighlighterFactory.getSyntaxHighlighter(fileType, currentProject,null);
                // Get editor colors scheme
                EditorColorsScheme colorsScheme = EditorColorsManager.getInstance().getGlobalScheme();

                // Create highlighter and apply it to the editor
                EditorHighlighter highlighter = EditorHighlighterFactory.getInstance().createEditorHighlighter(syntaxHighlighter, colorsScheme);

                highlighter.setText(codeEditor.getDocument().getImmutableCharSequence());
                highlighter.setColorScheme(colorsScheme);
                ((EditorImpl) codeEditor).setHighlighter(highlighter);
                codeEditor.getDocument().setReadOnly(true);
            });

        }
    }

    public void setNoteDetail(NoteData noteData) {
        this.noteData = noteData;

        titleLabel.setText(noteData.noteTitle);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        creationTimeLabel.setText("Create time: " + dateFormat.format(noteData.createTime));
        updateTimeLabel.setText("Update time: " + dateFormat.format(noteData.updateTime));

        filePathLabel.setHyperlinkText(noteData.noteFilePath + "#" + noteData.noteLineNumber);
        //注释此处 也会 在打开新的窗口后 不更新右侧ui
        setEditorContent(noteData.selectCode);
        //codeTextArea.setText(noteData.selectCode);
    }
}

