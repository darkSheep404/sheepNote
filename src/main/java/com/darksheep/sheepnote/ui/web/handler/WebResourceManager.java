package com.darksheep.sheepnote.ui.web.handler;

import com.intellij.ui.jcef.JBCefBrowser;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class WebResourceManager {
    private static final Map<String, String> MIME_TYPES = new HashMap<>();
    private static JBCefLocalRequestHandler localRequestHandler;
    private static Object resourceParent = new Object(); // 用于传递给 JBCefStreamResourceHandler
    
    static {
        MIME_TYPES.put("html", "text/html");
        MIME_TYPES.put("css", "text/css");
        MIME_TYPES.put("js", "application/javascript");
        MIME_TYPES.put("svg", "image/svg+xml");
        MIME_TYPES.put("ico", "image/x-icon");
        MIME_TYPES.put("png", "image/png");
        MIME_TYPES.put("jpg", "image/jpeg");
        MIME_TYPES.put("jpeg", "image/jpeg");
    }

    public static synchronized JBCefLocalRequestHandler getResourceHandler() {
        if (localRequestHandler == null) {
            localRequestHandler = new JBCefLocalRequestHandler("http", "localhost");
            addDirectoryResources(localRequestHandler, "/META-INF/web");
        }
        return localRequestHandler;
    }

    public static void setupBrowser(JBCefBrowser browser) {
        JBCefLocalRequestHandler handler = getResourceHandler();
        browser.getJBCefClient().addRequestHandler(handler, browser.getCefBrowser());
    }

    private static void addDirectoryResources(JBCefLocalRequestHandler handler, String directoryPath) {
        try {
            URI uri = WebResourceManager.class.getResource(directoryPath).toURI();
            FileSystem fileSystem;
            
            if (uri.getScheme().equals("jar")) {
                try {
                    fileSystem = FileSystems.getFileSystem(uri);
                } catch (Exception e) {
                    fileSystem = FileSystems.newFileSystem(uri, new HashMap<>());
                }
            } else {
                fileSystem = FileSystems.getDefault();
            }

            Path dirPath = fileSystem.getPath(directoryPath);
            try (Stream<Path> paths = Files.walk(dirPath)) {
                paths.filter(Files::isRegularFile).forEach(filePath -> {
                    String relativePath = dirPath.relativize(filePath).toString().replace("\\", "/");
                    String extension = getExtension(relativePath);
                    String mimeType = MIME_TYPES.getOrDefault(extension, "application/octet-stream");

                    handler.addResource("/" + relativePath, () -> {
                        try {
                            InputStream resourceStream = Files.newInputStream(filePath);
                            return new JBCefStreamResourceHandler(resourceStream, mimeType, resourceParent);
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

    private static String getExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1);
    }
} 