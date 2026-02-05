module com.icuxika.markdown.stream.render.javafx {
    requires com.icuxika.markdown.stream.render.core;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;

    exports com.icuxika.markdown.stream.render.javafx;
    exports com.icuxika.markdown.stream.render.javafx.renderer;
}