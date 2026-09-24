# 视觉设计权威文档（Design Authority）

`docs/design/` 保存当前产品界面视觉设计的规范性 Authority。这里描述视觉语言、设计 token、组件 presentation、主题、形状、排版、间距、视觉状态、响应式视觉规则与已知视觉缺口；不拥有业务 Requirement、页面行为 Specification、Architecture 或 implementation HOW。

## 当前 Design owner

- `public-site/DESIGN.md` — Main / Party 公开站视觉设计 Authority；采用 Google `DESIGN.md` 格式，文件名保持标准兼容性，目录表达其产品作用域。

## 使用边界

Design Authority 按语义责任组织，不跟随当前源码目录生命周期。Public Renderer 可以被替换或重建，但其已经确认的视觉设计输入应继续从本目录恢复。

修改页面结构、业务区域关系、用户交互或失败行为时返回对应 Specification；修改跨 Feature renderer 实现责任时返回 Architecture / Technical；Vue / CSS / TS 只实现 Design Authority，不因当前实现值存在就自动取得设计事实所有权。

`DESIGN.md` 中明确列出的 Known Gaps 表示当前尚未形成唯一规范值，不授权实现层自行选择并反向提升为设计要求。
