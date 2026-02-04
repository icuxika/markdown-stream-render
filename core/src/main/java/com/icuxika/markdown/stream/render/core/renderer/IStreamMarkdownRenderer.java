package com.icuxika.markdown.stream.render.core.renderer;

import com.icuxika.markdown.stream.render.core.ast.Node;

/**
 * 流式 Markdown 渲染器接口。
 * <p>
 * 用于处理增量解析器产生的节点事件。
 * </p>
 */
public interface IStreamMarkdownRenderer {
    
    /**
     * 当一个节点解析完成时调用。
     * <p>
     * 对于块级元素（如 Paragraph, Heading），这意味着该块已经结束（闭合）。
     * 对于行内元素，它们通常作为块级元素的子节点被一并处理，但也可能独立触发（取决于解析器实现）。
     * </p>
     *
     * @param node 解析完成的节点
     */
    void renderNode(Node node);

    /**
     * 当一个容器块（如 BlockQuote, List, CustomBlock）开始时调用。
     *
     * @param node 开始的节点
     */
    default void openBlock(Node node) {}

    /**
     * 当一个容器块结束时调用。
     *
     * @param node 结束的节点
     */
    default void closeBlock(Node node) {}
}
