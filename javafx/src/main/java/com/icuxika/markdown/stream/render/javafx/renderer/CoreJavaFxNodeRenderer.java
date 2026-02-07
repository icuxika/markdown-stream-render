package com.icuxika.markdown.stream.render.javafx.renderer;

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
import java.util.Stack;
import java.util.function.Consumer;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextFlow;

/**
 * 核心 JavaFX 节点渲染器.
 * <p>
 * 实现了标准 CommonMark 节点（如段落、标题、列表、表格、代码块等）的 JavaFX 渲染逻辑。
 * </p>
 */
public class CoreJavaFxNodeRenderer implements JavaFxNodeRenderer {

    private final JavaFxNodeRendererContext context;
    private final Consumer<String> onLinkClick; // Passed from renderer

    // Style state (moved from JavaFxRenderer)
    private boolean bold = false;
    private boolean italic = false;
    private boolean code = false; // Monospace
    private boolean strike = false;
    private boolean codeVal = false;
    private boolean isLink = false;
    private String linkDestination = null;
    private int headingLevel = 0;

    // Table state
    private int tableRowIndex = 0;
    private int tableColIndex = 0;

    // List state
    private static class ListState {
        boolean isOrdered;
        int index;
        char delimiter;

        ListState(boolean isOrdered, int index, char delimiter) {
            this.isOrdered = isOrdered;
            this.index = index;
            this.delimiter = delimiter;
        }
    }

    private final Stack<ListState> listStack = new Stack<>();

    // Inlines
    private TextFlow currentTextFlow;

    // Image cache to prevent reloading and flickering
    private static final java.util.Map<String, javafx.scene.image.Image> IMAGE_CACHE = new java.util.concurrent.ConcurrentHashMap<>();

    public CoreJavaFxNodeRenderer(JavaFxNodeRendererContext context, Consumer<String> onLinkClick) {
        this.context = context;
        this.onLinkClick = onLinkClick;
    }

    @Override
    public Set<Class<? extends Node>> getNodeTypes() {
        Set<Class<? extends Node>> types = new HashSet<>();
        types.add(Document.class);
        types.add(HtmlBlock.class);
        types.add(HtmlInline.class);
        types.add(Paragraph.class);
        types.add(Heading.class);
        types.add(Text.class);
        types.add(SoftBreak.class);
        types.add(HardBreak.class);
        types.add(Emphasis.class);
        types.add(StrongEmphasis.class);
        types.add(BlockQuote.class);
        types.add(BulletList.class);
        types.add(OrderedList.class);
        types.add(ListItem.class);
        types.add(Strikethrough.class);
        types.add(Code.class);
        types.add(ThematicBreak.class);
        types.add(CodeBlock.class);
        types.add(Link.class);
        types.add(Image.class);
        types.add(Table.class);
        types.add(TableHead.class);
        types.add(TableBody.class);
        types.add(TableRow.class);
        types.add(TableCell.class);
        return types;
    }

    @Override
    public void render(Node node) {
        if (node instanceof Document) {
            context.renderChildren(node);
        } else if (node instanceof HtmlBlock) {
            renderHtmlBlock((HtmlBlock) node);
        } else if (node instanceof HtmlInline) {
            renderHtmlInline((HtmlInline) node);
        } else if (node instanceof Paragraph) {
            renderParagraph((Paragraph) node);
        } else if (node instanceof Heading) {
            renderHeading((Heading) node);
        } else if (node instanceof Text) {
            renderText((Text) node);
        } else if (node instanceof SoftBreak) {
            renderSoftBreak((SoftBreak) node);
        } else if (node instanceof HardBreak) {
            renderHardBreak((HardBreak) node);
        } else if (node instanceof Emphasis) {
            renderEmphasis((Emphasis) node);
        } else if (node instanceof StrongEmphasis) {
            renderStrongEmphasis((StrongEmphasis) node);
        } else if (node instanceof BlockQuote) {
            renderBlockQuote((BlockQuote) node);
        } else if (node instanceof BulletList) {
            renderBulletList((BulletList) node);
        } else if (node instanceof OrderedList) {
            renderOrderedList((OrderedList) node);
        } else if (node instanceof ListItem) {
            renderListItem((ListItem) node);
        } else if (node instanceof Strikethrough) {
            renderStrikethrough((Strikethrough) node);
        } else if (node instanceof Code) {
            renderCode((Code) node);
        } else if (node instanceof ThematicBreak) {
            renderThematicBreak((ThematicBreak) node);
        } else if (node instanceof CodeBlock) {
            renderCodeBlock((CodeBlock) node);
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
        }
    }

