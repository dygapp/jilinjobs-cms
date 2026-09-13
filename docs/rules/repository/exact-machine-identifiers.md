---
id: rule:exact-machine-identifiers
type: rule
status: active
scope:
  phases: [planning, technical-planning, execute, converge]
  activities: [documentation, implementation]
  technologies: []
  artifacts: [configuration, code, document]
  risks: []
---

# 机器标识符保持精确

文件路径、命令、配置键、类名、方法名、字段名、枚举值、API、URL、协议名、SHA 与其他机器可解析标识符必须保持精确，不为语言统一、文案风格或术语美化进行机械翻译或近义替换。

若人类叙述需要中文解释，应在不改变原始标识符的前提下增加中文说明，而不是重命名机器接口。
