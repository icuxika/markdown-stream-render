package com.icuxika.markdown.stream.render.javafx.renderer;

import static org.junit.jupiter.api.Assertions.*;

import com.icuxika.markdown.stream.render.core.ast.Heading;
import com.icuxika.markdown.stream.render.core.ast.Paragraph;
import com.icuxika.markdown.stream.render.core.ast.Text;
import com.icuxika.markdown.stream.render.javafx.BaseTest;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextFlow;
import org.junit.jupiter.api.Test;

public class CoreJavaFxNodeRendererTest extends BaseTest {

    private CoreJavaFxNodeRenderer renderer;
    private VBox rootContainer;
    private JavaFxNodeRendererContext context;

    @Test
    public void testRenderParagraph() throws InterruptedException {
        runAndWait(() -> {
            initRenderer();

            // Mock AST: Paragraph -> Text("Hello")
            Paragraph paragraph = new Paragraph();
            Text text = new Text("Hello");
            paragraph.appendChild(text);

            renderer.render(paragraph);

            // Assertions
            assertEquals(1, rootContainer.getChildren().size());
            Node node = rootContainer.getChildren().get(0);
            assertTrue(node instanceof TextFlow);
            TextFlow flow = (TextFlow) node;
            assertTrue(flow.getStyleClass().contains("markdown-paragraph"));

            assertEquals(1, flow.getChildren().size());
            javafx.scene.text.Text fxText = (javafx.scene.text.Text) flow.getChildren().get(0);
            assertEquals("Hello", fxText.getText());
        });
    }

    @Test
    public void testRenderHeading() throws InterruptedException {
        runAndWait(() -> {
            initRenderer();

            // Mock AST: Heading(level=2) -> Text("Title")
            Heading heading = new Heading(2);
            Text text = new Text("Title");
            heading.appendChild(text);

            renderer.render(heading);

            // Assertions
            assertEquals(1, rootContainer.getChildren().size());
            Node node = rootContainer.getChildren().get(0);
            assertTrue(node instanceof TextFlow);
            TextFlow flow = (TextFlow) node;
            assertTrue(flow.getStyleClass().contains("markdown-heading"));
            assertTrue(flow.getStyleClass().contains("markdown-h2"));

            javafx.scene.text.Text fxText = (javafx.scene.text.Text) flow.getChildren().get(0);
            assertEquals("Title", fxText.getText());
        });
    }

    @Test
    public void testRenderBulletList() throws InterruptedException {
        runAndWait(() -> {
            initRenderer();

            // Mock AST: BulletList -> ListItem -> Paragraph -> Text("Item 1")
            com.icuxika.markdown.stream.render.core.ast.BulletList list = new com.icuxika.markdown.stream.render.core.ast.BulletList();
            com.icuxika.markdown.stream.render.core.ast.ListItem item = new com.icuxika.markdown.stream.render.core.ast.ListItem();
            Paragraph p = new Paragraph();
            Text text = new Text("Item 1");
            p.appendChild(text);
            item.appendChild(p);
            list.appendChild(item);

            renderer.render(list);

            // Assertions
            assertEquals(1, rootContainer.getChildren().size());
            Node listNode = rootContainer.getChildren().get(0);
            assertTrue(listNode instanceof VBox);
            VBox listBox = (VBox) listNode;
            assertTrue(listBox.getStyleClass().contains("markdown-list"));

            assertEquals(1, listBox.getChildren().size());
            Node itemNode = listBox.getChildren().get(0);
            assertTrue(itemNode instanceof javafx.scene.layout.HBox);
            javafx.scene.layout.HBox itemBox = (javafx.scene.layout.HBox) itemNode;
            assertTrue(itemBox.getStyleClass().contains("markdown-list-item"));

            // Check Marker
            Node marker = itemBox.getChildren().get(0);
            assertTrue(marker instanceof javafx.scene.control.Label);
            assertTrue(marker.getStyleClass().contains("markdown-list-marker"));
        });
    }

    @Test
    public void testRenderCodeBlock() throws InterruptedException {
        runAndWait(() -> {
            initRenderer();

            // Mock AST: CodeBlock
            com.icuxika.markdown.stream.render.core.ast.CodeBlock codeBlock = new com.icuxika.markdown.stream.render.core.ast.CodeBlock(
                    "System.out.println(\"Hello\");");
            codeBlock.setInfo("java");

            renderer.render(codeBlock);

            // Assertions
            assertEquals(1, rootContainer.getChildren().size());
            Node node = rootContainer.getChildren().get(0);
            assertTrue(node instanceof javafx.scene.layout.StackPane);
            javafx.scene.layout.StackPane stack = (javafx.scene.layout.StackPane) node;
            assertTrue(stack.getStyleClass().contains("markdown-code-block-container"));

            // Inside stack -> TextFlow (for Java syntax highlighting)
            boolean foundFlow = false;
            for (Node child : stack.getChildren()) {
                if (child instanceof TextFlow) {
                    foundFlow = true;
                    TextFlow flow = (TextFlow) child;
                    assertTrue(flow.getStyleClass().contains("markdown-code-block-flow"));
                    // Check content roughly
                    assertFalse(flow.getChildren().isEmpty());
                }
            }
            assertTrue(foundFlow, "Should contain TextFlow for syntax highlighting");
        });
    }

