package com.darksheep.sheepnote.ui.web.handler;


import org.cef.callback.CefCallback;
import org.cef.handler.CefResourceHandlerAdapter;

import org.cef.misc.IntRef;
import org.cef.misc.StringRef;
import org.cef.network.CefRequest;
import org.cef.network.CefResponse;


import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * Implementation of CefResourceHandler to serve resources from an InputStream.
 */
public class JBCefStreamResourceHandler extends CefResourceHandlerAdapter {
    private final InputStream myInputStream;
    private final String myMimeType;
    private final Map<String, String> myResponseHeaders;
    private boolean isFirstRequest = true;

    public JBCefStreamResourceHandler(InputStream inputStream, String mimeType, Object parent) {
        this(inputStream, mimeType, parent, null);
    }

    public JBCefStreamResourceHandler(InputStream inputStream, String mimeType, Object parent, Map<String, String> responseHeaders) {
        myInputStream = inputStream;
        myMimeType = mimeType;
        myResponseHeaders = responseHeaders;
    }

    @Override
    public boolean processRequest(CefRequest request, CefCallback callback) {
        isFirstRequest = true;
        callback.Continue();
        return true;
    }

    @Override
    public void getResponseHeaders(CefResponse response, IntRef response_length, StringRef redirectUrl) {
        response.setMimeType(myMimeType);
        response.setStatus(200);

        if (myResponseHeaders != null) {
            response.setHeaderMap(myResponseHeaders);
        }

        response_length.set(-1); // Use chunked transfer encoding
    }

    @Override
    public boolean readResponse(byte[] data_out, int bytes_to_read, IntRef bytes_read, CefCallback callback) {
        try {
            int read = myInputStream.read(data_out, 0, bytes_to_read);
            if (read <= 0) {
                bytes_read.set(0);
                try {
                    myInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return false;
            } else {
                bytes_read.set(read);
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
            bytes_read.set(0);
            return false;
        }
    }

    @Override
    public void cancel() {
        try {
            myInputStream.close();
        } catch (IOException ignored) {
            System.out.println("Failed to close stream");
            ignored.printStackTrace();
        }
    }
}
