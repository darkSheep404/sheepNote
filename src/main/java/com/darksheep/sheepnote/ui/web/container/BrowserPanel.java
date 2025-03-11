package com.darksheep.sheepnote.ui.web.container;

import com.darksheep.sheepnote.ui.web.container.actions.GBackAction;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.ui.jcef.JBCefBrowser;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.handler.CefDisplayHandlerAdapter;

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
        jbCefBrowser.getJBCefClient().addDisplayHandler(new CefDisplayHandlerAdapter() {
            @Override
            public void onAddressChange(CefBrowser browser, CefFrame frame, String url) {
                super.onAddressChange(browser, frame, url);
            }
        }, jbCefBrowser.getCefBrowser());
        urlField = new JTextField("https://bing.com");
        urlField.addActionListener(e -> navigateToUrl());
        urlField.setPreferredSize(new Dimension(600, 30));
        // Create search button
        //AllIcons.Actions.Search
        searchButton = new JButton();
        searchButton.setIcon(AllIcons.Actions.Search);
        // searchButton.setPreferredSize(new Dimension(searchButton.getIcon().getIconWidth() + 10, searchButton.getIcon().getIconHeight() + 10)); // 设置按钮大小包裹图标
        searchButton.addActionListener(e -> navigateToUrl());

        // Create a panel for the URL input and search button
        JPanel urlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        urlPanel.add(urlField);
        urlPanel.add(searchButton);

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
