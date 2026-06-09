# 系统级通用图标选择器开发文档 (Universal Icon Picker Development)
# 20260609 17:08 第一轮开发
## 实现效果
1. 统一数据模型与渲染器

- IconSource ：在 IconPickerModels.kt 中定义了 IconSource 密封类，支持 Emoji、Vector、Custom 图片的统一解析与存储（通过 EMOJI: 等前缀区分）。
- MiaoIcon ：创建了全局通用的 MiaoIcon 渲染组件，它能自动根据存储的字符串判断图标类型并完成渲染，确保了全系统图标显示的鲁棒性。
2. 标准化 Emoji 资源库

- 分门别类 ：将原本零散的 Emoji 整理为“常用”、“人物与表情”、“自然与动物”、“食物与饮品”等 8 大标准分类。
- 扩展性 ：资源结构完全解耦，未来只需在 CATEGORIZED_EMOJIS 数组中添加项即可完成扩充。
3. “Notion 级”图标选择器组件

- MiaoIconPickerSheet ：在 MiaoIconPicker.kt 中实现了全新的选择器：
  - 分段切换 ：顶部支持“表情符号”、“图标”、“上传”三段式 Tab 切换。
  - 锚点导航 ：底部集成分类锚点栏（含 🕒 最近使用、😀 表情等），点击可实现列表平滑滚动。
  - 实时搜索 ：支持对全量 Emoji 进行关键词过滤。
  - 随机功能 ：点击“随机”按钮可快速生成一个有趣的图标。
4. 全系统深度集成

- 分类管理页 ： CategoryManagementScreen.kt 已全面接入。现在点击分类图标会唤起全新的 MiaoIconPickerSheet ，且弹窗内的图标预览位支持点击触发。
- 分类选择器 ： ItemFormComponents.kt 及其新建分类弹窗也已同步升级，保持了跨页面的交互一致性。

## 问题
1. 谷歌对于emoji和图标是有分类的，直接使用这个分类呢