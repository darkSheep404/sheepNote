# SheepNote 开发文档

## 架构设计

### 前端实现 (JCEF + Web)
1. 笔记列表界面 (webNote.html)
   ```javascript
   // 核心数据结构
   let notes = [];          // 笔记列表
   let selectedNoteId = null; // 当前选中笔记
   let noteItem = null;     // 当前笔记详情
   ```
   - 自动保存实现：
     ```javascript
     // 标题和内容编辑使用 contenteditable
     // 失焦触发保存
     element.addEventListener('blur', saveNoteChanges);
     ```
   - 路径编辑采用模态框：
     - 避免与跳转功能冲突
     - 支持 ESC 关闭
     - 点击遮罩关闭

2. 事件处理
   - 新笔记添加：
     ```javascript
     function addNewNote(newNote) {
         notes.unshift(newNote);
         selectedNoteId = newNote.id;
         noteItem = newNote;
         showNoteDetail(newNote);
         renderNotes(notes);
     }
     ```
   - 搜索过滤：
     - 实时过滤，保持选中状态
     - 空结果时自动选择第一项

### 后端实现
1. 消息总线通信
   ```java
   // 发送消息
   messageBus.syncPublisher(AddNoteEventListener.ADD_NOTE_TOPIC)
             .onAddNoteEvent(noteData);

   // 接收消息
   messageBus.connect().subscribe(AddNoteEventListener.ADD_NOTE_TOPIC, 
       noteData -> {
           String noteJson = new Gson().toJson(noteData);
           ApplicationManager.getApplication().invokeLater(() -> {
               cefBrowser.executeJavaScript(
                   String.format("addNewNote(%s);", noteJson), 
                   null, 
                   0
               );
           });
       }
   );
   ```

2. 数据库操作
   ```sql
   -- 笔记表结构
   CREATE TABLE notes (
       id INTEGER PRIMARY KEY AUTOINCREMENT,
       title TEXT NOT NULL,
       file_path TEXT NOT NULL,
       line_number INTEGER NOT NULL,
       select_code TEXT NOT NULL,
       tags TEXT,
       create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
       update_time DATETIME DEFAULT CURRENT_TIMESTAMP
   );

   -- 流程图表结构
   CREATE TABLE flowcharts (
       id INTEGER PRIMARY KEY AUTOINCREMENT,
       name TEXT NOT NULL,
       data TEXT NOT NULL,
       create_time BIGINT NOT NULL
   );
   ```

3. 文件跳转实现
   ```java
   OpenFileDescriptor descriptor = new OpenFileDescriptor(
       currentProject, 
       virtualFile, 
       noteItem.getNoteLineNumber() - 1, 
       0
   );
   ApplicationManager.getApplication().invokeLater(() -> {
       editorManager.openEditor(descriptor, true);
   });
   ```

### UI 设计
1. 样式变量
   ```css
   :root {
       --system-background: #f5f5f7;
       --secondary-background: rgba(255, 255, 255, 0.8);
       --tertiary-background: rgba(255, 255, 255, 0.6);
       --label-color: #1d1d1f;
       --accent-color: #007AFF;
       --control-color: rgba(120, 120, 128, 0.16);
       --corner-radius: 14px;
   }
   ```

2. 磨砂效果实现
   ```css
   backdrop-filter: blur(30px);
   background: var(--secondary-background);
   ```

3. 交互动画
   ```css
   .noteItem {
       transition: all 0.2s ease;
   }
   .noteItem:hover {
       transform: translateY(-2px);
       box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
   }
   ```

## 已知问题
1. 编辑器线程问题
   - 症状：EventQueue.isDispatchThread()=false
   - 解决：使用 ApplicationManager.invokeLater

2. 路径处理
   - Windows 路径分隔符统一
   - 相对路径处理

## 待优化项
1. 性能优化
   - [ ] 虚拟列表实现
   - [ ] 延迟加载笔记内容
   - [ ] 缓存机制

2. 功能增强
   - [ ] 笔记历史版本
   - [ ] 批量操作支持
   - [ ] 快捷键支持

3. 代码优化
   - [ ] 提取公共组件
   - [ ] 统一错误处理
   - [ ] 添加单元测试

## 开发注意事项
1. UI 线程
   - 所有 UI 操作必须在 EDT 线程
   - 使用 ApplicationManager.invokeLater

2. 数据库操作
   - 使用事务确保一致性
   - 预处理语句防注入
   - 及时关闭连接

3. 事件处理
   - 避免循环订阅
   - 及时取消订阅
   - 处理异常情况

## 实现细节

