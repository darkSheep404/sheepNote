# SheepNote 开发规范

## 1. 项目概述
- 本项目是一个IntelliJ IDEA插件项目
- 主要功能包括代码笔记、流程图绘制、标签管理等
- 在ToolsWindow中采用 JCEF 技术实现基于H5的现代化 UI

## 2. 代码规范
### 2.1 UI 开发规范
- 作为专业的IOS风格UI工程师和代码工程师进行美观的UI设计
- 遵循 Apple 设计风格:
  - 使用磨砂玻璃效果
  - 圆角设计
  - 平滑动画过渡
  - 自适应暗色模式
- 前端代码规范:
  - 使用原生 JavaScript
  - CSS 变量管理主题
  - 模块化组织代码

### 2.2 代码规范
- 对数据库的操作应该使用 NoteDataRepository 类的static方法实现
- 使用jdk11的语法
- 所有java代码放在 com.darksheep.sheepnote(src/main/java/com/darksheep/sheepnote)包下,java swing相关的代码实现放在com.darksheep.sheepnote.ui.swing(src/main/java/com/darksheep/sheepnote/ui/swing）包下,JCEF相关代码参考当前代码放在com.darksheep.sheepnote.ui.web包(src/main/java/com/darksheep/sheepnote/ui/web)下
- 嵌入JCEF使用的html,css,js等文件放在resources/META-INF/web文件夹下
- 当前的NoteList对应resources/META-INF/web/WebNoteList.html和com.darksheep.sheepnote.ui.web.container.NotePanelWebVersion.java
- 当前的flowchart功能对应 resources/META-INF/web/flowchart.html和com.darksheep.sheepnote.ui.web.container.NoteFlowchartPanel.java
- 当前选中代码做笔记的功能在com.darksheep.sheepnote.CodeNoteAction.java中实现
- 实现功能时应该充分阅读已有代码,结合已有代码实现,不要盲目的新增无关的代码文件,不允许新增和之前文件名相同的文件和目录,新增文件,选择文件时应该参考之前的代码结构