package com.darksheep.sheepnote.ui.web.brower;

import com.darksheep.sheepnote.utils.LocalHtmlHelper;
import com.intellij.ui.jcef.JBCefBrowser;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.File;

public class JBCefBrowserSingleton {

    // 静态内部类持有JBCefBrowser实例
    private static class JBCefBrowserHolder {
        private static final JBCefBrowser INSTANCE = new JBCefBrowser();
    }

    // 私有化构造函数，防止外部实例化
    private JBCefBrowserSingleton() {
    }

    public static JBCefBrowser getInstance() {
        return JBCefBrowserHolder.INSTANCE;
    }

    public static JComponent getComponent() {
        return JBCefBrowserHolder.INSTANCE.getComponent();
    }

    public static void loadURL(String url) {
        JBCefBrowserHolder.INSTANCE.loadURL(url);
    }

    public static void loadHtmlByFilePath(String url) {
        JBCefBrowserHolder.INSTANCE.loadURL(LocalHtmlHelper.toHtmlString(new File(url)));
    }
}

