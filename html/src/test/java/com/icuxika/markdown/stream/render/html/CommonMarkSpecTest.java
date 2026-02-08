package com.icuxika.markdown.stream.render.html;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.icuxika.markdown.stream.render.core.ast.Node;
import com.icuxika.markdown.stream.render.core.parser.MarkdownParser;
import com.icuxika.markdown.stream.render.core.parser.MarkdownParserOptions;
import com.icuxika.markdown.stream.render.html.renderer.HtmlRenderer;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

/**
 * CommonMark 规范兼容性测试 (CommonMark Spec Conformance Tests)
 * <p>
 * 该类负责运行官方 CommonMark Spec (v0.31.2) 中的所有测试用例，确保解析器的正确性。 它包含两部分功能： 1.
 * {@link #commonMarkSpecTests()} - 生成 JUnit
 * DynamicTests，用于 IDE 中的单独测试运行。 2. {@link #generateSpecReport()} - 运行所有测试并生成
 * Markdown 格式的详细报告 (SPEC_REPORT.md)。
 */
public class CommonMarkSpecTest {

	static class SpecExample {
		public String markdown;
		public String html;
		public int example;
		public int start_line;
		public int end_line;
		public String section;
	}

	static class TestResult {
		int example;
		String section;
		boolean passed;
		String message;
	}

	/**
	 * 辅助方法：打印 AST 结构 (用于调试失败的测试用例)
	 */
	private void printAst(Node node, String indent) {
		System.out.println(indent + node.getClass().getSimpleName());
		Node child = node.getFirstChild();
		while (child != null) {
			printAst(child, indent + "  ");
			child = child.getNext();
		}
	}

	private List<SpecExample> loadExamples() throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		java.io.InputStream specStream = getClass().getResourceAsStream("/commonmark-spec-0.31.2.json");

		if (specStream == null) {
			throw new RuntimeException("Could not find commonmark-spec-0.31.2.json in classpath");
		}

		return mapper.readValue(specStream, new TypeReference<List<SpecExample>>() {
		});
	}

	/**
	 * 工厂方法：为每个规范示例生成一个动态测试 这使得 IDE 可以单独显示每个测试用例的通过/失败状态。
	 */
	@TestFactory
	Stream<DynamicTest> commonMarkSpecTests() throws IOException {
		List<SpecExample> examples = loadExamples();

		return examples.stream()
				// .filter(example -> "Images".equals(example.section)) // 可以取消注释以仅运行特定部分的测试
				.map(example -> DynamicTest.dynamicTest("Example " + example.example + " (" + example.section + ")",
						() -> {
							MarkdownParser parser = new MarkdownParser();
							parser.getOptions().setGfm(false); // Disable GFM extensions for strict spec tests

							MarkdownParserOptions renderOptions = new MarkdownParserOptions();
							renderOptions.setGfm(false);
							HtmlRenderer renderer = HtmlRenderer.builder().options(renderOptions).build();

							parser.parse(new java.io.StringReader(example.markdown), renderer);
							String actual = (String) renderer.getResult();

							// 规范化换行符以进行比较
							String expected = example.html.replace("\r\n", "\n");
							actual = actual.replace("\r\n", "\n");

							if (!expected.equals(actual)) {
								System.out.println("Failed Example " + example.example);
								System.out.println("Markdown: [" + example.markdown + "]");
								System.out.println("Expected: [" + expected + "]");
								System.out.println("Actual:   [" + actual + "]");
								printAst(parser.parse(example.markdown), "");
							}

							assertEquals(expected, actual);
						}));
	}

	/**
	 * 生成完整测试报告 运行所有测试，并将结果汇总写入项目根目录下的 SPEC_REPORT.md
	 */
	@Test
	public void generateSpecReport() throws IOException {
		Assumptions.assumeTrue(Boolean.getBoolean("generateSpecReport"));
		List<SpecExample> examples = loadExamples();
		examples.sort(Comparator.comparingInt(e -> e.example));

		List<TestResult> results = new ArrayList<>();
		int passed = 0;
		int failed = 0;

		for (SpecExample example : examples) {
			TestResult result = new TestResult();
			result.example = example.example;
			result.section = example.section;

			try {
				MarkdownParser parser = new MarkdownParser();
				parser.getOptions().setGfm(false); // Disable GFM extensions for strict spec tests

				MarkdownParserOptions renderOptions = new MarkdownParserOptions();
				renderOptions.setGfm(false);
				HtmlRenderer renderer = HtmlRenderer.builder().options(renderOptions).build();

				parser.parse(new java.io.StringReader(example.markdown), renderer);
				String actual = (String) renderer.getResult();

				String expected = example.html.replace("\r\n", "\n");
				actual = actual.replace("\r\n", "\n");

				if (expected.equals(actual)) {
					result.passed = true;
					passed++;
				} else {
					result.passed = false;
					result.message = "Expected length: " + expected.length() + ", Actual length: " + actual.length();
					failed++;
				}
			} catch (Exception e) {
				result.passed = false;
				result.message = "Exception: " + e.getMessage();
				failed++;
			}
			results.add(result);
		}

		File reportDir = new File("target/spec-reports");
		if (!reportDir.exists()) {
			reportDir.mkdirs();
		}
		File reportFile = new File(reportDir, "SPEC_REPORT.md");

		try (PrintWriter writer = new PrintWriter(new FileWriter(reportFile))) {
			writer.println("# CommonMark Spec Test Report");
			writer.println();
			writer.println("Total: " + examples.size() + ", Passed: " + passed + ", Failed: " + failed);
			writer.println();
			writer.println("| Example | Section | Status | Info |");
			writer.println("| :--- | :--- | :--- | :--- |");

			for (TestResult res : results) {
				writer.println("| " + res.example + " | " + res.section + " | " + (res.passed ? "✅ PASS" : "❌ FAIL")
						+ " | " + (res.message != null ? res.message : "") + " |");
			}
		}

		System.out.println("Report generated at " + reportFile.getAbsolutePath());
	}
}