    // --- Render Methods (Copied and adapted from JavaFxRenderer) ---

    private void renderHtmlBlock(HtmlBlock htmlBlock) {
        javafx.scene.text.Text text = new javafx.scene.text.Text(htmlBlock.getLiteral());
        text.getStyleClass().add("markdown-text");
        text.getStyleClass().add("markdown-html-block");
        context.getCurrentContainer().getChildren().add(text);
        context.registerNode(htmlBlock, text);
    }

    private void renderHtmlInline(HtmlInline htmlInline) {
        if (currentTextFlow != null) {
            javafx.scene.text.Text text = new javafx.scene.text.Text(htmlInline.getLiteral());
            text.getStyleClass().add("markdown-text");
            text.getStyleClass().add("markdown-html-inline");
            currentTextFlow.getChildren().add(text);
        }
    }

    /**
     * 渲染段落. 段落通常作为 TextFlow 容器，用于包含行内文本和元素。
     */
    private void renderParagraph(Paragraph paragraph) {
        TextFlow flow = new TextFlow();
        flow.getStyleClass().add("markdown-paragraph");

        context.getCurrentContainer().getChildren().add(flow);
        context.registerNode(paragraph, flow);

        context.pushContainer(flow);
        currentTextFlow = flow;
        context.renderChildren(paragraph);
        currentTextFlow = null;
        context.popContainer();
    }

    /**
     * 渲染标题. 根据标题级别应用不同的样式类（markdown-h1 ~ markdown-h6）。
     */
    private void renderHeading(Heading heading) {
        TextFlow flow = new TextFlow();
        flow.getStyleClass().add("markdown-heading");
        flow.getStyleClass().add("markdown-h" + heading.getLevel());
        context.getCurrentContainer().getChildren().add(flow);
        context.registerNode(heading, flow);

        context.pushContainer(flow);
        currentTextFlow = flow;

        int oldLevel = headingLevel;
        headingLevel = heading.getLevel();
        context.renderChildren(heading);
        headingLevel = oldLevel;

        currentTextFlow = null;
        context.popContainer();
    }

    /**
     * 渲染纯文本. 根据当前状态应用样式（粗体、斜体、删除线、代码、链接等）。
     */
    private void renderText(Text text) {
        javafx.scene.text.Text t = new javafx.scene.text.Text(text.getLiteral());
        applyStyle(t);
        if (currentTextFlow != null) {
            currentTextFlow.getChildren().add(t);
        } else {
            context.getCurrentContainer().getChildren().add(t);
        }
    }

    private void renderSoftBreak(SoftBreak softBreak) {
        if (currentTextFlow != null) {
            currentTextFlow.getChildren().add(new javafx.scene.text.Text("\n"));
        }
    }

    private void renderHardBreak(HardBreak hardBreak) {
        if (currentTextFlow != null) {
            currentTextFlow.getChildren().add(new javafx.scene.text.Text("\n"));
        }
    }

    private void renderEmphasis(Emphasis emphasis) {
        boolean old = italic;
        italic = true;
        context.renderChildren(emphasis);
        italic = old;
    }

    private void renderStrongEmphasis(StrongEmphasis strongEmphasis) {
        boolean old = bold;
        bold = true;
        context.renderChildren(strongEmphasis);
        bold = old;
    }

    private void renderBlockQuote(BlockQuote blockQuote) {
        VBox quoteBox = new VBox();
        quoteBox.getStyleClass().add("markdown-quote");
        context.getCurrentContainer().getChildren().add(quoteBox);
        context.registerNode(blockQuote, quoteBox);

        context.pushContainer(quoteBox);
        context.renderChildren(blockQuote);
        context.popContainer();
    }

