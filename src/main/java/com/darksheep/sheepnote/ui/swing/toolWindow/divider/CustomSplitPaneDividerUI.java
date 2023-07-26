package com.darksheep.sheepnote.ui.swing.toolWindow.divider;

import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import java.awt.*;

public class CustomSplitPaneDividerUI extends BasicSplitPaneDivider {
    public CustomSplitPaneDividerUI(BasicSplitPaneUI basicSplitPaneUI) {
        super(basicSplitPaneUI);
    }

    @Override
    public void paint(Graphics g) {
        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(0, 0, getWidth(), getHeight());
        super.paint(g);
    }

}
