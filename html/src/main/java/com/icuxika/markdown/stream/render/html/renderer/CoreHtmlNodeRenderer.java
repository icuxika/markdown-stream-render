package com.icuxika.markdown.stream.render.html.renderer;

import com.icuxika.markdown.stream.render.core.ast.Block;
import com.icuxika.markdown.stream.render.core.ast.BlockQuote;
import com.icuxika.markdown.stream.render.core.ast.BulletList;
import com.icuxika.markdown.stream.render.core.ast.Code;
import com.icuxika.markdown.stream.render.core.ast.CodeBlock;
import com.icuxika.markdown.stream.render.core.ast.Document;
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
import java.util.HashSet;
import java.util.Set;

/**
 * The default renderer that handles all core CommonMark nodes.
 */
public class CoreHtmlNodeRenderer implements HtmlNodeRenderer {

    private final HtmlNodeRendererContext context;
    private final HtmlWriter html;

    public CoreHtmlNodeRenderer(HtmlNodeRendererContext context) {
        this.context = context;
        this.html = context.getWriter();
    }

    @Override
    public Set<Class<? extends Node>> getNodeTypes() {
        // Return all standard node types
        Set<Class<? extends Node>> types = new HashSet<>();
        types.add(Document.class);
        types.add(Heading.class);
        types.add(Paragraph.class);
        types.add(BlockQuote.class);
        types.add(BulletList.class);
        types.add(OrderedList.class);
        types.add(ListItem.class);
        types.add(ThematicBreak.class);
        types.add(CodeBlock.class);
        types.add(HtmlBlock.class);
        types.add(Text.class);
        types.add(Emphasis.class);
        types.add(StrongEmphasis.class);
        types.add(Code.class);
        types.add(HtmlInline.class);
        types.add(SoftBreak.class);
        types.add(HardBreak.class);
        types.add(Link.class);
        types.add(Image.class);

        // GFM extensions
        types.add(Table.class);
        types.add(TableHead.class);
        types.add(TableBody.class);
        types.add(TableRow.class);
        types.add(TableCell.class);
        types.add(Strikethrough.class);

        return types;
    }

    @Override
    public void render(Node node) {
        if (node instanceof Document) {
            context.renderChildren(node);
        } else if (node instanceof Heading) {
            renderHeading((Heading) node);
        } else if (node instanceof Paragraph) {
            renderParagraph((Paragraph) node);
        } else if (node instanceof BlockQuote) {
            renderBlockQuote((BlockQuote) node);
        } else if (node instanceof BulletList) {
            renderBulletList((BulletList) node);
        } else if (node instanceof OrderedList) {
            renderOrderedList((OrderedList) node);
        } else if (node instanceof ListItem) {
            renderListItem((ListItem) node);
        } else if (node instanceof ThematicBreak) {
            renderThematicBreak((ThematicBreak) node);
        } else if (node instanceof CodeBlock) {
            renderCodeBlock((CodeBlock) node);
        } else if (node instanceof HtmlBlock) {
            renderHtmlBlock((HtmlBlock) node);
        } else if (node instanceof Text) {
            renderText((Text) node);
        } else if (node instanceof Emphasis) {
            renderEmphasis((Emphasis) node);
        } else if (node instanceof StrongEmphasis) {
            renderStrongEmphasis((StrongEmphasis) node);
        } else if (node instanceof Code) {
            renderCode((Code) node);
        } else if (node instanceof HtmlInline) {
            renderHtmlInline((HtmlInline) node);
        } else if (node instanceof SoftBreak) {
            renderSoftBreak((SoftBreak) node);
        } else if (node instanceof HardBreak) {
            renderHardBreak((HardBreak) node);
        } else if (node instanceof Link) {
            renderLink((Link) node);
        } else if (node instanceof Image) {
            renderImage((Image) node);
        } else if (node instanceof Table) {
            renderTable((Table) node);
        } else if (node instanceof TableHead) {
            renderTableHead((TableHead) node);
        } else if (node instanceof TableBody) {
            renderTableBody((TableBody) node);
        } else if (node instanceof TableRow) {
            renderTableRow((TableRow) node);
        } else if (node instanceof TableCell) {
            renderTableCell((TableCell) node);
        } else if (node instanceof Strikethrough) {
            renderStrikethrough((Strikethrough) node);
        }
    }

