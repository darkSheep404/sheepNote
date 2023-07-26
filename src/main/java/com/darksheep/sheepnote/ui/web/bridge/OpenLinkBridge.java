package com.darksheep.sheepnote.ui.web.bridge;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class OpenLinkBridge {
    public void openLink(String url) {
        // 调用系统浏览器打开链接
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }
}
