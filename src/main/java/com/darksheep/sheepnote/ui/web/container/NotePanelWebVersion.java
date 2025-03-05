package com.darksheep.sheepnote.ui.web.container;

import com.darksheep.sheepnote.config.NoteDataRepository;
import com.darksheep.sheepnote.data.NoteData;

import com.darksheep.sheepnote.ui.web.handler.JBCefLocalRequestHandler;
import com.darksheep.sheepnote.ui.web.handler.JBCefStreamResourceHandler;
import com.darksheep.sheepnote.utils.LocalHtmlHelper;
import com.google.gson.Gson;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.jcef.JBCefApp;
import com.intellij.ui.jcef.JBCefBrowser;
import org.apache.commons.lang.StringEscapeUtils;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.handler.CefDisplayHandlerAdapter;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class NotePanelWebVersion extends SimpleToolWindowPanel {

    public NotePanelWebVersion() {
        super(true, true);
        JBCefBrowser webNoteBrowser = new JBCefBrowser();
        // Create a local resource request handler
        JBCefLocalRequestHandler localRequestHandler = new JBCefLocalRequestHandler("http", "localhost");
        // Add all resources from the directory
        addDirectoryResources(localRequestHandler, "/META-INF/web");


        webNoteBrowser.getJBCefClient().addRequestHandler(localRequestHandler,webNoteBrowser.getCefBrowser());
        // Load the main HTML resource
        webNoteBrowser.loadURL("http://localhost/webNote.html");
        //webNoteBrowser.loadHTML(LocalHtmlHelper.loadByResourceInWebDir("/webNote.html"));

        webNoteBrowser.openDevtools();
        CefBrowser cefBrowser = webNoteBrowser.getCefBrowser();
        try{
            List<NoteData> noteDataList = NoteDataRepository.getAllNoteData();
            String noteDataJson = new Gson().toJson(noteDataList);
            String escapedJson = StringEscapeUtils.escapeJavaScript(noteDataJson);
            cefBrowser.getClient().addDisplayHandler(new CefDisplayHandlerAdapter() {
                @Override
                public void onAddressChange(CefBrowser browser, CefFrame frame, String url) {
                    String script1= "document.addEventListener('DOMContentLoaded', function () {\n" +
                            "initializeNotes('" + escapedJson + "');"+
                            "});";
                    System.out.println(noteDataJson);
                    browser.executeJavaScript(script1,null,0);
                    // webNoteBrowser.getCefBrowser().executeJavaScript("initializeNotes('" + noteDataJson + "');", webNoteBrowser.getCefBrowser().getURL(), 0);
                    super.onAddressChange(browser, frame, url);
                }
            });
        }
        catch (Exception e){
            e.printStackTrace();
        }
        setContent(webNoteBrowser.getComponent());
        // Dispose resources when no longer needed
        Disposer.register(ApplicationManager.getApplication(), webNoteBrowser);
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

            if (fileSystem != FileSystems.getDefault()) {
                fileSystem.close();
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
