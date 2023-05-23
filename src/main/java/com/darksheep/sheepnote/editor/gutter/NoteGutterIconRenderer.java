package com.darksheep.sheepnote.editor.gutter;

import com.darksheep.sheepnote.data.NoteData;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.EmptyIcon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class NoteGutterIconRenderer extends GutterIconRenderer {
    private final Icon noteIcon;

    public NoteGutterIconRenderer() {
        noteIcon = new EmptyIcon(AllIcons.General.Error.getIconWidth(), AllIcons.General.Error.getIconHeight()) {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(JBColor.BLUE);
                g2.fillOval(x, y, 12, 12);
                g2.dispose();
            }
        };
    }

    @Override
    public boolean equals(Object obj) {
        return false;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @NotNull
    @Override
    public Icon getIcon() {
        return noteIcon;
    }

    public static GutterIconRenderer createGutterIconRenderer(NoteData noteData, Editor editor) {
        return new NoteGutterIconRenderer() {
            @Nullable
            @Override
            public AnAction getClickAction() {
                return new GutterAction(noteData);
            }
        };
    }
}
