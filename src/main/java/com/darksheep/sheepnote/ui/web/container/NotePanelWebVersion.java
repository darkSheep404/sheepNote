package com.darksheep.sheepnote.ui.web.container;

import com.darksheep.sheepnote.config.AddNoteEventListener;
import com.darksheep.sheepnote.config.NoteDataRepository;
import com.darksheep.sheepnote.data.NoteData;
import com.darksheep.sheepnote.ui.web.handler.JBCefLocalRequestHandler;
import com.darksheep.sheepnote.ui.web.handler.JBCefStreamResourceHandler;
import com.darksheep.sheepnote.ui.web.handler.WebResourceManager;
import com.google.gson.Gson;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.ui.jcef.JBCefBrowser;
import com.intellij.ui.jcef.JBCefBrowserBase;
import com.intellij.ui.jcef.JBCefJSQuery;
import org.apache.commons.lang.StringEscapeUtils;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.handler.CefDisplayHandlerAdapter;
import com.intellij.util.messages.MessageBus;


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

public class NotePanelWebVersion extends SimpleToolWindowPanel {
    private CefBrowser cefBrowser;
    private JBCefBrowser webNoteBrowser;

    public NotePanelWebVersion(Project project) {
        super(true, true);
        webNoteBrowser = new JBCefBrowser();
        cefBrowser = webNoteBrowser.getCefBrowser();
        
        WebResourceManager.setupBrowser(webNoteBrowser);
        webNoteBrowser.loadURL("http://localhost/webNote.html");
        webNoteBrowser.openDevtools();
        
        initNoteListFromDB(webNoteBrowser);
        setContent(webNoteBrowser.getComponent());

        // Dispose resources when no longer needed
        Disposer.register(ApplicationManager.getApplication(), webNoteBrowser);

        // 订阅添加笔记事件
        MessageBus messageBus = project.getMessageBus();
        messageBus.connect(project).subscribe(AddNoteEventListener.ADD_NOTE_TOPIC, noteData -> {
            System.out.println("Received new note: " + noteData);
            ApplicationManager.getApplication().invokeLater(() -> {
                try {
                    String noteJson = new Gson().toJson(noteData);
                    String script = String.format("if(typeof addNewNote === 'function') { addNewNote(%s); } else { console.error('addNewNote function not found'); }", noteJson);
                    cefBrowser.executeJavaScript(script, webNoteBrowser.getCefBrowser().getURL(), 0);
                } catch (Exception e) {
                    System.err.println("Error executing JavaScript: " + e.getMessage());
                    e.printStackTrace();
                }
            });
        });
        

    }

