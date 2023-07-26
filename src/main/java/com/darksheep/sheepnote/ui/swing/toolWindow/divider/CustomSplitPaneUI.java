package com.darksheep.sheepnote.ui.swing.toolWindow.divider;

import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;

public class CustomSplitPaneUI extends BasicSplitPaneUI {
    @Override
    public BasicSplitPaneDivider createDefaultDivider() {
        return new CustomSplitPaneDividerUI(this);
    }
}
