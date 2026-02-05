module com.icuxika.markdown.stream.render.demo {
    requires com.icuxika.markdown.stream.render.core;
    requires com.icuxika.markdown.stream.render.html;
    requires com.icuxika.markdown.stream.render.javafx;

    requires java.desktop;

    requires javafx.controls;
    requires javafx.graphics;
    requires jfx.incubator.richtext;

    requires java.datatransfer;
    requires java.net.http;
    requires jdk.httpserver;

    exports com.icuxika.markdown.stream.render.demo to javafx.graphics;
}