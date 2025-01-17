package com.darksheep.sheepnote.ui.web.container;

import com.darksheep.sheepnote.data.NoteData;
import com.darksheep.sheepnote.utils.LocalHtmlHelper;
import com.intellij.ui.jcef.JBCefApp;
import com.intellij.ui.jcef.JBCefBrowser;
import com.intellij.ui.jcef.JBCefBrowserBase;
import com.intellij.ui.jcef.JBCefClient;
import com.intellij.ui.jcef.JBCefJSQuery;
import netscape.javascript.JSObject;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.util.List;

public class FlowchartPanel {
    private final JBCefBrowser browser;
    private static final String HTML_PATH = "/Flowchart.html"; // TODO: 更新这里
    JBCefJSQuery myJSQueryOpenInBrowser;
    public FlowchartPanel() {

        browser = new JBCefBrowser();
        browser.loadHTML(LocalHtmlHelper.loadByResourceInWebDir(HTML_PATH));
        JBCefJSQuery myJSQueryOpenInBrowser =
                JBCefJSQuery.create((JBCefBrowserBase)browser);
    }

    public JBCefBrowser getBrowser() {
        return browser;
    }

    public void updateDiagram(String diagramCode) {
        browser.getCefBrowser().executeJavaScript("renderFlowchart(`"+diagramCode+"`);", browser.getCefBrowser().getURL(), 0);
    }
}