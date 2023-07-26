package com.darksheep.sheepnote.ui.web.container.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import com.intellij.ui.jcef.JBCefBrowser;

import javax.swing.*;

public class GBackAction extends AnAction implements DumbAware {
    private JBCefBrowser jbCefBrowser;

    public GBackAction(JBCefBrowser jbCefBrowser, Icon icon) {
        super(icon);
        this.jbCefBrowser = jbCefBrowser;
    }

    @Override
    public void update(AnActionEvent e) {
        if (!jbCefBrowser.getCefBrowser().canGoBack()) {
            e.getPresentation().setEnabled(false);
            return;
        }
        super.update(e);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        jbCefBrowser.getCefBrowser().canGoBack();
    }
}
