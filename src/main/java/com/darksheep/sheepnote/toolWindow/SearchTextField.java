package com.darksheep.sheepnote.toolWindow;

import com.intellij.icons.AllIcons;


import javax.swing.*;
import java.awt.*;

public class SearchTextField extends JTextField {
    private int horizontalPadding = 6;
    public Icon searchIcon = AllIcons.Actions.Search;

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (g instanceof Graphics2D) {
            Graphics2D g2 = (Graphics2D) g;
            searchIcon.paintIcon(this, g2, horizontalPadding, (getHeight() - searchIcon.getIconHeight()) / 2);
        }
    }
}
