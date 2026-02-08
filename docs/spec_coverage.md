# 规范覆盖范围（CommonMark / GFM）

## CommonMark

- 目标：尽可能完整覆盖 CommonMark spec
- 当前基线：v0.31.2（652 个用例）
- 推荐验证方式：运行 `html` 模块的 `CommonMarkSpecTest`

## GFM

- 目标：覆盖 GFM 的“扩展章节（extension sections）”
- 当前基线：v0.29.0（仅扩展章节）
- 说明：GFM 的完整覆盖范围更大且包含大量与 CommonMark 行为差异的细节，项目当前优先保证 CommonMark 完整覆盖，并以 GFM 扩展作为增量能力

## 运行方式（本地）

- CommonMark：`mvn -pl html test`
- GFM（扩展章节）：`mvn -pl html test -Dtest=GfmSpecTest`

