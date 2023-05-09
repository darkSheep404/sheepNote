package com.darksheep.sheepnote.editor.failtest;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.editor.EditorCustomElementRenderer;
import com.intellij.openapi.editor.Inlay;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.util.ui.JBFont;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;

public class SimpleLinePainter extends JComponent implements EditorCustomElementRenderer {
    private final String text;

    public SimpleLinePainter(String text) {
        this.text = text;
        setFont(JBFont.label()); // 为组件设置字体
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawString(text, 0, g.getFontMetrics().getAscent());
    }

    @Override
    public int calcWidthInPixels(@NotNull Inlay inlay) {
        FontMetrics fontMetrics = getFontMetrics(getFont());
        return fontMetrics.stringWidth(text);
    }

    @Override
    public int calcHeightInPixels(@NotNull Inlay inlay) {
        FontMetrics fontMetrics = getFontMetrics(getFont());
        return fontMetrics.getHeight();
    }

    @Override
    public void paint(@NotNull Inlay inlay, @NotNull Graphics g, @NotNull Rectangle targetRegion, @NotNull TextAttributes textAttributes) {
        EditorCustomElementRenderer.super.paint(inlay, g, targetRegion, new TextAttributes());
    }

    @Override
    public void paint(@NotNull Inlay inlay, @NotNull Graphics2D g, @NotNull Rectangle2D targetRegion, @NotNull TextAttributes textAttributes) {
        EditorCustomElementRenderer.super.paint(inlay, g, targetRegion, textAttributes);
    }

    @Override
    public @Nullable @NonNls String getContextMenuGroupId(@NotNull Inlay inlay) {
        return EditorCustomElementRenderer.super.getContextMenuGroupId(inlay);
    }

    @Override
    public @Nullable ActionGroup getContextMenuGroup(@NotNull Inlay inlay) {
        return EditorCustomElementRenderer.super.getContextMenuGroup(inlay);
    }

    @Override
    public @Nullable GutterIconRenderer calcGutterIconRenderer(@NotNull Inlay inlay) {
        return EditorCustomElementRenderer.super.calcGutterIconRenderer(inlay);
    }
}