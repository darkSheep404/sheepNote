package com.darksheep.sheepnote.ui.web.handler;

import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.callback.CefCallback;
import org.cef.handler.CefRequestHandlerAdapter;
import org.cef.handler.CefResourceHandler;
import org.cef.handler.CefResourceHandlerAdapter;
import org.cef.handler.CefResourceRequestHandlerAdapter;
import org.cef.misc.BoolRef;
import org.cef.network.CefRequest;


import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * In cases when a plugin feature implements a web-based UI, the plugin may provide HTML, CSS, and JavaScript files in its distribution or build them on the fly depending on some configuration. The browser cannot easily access such resources. They can be made accessible by implementing proper request handlers, which make them available to the browser at predefined URLs.
 *
 * This approach requires implementing CefRequestHandler, and CefResourceRequestHandler, which map resource paths to resource providers.
 *
 * Serving such resources is implemented by the Image Viewer component responsible for displaying SVG files in IntelliJ Platform-based IDEs. See JCefImageViewer and related classes for the implementation details.
 *
 * copy from https://github.com/JetBrains/intellij-community/blob/idea/243.25659.39/images/src/org/intellij/images/editor/impl/jcef/CefStreamResourceHandler.kt
 *
 * Handles local protocol-specific CEF resource requests for a defined `protocol` and `authority`.
 *
 * This class implements a mechanism to serve protocol-specific resources based on mappings provided
 * through the `addResource` function. Only requests matching the configured protocol and authority are processed,
 * while others are rejected.
 *
 * @param myProtocol The protocol to handle (e.g., "http", "file").
 * @param myAuthority The authority of the requests (e.g., "localhost", "mydomain").
 */
public class JBCefLocalRequestHandler extends CefRequestHandlerAdapter {
    private final String myProtocol;
    private final String myAuthority;
    private final Map<String, ResourceProvider> myResources = new HashMap<>();

    private final CefResourceHandler REJECTING_RESOURCE_HANDLER = new CefResourceHandlerAdapter() {
        @Override
        public boolean processRequest(CefRequest request, CefCallback callback) {
            callback.cancel();
            return false;
        }
    };

    private final CefResourceRequestHandlerAdapter RESOURCE_REQUEST_HANDLER = new CefResourceRequestHandlerAdapter() {
        @Override
        public CefResourceHandler getResourceHandler(CefBrowser browser, CefFrame frame, CefRequest request) {
            try {
                URL url = new URL(request.getURL());
                if (!url.getProtocol().equals(myProtocol) || !url.getAuthority().equals(myAuthority)) {
                    return REJECTING_RESOURCE_HANDLER;
                }

                String path = url.getPath().replaceFirst("/", "");
                return myResources.getOrDefault(path, () -> null).getResourceHandler();
            } catch (Exception e) {
                e.printStackTrace();
                return REJECTING_RESOURCE_HANDLER;
            }
        }
    };

    public JBCefLocalRequestHandler(String protocol, String authority) {
        this.myProtocol = protocol;
        this.myAuthority = authority;
    }

    public void addResource(String resourcePath, ResourceProvider resourceProvider) {
        String normalizedPath = resourcePath.replaceFirst("/", "");
        myResources.put(normalizedPath, resourceProvider);
    }

    public String createResource(String resourcePath, ResourceProvider resourceProvider) {
        String normalizedPath = resourcePath.replaceFirst("/", "");
        myResources.put(normalizedPath, resourceProvider);
        return myProtocol + "://" + myAuthority + "/" + normalizedPath;
    }

    @Override
    public CefResourceRequestHandlerAdapter getResourceRequestHandler(CefBrowser browser, CefFrame frame, CefRequest request, boolean isNavigation, boolean isDownload, String requestInitiator, BoolRef disableDefaultHandling) {
        return RESOURCE_REQUEST_HANDLER;
    }

    public interface ResourceProvider {
        CefResourceHandler getResourceHandler();
    }
}
