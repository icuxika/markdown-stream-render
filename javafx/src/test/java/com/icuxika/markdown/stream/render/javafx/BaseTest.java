package com.icuxika.markdown.stream.render.javafx;

import javafx.application.Platform;
import javafx.scene.control.Button;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;


public class BaseTest {

    @BeforeAll
    public static void setup() {
        try {
            Platform.startup(() -> {
            });
        } catch (IllegalStateException e) {
            // Toolkit already initialized, ignore
        }
    }

    @Test
    public void buttonGraphicTest() {
        Button button = new Button();
        Assertions.assertNull(button.getGraphic());
    }
}
