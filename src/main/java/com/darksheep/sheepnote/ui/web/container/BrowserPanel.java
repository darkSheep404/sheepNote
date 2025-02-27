package com.darksheep.sheepnote.ui.web.container;

import com.darksheep.sheepnote.ui.web.container.actions.GBackAction;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.ui.jcef.JBCefBrowser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class BrowserPanel extends SimpleToolWindowPanel {

    private JBCefBrowser jbCefBrowser;
    private JTextField urlField;
    private JButton searchButton;

    public BrowserPanel() {
        super(true, true);
        jbCefBrowser = new JBCefBrowser("bing.com");

        urlField = new JTextField("https://bing.com", 30);
        urlField.addActionListener(e -> navigateToUrl());
        // Create search button
        searchButton = new JButton(AllIcons.Actions.Search);
        searchButton.addActionListener(e -> navigateToUrl());

        // Create a panel for the URL input and search button
        JPanel urlPanel = new JPanel(new BorderLayout());
        urlPanel.add(urlField, BorderLayout.CENTER);
        urlPanel.add(searchButton, BorderLayout.EAST);

        // Create toolbar
        ActionToolbar actionToolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.CONTEXT_TOOLBAR, buildToolbar(), true);
        actionToolbar.setTargetComponent(this);

        // Add components to the panel
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(urlPanel, BorderLayout.NORTH);
        topPanel.add(actionToolbar.getComponent(), BorderLayout.SOUTH);

        setContent(jbCefBrowser.getComponent());
        setToolbar(topPanel);

    }
    private DefaultActionGroup buildToolbar() {
        DefaultActionGroup toolbar = new DefaultActionGroup();
        GBackAction backButton = new GBackAction(jbCefBrowser, AllIcons.Actions.Back);
        toolbar.add(backButton);
        return toolbar;
    }

    private void navigateToUrl() {
        String url = urlField.getText();
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "http://" + url;
        }
        jbCefBrowser.loadURL(url);
    }

}
