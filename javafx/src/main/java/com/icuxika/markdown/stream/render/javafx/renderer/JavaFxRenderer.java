package com.icuxika.markdown.stream.render.javafx.renderer;

import com.icuxika.markdown.stream.render.core.ast.*;
import com.icuxika.markdown.stream.render.core.renderer.IMarkdownRenderer;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextFlow;

import java.util.Stack;

public class JavaFxRenderer implements IMarkdownRenderer {
    private final VBox root = new VBox();
    private final Stack<Pane> blockStack = new Stack<>();
    
    // For inlines
    private TextFlow currentTextFlow;
    
    // Style state
    private boolean bold = false;
    private boolean italic = false;
    private boolean code = false; // Monospace
    
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

    public JavaFxRenderer() {
        blockStack.push(root);
        root.setSpacing(10);
        root.getStyleClass().add("markdown-root");
    }

    @Override
    public Object getResult() {
        return root;
    }

    @Override
    public void visit(Document document) {
        visitChildren(document);
    }

    @Override
    public void visit(HtmlBlock htmlBlock) {
        javafx.scene.text.Text text = new javafx.scene.text.Text(htmlBlock.getLiteral());
        text.getStyleClass().add("markdown-text");
        text.getStyleClass().add("markdown-html-block");
        blockStack.peek().getChildren().add(text);
    }

    @Override
    public void visit(HtmlInline htmlInline) {
        if (currentTextFlow != null) {
            javafx.scene.text.Text text = new javafx.scene.text.Text(htmlInline.getLiteral());
            text.getStyleClass().add("markdown-text");
            text.getStyleClass().add("markdown-html-inline");
            currentTextFlow.getChildren().add(text);
        }
    }

    @Override
    public void visit(Paragraph paragraph) {
        TextFlow flow = new TextFlow();
        flow.getStyleClass().add("markdown-paragraph");
        blockStack.peek().getChildren().add(flow);
        currentTextFlow = flow;
        visitChildren(paragraph);
        currentTextFlow = null;
    }

    @Override
    public void visit(Heading heading) {
        TextFlow flow = new TextFlow();
        flow.getStyleClass().add("markdown-heading");
        flow.getStyleClass().add("markdown-h" + heading.getLevel());
        blockStack.peek().getChildren().add(flow);
        currentTextFlow = flow;
        
        int oldLevel = headingLevel;
        headingLevel = heading.getLevel();
        visitChildren(heading);
        headingLevel = oldLevel;
        
        currentTextFlow = null;
    }
    
    private int headingLevel = 0;

    @Override
    public void visit(Text text) {
        javafx.scene.text.Text t = new javafx.scene.text.Text(text.getLiteral());
        applyStyle(t);
        if (currentTextFlow != null) {
            currentTextFlow.getChildren().add(t);
        } else {
            blockStack.peek().getChildren().add(t);
        }
    }

    @Override
    public void visit(SoftBreak softBreak) {
        if (currentTextFlow != null) {
            currentTextFlow.getChildren().add(new javafx.scene.text.Text("\n"));
        }
    }

    @Override
    public void visit(HardBreak hardBreak) {
        if (currentTextFlow != null) {
            currentTextFlow.getChildren().add(new javafx.scene.text.Text("\n"));
        }
    }

    @Override
    public void visit(Emphasis emphasis) {
        boolean old = italic;
        italic = true;
        visitChildren(emphasis);
        italic = old;
    }

    @Override
    public void visit(StrongEmphasis strongEmphasis) {
        boolean old = bold;
        bold = true;
        visitChildren(strongEmphasis);
        bold = old;
    }

    @Override
    public void visit(BlockQuote blockQuote) {
        VBox quoteBox = new VBox();
        quoteBox.getStyleClass().add("markdown-quote");
        blockStack.peek().getChildren().add(quoteBox);
        blockStack.push(quoteBox);
        visitChildren(blockQuote);
        blockStack.pop();
    }

    @Override
    public void visit(BulletList bulletList) {
        VBox listBox = new VBox();
        listBox.getStyleClass().add("markdown-list");
        listBox.setSpacing(5);
        blockStack.peek().getChildren().add(listBox);
        blockStack.push(listBox);
        listStack.push(new ListState(false, 0, '.'));
        visitChildren(bulletList);
        listStack.pop();
        blockStack.pop();
    }

    @Override
    public void visit(OrderedList orderedList) {
        VBox listBox = new VBox();
        listBox.getStyleClass().add("markdown-list");
        listBox.setSpacing(5);
        blockStack.peek().getChildren().add(listBox);
        blockStack.push(listBox);
        listStack.push(new ListState(true, orderedList.getStartNumber(), orderedList.getDelimiter()));
        visitChildren(orderedList);
        listStack.pop();
        blockStack.pop();
    }

