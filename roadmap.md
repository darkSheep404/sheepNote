
---
title: darksheepNote-roadmap
createdTime: 23-05-09 (星期二) 21:06
updateTime: 23-05-09 (星期二) 21:16
---

## DarkSheepNote插件 RoadMap
备注:rootMap时间 之前时间不可查 以commit时间为准 但是可能与开发时间相差较大

---
version: v0.0.0
updateTime: 2023-05-01 (星期四)
remark: 初始化项目 选中代码可以弹出弹出框

---

---
version: v0.0.1
updateTime: 2023-05-04 (星期二)
remark: 记笔记时 向sqllite 数据库插入一条记录

---

---
version: v0.0.2
updateTime: 2023-05-07 (星期日)
remark:
* 实现笔记面板列表
* 右侧增加笔记详情面板
* 点击面板中的 文件地址和行号 可以跳转到文件位置和指定行号
---

---
version: v0.0.3
updateTime: 2023-05-08 (星期一)
remark: 重构代码 实现按钮的点击事件

---


---
version: v0.0.4
updateTime: 2023-05-09 (星期二)
remark: 实现了在编辑器内渲染已经做过的笔记

效果图
![](https://sheepnote.oss-cn-shenzhen.aliyuncs.com/ita/20230509210850.png)
可能存在的问题
- [ ] 当同一个多个笔记时 显示信息可能错乱
- [ ] 基于当前的监听器 可能出现打开文件时没有命中监听器 导致不渲染笔记内容

---

---
version: v0.0.4
updateTime: 2025-01-17 (星期五)
remark: 增加兼容版本,提交本地草稿代码,增加about.html的功能

效果图
![](https://sheepnote.oss-cn-shenzhen.aliyuncs.com/ita/20230509210850.png)
可能存在的问题
- [ ] 当同一个多个笔记时 显示信息可能错乱
- [ ] 基于当前的监听器 可能出现打开文件时没有命中监听器 导致不渲染笔记内容

---

参考
https://shields.io/badges/jet-brains-plugins
https://simpleicons.org/?q=idea
更新reademe


## UI重绘计划V1.0.0
将笔记UI从swing 更换为基于JCEF嵌入浏览器+H5实现的UI

### v1.0.0 草图UI上下对比



![image-20250305222241331](https://sheepnote.oss-cn-shenzhen.aliyuncs.com/ita/image-20250305222241331.png)

### 使用LocalRquestHandler替换loadByHtmlString fix 页面加载去除空行的功能

参考

>
>
>In cases when a plugin feature implements a web-based UI, the plugin may provide HTML, CSS, and JavaScript files in its distribution or build them on the fly depending on some configuration. The browser cannot easily access such resources. They can be made accessible by implementing proper request handlers, which make them available to the browser at predefined URLs.
>
>This approach requires implementing CefRequestHandler, and CefResourceRequestHandler, which map resource paths to resource providers.
>
>Serving such resources is implemented by the Image Viewer component responsible for displaying SVG files in IntelliJ Platform-based IDEs. See JCefImageViewer and related classes for the implementation details.
>
>
>REF:
> https://github.com/copilot/c/8086f966-16ce-413b-87f9-4f3a62e5419e
> 
>[ImagePreview CefLocalRequestHandler.kt](https://github.com/JetBrains/intellij-community/blob/idea/243.25659.39/images/src/org/intellij/images/editor/impl/jcef/CefLocalRequestHandler.kt)
>
>[embedded-browser-jcef#Loading Resources From Plugin Distribution](https://plugins.jetbrains.com/docs/intellij/embedded-browser-jcef.html#loading-resources-from-plugin-distribution)
>
>[LigaAI 在 IDE 插件开发中接入 JCEF 框架](https://zhuanlan.zhihu.com/p/668561545)