package com.icuxika.markdown.stream.render.core.renderer;

import com.icuxika.markdown.stream.render.core.ast.*;

public class HtmlRenderer implements IMarkdownRenderer {
    private final StringBuilder sb = new StringBuilder();

    @Override
    public Object getResult() {
        return sb.toString();
    }

    @Override
    public void visit(Document document) {
        Node child = document.getFirstChild();
        while (child != null) {
            child.accept(this);
            child = child.getNext();
        }
    }

    @Override
    public void visit(HtmlBlock htmlBlock) {
        sb.append(htmlBlock.getLiteral());
    }

    @Override
    public void visit(HtmlInline htmlInline) {
        sb.append(htmlInline.getLiteral());
    }

    @Override
    public void visit(Paragraph paragraph) {
        boolean inTightList = false;
        Node parent = paragraph.getParent();
        if (parent instanceof ListItem) {
            Node grandParent = parent.getParent();
            if (grandParent instanceof BulletList) {
                inTightList = ((BulletList) grandParent).isTight();
            } else if (grandParent instanceof OrderedList) {
                inTightList = ((OrderedList) grandParent).isTight();
            }
        }

        if (inTightList) {
            visitChildren(paragraph);
            if (paragraph.getNext() != null) {
                sb.append("\n");
            }
        } else {
            sb.append("<p>");
            visitChildren(paragraph);
            sb.append("</p>\n");
        }
    }

    @Override
    public void visit(Heading heading) {
        sb.append("<h").append(heading.getLevel()).append(">");
        visitChildren(heading);
        sb.append("</h").append(heading.getLevel()).append(">\n");
    }

    @Override
    public void visit(Text text) {
        sb.append(escapeContent(text.getLiteral()));
    }

    @Override
    public void visit(SoftBreak softBreak) {
        sb.append("\n");
    }

    @Override
    public void visit(HardBreak hardBreak) {
        sb.append("<br />\n");
    }

    @Override
    public void visit(Emphasis emphasis) {
        sb.append("<em>");
        visitChildren(emphasis);
        sb.append("</em>");
    }

    @Override
    public void visit(StrongEmphasis strongEmphasis) {
        sb.append("<strong>");
        visitChildren(strongEmphasis);
        sb.append("</strong>");
    }

    @Override
    public void visit(BlockQuote blockQuote) {
        sb.append("<blockquote>\n");
        visitChildren(blockQuote);
        sb.append("</blockquote>\n");
    }

    @Override
    public void visit(BulletList bulletList) {
        sb.append("<ul>\n");
        visitChildren(bulletList);
        sb.append("</ul>\n");
    }

    @Override
    public void visit(OrderedList orderedList) {
        sb.append("<ol");
        if (orderedList.getStartNumber() != 1) {
            sb.append(" start=\"").append(orderedList.getStartNumber()).append("\"");
        }
        sb.append(">\n");
        visitChildren(orderedList);
        sb.append("</ol>\n");
    }

    @Override
    public void visit(ListItem listItem) {
        sb.append("<li>");

        boolean tight = false;
        Node parent = listItem.getParent();
        if (parent instanceof BulletList) {
            tight = ((BulletList) parent).isTight();
        } else if (parent instanceof OrderedList) {
            tight = ((OrderedList) parent).isTight();
        }

        boolean addNewline = !tight;
        if (listItem.getFirstChild() == null) {
            addNewline = false;
        } else if (tight) {
            Node first = listItem.getFirstChild();
            if (first instanceof Block && !(first instanceof Paragraph)) {
                addNewline = true;
            }
        }

        if (addNewline) {
            sb.append("\n");
        }

        visitChildren(listItem);
        sb.append("</li>\n");
    }

    @Override
    public void visit(Code code) {
        sb.append("<code>");
        sb.append(escapeContent(code.getLiteral()));
        sb.append("</code>");
    }

    @Override
    public void visit(ThematicBreak thematicBreak) {
        sb.append("<hr />\n");
    }

