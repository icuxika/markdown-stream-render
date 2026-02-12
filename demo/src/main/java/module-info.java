module com.icuxika.markdown.stream.render.demo {
	requires com.icuxika.markdown.stream.render.core;
	requires com.icuxika.markdown.stream.render.html;
	requires com.icuxika.markdown.stream.render.javafx;

	requires java.desktop;

	requires javafx.controls;
	requires javafx.graphics;
	requires javafx.swing;
	requires jfx.incubator.richtext;
	requires jlatexmath;

	requires java.datatransfer;
	requires java.net.http;
	requires jdk.httpserver;

	exports com.icuxika.markdown.stream.render.demo to javafx.graphics;
	exports com.icuxika.markdown.stream.render.demo.javafx to javafx.graphics;
	exports com.icuxika.markdown.stream.render.demo.javafx.modernchat to javafx.graphics;
	exports com.icuxika.markdown.stream.render.demo.javafx.chirpchat to javafx.graphics;
}