    private void renderBulletList(BulletList bulletList) {
        VBox listBox = new VBox();
        listBox.getStyleClass().add("markdown-list");
        listBox.setSpacing(5);
        context.getCurrentContainer().getChildren().add(listBox);
        context.registerNode(bulletList, listBox);

        context.pushContainer(listBox);
        listStack.push(new ListState(false, 0, '.'));
        context.renderChildren(bulletList);
        listStack.pop();
        context.popContainer();
    }

    private void renderOrderedList(OrderedList orderedList) {
        VBox listBox = new VBox();
        listBox.getStyleClass().add("markdown-list");
        listBox.setSpacing(5);
        context.getCurrentContainer().getChildren().add(listBox);
        context.registerNode(orderedList, listBox);

        context.pushContainer(listBox);
        listStack.push(new ListState(true, orderedList.getStartNumber(), orderedList.getDelimiter()));
        context.renderChildren(orderedList);
        listStack.pop();
        context.popContainer();
    }

    @SuppressWarnings("checkstyle:AvoidEscapedUnicodeCharacters")
    private void renderListItem(ListItem listItem) {
        javafx.scene.layout.HBox itemBox = new javafx.scene.layout.HBox();
        itemBox.setSpacing(5);
        itemBox.getStyleClass().add("markdown-list-item");

        // 1. Determine Bullet/Number Marker
        boolean isOrdered = false;
        int index = 0;
        char delimiter = '.';

        if (!listStack.isEmpty()) {
            ListState state = listStack.peek();
            isOrdered = state.isOrdered;
            index = state.index;
            delimiter = state.delimiter;

            // Increment index for next item
            if (isOrdered) {
                state.index++;
            }
        }

        java.util.List<javafx.scene.Node> markers = new java.util.ArrayList<>();

        if (isOrdered) {
            String markerText = index + String.valueOf(delimiter);
            Label markerLabel = new Label(markerText);
            markerLabel.getStyleClass().add("markdown-list-marker");
            markerLabel.setMinWidth(20);
            markerLabel.setAlignment(javafx.geometry.Pos.TOP_RIGHT);
            markers.add(markerLabel);
        } else if (!listItem.isTask()) {
            // Only show bullet if NOT a task (GitHub style: bullet replaced by checkbox)
            String markerText = "\u2022";
            Label markerLabel = new Label(markerText);
            markerLabel.getStyleClass().add("markdown-list-marker");
            markerLabel.setMinWidth(20);
            markerLabel.setAlignment(javafx.geometry.Pos.TOP_RIGHT);
            markers.add(markerLabel);
        }

        // 2. Add Checkbox if Task
        if (listItem.isTask()) {
            javafx.scene.control.CheckBox checkBox = new javafx.scene.control.CheckBox();
            checkBox.setSelected(listItem.isChecked());
            checkBox.setDisable(true); // Read-only
            checkBox.getStyleClass().add("markdown-task-checkbox");

            // Fix vertical alignment: CheckBox in JavaFX defaults to CENTER_LEFT.
            // If the text is multi-line, we want it top-aligned?
            // Actually, usually CheckBox icon is small, so CENTER_LEFT relative to first
            // line of text is good.
            // But if we put it in an HBox with TOP_LEFT alignment, it might stick to top.
            // Let's ensure it has a min-height matching the text line-height to center the
            // box relative to text.
            checkBox.setMinHeight(20);
            checkBox.setAlignment(javafx.geometry.Pos.TOP_LEFT); // Align the box itself to top if it has height?

            markers.add(checkBox);
        }

        VBox contentBox = new VBox();
        contentBox.getStyleClass().add("markdown-list-content");
        // Allow contentBox to grow and fill available width in HBox
        HBox.setHgrow(contentBox, Priority.ALWAYS);

        itemBox.getChildren().addAll(markers);
        itemBox.getChildren().add(contentBox);

        context.getCurrentContainer().getChildren().add(itemBox);
        context.registerNode(listItem, itemBox);

        context.pushContainer(contentBox);
        context.renderChildren(listItem);
        context.popContainer();
    }

    private void renderStrikethrough(Strikethrough strikethrough) {
        boolean old = strike;
        strike = true;
        context.renderChildren(strikethrough);
        strike = old;
    }

