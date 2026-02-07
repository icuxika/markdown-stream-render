package com.icuxika.markdown.stream.render.html.renderer;

import com.icuxika.markdown.stream.render.core.ast.BlockQuote;
import com.icuxika.markdown.stream.render.core.ast.BulletList;
import com.icuxika.markdown.stream.render.core.ast.Code;
import com.icuxika.markdown.stream.render.core.ast.CodeBlock;
import com.icuxika.markdown.stream.render.core.ast.Emphasis;
import com.icuxika.markdown.stream.render.core.ast.HardBreak;
import com.icuxika.markdown.stream.render.core.ast.Heading;
import com.icuxika.markdown.stream.render.core.ast.HtmlBlock;
import com.icuxika.markdown.stream.render.core.ast.HtmlInline;
import com.icuxika.markdown.stream.render.core.ast.Image;
import com.icuxika.markdown.stream.render.core.ast.Link;
import com.icuxika.markdown.stream.render.core.ast.ListItem;
import com.icuxika.markdown.stream.render.core.ast.Node;
import com.icuxika.markdown.stream.render.core.ast.OrderedList;
import com.icuxika.markdown.stream.render.core.ast.Paragraph;
import com.icuxika.markdown.stream.render.core.ast.SoftBreak;
import com.icuxika.markdown.stream.render.core.ast.Strikethrough;
import com.icuxika.markdown.stream.render.core.ast.StrongEmphasis;
import com.icuxika.markdown.stream.render.core.ast.Table;
import com.icuxika.markdown.stream.render.core.ast.TableBody;
import com.icuxika.markdown.stream.render.core.ast.TableCell;
import com.icuxika.markdown.stream.render.core.ast.TableHead;
import com.icuxika.markdown.stream.render.core.ast.TableRow;
import com.icuxika.markdown.stream.render.core.ast.Text;
import com.icuxika.markdown.stream.render.core.ast.ThematicBreak;
import com.icuxika.markdown.stream.render.core.extension.admonition.AdmonitionBlock;
import com.icuxika.markdown.stream.render.core.extension.math.MathNode;
import com.icuxika.markdown.stream.render.core.renderer.IStreamMarkdownRenderer;
import java.io.IOException;

/**
 * HTML 流式渲染器.
 * <p>
 * 将接收到的 AST 节点直接转换为 HTML 字符串并写入输出流。
 * </p>
 */
public class HtmlStreamRenderer implements IStreamMarkdownRenderer {

    private final Appendable out;

    public HtmlStreamRenderer(Appendable out) {
        this.out = out;
    }