    private  void initNoteListFromDB(JBCefBrowser webNoteBrowser) {
        CefBrowser cefBrowser = webNoteBrowser.getCefBrowser();
        List<NoteData> noteDataList = NoteDataRepository.getAllNoteData();
        if(noteDataList.isEmpty())
            noteDataList.add(NoteData.buildExampleNote());
        
        // 按创建时间降序排序
        noteDataList.sort((a, b) -> {
            try {
                return -a.getCreateTime().compareTo(b.getCreateTime());
            } catch (Exception e) {
                return 0;
            }
        });
        
        String noteDataJson = new Gson().toJson(noteDataList);
        String escapedJson = StringEscapeUtils.escapeJavaScript(noteDataJson);

        JBCefJSQuery openLinkQuery = JBCefJSQuery.create((JBCefBrowserBase)webNoteBrowser);
        JBCefJSQuery updateNoteQuery = JBCefJSQuery.create((JBCefBrowserBase)webNoteBrowser);
        JBCefJSQuery refreshNotesQuery = JBCefJSQuery.create((JBCefBrowserBase)webNoteBrowser);
        JBCefJSQuery deleteNoteQuery = JBCefJSQuery.create((JBCefBrowserBase)webNoteBrowser);
        
        deleteNoteQuery.addHandler(noteId -> {
            try {
                NoteDataRepository.deleteNoteData(Integer.parseInt(noteId));
                return new JBCefJSQuery.Response("OK");
            } catch (Exception e) {
                e.printStackTrace();
                return new JBCefJSQuery.Response("ERROR", 500, "Failed to delete note: " + e.getMessage());
            }
        });

        refreshNotesQuery.addHandler(ignored -> {
            try {
                List<NoteData> newDataList = NoteDataRepository.getAllNoteData();
                // 按创建时间降序排序
                newDataList.sort((a, b) -> {
                    try {
                        return -a.getCreateTime().compareTo(b.getCreateTime());
                    } catch (Exception e) {
                        return 0;
                    }
                });
                String newNoteDataJson = new Gson().toJson(newDataList);
                String script = String.format("notes = %s; renderNotes(notes); if(notes.length > 0) { selectNote(notes[0]); }", newNoteDataJson);
                ApplicationManager.getApplication().invokeLater(() -> {
                    cefBrowser.executeJavaScript(script, null, 0);
                });
                return new JBCefJSQuery.Response("OK");
            } catch (Exception e) {
                e.printStackTrace();
                return new JBCefJSQuery.Response("ERROR", 500, "Failed to refresh notes: " + e.getMessage());
            }
        });

        updateNoteQuery.addHandler(noteItemJson -> {
            try {
                NoteData updatedNote = new Gson().fromJson(noteItemJson, NoteData.class);
                NoteDataRepository.updateNote(updatedNote);
                return new JBCefJSQuery.Response("OK");
            } catch (Exception e) {
                e.printStackTrace();
                return new JBCefJSQuery.Response("ERROR", 500, "Failed to update note: " + e.getMessage());
            }
        });

        //TODO 刷新,编辑,按照文件夹/tag 分类查看
        //TODO 图表形式组织笔记==>如何实现
        openLinkQuery.addHandler((noteItemJson)->{
            NoteData noteItem = new Gson().fromJson(noteItemJson, NoteData.class);
            Project currentProject = IdeFocusManager.getGlobalInstance().getLastFocusedFrame().getProject();
            if (currentProject == null) {
                return new JBCefJSQuery.Response("ERROR",400,"Current project not found");
            }
            VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByPath(noteItem.getNoteFilePath());
            if(virtualFile==null){
                return new JBCefJSQuery.Response("ERROR",500,"File not found");
            }
            FileEditorManager editorManager = FileEditorManager.getInstance(currentProject);
            OpenFileDescriptor descriptor = new OpenFileDescriptor(currentProject, virtualFile, noteItem.getNoteLineNumber() - 1, 0);
            /**
             * use editorManager.openEditor(descriptor, true); in Application().invokeLater
             * fix EventQueue.isDispatchThread()=false
             * Current thread: Thread[Thread-1200,5,main] 1324346015
             * https://intellij-support.jetbrains.com/hc/en-us/community/posts/7602780460306-Access-is-allowed-from-event-dispatch-thread-only
             * UI code must be run on EDT thread, so try using Application.invokeLater https://plugins.jetbrains.com/docs/intellij/general-threading-rules.html#modality-and-invokelater
             */
            ApplicationManager.getApplication().invokeLater(()->{
                editorManager.openEditor(descriptor, true);
            });
            return new JBCefJSQuery.Response("OK");
        });

        cefBrowser.getClient().addDisplayHandler(new CefDisplayHandlerAdapter() {
            @Override
            public void onAddressChange(CefBrowser browser, CefFrame frame, String url) {
                super.onAddressChange(browser, frame, url);
                String script1 = "document.addEventListener('DOMContentLoaded', function () {\n" +
                        "initializeNotes('" + escapedJson + "');"+
                        "});";
                String script2 = "window.openLink = function(link) {" +
                        openLinkQuery.inject(
                                "link",
                                "function(response){" +
                                        "console.log('Link opened successfully');" +
                                        "}",
                                "function(error_code,error_message){" +
                                        "alert(error_code+ ':' + error_message)" +
                                        "}")
                        +"};";
                String script3 = "window.updateNote = function(note) {" +
                        "return new Promise((resolve, reject) => {" +
                        updateNoteQuery.inject(
                                "JSON.stringify(note)",
                                "function(response) { console.log('Note updated successfully'); resolve(response); }",
                                "function(error_code, error_message) { reject(new Error(error_code + ': ' + error_message)); }"
                        ) +
                        "});" +
                        "};";
                String script4 = "window.refreshNoteList = function() {" +
                        "return new Promise((resolve, reject) => {" +
                        refreshNotesQuery.inject(
                                "''",
                                "function(response) { resolve(response); }",
                                "function(error_code, error_message) { reject(new Error(error_code + ': ' + error_message)); }"
                        ) +
                        "});" +
                        "};";
                String script5 = "window.deleteNoteFromDB = function(noteId) {" +
                        "return new Promise((resolve, reject) => {" +
                        deleteNoteQuery.inject(
                                "noteId.toString()",
                                "function(response) { console.log('Note deleted successfully'); resolve(response); }",
                                "function(error_code, error_message) { reject(new Error(error_code + ': ' + error_message)); }"
                        ) +
                        "});" +
                        "};";
                
                browser.executeJavaScript(script1, null, 0);
                browser.executeJavaScript(script2, null, 0);
                browser.executeJavaScript(script3, null, 0);
                browser.executeJavaScript(script4, null, 0);
                browser.executeJavaScript(script5, null, 0);
            }
        });

    }


    private void addDirectoryResources(JBCefLocalRequestHandler localRequestHandler, String directoryPath) {
        FileSystem fileSystem;
        try {
            Map<String, String> mimeTypes = new HashMap<>();
            mimeTypes.put("html", "text/html");
            mimeTypes.put("css", "text/css");
            mimeTypes.put("js", "application/javascript");
            mimeTypes.put("svg", "image/svg+xml");
            mimeTypes.put("ico", "image/x-icon");
            mimeTypes.put("png", "image/png");
            mimeTypes.put("jpg", "image/jpeg");
            mimeTypes.put("jpeg", "image/jpeg");

            /** 如果资源在 JAR 文件中，使用新的文件系统获取目录路径 **/
            URI uri = getClass().getResource(directoryPath).toURI();
            if (uri.getScheme().equals("jar")) {
                fileSystem = FileSystems.newFileSystem(uri, new HashMap<>());
            } else {
                fileSystem = FileSystems.getDefault();
            }

            Path dirPath = fileSystem.getPath(directoryPath);
            try (Stream<Path> paths = Files.walk(dirPath)) {
                paths.filter(Files::isRegularFile).forEach(filePath -> {
                    String relativePath = dirPath.relativize(filePath).toString().replace("\\", "/");
                    String extension = getExtension(relativePath);
                    String mimeType = mimeTypes.getOrDefault(extension, "application/octet-stream");

                    localRequestHandler.addResource("/" + relativePath, () -> {
                        try {
                            InputStream resourceStream = Files.newInputStream(filePath);
                            return new JBCefStreamResourceHandler(resourceStream, mimeType, this);
                        } catch (IOException e) {
                            e.printStackTrace();
                            return null;
                        }
                    });
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1);
    }
}
