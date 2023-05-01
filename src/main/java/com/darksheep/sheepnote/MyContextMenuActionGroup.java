package com.darksheep.sheepnote;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MyContextMenuActionGroup extends ActionGroup {
    @Override
    public @NotNull AnAction[] getChildren(@Nullable AnActionEvent e) {
        // 创建子菜单的 Action
        AnAction myAction = new MyAction("My Action");
        return new AnAction[] { myAction };
    }
}
