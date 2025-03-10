# IDEA SheepNote插件开发文档

## 功能概述

### 1. 代码笔记功能
- 选中代码后，通过右键菜单可以对选中的代码片段进行笔记
- 支持多种格式的笔记内容
- 笔记与代码位置关联，方便后续查看和管理

### 2. 笔记管理界面
#### 2.1 工具窗口实现
- NoteList：使用Java Swing进行原生UI开发
- NoteList V2：采用JCEF(Chromium Embedded Framework)技术进行现代化UI开发
- BrowserPanel：基于JCEF提供完整的浏览器功能，支持富文本编辑
- noteFlowchartPanel: 基于JCEF,使用jsPlumb,为用户提供绘制流程图功能,新建流程图节点时,模糊搜索之前的笔记item,搜索不到时,也可以自由输入文本,点击笔记item节点右键有弹出菜单,可以根据跳转到对应文件(类似NoteList V2,点击notePath时的视线),流程图有保存按钮,保存后存储到db,下次在noteFlowchartPanel可以选择新建流程图或者打开之前的某个流程图

#### 2.2 笔记列表布局
笔记列表采用左右双栏布局设计：
- 左侧栏：显示笔记列表，支持搜索、过滤和分类功能
- 右侧栏：展示笔记详细内容，支持编辑和预览模式

### 3. 可视化提示
- 在已添加笔记的代码行号旁显示特殊的gutter图标
- 通过图标可以快速识别和访问对应位置的笔记
- 支持图标点击交互，方便查看和管理笔记

## JCEF开发指南

### 1. Java和JavaScript交互示例

#### 1.1 Java调用JavaScript
```java
// 在Java中执行JavaScript代码
browser.getCefBrowser().executeJavaScript(script, url, line);

// 示例：初始化笔记数据
String noteDataJson = new Gson().toJson(noteDataList);
String escapedJson = StringEscapeUtils.escapeJavaScript(noteDataJson);
String script = "document.addEventListener('DOMContentLoaded', function () {\n" +
                "initializeNotes('" + escapedJson + "');" +
                "});";
browser.executeJavaScript(script, null, 0);
```

#### 1.2 JavaScript调用Java
```java
// 1. 创建JS查询接口
JBCefJSQuery jsQuery = JBCefJSQuery.create((JBCefBrowserBase)browser);

// 2. 添加处理器
jsQuery.addHandler((String data) -> {
    try {
        // 处理从JavaScript传来的数据
        return new JBCefJSQuery.Response("OK");
    } catch (Exception e) {
        return new JBCefJSQuery.Response("ERROR", 500, e.getMessage());
    }
});

// 3. 注入JavaScript函数
String script = "window.callJava = function(data) {" +
    jsQuery.inject(
        "data",
        "function(response) { console.log('Success:', response); }",
        "function(error_code, error_message) { console.error('Error:', error_code, error_message); }"
    ) +
"};";
browser.executeJavaScript(script, null, 0);
```

#### 1.3 JavaScript端调用示例
```javascript
// 调用注入的Java函数
window.callJava({
    type: 'action',
    data: 'someData'
});

// 或者在Java端注入时已经定义了回调函数，直接调用即可
window.callJava('someData');
```

### 2. JCEF资源加载

#### 2.1 本地资源处理器
```java
// 创建本地资源请求处理器
JBCefLocalRequestHandler localRequestHandler = new JBCefLocalRequestHandler("http", "localhost");

// 添加资源处理
localRequestHandler.addResource("/path", () -> {
    try {
        InputStream resourceStream = Files.newInputStream(filePath);
        return new JBCefStreamResourceHandler(resourceStream, mimeType, this);
    } catch (IOException e) {
        e.printStackTrace();
        return null;
    }
});

// 注册处理器
browser.getJBCefClient().addRequestHandler(localRequestHandler, browser.getCefBrowser());
```

#### 2.2 加载本地HTML
```java
// 方式1：直接加载URL
browser.loadURL("http://localhost/index.html");

// 方式2：加载HTML内容
browser.loadHTML("<html>...</html>");
```

### 3. 事件处理

#### 3.1 页面加载事件
```java
browser.getClient().addDisplayHandler(new CefDisplayHandlerAdapter() {
    @Override
    public void onAddressChange(CefBrowser browser, CefFrame frame, String url) {
        // 页面URL改变时触发
        // 可以在这里执行初始化脚本
    }
});
```

#### 3.2 开发调试
```java
// 打开开发者工具
browser.openDevtools();
```

## 技术实现
- 基于IntelliJ IDEA插件开发框架
- 使用JCEF技术实现现代化UI
- 采用Java进行原生功能开发

## 使用说明
1. 添加笔记：选中代码 -> 右键菜单 -> 添加笔记
2. 查看笔记：通过工具窗口或gutter图标访问
3. 管理笔记：在笔记列表中进行编辑、删除等操作