    @Override
    public void visit(ListItem listItem) {
        javafx.scene.layout.HBox itemBox = new javafx.scene.layout.HBox();
        itemBox.setSpacing(5);
        itemBox.getStyleClass().add("markdown-list-item");
        
        String markerText = "\u2022";
        if (!listStack.isEmpty()) {
            ListState state = listStack.peek();
            if (state.isOrdered) {
                markerText = state.index + String.valueOf(state.delimiter);
                state.index++;
            }
        }
        
        Label marker = new Label(markerText);
        marker.getStyleClass().add("markdown-list-marker");
        marker.setMinWidth(20); // Fixed width for alignment
        marker.setAlignment(javafx.geometry.Pos.TOP_RIGHT);
        
        VBox contentBox = new VBox();
        contentBox.getStyleClass().add("markdown-list-content");
        itemBox.getChildren().addAll(marker, contentBox);
        
        blockStack.peek().getChildren().add(itemBox);
        blockStack.push(contentBox);
        visitChildren(listItem);
        blockStack.pop();
    }

    @Override
    public void visit(Code code) {
        boolean old = codeVal;
        codeVal = true;
        javafx.scene.text.Text t = new javafx.scene.text.Text(code.getLiteral());
        applyStyle(t);
        if (currentTextFlow != null) {
            currentTextFlow.getChildren().add(t);
        }
        codeVal = old;
    }
    
    private boolean codeVal = false;

    @Override
    public void visit(ThematicBreak thematicBreak) {
        javafx.scene.control.Separator sep = new javafx.scene.control.Separator();
        sep.getStyleClass().add("markdown-separator");
        blockStack.peek().getChildren().add(sep);
    }

    @Override
    public void visit(CodeBlock codeBlock) {
        Label l = new Label(codeBlock.getLiteral());
        l.getStyleClass().add("markdown-code-block");
        blockStack.peek().getChildren().add(l);
    }

    @Override
    public void visit(Link link) {
        String dest = link.getDestination();
        linkDestination = dest;
        isLink = true;
        visitChildren(link);
        isLink = false;
        linkDestination = null;
    }
    
    private boolean isLink = false;
    private String linkDestination = null;

    // Image cache to prevent reloading and flickering
    private static final java.util.Map<String, javafx.scene.image.Image> IMAGE_CACHE = new java.util.concurrent.ConcurrentHashMap<>();

    @Override
    public void visit(Image image) {
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
                blockStack.peek().getChildren().add(iv);
            }
        } catch (Exception e) {
            if (currentTextFlow != null) {
                currentTextFlow.getChildren().add(new javafx.scene.text.Text("[Image: " + image.getDestination() + "]"));
            }
        }
    }

    @Override
    public void visit(Table table) {
        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.getStyleClass().add("markdown-table");
        grid.setHgap(10);
        grid.setVgap(5);
        
        blockStack.peek().getChildren().add(grid);
        blockStack.push(grid);
        tableRowIndex = 0;
        visitChildren(table);
        blockStack.pop();
    }
    
    private int tableRowIndex = 0;
    private int tableColIndex = 0;

    @Override
    public void visit(TableHead tableHead) {
        visitChildren(tableHead);
    }

    @Override
    public void visit(TableBody tableBody) {
        visitChildren(tableBody);
    }

    @Override
    public void visit(TableRow tableRow) {
        tableColIndex = 0;
        visitChildren(tableRow);
        tableRowIndex++;
    }

    @Override
    public void visit(TableCell tableCell) {
        Pane parent = blockStack.peek();
        if (parent instanceof javafx.scene.layout.GridPane) {
            javafx.scene.layout.GridPane grid = (javafx.scene.layout.GridPane) parent;
            
            TextFlow flow = new TextFlow();
            flow.getStyleClass().add("markdown-table-cell");
            if (tableCell.isHeader()) {
                flow.getStyleClass().add("markdown-table-header");
            }
            
            // Handle alignment
            if (tableCell.getAlignment() != null) {
                switch (tableCell.getAlignment()) {
                    case CENTER: flow.setTextAlignment(javafx.scene.text.TextAlignment.CENTER); break;
                    case RIGHT: flow.setTextAlignment(javafx.scene.text.TextAlignment.RIGHT); break;
                    case LEFT: flow.setTextAlignment(javafx.scene.text.TextAlignment.LEFT); break;
                }
            }
            
            blockStack.push(flow); // Temporarily push flow to capture children
            currentTextFlow = flow;
            visitChildren(tableCell);
            currentTextFlow = null;
            blockStack.pop();
            
            grid.add(flow, tableColIndex, tableRowIndex);
            tableColIndex++;
        }
    }

    private void visitChildren(Node parent) {
        Node child = parent.getFirstChild();
        while (child != null) {
            child.accept(this);
            child = child.getNext();
        }
    }
    
    private void applyStyle(javafx.scene.text.Text t) {
        t.getStyleClass().add("markdown-text");
        
        if (headingLevel > 0) {
            t.getStyleClass().add("markdown-h" + headingLevel + "-text");
        }
        
        if (bold) {
            t.getStyleClass().add("markdown-bold");
            t.setStyle(t.getStyle() + "-fx-font-weight: bold;"); // Fallback if css not enough for text node inside TextFlow?
            // Actually, for Text nodes in TextFlow, style class works if defined properly.
            // But -fx-font-weight works on Text.
        }
        if (italic) {
            t.getStyleClass().add("markdown-italic");
            t.setStyle(t.getStyle() + "-fx-font-style: italic;");
        }
        if (codeVal) {
            t.getStyleClass().add("markdown-code");
        }
        
        if (isLink) {
            t.getStyleClass().add("markdown-link");
            t.setOnMouseClicked(e -> {
                System.out.println("Link clicked: " + linkDestination);
            });
        }
    }
}
