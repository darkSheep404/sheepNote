package com.darksheep.sheepnote;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class MyAction extends AnAction {
    public MyAction(String text) {
        super(text);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        // 在此处弹出弹出窗口
        // 可以使用 Swing 或者 JavaFX 实现 UI
    }
}

