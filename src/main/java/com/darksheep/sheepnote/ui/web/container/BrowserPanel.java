package com.darksheep.sheepnote.ui.web.container;

import com.darksheep.sheepnote.ui.web.container.actions.GBackAction;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.ui.jcef.JBCefBrowser;

public class BrowserPanel extends SimpleToolWindowPanel {

    private JBCefBrowser jbCefBrowser;

    public BrowserPanel() {
        super(true, true);
        jbCefBrowser = new JBCefBrowser("bing.com");
        ActionToolbar actionToolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.CONTEXT_TOOLBAR, buildToolbar(), true);
        setContent(jbCefBrowser.getComponent());
        setToolbar(actionToolbar.getComponent());

    }
    private DefaultActionGroup buildToolbar() {
        DefaultActionGroup toolbar = new DefaultActionGroup();
        GBackAction backButton = new GBackAction(jbCefBrowser, AllIcons.Actions.Back);
        toolbar.add(backButton);
        return toolbar;
    }

}
