package com.darksheep.sheepnote.ui.web.container;

import com.darksheep.sheepnote.data.FlowchartData;
import com.darksheep.sheepnote.data.NoteData;
import com.darksheep.sheepnote.data.service.FlowchartService;
import com.darksheep.sheepnote.ui.web.handler.WebResourceManager;
import com.intellij.openapi.application.ApplicationManager;
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
import java.util.List;
import java.util.Map;

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
                FlowchartData flowchartData = gson.fromJson(data, FlowchartData.class);
                Integer id = flowchartService.saveFlowchart(flowchartData);
                return new JBCefJSQuery.Response(id.toString());
            } catch (Exception e) {
                e.printStackTrace();
                return new JBCefJSQuery.Response("ERROR", 500, e.getMessage());
            }
        });

        // 获取流程图列表的处理器
        JBCefJSQuery getFlowchartsQuery = JBCefJSQuery.create((JBCefBrowserBase)browser);
        getFlowchartsQuery.addHandler((String data) -> {
            try {
                List<FlowchartData> flowcharts = flowchartService.getFlowcharts();
                return new JBCefJSQuery.Response(gson.toJson(flowcharts));
            } catch (Exception e) {
                e.printStackTrace();
                return new JBCefJSQuery.Response("ERROR", 500, e.getMessage());
            }
        });
        //获取流程图列表 item
        JBCefJSQuery getFlowchartItemQuery = JBCefJSQuery.create((JBCefBrowserBase)browser);
        getFlowchartItemQuery.addHandler((String flowchartId) -> {
            try {
                String flowchart = flowchartService.getFlowchartById(flowchartId);
                System.out.println(flowchart);
                return new JBCefJSQuery.Response(flowchart);
            } catch (Exception e) {
                e.printStackTrace();
                return new JBCefJSQuery.Response("ERROR", 500, e.getMessage());
            }
        });


        // 搜索笔记的处理器
        JBCefJSQuery searchNotesQuery = JBCefJSQuery.create((JBCefBrowserBase)browser);
        searchNotesQuery.addHandler((String keyword) -> {
            try {
                List<NoteData> results = flowchartService.searchNotes(keyword);
                return new JBCefJSQuery.Response(gson.toJson(results));
            } catch (Exception e) {
                e.printStackTrace();
                return new JBCefJSQuery.Response("ERROR", 500, e.getMessage());
            }
        });

        // 文件跳转的处理器
        JBCefJSQuery jumpToFileQuery = JBCefJSQuery.create((JBCefBrowserBase)browser);
        jumpToFileQuery.addHandler((String filePath) -> {
            try {
                ApplicationManager.getApplication().invokeLater(()->{
                    VirtualFile file = LocalFileSystem.getInstance().findFileByPath(filePath);
                    if (file != null && file.exists()) {
                        FileEditorManager.getInstance(project).openFile(file, true);
                    }
                    else {
                        JOptionPane.showMessageDialog(null, "File not found: " + filePath);
                    }
                });
                return new JBCefJSQuery.Response("OK");
            } catch (Exception e) {
                e.printStackTrace();
                return new JBCefJSQuery.Response("ERROR", 500, e.getMessage());
            }
        });


        client.addDisplayHandler(new CefDisplayHandlerAdapter() {
            @Override
            public void onAddressChange(CefBrowser browser, CefFrame frame, String url) {
                super.onAddressChange(browser, frame, url);
                // 注入所有JavaScript桥接函数
                injectJSBridge(saveFlowchartQuery, searchNotesQuery, jumpToFileQuery, getFlowchartsQuery,getFlowchartItemQuery);
            }
        }, browser.getCefBrowser());
        // 注册浏览器加载完成事件
    }

    private void injectJSBridge(JBCefJSQuery saveFlowchartQuery, JBCefJSQuery searchNotesQuery,
                                JBCefJSQuery jumpToFileQuery, JBCefJSQuery getFlowchartsQuery, JBCefJSQuery getFlowchartItemQuery) {
        StringBuilder script = new StringBuilder();
        
        // 保存流程图函数
        script.append("window.saveFlowchart = function(data) {" +
            "return new Promise((resolve, reject) => {" +
                saveFlowchartQuery.inject(
                    "JSON.stringify(data)",
                    "function(response) { resolve(response); }",
                    "function(error_code, error_message) { reject(new Error(error_code + ': ' + error_message)); }"
                ) +
            "});" +
        "};");

        // 获取流程图列表函数
        script.append("window.getFlowcharts = function() {" +
            "return new Promise((resolve, reject) => {" +
                getFlowchartsQuery.inject(
                    "''",
                    "function(response) { console.log('window.getFlowcharts Success:',JSON.parse(response)); resolve(JSON.parse(response)); }",
                    "function(error_code, error_message) { reject(new Error(error_code + ': ' + error_message)); }"
                ) +
            "});" +
        "};");

        // 获取流程图列表函数
        script.append("window.getFlowchartById = function(flowchartId) {" +
                "return new Promise((resolve, reject) => {" +
                getFlowchartItemQuery.inject(
                        "flowchartId",
                        "function(response) { console.log('window.getFlowchartById Success:',JSON.parse(response)); resolve(JSON.parse(response)); }",
                        "function(error_code, error_message) { reject(new Error(error_code + ': ' + error_message)); }"
                ) +
                "});" +
                "};");

        // 搜索笔记函数
        script.append("window.searchNotes = function(keyword) {" +
            "return new Promise((resolve, reject) => {" +
                searchNotesQuery.inject(
                    "keyword",
                    "function(response) { console.log('window.searchNotes Success:', response); resolve(JSON.parse(response)); }",
                    "function(error_code, error_message) { reject(new Error(error_code + ': ' + error_message)); }"
                ) +
            "});" +
        "};");

        // 跳转到文件函数
        script.append("window.jumpToFile = function(path) {" +
            "return new Promise((resolve, reject) => {" +
                jumpToFileQuery.inject(
                    "path",
                    "function(response) { resolve(response); }",
                    "function(error_code, error_message) { reject(new Error(error_code + ': ' + error_message)); }"
                ) +
            "});" +
        "};");

        browser.getCefBrowser().executeJavaScript(script.toString(), "", 0);
    }
    private String getExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1);
    }
} 