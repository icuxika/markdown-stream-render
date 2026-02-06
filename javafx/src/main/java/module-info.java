module com.icuxika.markdown.stream.render.javafx {
    requires transitive com.icuxika.markdown.stream.render.core;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;

    exports com.icuxika.markdown.stream.render.javafx;
    exports com.icuxika.markdown.stream.render.javafx.renderer;
    exports com.icuxika.markdown.stream.render.javafx.extension.admonition;
    exports com.icuxika.markdown.stream.render.javafx.extension.math;

    // Open CSS resources to other modules (e.g. demo)
    opens com.icuxika.markdown.stream.render.javafx.css;
    opens com.icuxika.markdown.stream.render.javafx.css.extensions;
}