    private void renderCode(Code code) {
        boolean old = codeVal;
        codeVal = true;

        // Use Label for inline code to support background color and padding
        Label codeLabel = new Label(code.getLiteral());
        codeLabel.getStyleClass().add("markdown-code");

        if (currentTextFlow != null) {
            currentTextFlow.getChildren().add(codeLabel);
        } else {
            context.getCurrentContainer().getChildren().add(codeLabel);
        }

        codeVal = old;
    }

    private void renderThematicBreak(ThematicBreak thematicBreak) {
        javafx.scene.control.Separator sep = new javafx.scene.control.Separator();
        sep.getStyleClass().add("markdown-separator");
        context.getCurrentContainer().getChildren().add(sep);
        context.registerNode(thematicBreak, sep);
    }

    private void renderCodeBlock(CodeBlock codeBlock) {
        String info = codeBlock.getInfo();
        String literal = codeBlock.getLiteral();

        if (info != null && !info.isBlank()) {
            String language = info.trim().split("\\s+")[0].toLowerCase();
            if ("java".equals(language)) {
                renderJavaCodeBlock(literal);
                return;
            } else if ("json".equals(language)) {
                renderJsonCodeBlock(literal);
                return;
            } else if ("xml".equals(language) || "html".equals(language)) {
                renderXmlCodeBlock(literal);
                return;
            } else if ("css".equals(language)) {
                renderCssCodeBlock(literal);
                return;
            } else if ("sql".equals(language)) {
                renderSqlCodeBlock(literal);
                return;
            } else if ("bash".equals(language) || "sh".equals(language)) {
                renderBashCodeBlock(literal);
                return;
            }
        }

        // Fallback for other languages or no language
        // Wrap in StackPane to allow adding copy button
        javafx.scene.layout.StackPane stack = new javafx.scene.layout.StackPane();
        stack.getStyleClass().add("markdown-code-block-container");

        Label l = new Label(literal);
        l.getStyleClass().add("markdown-code-block");
        l.setMaxWidth(Double.MAX_VALUE);
        stack.getChildren().add(l);

        addCopyButton(stack, literal);

        context.getCurrentContainer().getChildren().add(stack);
        context.registerNode(codeBlock, stack);
    }

    private void renderJavaCodeBlock(String code) {
        javafx.scene.layout.StackPane stack = new javafx.scene.layout.StackPane();
        stack.getStyleClass().add("markdown-code-block-container");

        TextFlow flow = new TextFlow();
        flow.getStyleClass().add("markdown-code-block-flow");

        // Simple tokenizer for Java keywords
        String[] tokens = code.split("(?<=\\s)|(?=\\s)|(?<=[\\(\\)\\{\\};\\.])|(?=[\\(\\)\\{\\};\\.])");
        for (String token : tokens) {
            javafx.scene.text.Text t = new javafx.scene.text.Text(token);
            t.getStyleClass().add("markdown-code-text");

            if (isJavaKeyword(token)) {
                t.getStyleClass().add("code-keyword");
            } else if (token.matches("\".*\"")) {
                t.getStyleClass().add("code-string");
            } else if (token.matches("//.*|/\\*.*\\*/")) { // Very basic comment check (single token only)
                t.getStyleClass().add("code-comment");
            }
            flow.getChildren().add(t);
        }

        stack.getChildren().add(flow);
        addCopyButton(stack, code);

        context.getCurrentContainer().getChildren().add(stack);
    }

    private void renderJsonCodeBlock(String code) {
        javafx.scene.layout.StackPane stack = new javafx.scene.layout.StackPane();
        stack.getStyleClass().add("markdown-code-block-container");

        TextFlow flow = new TextFlow();
        flow.getStyleClass().add("markdown-code-block-flow");

        // Simple tokenizer for JSON
        String[] tokens = code.split("(?<=\\s)|(?=\\s)|(?<=[\\{\\}\\[\\]:,])|(?=[\\{\\}\\[\\]:,])");
        for (String token : tokens) {
            javafx.scene.text.Text t = new javafx.scene.text.Text(token);
            t.getStyleClass().add("markdown-code-text");

            if (token.matches("\".*\":")) { // Key
                t.getStyleClass().add("code-json-key");
            } else if (token.matches("\".*\"")) { // String value
                t.getStyleClass().add("code-string");
            } else if (token.matches("true|false|null")) {
                t.getStyleClass().add("code-keyword");
            } else if (token.matches("-?\\d+(\\.\\d+)?")) {
                t.getStyleClass().add("code-number");
            }
            flow.getChildren().add(t);
        }

        stack.getChildren().add(flow);
        addCopyButton(stack, code);

        context.getCurrentContainer().getChildren().add(stack);
    }

