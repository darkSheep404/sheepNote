package com.darksheep.sheepnote.ui.web.container;

import com.darksheep.sheepnote.data.service.FlowchartService;
import com.darksheep.sheepnote.ui.web.handler.JBCefLocalRequestHandler;
import com.darksheep.sheepnote.ui.web.handler.JBCefStreamResourceHandler;
import com.darksheep.sheepnote.ui.web.handler.WebResourceManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.jcef.JBCefBrowser;
import com.intellij.ui.jcef.JBCefBrowserBase;
import com.intellij.ui.jcef.JBCefClient;
import com.intellij.ui.jcef.JBCefJSQuery;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.handler.CefDisplayHandlerAdapter;
import com.google.gson.Gson;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class NoteFlowchartPanel extends JPanel {
    private final JBCefBrowser browser;
    private final JBCefClient client;
    private final Project project;
    private final FlowchartService flowchartService;
    private final Gson gson;

    public NoteFlowchartPanel(Project project) {
        this.project = project;
        this.flowchartService = project.getService(FlowchartService.class);
        this.gson = new Gson();

        setLayout(new BorderLayout());
        browser = new JBCefBrowser();
        client = browser.getJBCefClient();
        loadFlowWebPage();
        initJSBridge();
        add(browser.getComponent(), BorderLayout.CENTER);
    }

    private void loadFlowWebPage() {
        WebResourceManager.setupBrowser(browser);
        browser.loadURL("http://localhost/flowchart/index.html");
    }

    private void initJSBridge() {
        // 保存流程图的处理器
        JBCefJSQuery saveFlowchartQuery = JBCefJSQuery.create((JBCefBrowserBase)browser);
        saveFlowchartQuery.addHandler((String data) -> {
            try {
                Map<String, Object> flowchartData = gson.fromJson(data, Map.class);
                String name = (String) flowchartData.get("name");
                String content = gson.toJson(flowchartData.get("content"));
                flowchartService.saveFlowchart(name, content);
                return new JBCefJSQuery.Response("OK");
            } catch (Exception e) {
                return new JBCefJSQuery.Response("ERROR", 500, e.getMessage());
            }
        });

        // 搜索笔记的处理器
        JBCefJSQuery searchNotesQuery = JBCefJSQuery.create((JBCefBrowserBase)browser);
        searchNotesQuery.addHandler((String keyword) -> {
            try {
                List<Map<String, String>> results = flowchartService.searchNotes(keyword);
                return new JBCefJSQuery.Response(gson.toJson(results));
            } catch (Exception e) {
                return new JBCefJSQuery.Response("ERROR", 500, e.getMessage());
            }
        });

        // 文件跳转的处理器
        JBCefJSQuery jumpToFileQuery = JBCefJSQuery.create((JBCefBrowserBase)browser);
        jumpToFileQuery.addHandler((String filePath) -> {
            try {
                SwingUtilities.invokeLater(() -> {
                    VirtualFile file = LocalFileSystem.getInstance().findFileByPath(filePath);
                    if (file != null && file.exists()) {
                        FileEditorManager.getInstance(project).openFile(file, true);
                    }
                });
                return new JBCefJSQuery.Response("OK");
            } catch (Exception e) {
                return new JBCefJSQuery.Response("ERROR", 500, e.getMessage());
            }
        });
        client.addDisplayHandler(new CefDisplayHandlerAdapter() {
            @Override
            public void onAddressChange(CefBrowser browser, CefFrame frame, String url) {
                super.onAddressChange(browser, frame, url);
                // 注入所有JavaScript桥接函数
                injectJSBridge(saveFlowchartQuery, searchNotesQuery, jumpToFileQuery);
            }
        }, browser.getCefBrowser());
        // 注册浏览器加载完成事件
    }

    private void injectJSBridge(JBCefJSQuery saveFlowchartQuery, JBCefJSQuery searchNotesQuery, JBCefJSQuery jumpToFileQuery) {
        StringBuilder script = new StringBuilder();
        
        // 保存流程图函数
        script.append("window.saveFlowchart = function(data) {" +
            saveFlowchartQuery.inject(
                "JSON.stringify(data)",
                "function(response) { return response; }",
                "function(error_code, error_message) { throw new Error(error_code + ': ' + error_message); }"
            ) +
        "};");

        // 搜索笔记函数
        script.append("window.searchNotes = function(keyword) {" +
            searchNotesQuery.inject(
                "keyword",
                "function(response) { return JSON.parse(response); }",
                "function(error_code, error_message) { throw new Error(error_code + ': ' + error_message); }"
            ) +
        "};");

        // 跳转到文件函数
        script.append("window.jumpToFile = function(path) {" +
            jumpToFileQuery.inject(
                "path",
                "function(response) { return response; }",
                "function(error_code, error_message) { throw new Error(error_code + ': ' + error_message); }"
            ) +
        "};");

        browser.getCefBrowser().executeJavaScript(script.toString(), "", 0);
    }

    public void loadSavedFlowchart(String flowchartId) {
        String flowchartData = flowchartService.getFlowchartById(flowchartId);
        if (flowchartData != null) {
            String script = String.format("loadFlowchart(%s);", flowchartData);
            browser.getCefBrowser().executeJavaScript(script, "", 0);
        }
    }
    private String getExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1);
    }
} 