### 1. 笔记添加流程
```java
// 1. 用户在编辑器中选中代码，触发添加笔记
public class AddNoteAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        String selectedText = editor.getSelectionModel().getSelectedText();
        
        // 2. 创建笔记数据
        NoteData noteData = new NoteData();
        noteData.selectCode = selectedText;
        // ... 设置其他属性
        
        // 3. 保存到数据库
        int id = NoteDataRepository.insert(noteData);
        noteData.id = id;
        
        // 4. 通过消息总线通知UI更新
        MessageBus messageBus = project.getMessageBus();
        messageBus.syncPublisher(AddNoteEventListener.ADD_NOTE_TOPIC)
                 .onAddNoteEvent(noteData);
    }
}

// 5. NotePanelWebVersion 接收消息并更新UI
public class NotePanelWebVersion extends SimpleToolWindowPanel {
    public NotePanelWebVersion() {
        // ... 其他初始化代码
        
        // 订阅添加笔记事件
        Project project = ProjectManager.getInstance().getDefaultProject();
        MessageBus messageBus = project.getMessageBus();
        messageBus.connect().subscribe(AddNoteEventListener.ADD_NOTE_TOPIC, 
            noteData -> {
                // 转换为JSON
                String noteJson = new Gson().toJson(noteData);
                // 调用前端方法添加笔记
                String script = String.format("addNewNote(%s);", noteJson);
                ApplicationManager.getApplication().invokeLater(() -> {
                    cefBrowser.executeJavaScript(script, null, 0);
                });
            }
        );
    }
}
```

### 2. 笔记编辑保存流程
```javascript
// 1. 前端监听编辑事件
document.getElementById('titleText').addEventListener('blur', function() {
    saveNoteChanges();
});

// 2. 构建更新数据
function saveNoteChanges() {
    if (!noteItem) return;
    
    const updatedNote = {
        id: noteItem.id,
        noteTitle: document.getElementById('titleText').textContent,
        noteFilePath: document.getElementById('notePath').textContent,
        noteLineNumber: noteItem.noteLineNumber,
        selectCode: document.getElementById('noteContent').textContent,
        tags: noteItem.tags
    };

    // 3. 调用后端更新方法
    updateNote(JSON.stringify(updatedNote));
}

// 4. 后端处理更新请求
updateNoteQuery.addHandler((noteItemJson) -> {
    try {
        NoteData updatedNote = new Gson().fromJson(noteItemJson, NoteData.class);
        NoteDataRepository.updateNote(updatedNote);
        return new JBCefJSQuery.Response("OK");
    } catch (Exception e) {
        e.printStackTrace();
        return new JBCefJSQuery.Response("ERROR", 500, e.getMessage());
    }
});
```

### 3. 文件跳转流程
```javascript
// 1. 前端触发跳转
document.getElementById('notePath').onclick = function() {
    openLink(JSON.stringify(noteItem));
};

// 2. 后端处理跳转
openLinkQuery.addHandler((noteItemJson) -> {
    NoteData noteItem = new Gson().fromJson(noteItemJson, NoteData.class);
    Project currentProject = IdeFocusManager.getGlobalInstance()
                                          .getLastFocusedFrame()
                                          .getProject();
    
    // 查找目标文件
    VirtualFile virtualFile = LocalFileSystem.getInstance()
                                           .findFileByPath(noteItem.getNoteFilePath());
    
    // 创建跳转描述符
    OpenFileDescriptor descriptor = new OpenFileDescriptor(
        currentProject, 
        virtualFile, 
        noteItem.getNoteLineNumber() - 1,
        0
    );
    
    // 在EDT线程中执行跳转
    ApplicationManager.getApplication().invokeLater(() -> {
        FileEditorManager.getInstance(currentProject)
                        .openEditor(descriptor, true);
    });
    return new JBCefJSQuery.Response("OK");
});
```

### 4. 数据同步机制
1. 前端到后端：
   - 编辑操作触发 `updateNote` 方法
   - 使用 JBCefJSQuery 发送数据
   - 后端更新数据库并返回结果

2. 后端到前端：
   - 使用 MessageBus 发布事件
   - NotePanelWebVersion 接收事件
   - 通过 executeJavaScript 更新UI

3. 错误处理：
   ```java
   try {
       // 数据库操作
       return new JBCefJSQuery.Response("OK");
   } catch (Exception e) {
       return new JBCefJSQuery.Response("ERROR", 500, e.getMessage());
   }
   ```

### 5. 流程图实现
1. 前端与后端通信
```javascript
// 前端保存流程图
window.saveFlowchart = async function(data) {
    try {
        const response = await window.saveFlowchart(data);
        return response;
    } catch (error) {
        console.error('Save flowchart failed:', error);
        throw error;
    }
}

// 获取流程图列表
window.getFlowcharts = async function() {
    try {
        const flowcharts = await window.getFlowcharts();
        return flowcharts;
    } catch (error) {
        console.error('Get flowcharts failed:', error);
        throw error;
    }
}
```