    private void renderXmlCodeBlock(String code) {
        javafx.scene.layout.StackPane stack = new javafx.scene.layout.StackPane();
        stack.getStyleClass().add("markdown-code-block-container");

        TextFlow flow = new TextFlow();
        flow.getStyleClass().add("markdown-code-block-flow");

        // Simple tokenizer for XML/HTML: <tag> attr="val" </tag> <!-- comment -->
        String[] tokens = code.split("(?<=\\s)|(?=\\s)|(?<=[<>])|(?=[<>])|(?<==)|(?==)");
        for (String token : tokens) {
            javafx.scene.text.Text t = new javafx.scene.text.Text(token);
            t.getStyleClass().add("markdown-code-text");

            if (token.startsWith("<") && token.length() > 1 && !token.startsWith("<!--")) {
                t.getStyleClass().add("code-tag");
            } else if (token.equals(">") || token.equals("<") || token.equals("/>") || token.equals("</")) {
                t.getStyleClass().add("code-tag");
            } else if (token.startsWith("<!--")) {
                t.getStyleClass().add("code-comment");
            } else if (token.matches("\".*\"") || token.matches("'.*'")) {
                t.getStyleClass().add("code-string");
            }
            // Basic attr detection is hard without state, but strings cover values.
            // Keys usually precede '='.
            flow.getChildren().add(t);
        }

        stack.getChildren().add(flow);
        addCopyButton(stack, code);
        context.getCurrentContainer().getChildren().add(stack);
    }

    private void renderCssCodeBlock(String code) {
        javafx.scene.layout.StackPane stack = new javafx.scene.layout.StackPane();
        stack.getStyleClass().add("markdown-code-block-container");

        TextFlow flow = new TextFlow();
        flow.getStyleClass().add("markdown-code-block-flow");

        // Simple tokenizer for CSS: selector { prop: val; }
        String[] tokens = code.split("(?<=\\s)|(?=\\s)|(?<=[\\{\\}:;])|(?=[\\{\\}:;])");
        for (String token : tokens) {
            javafx.scene.text.Text t = new javafx.scene.text.Text(token);
            t.getStyleClass().add("markdown-code-text");

            if (token.startsWith(".")) {
                t.getStyleClass().add("code-class");
            } else if (token.startsWith("#")) {
                t.getStyleClass().add("code-id");
            } else if (token.matches("-?[0-9]+(px|em|rem|%)?")) {
                t.getStyleClass().add("code-number");
            } else if (token.matches("#[0-9a-fA-F]{3,6}")) {
                t.getStyleClass().add("code-color");
            }
            flow.getChildren().add(t);
        }

        stack.getChildren().add(flow);
        addCopyButton(stack, code);
        context.getCurrentContainer().getChildren().add(stack);
    }

    private void renderSqlCodeBlock(String code) {
        javafx.scene.layout.StackPane stack = new javafx.scene.layout.StackPane();
        stack.getStyleClass().add("markdown-code-block-container");

        TextFlow flow = new TextFlow();
        flow.getStyleClass().add("markdown-code-block-flow");

        String[] tokens = code.split("(?<=\\s)|(?=\\s)|(?<=[(),;])|(?=[(),;])");
        for (String token : tokens) {
            javafx.scene.text.Text t = new javafx.scene.text.Text(token);
            t.getStyleClass().add("markdown-code-text");

            if (isSqlKeyword(token)) {
                t.getStyleClass().add("code-keyword");
            } else if (token.matches("'.*'") || token.matches("\".*\"")) {
                t.getStyleClass().add("code-string");
            } else if (token.matches("-?\\d+(\\.\\d+)?")) {
                t.getStyleClass().add("code-number");
            }
            flow.getChildren().add(t);
        }

        stack.getChildren().add(flow);
        addCopyButton(stack, code);
        context.getCurrentContainer().getChildren().add(stack);
    }

