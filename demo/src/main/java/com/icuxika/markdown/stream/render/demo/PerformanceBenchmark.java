package com.icuxika.markdown.stream.render.demo;

import com.icuxika.markdown.stream.render.core.ast.Node;
import com.icuxika.markdown.stream.render.core.ast.Paragraph;
import com.icuxika.markdown.stream.render.core.ast.Text;
import com.icuxika.markdown.stream.render.javafx.renderer.JavaFxRenderer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class PerformanceBenchmark extends Application {

    private static final int ITEM_COUNT = 5000;

    @Override
    public void start(Stage primaryStage) {
        // Prevent implicit exit when closing stages
        Platform.setImplicitExit(false);

        // We will run two tests sequentially
        new Thread(() -> {
            try {
                runVBoxTest();
                Thread.sleep(2000); // Give GC time to run and UI to settle
                runVirtualTest();
                Platform.exit();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void runVBoxTest() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            System.out.println("=== VBox Benchmark (Traditional) ===");
            long start = System.currentTimeMillis();

            VBox root = new VBox();
            JavaFxRenderer renderer = new JavaFxRenderer(); // Use one renderer for config, but render individual nodes?
            // In traditional mode, we render the whole document or append nodes to VBox.
            // Let's simulate append.

            for (int i = 0; i < ITEM_COUNT; i++) {
                Paragraph p = new Paragraph();
                p.appendChild(new Text("This is paragraph #" + i + " in a VBox. It consumes memory and layout time."));
                // Render node to JavaFX Node
                renderer.render(p);
                // In traditional usage, renderer accumulates result in its own root VBox.
            }

            VBox output = renderer.getRoot();
            Scene scene = new Scene(output, 800, 600);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.show();

            long end = System.currentTimeMillis();
            System.out.println("Build Time: " + (end - start) + " ms");

            // Count nodes
            int nodeCount = countNodes(output);
            System.out.println("Scene Graph Nodes: " + nodeCount);

            measureMemory("VBox");

            stage.close();
            latch.countDown();
        });
        latch.await();
    }

    private void runVirtualTest() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            System.out.println("\n=== ListView Benchmark (Virtualization) ===");
            long start = System.currentTimeMillis();

            ListView<Node> listView = new ListView<>();
            List<Node> items = new ArrayList<>();

            for (int i = 0; i < ITEM_COUNT; i++) {
                Paragraph p = new Paragraph();
                p.appendChild(new Text("This is paragraph #" + i + " in a ListView. It is virtualized."));
                items.add(p);
            }

            listView.getItems().addAll(items);

            Scene scene = new Scene(listView, 800, 600);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.show();

            // Force layout pass
            listView.layout();
            listView.applyCss();

            long end = System.currentTimeMillis();
            System.out.println("Build Time: " + (end - start) + " ms");

            // Count nodes (only visible ones exist in Scene Graph)
            int nodeCount = countNodes(listView);
            System.out.println("Scene Graph Nodes: " + nodeCount + " (Only visible items)");

            measureMemory("ListView");

            stage.close();
            latch.countDown();
        });
        latch.await();
    }

    private int countNodes(javafx.scene.Parent parent) {
        int count = 1; // self
        for (javafx.scene.Node child : parent.getChildrenUnmodifiable()) {
            if (child instanceof javafx.scene.Parent) {
                count += countNodes((javafx.scene.Parent) child);
            } else {
                count++;
            }
        }
        return count;
    }

    private void measureMemory(String label) {
        System.gc();
        try {
            Thread.sleep(100);
        } catch (Exception e) {
        }
        Runtime rt = Runtime.getRuntime();
        long used = (rt.totalMemory() - rt.freeMemory()) / 1024 / 1024;
        System.out.println("Approx. Memory Used (" + label + "): " + used + " MB");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