2. 后端处理逻辑
```java
// NoteFlowchartPanel.java 中的处理器注册
private void initJSBridge() {
    // 保存流程图
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

    // 获取流程图列表
    JBCefJSQuery getFlowchartsQuery = JBCefJSQuery.create((JBCefBrowserBase)browser);
    getFlowchartsQuery.addHandler((String data) -> {
        try {
            List<Map<String, String>> flowcharts = flowchartService.getFlowcharts();
            return new JBCefJSQuery.Response(gson.toJson(flowcharts));
        } catch (Exception e) {
            return new JBCefJSQuery.Response("ERROR", 500, e.getMessage());
        }
    });
}
```

### 6. 编辑器渲染实现
1. 文件打开监听
```java
// CustomEditorFileOpenedListener.java
public class CustomEditorFileOpenedListener implements FileEditorManagerListener {
    @Override
    public void fileOpened(@NotNull FileEditorManager manager, @NotNull VirtualFile file) {
        Editor editor = manager.getSelectedTextEditor();
        if (editor == null) return;
        
        VirtualFile virtualFile = FileDocumentManager.getInstance()
            .getFile(editor.getDocument());
        if (virtualFile == null) return;
        
        // 获取该文件相关的所有笔记
        List<NoteData> noteDataList = NoteDataRepository.getAllNoteData()
            .stream()
            .filter(noteData -> StringUtils.equals(
                virtualFile.getPath(),
                noteData.getNoteFilePath()
            ))
            .collect(Collectors.toList());
            
        // 为每个笔记添加渲染
        for (NoteData noteData : noteDataList) {
            EditorHelper.drawNoteAddNoteNumber(editor, noteData);
        }
    }
}
```

2. 自定义渲染器
```java
// EditorHelper.java
static class CustomTextRenderer implements EditorCustomElementRenderer {
    public String text;

    public CustomTextRenderer(String text) {
        this.text = text;
    }

    @Override
    public int calcWidthInPixels(@NotNull Inlay inlay) {
        return 80; // 固定宽度
    }

    @Override
    public void paint(@NotNull Inlay inlay, @NotNull Graphics g, 
                     @NotNull Rectangle targetRegion, 
                     @NotNull TextAttributes textAttributes) {
        Graphics2D g2d = (Graphics2D) g;
        // 设置黄色背景
        g2d.setColor(JBColor.yellow);
        // 使用微软雅黑字体
        Font font = new Font("Microsoft YaHei", 
                           Font.ROMAN_BASELINE, 
                           g.getFont().getSize());
        FontMetrics fontMetrics = g2d.getFontMetrics(font);
        g2d.setFont(font);
        // 绘制文本
        g2d.drawString("// "+text, 
                      targetRegion.x, 
                      targetRegion.y + fontMetrics.getAscent());
    }
}
```

3. 渲染辅助方法
```java
// EditorHelper.java
public static void drawNoteAddNoteNumber(Editor editor, NoteData noteData) {
    // 检查行号是否有效
    if(editor.getDocument().getLineCount() < noteData.noteLineNumber)
        return;
        
    // 获取行尾偏移量
    int lineEndOffset = editor.getDocument()
        .getLineEndOffset(noteData.noteLineNumber - 1);
        
    // 创建渲染器并添加到行尾
    EditorCustomElementRenderer renderer = 
        new CustomTextRenderer(noteData.noteTitle);
    editor.getInlayModel()
        .addAfterLineEndElement(lineEndOffset, true, renderer);
}

// 删除笔记时移除渲染
public static void removeTextRenderInEditor(NoteData noteData, 
                                          Project project) {
    Editor activeEditor = getActiveEditor(project);
    if(activeEditor == null) return;
    
    VirtualFile virtualFile = FileDocumentManager.getInstance()
        .getFile(activeEditor.getDocument());
    if (virtualFile == null) return;
    
    // 检查是否是目标文件
    if(StringUtils.equals(virtualFile.getPath(), 
                         noteData.noteFilePath)) {
        InlayModel inlayModel = activeEditor.getInlayModel();
        // 获取指定行的所有行尾元素
        List<Inlay<?>> afterLineEndElements = 
            inlayModel.getAfterLineEndElementsForLogicalLine(
                noteData.noteLineNumber - 1
            );
            
        // 查找并移除匹配的渲染元素
        for (Inlay<?> element : afterLineEndElements) {
            if(element.getRenderer() instanceof CustomTextRenderer) {
                CustomTextRenderer renderer = 
                    (CustomTextRenderer) element.getRenderer();
                if(StringUtils.equals(renderer.text, 
                                    noteData.noteTitle)) {
                    Disposer.dispose(element);
                }
            }
        }
    }
}
```

### 7. 实现要点
1. 流程图功能
   - 使用JCEF桥接前后端通信
   - 统一的错误处理机制
   - 异步操作处理
   - 数据格式转换和验证

2. 编辑器渲染
   - 文件打开时自动加载笔记标记
   - 自定义渲染器实现行尾注释效果
   - 笔记删除时清理对应渲染
   - 使用Disposer确保资源正确释放
   - 字体和颜色适配IDE主题 