    @Test
    public void testRenderInlineStyles() throws InterruptedException {
        runAndWait(() -> {
            initRenderer();

            // Mock AST: Paragraph -> StrongEmphasis -> Text("Bold")
            Paragraph p = new Paragraph();
            com.icuxika.markdown.stream.render.core.ast.StrongEmphasis strong = new com.icuxika.markdown.stream.render.core.ast.StrongEmphasis();
            Text text = new Text("Bold");
            strong.appendChild(text);
            p.appendChild(strong);

            renderer.render(p);

            // Assertions
            TextFlow flow = (TextFlow) rootContainer.getChildren().get(0);
            javafx.scene.text.Text fxText = (javafx.scene.text.Text) flow.getChildren().get(0);
            assertEquals("Bold", fxText.getText());
            assertTrue(fxText.getStyleClass().contains("markdown-bold"));
        });
    }

    @Test
    public void testRenderBlockQuote() throws InterruptedException {
        runAndWait(() -> {
            initRenderer();

            // Mock AST: BlockQuote -> Paragraph -> Text("Quote")
            com.icuxika.markdown.stream.render.core.ast.BlockQuote quote = new com.icuxika.markdown.stream.render.core.ast.BlockQuote();
            Paragraph p = new Paragraph();
            Text text = new Text("Quote");
            p.appendChild(text);
            quote.appendChild(p);

            renderer.render(quote);

            // Assertions
            assertEquals(1, rootContainer.getChildren().size());
            Node node = rootContainer.getChildren().get(0);
            assertTrue(node instanceof VBox);
            VBox quoteBox = (VBox) node;
            assertTrue(quoteBox.getStyleClass().contains("markdown-quote"));

            // Content inside
            Node content = quoteBox.getChildren().get(0);
            assertTrue(content instanceof TextFlow); // Paragraph inside quote
        });
    }

    @Test
    public void testRenderTable() throws InterruptedException {
        runAndWait(() -> {
            initRenderer();

            // Mock AST: Table -> TableHead -> TableRow -> TableCell -> Text("Header")
            com.icuxika.markdown.stream.render.core.ast.Table table = new com.icuxika.markdown.stream.render.core.ast.Table();
            com.icuxika.markdown.stream.render.core.ast.TableHead head = new com.icuxika.markdown.stream.render.core.ast.TableHead();
            com.icuxika.markdown.stream.render.core.ast.TableRow row = new com.icuxika.markdown.stream.render.core.ast.TableRow();
            com.icuxika.markdown.stream.render.core.ast.TableCell cell = new com.icuxika.markdown.stream.render.core.ast.TableCell();
            cell.setHeader(true);
            Text text = new Text("Header");
            cell.appendChild(text);
            row.appendChild(cell);
            head.appendChild(row);
            table.appendChild(head);

            renderer.render(table);

            // Assertions
            assertEquals(1, rootContainer.getChildren().size());
            Node node = rootContainer.getChildren().get(0);
            assertTrue(node instanceof javafx.scene.layout.GridPane);
            javafx.scene.layout.GridPane grid = (javafx.scene.layout.GridPane) node;
            assertTrue(grid.getStyleClass().contains("markdown-table"));

            // Check cell content
            assertFalse(grid.getChildren().isEmpty());
            Node cellNode = grid.getChildren().get(0);
            assertTrue(cellNode instanceof TextFlow);
            assertTrue(cellNode.getStyleClass().contains("markdown-table-cell"));
            assertTrue(cellNode.getStyleClass().contains("markdown-table-header"));
        });
    }

    private void initRenderer() {
        rootContainer = new VBox();
        context = new JavaFxNodeRendererContext() {
            private final java.util.Stack<javafx.scene.layout.Pane> containers = new java.util.Stack<>();

            {
                containers.push(rootContainer);
            }

            @Override
            public javafx.scene.layout.Pane getCurrentContainer() {
                return containers.peek();
            }

            @Override
            public void pushContainer(javafx.scene.layout.Pane container) {
                containers.push(container);
            }

            @Override
            public javafx.scene.layout.Pane popContainer() {
                return containers.pop();
            }

            @Override
            public void registerNode(com.icuxika.markdown.stream.render.core.ast.Node astNode, Node fxNode) {
                // No-op for test
            }

            @Override
            public VBox getRoot() {
                return rootContainer;
            }

            @Override
            public void renderChildren(com.icuxika.markdown.stream.render.core.ast.Node parent) {
                com.icuxika.markdown.stream.render.core.ast.Node child = parent.getFirstChild();
                while (child != null) {
                    renderer.render(child);
                    child = child.getNext();
                }
            }
        };
        renderer = new CoreJavaFxNodeRenderer(context, link -> {
        });
    }

    private void runAndWait(Runnable action) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                action.run();
            } finally {
                latch.countDown();
            }
        });
        assertTrue(latch.await(5, TimeUnit.SECONDS), "JavaFX thread timed out");
    }
}
