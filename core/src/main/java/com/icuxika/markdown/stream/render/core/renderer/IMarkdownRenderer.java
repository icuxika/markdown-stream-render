package com.icuxika.markdown.stream.render.core.renderer;

import com.icuxika.markdown.stream.render.core.ast.Visitor;

/**
 * Markdown 渲染器接口。
 * 继承自 {@link Visitor}，通过遍历 AST 节点来生成目标格式（如 HTML, JavaFX Node 等）。
 */
public interface IMarkdownRenderer extends Visitor {
    /**
     * 获取渲染结果。
     *
     * @return 渲染后的对象（类型取决于具体实现，如 String 或 javafx.scene.Node）
     */
    Object getResult();
}