    private void renderBashCodeBlock(String code) {
        javafx.scene.layout.StackPane stack = new javafx.scene.layout.StackPane();
        stack.getStyleClass().add("markdown-code-block-container");

        TextFlow flow = new TextFlow();
        flow.getStyleClass().add("markdown-code-block-flow");

        String[] tokens = code.split("(?<=\\s)|(?=\\s)");
        for (String token : tokens) {
            javafx.scene.text.Text t = new javafx.scene.text.Text(token);
            t.getStyleClass().add("markdown-code-text");

            if (token.startsWith("#")) {
                t.getStyleClass().add("code-comment");
            } else if (token.startsWith("-")) {
                t.getStyleClass().add("code-attr");
            } else if (token.matches("\".*\"") || token.matches("'.*'")) {
                t.getStyleClass().add("code-string");
            } else if (isBashKeyword(token)) {
                t.getStyleClass().add("code-keyword");
            }
            flow.getChildren().add(t);
        }

        stack.getChildren().add(flow);
        addCopyButton(stack, code);
        context.getCurrentContainer().getChildren().add(stack);
    }

    private boolean isSqlKeyword(String text) {
        Set<String> keywords = Set.of("SELECT", "FROM", "WHERE", "INSERT", "INTO", "VALUES", "UPDATE", "SET", "DELETE",
                "CREATE", "TABLE", "DROP", "ALTER", "INDEX", "AND", "OR", "NOT", "NULL", "TRUE", "FALSE", "JOIN",
                "LEFT", "RIGHT", "INNER", "OUTER", "ON", "GROUP", "BY", "ORDER", "ASC", "DESC", "LIMIT", "OFFSET",
                "COUNT", "SUM", "AVG", "MAX", "MIN", "select", "from", "where", "insert", "into", "values", "update",
                "set", "delete", "create", "table", "drop", "alter", "index", "and", "or", "not", "null", "true",
                "false", "join", "left", "right", "inner", "outer", "on", "group", "by", "order", "asc", "desc",
                "limit", "offset", "count", "sum", "avg", "max", "min");
        return keywords.contains(text.trim());
    }

    private boolean isBashKeyword(String text) {
        Set<String> keywords = Set.of("if", "then", "else", "elif", "fi", "case", "esac", "for", "select", "while",
                "until", "do", "done", "in", "function", "time", "{", "}", "!", "[[", "]]", "return", "exit", "echo",
                "printf", "cd", "pwd", "ls");
        return keywords.contains(text.trim());
    }

    private void addCopyButton(javafx.scene.layout.StackPane stack, String content) {
        javafx.scene.control.Button copyBtn = new javafx.scene.control.Button("Copy");
        copyBtn.getStyleClass().add("markdown-copy-button");
        copyBtn.setVisible(false);

        // Position top-right
        javafx.scene.layout.StackPane.setAlignment(copyBtn, javafx.geometry.Pos.TOP_RIGHT);
        javafx.scene.layout.StackPane.setMargin(copyBtn, new javafx.geometry.Insets(5));

        copyBtn.setOnAction(e -> {
            javafx.scene.input.ClipboardContent clipboardContent = new javafx.scene.input.ClipboardContent();
            clipboardContent.putString(content);
            javafx.scene.input.Clipboard.getSystemClipboard().setContent(clipboardContent);
            copyBtn.setText("Copied!");
            // Revert text after 2 seconds
            new java.util.Timer().schedule(new java.util.TimerTask() {
                @Override
                public void run() {
                    javafx.application.Platform.runLater(() -> copyBtn.setText("Copy"));
                }
            }, 2000);
        });

        stack.setOnMouseEntered(e -> copyBtn.setVisible(true));
        stack.setOnMouseExited(e -> copyBtn.setVisible(false));

        stack.getChildren().add(copyBtn);
    }

