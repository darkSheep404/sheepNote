package com.darksheep.sheepnote.toolWindow;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

/**
 * 实现一个简单的toolsWindow 用以对照参考 实际需要生产的ToolsWindows
 */
public class DarkSheepNoteToolWindow implements ToolWindowFactory {
    private JPanel contentPanel;

    @Override
    public void createToolWindowContent(Project project, ToolWindow toolWindow) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());
        JButton button1 = new JButton("helloWorld01");
        JButton button2 = new JButton("helloWorld02");
        buttonPanel.add(button1);
        buttonPanel.add(button2);
        JTextField textField = new JTextField();
        textField.setPreferredSize(new Dimension(150, 24));
        textField.setText("111");
        JBList<String> list = new JBList<>(new String[]{"5555", "5555", "5555"});
        list.setCellRenderer(new NoteListCellRenderer());
        JBScrollPane scrollPane = new JBScrollPane(list);
        panel.add(buttonPanel, BorderLayout.NORTH);
        panel.add(textField, BorderLayout.CENTER);
        panel.add(scrollPane, BorderLayout.SOUTH);

        toolWindow.setAvailable(true, null);
        Content content = toolWindow.getContentManager().getFactory().createContent(panel, null, false);
        toolWindow.getContentManager().addContent(content);

        panel.setMinimumSize(new Dimension(200, 500));

        // 这段代码会覆盖左侧的区域 使得无法显示左侧的代码
      /*  JLabel label = new JLabel("helloWorld--右侧面板");
        label.setHorizontalAlignment(SwingConstants.CENTER);
        toolWindow.getComponent().add(label, BorderLayout.CENTER);*/
    }

    class NoteListCellRenderer extends DefaultListCellRenderer {

        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
           /* if (value instanceof Note) {
                Note note = (Note) value;
                setText(note.getNoteTitle() + " - " + note.getNoteFilePath() + " : " + note.getNoteLineNumber());
            }*/
            return this;
        }
    }
}
