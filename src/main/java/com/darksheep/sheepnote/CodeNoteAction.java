package com.darksheep.sheepnote;

import com.darksheep.sheepnote.config.AddNoteEventListener;
import com.darksheep.sheepnote.config.NoteDataRepository;
import com.darksheep.sheepnote.data.NoteData;
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
import com.intellij.util.messages.MessageBus;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.awt.*;
import java.util.Date;
import java.util.List;

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
            Messages.showMessageDialog("Please select code to make note for", "notice", Messages.getInformationIcon());
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
        dialogBuilder.setTitle("code note");
        // 设置 Dialog 的内容
        dialogBuilder.setCenterPanel(createNotePanel(selectedText, dialogBuilder,startLine,filePath));
        // 添加确认按钮
        dialogBuilder.addOkAction().setText("save");
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
    private JPanel createNotePanel(String selectedText, DialogBuilder dialogBuilder, int startLine, String filePath) {
        Project currentProject = IdeFocusManager.getGlobalInstance().getLastFocusedFrame().getProject();
        
        // 创建主面板
        JPanel notePanel = new JPanel(new GridBagLayout());
        notePanel.setBackground(new Color(245, 245, 247));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.insets = new Insets(16, 16, 8, 16);

        // 统一的输入框样式
        Color inputBackground = new Color(250, 250, 252);
        Color inputForeground = new Color(0, 0, 0, 217);
        Color borderColor = new Color(0, 122, 255, 76);
        Font labelFont = new Font("Microsoft YaHei", Font.BOLD, 14);
        Font inputFont = new Font("Microsoft YaHei", Font.PLAIN, 13);

        // 标题部分
        JLabel titleLabel = new JLabel("Note Title");
        titleLabel.setFont(labelFont);
        titleLabel.setForeground(inputForeground);
        notePanel.add(titleLabel, gbc);

        // 标题输入框 - 默认使用选中代码的第一行作为标题
        JTextField titleTextField = new JTextField();
        titleTextField.setFont(inputFont);
        titleTextField.setBackground(inputBackground);
        titleTextField.setForeground(inputForeground);
        titleTextField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(borderColor, 1, true),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        // 设置默认标题
        String defaultTitle = selectedText.lines().findFirst().orElse("").trim();
        if (defaultTitle.length() > 50) {
            defaultTitle = defaultTitle.substring(0, 47) + "...";
        }
        titleTextField.setText(defaultTitle);
        
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 16, 16, 16);
        notePanel.add(titleTextField, gbc);

        // 标签部分
        JLabel tagsLabel = new JLabel("Tags(Enter to add New tag when no tags to select)");
        tagsLabel.setFont(labelFont);
        tagsLabel.setForeground(inputForeground);
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 16, 8, 16);
        notePanel.add(tagsLabel, gbc);

        // 获取所有已存在的标签和最新笔记
        List<String> existingTags = NoteDataRepository.getAllTags();
        List<NoteData> recentNotes = NoteDataRepository.getAllNoteData();
        
        // 创建标签面板
        JPanel tagsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        tagsPanel.setBackground(notePanel.getBackground());

        // 标签输入框
        JTextField tagsTextField = new JTextField();
        tagsTextField.setFont(inputFont);
        tagsTextField.setBackground(inputBackground);
        tagsTextField.setForeground(inputForeground);
        tagsTextField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(borderColor, 1, true),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        // 创建自动完成弹出窗口
        JPopupMenu autoCompletePopup = new JPopupMenu();
        autoCompletePopup.setBackground(inputBackground);
        autoCompletePopup.setBorder(BorderFactory.createLineBorder(borderColor, 1, true));
        UIManager.put("PopupMenu.font", inputFont);
        autoCompletePopup.setFont(inputFont);

        // 如果有最新笔记，获取其标签并添加到面板
        if (!recentNotes.isEmpty()) {
            NoteData latestNote = recentNotes.get(0);
            if (latestNote != null && latestNote.getTags() != null && !latestNote.getTags().isEmpty()) {
                System.out.println("Latest note tags: " + latestNote.getTags());  // 调试输出
                updateTagsDisplay(latestNote.getTags(), tagsPanel, tagsTextField);
            }
        }

        // 标签输入监听
        tagsTextField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void updatePopup() {
                SwingUtilities.invokeLater(() -> {
                    String input = tagsTextField.getText().trim();
                    String[] parts = input.split(",");
                    String currentTag = parts[parts.length - 1].trim().toLowerCase();
                    
                    if (currentTag.isEmpty()) {
                        autoCompletePopup.setVisible(false);
                        return;
                    }

                    autoCompletePopup.removeAll();
                    boolean hasMatches = false;

                    // 只显示匹配的现有标签
                    for (String tag : existingTags) {
                        if (tag.toLowerCase().contains(currentTag)) {
                            hasMatches = true;
                            JMenuItem item = new JMenuItem(tag);
                            item.setFont(inputFont);
                            item.setBackground(inputBackground);
                            item.setForeground(inputForeground);
                            item.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
                            
                            item.addActionListener(e -> {
                                addTagToPanel(tag, tagsPanel, tagsTextField);
                                tagsTextField.setText("");
                                autoCompletePopup.setVisible(false);
                                tagsTextField.requestFocusInWindow();
                            });
                            
                            autoCompletePopup.add(item);
                        }
                    }

                    if (hasMatches) {
                        if (!autoCompletePopup.isVisible()) {
                            autoCompletePopup.show(tagsTextField, 0, tagsTextField.getHeight());
                        }
                        autoCompletePopup.setPopupSize(tagsTextField.getWidth(), 
                            Math.min(200, autoCompletePopup.getComponentCount() * 35));
                        tagsTextField.requestFocusInWindow();
                    } else {
                        autoCompletePopup.setVisible(false);
                    }
                });
            }

            public void insertUpdate(javax.swing.event.DocumentEvent e) { updatePopup(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updatePopup(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updatePopup(); }
        });

        // 添加键盘事件监听，支持回车和逗号添加标签
        tagsTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER || e.getKeyChar() == ',') {
                    String text = tagsTextField.getText().trim();
                    if (!text.isEmpty()) {
                        if (text.endsWith(",")) {
                            text = text.substring(0, text.length() - 1);
                        }
                        // 直接添加用户输入的标签，不管是否存在
                        addTagToPanel(text, tagsPanel, tagsTextField);
                        tagsTextField.setText("");
                        e.consume();
                    }
                } else if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ESCAPE) {
                    autoCompletePopup.setVisible(false);
                }
            }
        });

        JPanel tagInputPanel = new JPanel(new BorderLayout());
        tagInputPanel.setBackground(notePanel.getBackground());
        tagInputPanel.add(tagsPanel, BorderLayout.NORTH);
        tagInputPanel.add(tagsTextField, BorderLayout.SOUTH);
        
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 16, 16, 16);
        notePanel.add(tagInputPanel, gbc);

        // 代码部分
        JLabel codeLabel = new JLabel("Code");
        codeLabel.setFont(labelFont);
        codeLabel.setForeground(inputForeground);
        gbc.gridy = 4;
        gbc.insets = new Insets(0, 16, 8, 16);
        notePanel.add(codeLabel, gbc);

        // 代码文本区域
        JTextArea codeTextArea = new JTextArea();
        codeTextArea.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        codeTextArea.setLineWrap(true);
        codeTextArea.setWrapStyleWord(true);
        codeTextArea.setText(selectedText);
        codeTextArea.setBackground(inputBackground);
        codeTextArea.setForeground(inputForeground);
        codeTextArea.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        // 滚动面板
        JBScrollPane scrollPane = new JBScrollPane(codeTextArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(borderColor, 1, true));
        scrollPane.setBackground(inputBackground);

        gbc.gridy = 5;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1;
        gbc.insets = new Insets(0, 16, 16, 16);

        // 设置首选大小
        Dimension preferredSize = new Dimension(500, 400);
        scrollPane.setPreferredSize(preferredSize);
        notePanel.add(scrollPane, gbc);

        // 设置对话框操作
        dialogBuilder.setOkOperation(() -> {
            String title = titleTextField.getText();
            String code = codeTextArea.getText();
            // 收集所有标签
            String tags = getTagsFromPanel(tagsPanel);
            System.out.println("getTagsFromPanel"+ tags);
            if (StringUtil.isEmpty(title) || StringUtil.isEmpty(code)) {
                Messages.showMessageDialog("Please enter both title and note", "Error", Messages.getErrorIcon());
                return;
            }

            try {
                NoteData noteData = new NoteData(title, filePath, startLine, code);
                noteData.setTags(tags);
                int id = NoteDataRepository.insert(noteData);
                noteData.id = id;
                noteData.createTime = new Date();
                noteData.updateTime = new Date();

                MessageBus messageBus = currentProject.getMessageBus();
                messageBus.syncPublisher(AddNoteEventListener.ADD_NOTE_TOPIC).onAddNoteEvent(noteData);
            } catch (Exception e) {
                e.printStackTrace();
                Messages.showErrorDialog("Failed to save note: " + e.getMessage(), "Error");
                return;
            }

            dialogBuilder.getDialogWrapper().close(DialogWrapper.OK_EXIT_CODE);
        });

        dialogBuilder.setCancelOperation(() -> {
            dialogBuilder.getDialogWrapper().close(DialogWrapper.CANCEL_EXIT_CODE);
        });

        return notePanel;
    }

    // 更新标签显示
    private void updateTagsDisplay(String tagsString, JPanel tagsPanel, JTextField tagsTextField) {
        tagsPanel.removeAll();
        tagsPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 6, 6)); // 增加间距
        String[] tags = tagsString.split(",");
        Color[] tagColors = {
            new Color(88, 157, 246),   // 蓝色
            new Color(130, 108, 246),  // 紫色
            new Color(246, 108, 188),  // 粉色
            new Color(246, 157, 88),   // 橙色
            new Color(88, 246, 157)    // 绿色
        };

        for (int i = 0; i < tags.length; i++) {
            String tag = tags[i].trim();
            if (!tag.isEmpty()) {
                Color tagColor = tagColors[i % tagColors.length];
                JPanel tagPanel = createTagPanel(tag, tagColor, tagsPanel, tagsTextField);
                tagsPanel.add(tagPanel);
            }
        }
        tagsPanel.revalidate();
        tagsPanel.repaint();
    }

    // 创建标签面板
    private JPanel createTagPanel(String tagText, Color baseColor, JPanel parentPanel, JTextField tagsTextField) {
        JPanel tagPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        tagPanel.setBackground(new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), 20));
        
        // 增加圆角半径
        int radius = 20;
        tagPanel.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), 80), radius),
            BorderFactory.createEmptyBorder(6, 14, 6, 10)
        ));

        // 添加标签文本
        JLabel tagLabel = new JLabel(tagText);
        tagLabel.setForeground(new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), 220));
        tagLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        tagPanel.add(tagLabel);

        // 添加删除按钮
        JLabel deleteButton = new JLabel("×");
        deleteButton.setForeground(new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), 180));
        deleteButton.setFont(new Font("Microsoft YaHei", Font.BOLD, 14));
        deleteButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        deleteButton.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 2));
        
        // 添加删除按钮的鼠标事件
        deleteButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                parentPanel.remove(tagPanel);
                parentPanel.revalidate();
                parentPanel.repaint();
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                // 当鼠标悬停在删除按钮上时，改变整个标签的样式
                tagPanel.setBackground(new Color(255, 69, 58, 20)); // 淡红色背景
                tagPanel.setBorder(BorderFactory.createCompoundBorder(
                    new RoundedBorder(new Color(255, 69, 58, 80), radius), // 红色边框
                    BorderFactory.createEmptyBorder(6, 14, 6, 10)
                ));
                tagLabel.setForeground(new Color(255, 69, 58, 220)); // 文字变红
                deleteButton.setForeground(new Color(255, 69, 58, 220)); // 删除按钮变红
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                // 当鼠标移出删除按钮时，恢复原来的样式
                tagPanel.setBackground(new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), 20));
                tagPanel.setBorder(BorderFactory.createCompoundBorder(
                    new RoundedBorder(new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), 80), radius),
                    BorderFactory.createEmptyBorder(6, 14, 6, 10)
                ));
                tagLabel.setForeground(new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), 220));
                deleteButton.setForeground(new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), 180));
            }
        });

        tagPanel.add(deleteButton);
        return tagPanel;
    }

    // 自定义圆角边框类
    private static class RoundedBorder extends AbstractBorder {
        private final Color color;
        private final int radius;

        RoundedBorder(Color color, int radius) {
            this.color = color;
            this.radius = radius;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(1, 1, 1, 1);
        }

        @Override
        public boolean isBorderOpaque() {
            return false;
        }
    }

    // 从标签面板获取所有标签
    private String getTagsFromPanel(JPanel tagsPanel) {
        StringBuilder tags = new StringBuilder();
        boolean isFirst = true;
        for (Component comp : tagsPanel.getComponents()) {
            if (comp instanceof JPanel) {
                for (Component subComp : ((JPanel) comp).getComponents()) {
                    if (subComp instanceof JLabel) {
                        String text = ((JLabel) subComp).getText();
                        if (!text.equals("×")) {  // 排除删除按钮
                            if (!isFirst) {
                                tags.append(",");
                            }
                            tags.append(text.trim());
                            isFirst = false;
                            break;  // 找到标签文本后跳出内层循环
                        }
                    }
                }
            }
        }
        System.out.println("Collected tags: " + tags.toString());  // 调试输出
        return tags.toString();
    }

    private void addTagToPanel(String newTag, JPanel tagsPanel, JTextField tagsTextField) {
        newTag = newTag.trim();
        if (newTag.isEmpty()) return;

        // 检查标签是否已存在
        for (Component comp : tagsPanel.getComponents()) {
            if (comp instanceof JPanel) {
                for (Component subComp : ((JPanel) comp).getComponents()) {
                    if (subComp instanceof JLabel) {
                        String existingTag = ((JLabel) subComp).getText();
                        if (!existingTag.equals("×") && existingTag.trim().equalsIgnoreCase(newTag)) {
                            return; // 标签已存在，直接返回
                        }
                    }
                }
            }
        }

        // 创建新标签
        Color[] tagColors = {
            new Color(88, 157, 246),   // 蓝色
            new Color(130, 108, 246),  // 紫色
            new Color(246, 108, 188),  // 粉色
            new Color(246, 157, 88),   // 橙色
            new Color(88, 246, 157)    // 绿色
        };
        int tagIndex = tagsPanel.getComponentCount() % tagColors.length;
        Color tagColor = tagColors[tagIndex];

        JPanel tagPanel = createTagPanel(newTag, tagColor, tagsPanel, tagsTextField);
        tagsPanel.add(tagPanel);
        tagsPanel.revalidate();
        tagsPanel.repaint();
    }

}