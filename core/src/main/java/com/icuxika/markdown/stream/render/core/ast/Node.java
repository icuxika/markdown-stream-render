package com.icuxika.markdown.stream.render.core.ast;

public abstract class Node {
    private Node parent;
    private Node firstChild;
    private Node lastChild;
    private Node next;
    private Node previous;

    public abstract void accept(Visitor visitor);

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
