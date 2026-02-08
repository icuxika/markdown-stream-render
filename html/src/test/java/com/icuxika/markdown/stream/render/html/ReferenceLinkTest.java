package com.icuxika.markdown.stream.render.html;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.icuxika.markdown.stream.render.core.ast.Document;
import com.icuxika.markdown.stream.render.core.ast.LinkReference;
import com.icuxika.markdown.stream.render.core.parser.MarkdownParser;
import com.icuxika.markdown.stream.render.html.renderer.HtmlRenderer;
import java.io.StringReader;
import org.junit.jupiter.api.Test;

/**
 * 引用链接测试类 (Reference Link Tests) 专门测试 Markdown 的引用式链接定义 ([id]: url "title")
 * 及其解析。
 */
public class ReferenceLinkTest {

	/**
	 * 测试基本的引用链接解析与渲染
	 */
	@Test
	public void testReferenceParsing() throws java.io.IOException {
		String input = "[foo]: /url \"title\"\n\n[foo]";
		MarkdownParser parser = new MarkdownParser();
		HtmlRenderer renderer = new HtmlRenderer();
		parser.parse(new StringReader(input), renderer);

		String result = (String) renderer.getResult();
		System.out.println("Result: " + result);

		assertEquals("<p><a href=\"/url\" title=\"title\">foo</a></p>\n", result);
	}

	/**
	 * 测试 AST 中是否正确提取了链接引用定义
	 */
	@Test
	public void testReferenceMap() {
		String input = "[foo]: /url \"title\"";
		MarkdownParser parser = new MarkdownParser();
		Document doc = parser.parse(input);

		LinkReference ref = doc.getLinkReference("foo");
		assertNotNull(ref, "Reference 'foo' should be parsed");
		assertEquals("/url", ref.getDestination());
		assertEquals("title", ref.getTitle());
	}

	/**
	 * 测试引用式图片
	 */
	@Test
	public void testImageReference() throws java.io.IOException {
		String input = "![foo][bar]\n\n[bar]: /image.jpg";
		MarkdownParser parser = new MarkdownParser();
		HtmlRenderer renderer = new HtmlRenderer();
		parser.parse(new StringReader(input), renderer);

		String result = (String) renderer.getResult();
		System.out.println("Result: " + result);

		assertEquals("<p><img src=\"/image.jpg\" alt=\"foo\" /></p>\n", result);
	}

	/**
	 * 来自 DebugReferenceLinkTest: 测试紧凑的引用链接定义（无空行）
	 */
	@Test
	public void testReferenceLinkNoBlankLine() {
		String input = "[foo]: /url \"title\"\n[foo]";
		MarkdownParser parser = new MarkdownParser();
		Document doc = parser.parse(input);
		HtmlRenderer renderer = new HtmlRenderer();
		doc.accept(renderer);
		String output = (String) renderer.getResult();

		System.out.println("Output NoBlank: " + output);
		assertEquals("<p><a href=\"/url\" title=\"title\">foo</a></p>\n", output);
	}
}