    @Override
    public void openBlock(Node node) {
        try {
            if (node instanceof BlockQuote) {
                out.append("<blockquote>\n");
            } else if (node instanceof BulletList) {
                out.append("<ul>\n");
            } else if (node instanceof OrderedList) {
                OrderedList ol = (OrderedList) node;
                if (ol.getStartNumber() != 1) {
                    out.append("<ol start=\"").append(String.valueOf(ol.getStartNumber())).append("\">\n");
                } else {
                    out.append("<ol>\n");
                }
            } else if (node instanceof ListItem) {
                out.append("<li>");
            } else if (node instanceof AdmonitionBlock) {
                AdmonitionBlock admonition = (AdmonitionBlock) node;
                out.append("<div class=\"admonition admonition-").append(admonition.getType()).append("\">\n");
                if (admonition.getTitle() != null) {
                    out.append("<p class=\"admonition-title\">").append(escapeXml(admonition.getTitle()))
                            .append("</p>\n");
                }
            }
            if (out instanceof java.io.Flushable) {
                ((java.io.Flushable) out).flush();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void closeBlock(Node node) {
        try {
            if (node instanceof BlockQuote) {
                out.append("</blockquote>\n");
            } else if (node instanceof BulletList) {
                out.append("</ul>\n");
            } else if (node instanceof OrderedList) {
                out.append("</ol>\n");
            } else if (node instanceof ListItem) {
                out.append("</li>\n");
            } else if (node instanceof AdmonitionBlock) {
                out.append("</div>\n");
            }
            if (out instanceof java.io.Flushable) {
                ((java.io.Flushable) out).flush();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void renderNode(Node node) {
        try {
            // Only render Leaf Nodes
            if (node instanceof Paragraph || node instanceof Heading || node instanceof CodeBlock
                    || node instanceof HtmlBlock || node instanceof ThematicBreak || node instanceof Table) {
                render(node);
                if (out instanceof java.io.Flushable) {
                    ((java.io.Flushable) out).flush();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error writing to output", e);
        }
    }

    private void render(Node node) throws IOException {
        if (node instanceof Paragraph) {
            // Check if parent is tight list item?
            // Simplified: always render P
            out.append("<p>");
            renderInlines(node);
            out.append("</p>\n");
        } else if (node instanceof Heading) {
            Heading h = (Heading) node;
            out.append("<h").append(String.valueOf(h.getLevel()));
            if (h.getAnchorId() != null) {
                out.append(" id=\"").append(escapeXml(h.getAnchorId())).append("\"");
            }
            out.append(">");
            renderInlines(node);
            out.append("</h").append(String.valueOf(h.getLevel())).append(">\n");
        } else if (node instanceof CodeBlock) {
            out.append("<pre><code>");
            out.append(escapeXml(((CodeBlock) node).getLiteral()));
            out.append("</code></pre>\n");
        } else if (node instanceof HtmlBlock) {
            out.append(((HtmlBlock) node).getLiteral());
        } else if (node instanceof ThematicBreak) {
            out.append("<hr />\n");
        } else if (node instanceof Table) {
            out.append("<table>\n");
            // Table rendering requires traversing its children (Head, Body, Row, Cell)
            // Since Table is "finalized" as a whole, we can iterate its children safely.
            renderTableChildren(node);
            out.append("</table>\n");
        }
    }

    private void renderTableChildren(Node parent) throws IOException {
        Node child = parent.getFirstChild();
        while (child != null) {
            if (child instanceof TableHead) {
                out.append("<thead>\n");
                renderTableChildren(child);
                out.append("</thead>\n");
            } else if (child instanceof TableBody) {
                out.append("<tbody>\n");
                renderTableChildren(child);
                out.append("</tbody>\n");
            } else if (child instanceof TableRow) {
                out.append("<tr>\n");
                renderTableChildren(child);
                out.append("</tr>\n");
            } else if (child instanceof TableCell) {
                TableCell cell = (TableCell) child;
                String tag = cell.isHeader() ? "th" : "td";
                out.append("<").append(tag);
                if (cell.getAlignment() != null && cell.getAlignment() != TableCell.Alignment.NONE) {
                    out.append(" align=\"").append(cell.getAlignment().name().toLowerCase()).append("\"");
                }
                out.append(">");
                renderInlines(child);
                out.append("</").append(tag).append(">\n");
            }
            child = child.getNext();
        }
    }

    private void renderInlines(Node parent) throws IOException {
        Node child = parent.getFirstChild();
        while (child != null) {
            renderInline(child);
            child = child.getNext();
        }
    }

    private void renderInline(Node child) throws IOException {
        if (child instanceof Text) {
            out.append(escapeXml(((Text) child).getLiteral()));
        } else if (child instanceof Emphasis) {
            out.append("<em>");
            renderInlines(child);
            out.append("</em>");
        } else if (child instanceof StrongEmphasis) {
            out.append("<strong>");
            renderInlines(child);
            out.append("</strong>");
        } else if (child instanceof Code) {
            out.append("<code>");
            out.append(escapeXml(((Code) child).getLiteral()));
            out.append("</code>");
        } else if (child instanceof SoftBreak) {
            out.append("\n");
        } else if (child instanceof HardBreak) {
            out.append("<br />\n");
        } else if (child instanceof Link) {
            Link l = (Link) child;
            out.append("<a href=\"").append(escapeXml(l.getDestination())).append("\">");
            renderInlines(child);
            out.append("</a>");
        } else if (child instanceof Image) {
            Image i = (Image) child;
            out.append("<img src=\"").append(escapeXml(i.getDestination())).append("\" alt=\"placeholder\" />");
        } else if (child instanceof HtmlInline) {
            out.append(((HtmlInline) child).getLiteral());
        } else if (child instanceof Strikethrough) {
            out.append("<del>");
            renderInlines(child);
            out.append("</del>");
        } else if (child instanceof MathNode) {
            out.append("<span class=\"markdown-math\">");
            out.append(escapeXml(((MathNode) child).getContent()));
            out.append("</span>");
        } else {
            renderInlines(child);
        }
    }

    private String escapeXml(String s) {
        if (s == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '<' :
                    sb.append("&lt;");
                    break;
                case '>' :
                    sb.append("&gt;");
                    break;
                case '&' :
                    sb.append("&amp;");
                    break;
                case '"' :
                    sb.append("&quot;");
                    break;
                default :
                    sb.append(c);
            }
        }
        return sb.toString();
    }
}