    private boolean isJavaKeyword(String text) {
        Set<String> keywords = Set.of("public", "private", "protected", "class", "interface", "enum", "extends",
                "implements", "static", "final", "void", "return", "if", "else", "for", "while", "do", "switch", "case",
                "try", "catch", "finally", "throw", "throws", "new", "this", "super", "boolean", "int", "long", "float",
                "double", "byte", "short", "char", "package", "import", "null", "true", "false");
        return keywords.contains(text.trim());
    }

    private void renderLink(Link link) {
        String dest = link.getDestination();
        linkDestination = dest;
        isLink = true;
        context.renderChildren(link);
        isLink = false;
        linkDestination = null;
    }

    private void renderImage(Image image) {
        try {
            String url = image.getDestination();

            // Use background loading (true)
            javafx.scene.image.Image img = IMAGE_CACHE.computeIfAbsent(url, k -> new javafx.scene.image.Image(k, true));

            ImageView iv = new ImageView(img);
            iv.getStyleClass().add("markdown-image");
            iv.setFitWidth(200); // Default width, can be adjusted via CSS or attributes if supported
            iv.setPreserveRatio(true);

            // Placeholder / Error handling
            // Since Image is loading in background, we can check progress or error
            // But ImageView handles loading state gracefully (transparent until loaded).
            // To show a placeholder, we could use a StackPane with a ProgressIndicator
            // behind the ImageView?
            // Or better: bind to image properties.

            // Simple Error Handling:
            // If image fails to load (error property), replace with broken image icon or
            // text
            // But we need a container to swap content.
            // TextFlow/VBox container is already there.
            // Let's wrap ImageView in a StackPane to show placeholder/error.

            javafx.scene.layout.StackPane imgContainer = new javafx.scene.layout.StackPane();
            imgContainer.getStyleClass().add("markdown-image-container");
            // Limit container size to match image fit width?
            imgContainer.setMaxWidth(javafx.scene.layout.Region.USE_PREF_SIZE);

            // Loading Indicator (optional, maybe too noisy for stream)
            // javafx.scene.control.ProgressIndicator pi = new
            // javafx.scene.control.ProgressIndicator();
            // pi.setMaxSize(20, 20);
            // pi.visibleProperty().bind(img.progressProperty().lessThan(1).and(img.errorProperty().not()));

            // Error Label
            Label errorLabel = new Label("❌ Image failed: " + url);
            errorLabel.getStyleClass().add("markdown-image-error");
            errorLabel.setVisible(false);
            errorLabel.setWrapText(true);

            // Set exception handler BEFORE starting loading if possible, or use listener.
            // Image(url, true) starts loading immediately.
            // If exception occurs, errorProperty becomes true and exception is set.
            // Sometimes it happens very fast (e.g. invalid URL format).

            if (img.isError()) {
                iv.setVisible(false);
                errorLabel.setVisible(true);
            }

            img.errorProperty().addListener((obs, old, isError) -> {
                if (isError) {
                    iv.setVisible(false);
                    errorLabel.setVisible(true);
                    // Also print stack trace if exception available
                    if (img.getException() != null) {
                        // Suppress full stack trace for expected errors in demo
                        // System.err.println("Image load error: " + url);
                        // img.getException().printStackTrace();
                    }
                }
            });

            imgContainer.getChildren().addAll(errorLabel, iv); // iv on top (if visible)

            if (currentTextFlow != null) {
                // StackPane inside TextFlow acts as inline graphic?
                // TextFlow accepts any Node.
                currentTextFlow.getChildren().add(imgContainer);
            } else {
                context.getCurrentContainer().getChildren().add(imgContainer);
            }
        } catch (Exception e) {
            if (currentTextFlow != null) {
                currentTextFlow.getChildren()
                        .add(new javafx.scene.text.Text("[Image Error: " + image.getDestination() + "]"));
            }
        }
    }

    /**
     * 渲染表格. 使用 GridPane 实现，并自动处理单元格对齐和斑马纹样式。
     */
    private void renderTable(Table table) {
        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.getStyleClass().add("markdown-table");
        grid.setHgap(0); // Handled by CSS borders
        grid.setVgap(0);
        // Prevent GridPane from expanding beyond its content's preferred size
        grid.setMaxWidth(javafx.scene.layout.Region.USE_PREF_SIZE);

        context.getCurrentContainer().getChildren().add(grid);
        context.registerNode(table, grid);

        context.pushContainer(grid);
        tableRowIndex = 0;
        context.renderChildren(table);
        context.popContainer();
    }