    // --- Render Methods (Copied and adapted from original HtmlRenderer) ---

    private void renderHeading(Heading heading) {
        java.util.Map<String, String> attrs = new java.util.HashMap<>();
        if (context.getOptions().isGenerateHeadingIds() && heading.getAnchorId() != null) {
            attrs.put("id", heading.getAnchorId());
        }
        html.tag("h" + heading.getLevel(), attrs);
        context.renderChildren(heading);
        html.closeTag("h" + heading.getLevel());
        html.line();
    }

    private void renderParagraph(Paragraph paragraph) {
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
            context.renderChildren(paragraph);
            if (paragraph.getNext() != null) {
                html.line();
            }
        } else {
            html.tag("p");
            context.renderChildren(paragraph);
            html.closeTag("p");
            html.line();
        }
    }

    private void renderBlockQuote(BlockQuote blockQuote) {
        html.tag("blockquote");
        html.line();
        context.renderChildren(blockQuote);
        html.closeTag("blockquote");
        html.line();
    }

    private void renderBulletList(BulletList bulletList) {
        html.tag("ul");
        html.line();
        context.renderChildren(bulletList);
        html.closeTag("ul");
        html.line();
    }

    private void renderOrderedList(OrderedList orderedList) {
        java.util.Map<String, String> attrs = new java.util.HashMap<>();
        if (orderedList.getStartNumber() != 1) {
            attrs.put("start", String.valueOf(orderedList.getStartNumber()));
        }
        html.tag("ol", attrs);
        html.line();
        context.renderChildren(orderedList);
        html.closeTag("ol");
        html.line();
    }

    private void renderListItem(ListItem listItem) {
        html.tag("li");

        if (listItem.isTask()) {
            java.util.Map<String, String> attrs = new java.util.LinkedHashMap<>();
            if (listItem.isChecked()) {
                attrs.put("checked", "");
            }
            attrs.put("disabled", "");
            attrs.put("type", "checkbox");
            html.tag("input", attrs, true);
            html.text(" ");
        }

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
            html.line();
        }

        context.renderChildren(listItem);
        html.closeTag("li");
        html.line();
    }

    private void renderThematicBreak(ThematicBreak thematicBreak) {
        html.tag("hr", null, true);
        html.line();
    }

    private void renderCodeBlock(CodeBlock codeBlock) {
        html.tag("pre");
        java.util.Map<String, String> attrs = new java.util.HashMap<>();
        if (codeBlock.getInfo() != null && !codeBlock.getInfo().isEmpty()) {
            String info = codeBlock.getInfo();
            int spaceIndex = info.indexOf(' ');
            if (spaceIndex != -1) {
                info = info.substring(0, spaceIndex);
            }
            attrs.put("class", "language-" + info);
        }
        html.tag("code", attrs);
        html.text(codeBlock.getLiteral());
        html.closeTag("code");
        html.closeTag("pre");
        html.line();
    }

    private void renderHtmlBlock(HtmlBlock htmlBlock) {
        // We need access to options, but Context doesn't expose them directly in the
        // interface.
        // We can cast context to HtmlRenderer, or just ignore safe mode here?
        // Ideally context should expose options or safe mode.
        // For now, let's assume we can cast or we need to add getOptions() to Context.
        // Let's modify NodeRendererContext to include getOptions() or similar.
        // Or for now, we can check instance.
        boolean safeMode = false;
        if (context instanceof HtmlRenderer) {
            safeMode = ((HtmlRenderer) context).getOptions().isSafeMode();
        }

        if (safeMode) {
            html.raw("<!-- Raw HTML Omitted -->\n");
        } else {
            html.raw(htmlBlock.getLiteral());
        }
    }

    private void renderText(Text text) {
        html.text(text.getLiteral());
    }

    private void renderEmphasis(Emphasis emphasis) {
        html.tag("em");
        context.renderChildren(emphasis);
        html.closeTag("em");
    }

    private void renderStrongEmphasis(StrongEmphasis strongEmphasis) {
        html.tag("strong");
        context.renderChildren(strongEmphasis);
        html.closeTag("strong");
    }

    private void renderCode(Code code) {
        html.tag("code");
        html.text(code.getLiteral());
        html.closeTag("code");
    }

    private void renderHtmlInline(HtmlInline htmlInline) {
        boolean safeMode = false;
        if (context instanceof HtmlRenderer) {
            safeMode = ((HtmlRenderer) context).getOptions().isSafeMode();
        }

        if (safeMode) {
            html.raw("<!-- Raw HTML Omitted -->");
        } else {
            html.raw(htmlInline.getLiteral());
        }
    }

    private void renderSoftBreak(SoftBreak softBreak) {
        html.line();
    }

    private void renderHardBreak(HardBreak hardBreak) {
        html.tag("br", null, true);
        html.line();
    }

    private void renderLink(Link link) {
        java.util.Map<String, String> attrs = new java.util.HashMap<>();
        attrs.put("href", encodeUrl(link.getDestination()));
        if (link.getTitle() != null && !link.getTitle().isEmpty()) {
            attrs.put("title", link.getTitle());
        }
        html.tag("a", attrs);
        context.renderChildren(link);
        html.closeTag("a");
    }

    private void renderImage(Image image) {
        // Use LinkedHashMap for predictable order
        java.util.Map<String, String> attrs = new java.util.LinkedHashMap<>();
        attrs.put("src", encodeUrl(image.getDestination()));
        attrs.put("alt", renderTextContent(image));
        if (image.getTitle() != null && !image.getTitle().isEmpty()) {
            attrs.put("title", image.getTitle());
        }
        html.tag("img", attrs, true);
    }

    private void renderTable(Table table) {
        html.tag("table");
        html.line();
        context.renderChildren(table);
        html.closeTag("table");
        html.line();
    }

    private void renderTableHead(TableHead tableHead) {
        html.tag("thead");
        html.line();
        context.renderChildren(tableHead);
        html.closeTag("thead");
        html.line();
    }

    private void renderTableBody(TableBody tableBody) {
        // GFM: If table body is empty (no rows), don't render tbody tag?
        // Spec Example 205: | abc | def |\n| --- | --- |\n
        // Output: <table><thead>...</thead></table> (No tbody)
        if (tableBody.getFirstChild() == null) {
            return;
        }

        html.tag("tbody");
        html.line();
        context.renderChildren(tableBody);
        html.closeTag("tbody");
        html.line();
    }

    private void renderTableRow(TableRow tableRow) {
        html.tag("tr");
        html.line();
        context.renderChildren(tableRow);
        html.closeTag("tr");
        html.line();
    }

    private void renderTableCell(TableCell tableCell) {
        String tag = tableCell.isHeader() ? "th" : "td";
        java.util.Map<String, String> attrs = new java.util.HashMap<>();
        if (tableCell.getAlignment() != null && tableCell.getAlignment() != TableCell.Alignment.NONE) {
            attrs.put("align", tableCell.getAlignment().name().toLowerCase());
        }
        html.tag(tag, attrs);
        context.renderChildren(tableCell);
        html.closeTag(tag);
        html.line();
    }

    private void renderStrikethrough(Strikethrough strikethrough) {
        html.tag("del");
        context.renderChildren(strikethrough);
        html.closeTag("del");
    }

    // Helper methods

    private String renderTextContent(Node parent) {
        // We can't easily reuse the one from HtmlRenderer because it's private.
        // Re-implementing simplified version.
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

    private String encodeUrl(String url) {
        // Reuse the logic from original or a simplified one?
        // Ideally this should be a utility method.
        // For brevity, I'll copy the logic.
        if (url == null) {
            return "";
        }
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

            if (c <= 32 || c >= 127 || c == '%' || c == '[' || c == ']' || c == '\\' || c == '"' || c == '<' || c == '>'
                    || c == '^' || c == '`' || c == '{' || c == '|' || c == '}') {
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
}
