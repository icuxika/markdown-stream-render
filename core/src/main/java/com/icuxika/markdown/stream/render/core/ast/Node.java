package com.icuxika.markdown.stream.render.core.ast;

/**
 * AST (抽象语法树) 节点的基类。 所有 Markdown 元素（如 Paragraph, List, Text 等）都继承自此类。 提供了节点树的遍历、链接和修改功能。
 */
public abstract class Node {
    private Node parent;
    private Node firstChild;
    private Node lastChild;
    private Node next;
    private Node previous;

    private int startLine = -1;
    private int endLine = -1;

    /**
     * 接受访问者（Visitor 模式）。
     *
     * @param visitor
     *            访问者实例
     */
    public abstract void accept(Visitor visitor);

    public int getStartLine() {
        return startLine;
    }

    public void setStartLine(int startLine) {
        this.startLine = startLine;
    }

    public int getEndLine() {
        return endLine;
    }

    public void setEndLine(int endLine) {
        this.endLine = endLine;
    }

    public Node getParent() {
        return parent;
    }

    public Node getFirstChild() {
        return firstChild;
    }

    public Node getLastChild() {
        return lastChild;
    }

    public Node getNext() {
        return next;
    }

    public Node getPrevious() {
        return previous;
    }

    /**
     * 将指定节点添加为当前节点的最后一个子节点。
     *
     * @param child
     *            要添加的子节点
     */
    public void appendChild(Node child) {
        child.unlink();
        child.parent = this;
        if (lastChild != null) {
            lastChild.next = child;
            child.previous = lastChild;
            lastChild = child;
        } else {
            firstChild = child;
            lastChild = child;
        }
    }

    /**
     * 将当前节点从树中移除（断开与父节点和兄弟节点的连接）。
     */
    public void unlink() {
        if (parent != null) {
            if (parent.firstChild == this) {
                parent.firstChild = next;
            }
            if (parent.lastChild == this) {
                parent.lastChild = previous;
            }
        }
        if (previous != null) {
            previous.next = next;
        }
        if (next != null) {
            next.previous = previous;
        }
        parent = null;
        next = null;
        previous = null;
    }
}
