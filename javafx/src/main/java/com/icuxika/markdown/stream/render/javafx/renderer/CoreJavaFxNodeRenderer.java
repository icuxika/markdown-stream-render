package com.icuxika.markdown.stream.render.javafx.renderer;

import com.icuxika.markdown.stream.render.core.ast.*;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextFlow;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import java.util.function.Consumer;

/**
 * 核心 JavaFX 节点渲染器。
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
     * 渲染段落。
     * 段落通常作为 TextFlow 容器，用于包含行内文本和元素。
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
     * 渲染标题。
     * 根据标题级别应用不同的样式类（markdown-h1 ~ markdown-h6）。
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
     * 渲染纯文本。
     * 根据当前状态应用样式（粗体、斜体、删除线、代码、链接等）。
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
        Label l = new Label(codeBlock.getLiteral());
        l.getStyleClass().add("markdown-code-block");
        context.getCurrentContainer().getChildren().add(l);
        context.registerNode(codeBlock, l);
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
            javafx.scene.image.Image img = IMAGE_CACHE.computeIfAbsent(url, k -> new javafx.scene.image.Image(k, true));

            ImageView iv = new ImageView(img);
            iv.getStyleClass().add("markdown-image");
            iv.setFitWidth(200);
            iv.setPreserveRatio(true);

            if (currentTextFlow != null) {
                currentTextFlow.getChildren().add(iv);
            } else {
                context.getCurrentContainer().getChildren().add(iv);
            }
        } catch (Exception e) {
            if (currentTextFlow != null) {
                currentTextFlow.getChildren().add(new javafx.scene.text.Text("[Image: " + image.getDestination() + "]"));
            }
        }
    }

    /**
     * 渲染表格。
     * 使用 GridPane 实现，并自动处理单元格对齐和斑马纹样式。
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
            javafx.scene.layout.GridPane grid = (javafx.scene.layout.GridPane) parent;

            TextFlow flow = new TextFlow();
            flow.getStyleClass().add("markdown-table-cell");
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
                    case CENTER:
                        flow.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
                        break;
                    case RIGHT:
                        flow.setTextAlignment(javafx.scene.text.TextAlignment.RIGHT);
                        break;
                    case LEFT:
                        flow.setTextAlignment(javafx.scene.text.TextAlignment.LEFT);
                        break;
                }
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