    @Override
    public void visit(CodeBlock codeBlock) {
        sb.append("<pre><code");
        if (codeBlock.getInfo() != null && !codeBlock.getInfo().isEmpty()) {
            String info = codeBlock.getInfo();
            // Get first word of info string
            int spaceIndex = info.indexOf(' ');
            if (spaceIndex != -1) {
                info = info.substring(0, spaceIndex);
            }
            sb.append(" class=\"language-").append(escapeAttribute(info)).append("\"");
        }
        sb.append(">");
        sb.append(escapeContent(codeBlock.getLiteral()));
        sb.append("</code></pre>\n");
    }

    @Override
    public void visit(Link link) {
        sb.append("<a href=\"").append(escapeAttribute(encodeUrl(link.getDestination()))).append("\"");
        if (link.getTitle() != null && !link.getTitle().isEmpty()) {
            sb.append(" title=\"").append(escapeAttribute(link.getTitle())).append("\"");
        }
        sb.append(">");
        visitChildren(link);
        sb.append("</a>");
    }

    @Override
    public void visit(Image image) {
        sb.append("<img src=\"").append(escapeAttribute(encodeUrl(image.getDestination()))).append("\"");
        sb.append(" alt=\"");
        sb.append(escapeAttribute(renderTextContent(image)));
        sb.append("\"");
        if (image.getTitle() != null && !image.getTitle().isEmpty()) {
            sb.append(" title=\"").append(escapeAttribute(image.getTitle())).append("\"");
        }
        sb.append(" />");
        // Do NOT visit children, as they are rendered in alt attribute
    }

    @Override
    public void visit(Table table) {
        sb.append("<table>\n");
        visitChildren(table);
        sb.append("</table>\n");
    }

    @Override
    public void visit(TableHead tableHead) {
        sb.append("<thead>\n");
        visitChildren(tableHead);
        sb.append("</thead>\n");
    }

    @Override
    public void visit(TableBody tableBody) {
        sb.append("<tbody>\n");
        visitChildren(tableBody);
        sb.append("</tbody>\n");
    }

    @Override
    public void visit(TableRow tableRow) {
        sb.append("<tr>\n");
        visitChildren(tableRow);
        sb.append("</tr>\n");
    }

    @Override
    public void visit(TableCell tableCell) {
        String tag = tableCell.isHeader() ? "th" : "td";
        sb.append("<").append(tag);
        if (tableCell.getAlignment() != null && tableCell.getAlignment() != TableCell.Alignment.NONE) {
            sb.append(" align=\"").append(tableCell.getAlignment().name().toLowerCase()).append("\"");
        }
        sb.append(">");
        visitChildren(tableCell);
        sb.append("</").append(tag).append(">\n");
    }

    private String encodeUrl(String url) {
        if (url == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < url.length(); i++) {
            char c = url.charAt(i);
            if (c == '%') {
                if (i + 2 < url.length()) {
                    char c1 = url.charAt(i + 1);
                    char c2 = url.charAt(i + 2);
                    if (isHex(c1) && isHex(c2)) {
                        sb.append(c);
                        sb.append(c1);
                        sb.append(c2);
                        i += 2;
                        continue;
                    }
                }
            }

            if (c <= 32 || c >= 127 || c == '%' || c == '[' || c == ']' || c == '\\' || c == '"' || c == '<' || c == '>' || c == '^' || c == '`' || c == '{' || c == '|' || c == '}') {
                byte[] bytes = new String(new char[]{c}).getBytes(java.nio.charset.StandardCharsets.UTF_8);
                for (byte b : bytes) {
                    sb.append(String.format("%%%02X", b));
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private boolean isHex(char c) {
        return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
    }

    private String renderTextContent(Node parent) {
        StringBuilder textContent = new StringBuilder();
        Node child = parent.getFirstChild();
        while (child != null) {
            if (child instanceof Text) {
                textContent.append(((Text) child).getLiteral());
            } else if (child instanceof Code) {
                textContent.append(((Code) child).getLiteral());
            } else if (child instanceof SoftBreak || child instanceof HardBreak) {
                textContent.append("\n");
            } else {
                textContent.append(renderTextContent(child));
            }
            child = child.getNext();
        }
        return textContent.toString();
    }

    private void visitChildren(Node parent) {
        Node child = parent.getFirstChild();
        while (child != null) {
            child.accept(this);
            child = child.getNext();
        }
    }

    private String escapeContent(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private String escapeAttribute(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