    private void renderTableHead(TableHead tableHead) {
        context.renderChildren(tableHead);
    }

    private void renderTableBody(TableBody tableBody) {
        context.renderChildren(tableBody);
    }

    private void renderTableRow(TableRow tableRow) {
        tableColIndex = 0;
        context.renderChildren(tableRow);
        tableRowIndex++;
    }

    private void renderTableCell(TableCell tableCell) {
        Pane parent = context.getCurrentContainer();
        if (parent instanceof javafx.scene.layout.GridPane) {
            final javafx.scene.layout.GridPane grid = (javafx.scene.layout.GridPane) parent;

            TextFlow flow = new TextFlow();
            flow.getStyleClass().add("markdown-table-cell");

            // Allow cell content to wrap if needed, but in tables usually we want to expand
            // TextFlow by default doesn't have a max width unless constrained.

            if (tableCell.isHeader()) {
                flow.getStyleClass().add("markdown-table-header");
            } else {
                // Zebra striping
                if (tableRowIndex % 2 == 0) {
                    flow.getStyleClass().add("markdown-table-row-even");
                } else {
                    flow.getStyleClass().add("markdown-table-row-odd");
                }
            }

            // Handle alignment
            if (tableCell.getAlignment() != null) {
                switch (tableCell.getAlignment()) {
                    case CENTER :
                        flow.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
                        break;
                    case RIGHT :
                        flow.setTextAlignment(javafx.scene.text.TextAlignment.RIGHT);
                        break;
                    case LEFT :
                        flow.setTextAlignment(javafx.scene.text.TextAlignment.LEFT);
                        break;
                    default :
                        break;
                }
            }

            // Create a StackPane wrapper to handle background fill properly if TextFlow
            // doesn't fill cell
            // But TextFlow with proper background-color should work if it stretches.
            // Let's ensure TextFlow fills the grid cell.
            javafx.scene.layout.GridPane.setFillWidth(flow, true);
            javafx.scene.layout.GridPane.setFillHeight(flow, true);
            javafx.scene.layout.GridPane.setHgrow(flow, Priority.ALWAYS);

            // Add Column Constraints to prevent excessive compression
            // Check if constraints exist for this column index
            if (grid.getColumnConstraints().size() <= tableColIndex) {
                javafx.scene.layout.ColumnConstraints cc = new javafx.scene.layout.ColumnConstraints();
                cc.setHgrow(Priority.SOMETIMES);
                cc.setMinWidth(60); // Set a reasonable minimum width to prevent word splitting
                cc.setPrefWidth(javafx.scene.layout.Region.USE_COMPUTED_SIZE);
                grid.getColumnConstraints().add(cc);
            }

            context.pushContainer(flow); // Temporarily push flow to capture children
            currentTextFlow = flow;
            context.renderChildren(tableCell);
            currentTextFlow = null;
            context.popContainer();

            grid.add(flow, tableColIndex, tableRowIndex);
            tableColIndex++;
        }
    }

    private void applyStyle(javafx.scene.text.Text t) {
        t.getStyleClass().add("markdown-text");

        if (headingLevel > 0) {
            t.getStyleClass().add("markdown-h" + headingLevel + "-text");
        }

        if (bold) {
            t.getStyleClass().add("markdown-bold");
        }
        if (italic) {
            t.getStyleClass().add("markdown-italic");
        }
        if (strike) {
            t.getStyleClass().add("markdown-strikethrough");
        }
        if (codeVal) {
            t.getStyleClass().add("markdown-code");
        }

        if (isLink) {
            t.getStyleClass().add("markdown-link");

            final String dest = linkDestination;
            t.setOnMouseClicked(e -> {
                if (onLinkClick != null) {
                    onLinkClick.accept(dest);
                } else {
                    // Fallback or debug info
                    System.out.println("Link clicked (no handler): " + dest);
                }
            });
        }
    }
}
