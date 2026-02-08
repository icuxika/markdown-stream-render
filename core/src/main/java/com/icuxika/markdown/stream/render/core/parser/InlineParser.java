package com.icuxika.markdown.stream.render.core.parser;

import com.icuxika.markdown.stream.render.core.ast.Code;
import com.icuxika.markdown.stream.render.core.ast.Emphasis;
import com.icuxika.markdown.stream.render.core.ast.HardBreak;
import com.icuxika.markdown.stream.render.core.ast.HtmlInline;
import com.icuxika.markdown.stream.render.core.ast.Image;
import com.icuxika.markdown.stream.render.core.ast.Link;
import com.icuxika.markdown.stream.render.core.ast.LinkReference;
import com.icuxika.markdown.stream.render.core.ast.Node;
import com.icuxika.markdown.stream.render.core.ast.SoftBreak;
import com.icuxika.markdown.stream.render.core.ast.Strikethrough;
import com.icuxika.markdown.stream.render.core.ast.StrongEmphasis;
import com.icuxika.markdown.stream.render.core.ast.Text;
import com.icuxika.markdown.stream.render.core.parser.inline.InlineContentParser;
import com.icuxika.markdown.stream.render.core.parser.inline.InlineContentParserFactory;
import com.icuxika.markdown.stream.render.core.parser.inline.InlineParserState;
import com.icuxika.markdown.stream.render.core.parser.inline.ParsedInline;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Inline Parser.
 */
@SuppressWarnings({ "checkstyle:NeedBraces", "checkstyle:AvoidEscapedUnicodeCharacters", "checkstyle:IllegalTokenText",
		"checkstyle:MemberName", "checkstyle:MissingJavadocMethod" })
public class InlineParser implements InlineParserState {

	private static final Pattern HTML_TAG = Pattern.compile(
			"^<([A-Za-z][A-Za-z0-9-]*)((?:\\s+[A-Za-z_:][A-Za-z0-9_.:-]*(?:\\s*=\\s*(?:[^\"'=<>`\\s]+|'[^']*'|\"[^\"]*\"))?)*)\\s*/?>");
	private static final Pattern HTML_CLOSE_TAG = Pattern.compile("^</([A-Za-z][A-Za-z0-9-]*)\\s*>");
	private static final Pattern HTML_COMMENT = Pattern.compile("^<!--((?!-->).)*-->", Pattern.DOTALL);
	private static final Pattern HTML_PI = Pattern.compile("^<\\?.*?\\?>", Pattern.DOTALL);
	private static final Pattern HTML_DECLARATION = Pattern.compile("^<![A-Z].*?>", Pattern.DOTALL);
	private static final Pattern HTML_CDATA = Pattern.compile("^<!\\[CDATA\\[.*?\\]\\]>", Pattern.DOTALL);

	// Additional pattern for <!--> which seems to be valid in CommonMark tests
	private static final Pattern HTML_COMMENT_SHORT = Pattern.compile("^<!-->");
	private static final Pattern HTML_COMMENT_SHORT_2 = Pattern.compile("^<!--->");

	private static final Pattern ENTITY = Pattern
			.compile("^&(?:([a-zA-Z0-9]+)|#([0-9]{1,7})|#(?i:x)([0-9a-fA-F]{1,6}));");

	// Autolinks
	private static final Pattern AUTOLINK_URI = Pattern.compile("^<[a-zA-Z][a-zA-Z0-9.+-]{1,31}:[^<>\u0000-\u0020]*>");
	private static final Pattern AUTOLINK_EMAIL = Pattern.compile(
			"^<[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*>");

	private final String text;
	private final Map<String, LinkReference> references;
	private final MarkdownParserOptions options;
	private int index = 0;
	private final List<Node> nodes = new ArrayList<>();

	private final List<InlineContentParserFactory> inlineParserFactories;
	private final Map<Character, List<InlineContentParser>> customParsers;

	// Emphasis delimiter stack
	private Delimiter lastDelimiter = null;

	public InlineParser(String text, MarkdownParserOptions options) {
		this(text, Collections.emptyMap(), options, Collections.emptyList());
	}

	public InlineParser(String text, Map<String, LinkReference> references, MarkdownParserOptions options) {
		this(text, references, options, Collections.emptyList());
	}

	public InlineParser(String text, Map<String, LinkReference> references, MarkdownParserOptions options,
			List<InlineContentParserFactory> inlineParserFactories) {
		this.text = text;
		this.references = references;
		this.options = options != null ? options : new MarkdownParserOptions();
		this.inlineParserFactories = inlineParserFactories != null ? inlineParserFactories : Collections.emptyList();

		// Initialize custom parsers map
		this.customParsers = new HashMap<>();
		for (InlineContentParserFactory factory : this.inlineParserFactories) {
			InlineContentParser parser = factory.create();
			for (Character c : factory.getTriggerCharacters()) {
				customParsers.computeIfAbsent(c, k -> new ArrayList<>()).add(parser);
			}
		}
	}

	/**
	 * Parse content.
	 *
	 * @return list of nodes
	 */
	public List<Node> parse() {
		while (index < text.length()) {
			char c = text.charAt(index);

			// GFM Extended Autolinks
			if (handleExtendedAutolink())
				continue;

			// Check custom parsers first
			List<InlineContentParser> parsers = customParsers.get(c);
			boolean handled = false;
			if (parsers != null) {
				for (InlineContentParser parser : parsers) {
					ParsedInline parsed = parser.tryParse(text, index, this);
					if (parsed != null) {
						if (parsed.getNode() != null) {
							nodes.add(parsed.getNode());
						}
						index = parsed.getNewIndex();
						handled = true;
						break;
					}
				}
			}
			if (handled)
				continue;
			if (parsers != null) {
				nodes.add(new Text(String.valueOf(c)));
				index++;
				continue;
			}

			if (c == '\n') {
				handleNewLine();
			} else if (c == '\\') {
				handleBackslash();
			} else if (c == '<') {
				handleLeftAngleBracket();
			} else if (c == '`') {
				handleBacktick();
			} else if (c == '&') {
				handleEntity();
			} else if (c == '*' || c == '_' || c == '~') {
				handleEmphasis(c);
			} else if (c == '[') {
				handleLeftSquareBracket();
			} else if (c == '!') {
				handleExclamationMark();
			} else {
				handleText();
			}
		}

		processEmphasis();
		trimTrailingSpaces();
		mergeTextNodes(nodes);
		return nodes;
	}

	private void mergeTextNodes(List<Node> nodeList) {
		for (int i = 0; i < nodeList.size() - 1; i++) {
			Node node1 = nodeList.get(i);
			Node node2 = nodeList.get(i + 1);

			if (node1 instanceof Text && node2 instanceof Text) {
				Text text1 = (Text) node1;
				Text text2 = (Text) node2;

				text1.setLiteral(text1.getLiteral() + text2.getLiteral());

				// Remove node2 from list
				nodeList.remove(i + 1);
				i--; // Retry
			} else {
				mergeChildTextNodes(node1);
			}
		}
		// Handle last node's children
		if (!nodeList.isEmpty()) {
			mergeChildTextNodes(nodeList.get(nodeList.size() - 1));
		}
	}

	private void mergeChildTextNodes(Node node) {
		Node child = node.getFirstChild();
		while (child != null) {
			Node next = child.getNext();
			if (child instanceof Text && next instanceof Text) {
				Text text1 = (Text) child;
				Text text2 = (Text) next;
				text1.setLiteral(text1.getLiteral() + text2.getLiteral());

				// Remove next
				next.unlink();
				// child stays same, next iteration will check child vs next.next
			} else {
				mergeChildTextNodes(child);
				child = next;
			}
		}
	}

	private void trimTrailingSpaces() {
		if (!nodes.isEmpty()) {
			Node last = nodes.get(nodes.size() - 1);
			if (last instanceof Text) {
				Text textNode = (Text) last;
				String literal = textNode.getLiteral();
				int i = literal.length() - 1;
				int count = 0;
				while (i >= 0 && literal.charAt(i) == ' ') {
					count++;
					i--;
				}
				if (count > 0) {
					textNode.setLiteral(literal.substring(0, literal.length() - count));
				}
			}
		}
	}

	private void handleNewLine() {
		int i = index - 1;
		int spaceCount = 0;
		while (i >= 0 && text.charAt(i) == ' ') {
			spaceCount++;
			i--;
		}

		trimTrailingSpacesFromLastText(spaceCount);
		if (spaceCount >= 2) {
			nodes.add(new HardBreak());
		} else {
			nodes.add(new SoftBreak());
		}
		index++;
	}

	private void trimTrailingSpacesFromLastText(int count) {
		if (!nodes.isEmpty()) {
			Node last = nodes.get(nodes.size() - 1);
			if (last instanceof Text) {
				Text textNode = (Text) last;
				String literal = textNode.getLiteral();
				if (literal.length() >= count) {
					textNode.setLiteral(literal.substring(0, literal.length() - count));
				}
			}
		}
	}

	private void handleLeftAngleBracket() {
		String remaining = text.substring(index);

		Matcher matcher;

		// Autolinks
		matcher = AUTOLINK_URI.matcher(remaining);
		if (matcher.find() && matcher.start() == 0) {
			String uri = matcher.group();
			String destination = uri.substring(1, uri.length() - 1);
			Link link = new Link(destination, "");
			link.appendChild(new Text(destination));
			nodes.add(link);
			index += uri.length();
			return;
		}

		matcher = AUTOLINK_EMAIL.matcher(remaining);
		if (matcher.find() && matcher.start() == 0) {
			String email = matcher.group();
			String address = email.substring(1, email.length() - 1);
			Link link = new Link("mailto:" + address, "");
			link.appendChild(new Text(address));
			nodes.add(link);
			index += email.length();
			return;
		}

		matcher = HTML_COMMENT_SHORT_2.matcher(remaining);
		if (matcher.find() && matcher.start() == 0) {
			String tag = matcher.group();
			nodes.add(new HtmlInline(tag));
			index += tag.length();
			return;
		}

		matcher = HTML_COMMENT_SHORT.matcher(remaining);
		if (matcher.find() && matcher.start() == 0) {
			String tag = matcher.group();
			nodes.add(new HtmlInline(tag));
			index += tag.length();
			return;
		}

		matcher = HTML_COMMENT.matcher(remaining);
		if (matcher.find() && matcher.start() == 0) {
			String tag = matcher.group();
			String content = tag.substring(4, tag.length() - 3);
			if (!content.startsWith(">") && !content.startsWith("->")) {
				nodes.add(new HtmlInline(tag));
				index += tag.length();
				return;
			}
		}

		matcher = HTML_PI.matcher(remaining);
		if (matcher.find() && matcher.start() == 0) {
			String tag = matcher.group();
			nodes.add(new HtmlInline(tag));
			index += tag.length();
			return;
		}

		matcher = HTML_DECLARATION.matcher(remaining);
		if (matcher.find() && matcher.start() == 0) {
			String tag = matcher.group();
			nodes.add(new HtmlInline(tag));
			index += tag.length();
			return;
		}

		matcher = HTML_CDATA.matcher(remaining);
		if (matcher.find() && matcher.start() == 0) {
			String tag = matcher.group();
			nodes.add(new HtmlInline(tag));
			index += tag.length();
			return;
		}

		matcher = HTML_TAG.matcher(remaining);
		if (matcher.find() && matcher.start() == 0) {
			String tag = matcher.group();

			// GFM Disallowed Raw HTML check
			if (options.isGfm()) {
				String tagName = matcher.group(1).toLowerCase(java.util.Locale.ROOT);
				if (isDisallowedTag(tagName)) {
					// Treat as text
					nodes.add(new Text("<"));
					index++;
					return;
				}
			}

			nodes.add(new HtmlInline(tag));
			index += tag.length();
			return;
		}

		matcher = HTML_CLOSE_TAG.matcher(remaining);
		if (matcher.find() && matcher.start() == 0) {
			String tag = matcher.group();

			// GFM Disallowed Raw HTML check
			if (options.isGfm()) {
				String tagName = matcher.group(1).toLowerCase(java.util.Locale.ROOT);
				if (isDisallowedTag(tagName)) {
					// Treat as text
					nodes.add(new Text("<"));
					index++;
					return;
				}
			}

			nodes.add(new HtmlInline(tag));
			index += tag.length();
			return;
		}

		nodes.add(new Text("<"));
		index++;
	}

	private boolean isDisallowedTag(String tagName) {
		// GFM disallowed tags
		return tagName.equals("title") || tagName.equals("textarea") || tagName.equals("style") || tagName.equals("xmp")
				|| tagName.equals("iframe") || tagName.equals("noembed") || tagName.equals("noframes")
				|| tagName.equals("script") || tagName.equals("plaintext");
	}

	private void handleEntity() {
		String remaining = text.substring(index);
		Matcher matcher = ENTITY.matcher(remaining);
		if (matcher.find() && matcher.start() == 0) {
			String entity = matcher.group();
			String decoded = decodeEntity(matcher);
			nodes.add(new Text(decoded));
			index += entity.length();
		} else {
			nodes.add(new Text("&"));
			index++;
		}
	}

	private String decodeEntity(Matcher matcher) {
		String name = matcher.group(1);
		String decimal = matcher.group(2);
		String hex = matcher.group(3);

		if (name != null) {
			String decoded = EntityDecoder.decode(name);
			if (decoded != null)
				return decoded;
			return matcher.group();
		} else if (decimal != null) {
			try {
				int codePoint = Integer.parseInt(decimal);
				if (codePoint == 0)
					return "\uFFFD";
				return new String(Character.toChars(codePoint));
			} catch (IllegalArgumentException e) {
				return "\uFFFD";
			}
		} else if (hex != null) {
			try {
				int codePoint = Integer.parseInt(hex, 16);
				if (codePoint == 0)
					return "\uFFFD";
				return new String(Character.toChars(codePoint));
			} catch (IllegalArgumentException e) {
				return "\uFFFD";
			}
		}
		return matcher.group();
	}

	private void handleBackslash() {
		if (index + 1 < text.length()) {
			char next = text.charAt(index + 1);
			if (next == '\n') {
				nodes.add(new HardBreak());
				index += 2;
			} else if (isPunctuation(next)) {
				nodes.add(new Text(String.valueOf(next)));
				index += 2;
			} else {
				nodes.add(new Text("\\"));
				index++;
			}
		} else {
			nodes.add(new Text("\\"));
			index++;
		}
	}

	private boolean isPunctuation(char c) {
		String punctuation = "!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~";
		return punctuation.indexOf(c) != -1;
	}

	private void handleBacktick() {
		int start = index;
		int runLength = 0;
		while (index < text.length() && text.charAt(index) == '`') {
			runLength++;
			index++;
		}

		int contentStart = index;
		int closeStart = -1;

		while (index < text.length()) {
			if (text.charAt(index) == '`') {
				int currentRun = 0;
				int tempIndex = index;
				while (tempIndex < text.length() && text.charAt(tempIndex) == '`') {
					currentRun++;
					tempIndex++;
				}

				if (currentRun == runLength) {
					closeStart = index;
					break;
				} else {
					index = tempIndex;
				}
			} else {
				index++;
			}
		}

		if (closeStart != -1) {
			String content = text.substring(contentStart, closeStart);
			content = content.replace('\n', ' ');
			if (content.length() >= 2 && content.startsWith(" ") && content.endsWith(" ")
					&& !content.trim().isEmpty()) {
				content = content.substring(1, content.length() - 1);
			}
			nodes.add(new Code(content));
			index = closeStart + runLength;
		} else {
			nodes.add(new Text(text.substring(start, start + runLength)));
			index = start + runLength;
		}
	}

	private void handleLeftSquareBracket() {
		// Look for matching closing bracket
		int start = index;
		int end = findMatchingBracket(start + 1);

		if (end == -1) {
			nodes.add(new Text("["));
			index++;
			return;
		}

		String textContent = text.substring(start + 1, end);
		int afterEnd = end + 1;

		// Check for inline link: (url) or (url "title")
		if (afterEnd < text.length() && text.charAt(afterEnd) == '(') {
			int i = afterEnd + 1;
			// Skip whitespace
			while (i < text.length() && isWhitespace(text.charAt(i))) {
				i++;
			}

			String destination = null;
			String title = null;
			boolean valid = false;

			if (i < text.length()) {
				// Parse Destination
				ParsedDestination parsedDest = parseLinkDestination(i);
				if (parsedDest != null) {
					destination = parsedDest.destination;
					i = parsedDest.endIndex;

					// Skip whitespace
					boolean hasWhitespace = false;
					while (i < text.length() && isWhitespace(text.charAt(i))) {
						hasWhitespace = true;
						i++;
					}

					// Parse Title (optional)
					if (i < text.length()) {
						char c = text.charAt(i);
						if (c == '"' || c == '\'' || c == '(') {
							if (hasWhitespace) {
								ParsedTitle parsedTitle = parseLinkTitle(i);
								if (parsedTitle != null) {
									title = parsedTitle.title;
									i = parsedTitle.endIndex;

									// Skip whitespace
									while (i < text.length() && isWhitespace(text.charAt(i))) {
										i++;
									}
								}
							} else {
								// Title MUST be separated by whitespace
								// If we see a quote but no whitespace, it's invalid unless it's the closing
								// paren?
								// Actually, if we are at ')' it is fine.
							}
						}
					}

					if (i < text.length() && text.charAt(i) == ')') {
						valid = true;
						i++; // Consume ')'
					}
				} else {
					// Empty destination is allowed? () -> dest=""
					// parseLinkDestination handles empty?
					// If parseLinkDestination returns null, it means invalid.
					// But () is valid.
					if (i < text.length() && text.charAt(i) == ')') {
						destination = "";
						valid = true;
						i++;
					}
				}
			}

			if (valid) {
				Link link = new Link(destination, title != null ? title : "");

				// Parse text content recursively
				InlineParser parser = new InlineParser(textContent, references, options);
				List<Node> children = parser.parse();
				for (Node child : children) {
					link.appendChild(child);
				}

				// Rule: Links may not contain other links
				if (containsLinkInChildren(link)) {
					nodes.add(new Text("["));
					index = start + 1;
					return;
				}

				nodes.add(link);
				index = i;
				return;
			}
		}

		// Check for reference link: [text][label] or [text][] or [text]
		String label = null;
		int refEnd = -1;

		if (afterEnd < text.length() && text.charAt(afterEnd) == '[') {
			// Full or Collapsed
			int labelClose = findMatchingBracket(afterEnd + 1);
			if (labelClose != -1) {
				String labelContent = text.substring(afterEnd + 1, labelClose);
				if (labelContent.trim().isEmpty()) {
					// Collapsed: [text][] -> label = text
					label = textContent;
				} else {
					// Full: [text][label]
					label = labelContent;
				}
				refEnd = labelClose + 1;
			}
		} else {
			// Shortcut: [text] -> label = text
			label = textContent;
			refEnd = end + 1;
		}

		if (label != null) {
			String normalizedLabel = normalizeLabel(label);
			if (references.containsKey(normalizedLabel)) {
				LinkReference ref = references.get(normalizedLabel);
				Link link = new Link(ref.getDestination(), ref.getTitle());

				InlineParser parser = new InlineParser(textContent, references, options);
				List<Node> children = parser.parse();
				for (Node child : children) {
					link.appendChild(child);
				}

				// Rule: Links may not contain other links
				if (containsLinkInChildren(link)) {
					nodes.add(new Text("["));
					index = start + 1;
					return;
				}

				nodes.add(link);
				index = refEnd;
				return;
			}
		}

		// Not a link, add brackets as text
		nodes.add(new Text("["));
		index++;
	}

	private boolean containsLinkInChildren(Node node) {
		Node child = node.getFirstChild();
		while (child != null) {
			if (containsLink(child))
				return true;
			child = child.getNext();
		}
		return false;
	}

	private boolean containsLink(Node node) {
		if (node instanceof Link)
			return true;
		Node child = node.getFirstChild();
		while (child != null) {
			if (containsLink(child))
				return true;
			child = child.getNext();
		}
		return false;
	}

	private void handleExclamationMark() {
		if (index + 1 < text.length() && text.charAt(index + 1) == '[') {
			int start = index;
			int bracketStart = index + 1;
			int end = findMatchingBracket(bracketStart + 1);

			if (end == -1) {
				nodes.add(new Text("!"));
				index++;
				return;
			}

			String altText = text.substring(bracketStart + 1, end);
			int afterEnd = end + 1;

			// Inline image: ![alt](url)
			if (afterEnd < text.length() && text.charAt(afterEnd) == '(') {
				int i = afterEnd + 1;
				while (i < text.length() && isWhitespace(text.charAt(i)))
					i++;

				String destination = null;
				String title = null;
				boolean valid = false;

				if (i < text.length()) {
					ParsedDestination parsedDest = parseLinkDestination(i);
					if (parsedDest != null) {
						destination = parsedDest.destination;
						i = parsedDest.endIndex;

						boolean hasWhitespace = false;
						while (i < text.length() && isWhitespace(text.charAt(i))) {
							hasWhitespace = true;
							i++;
						}

						if (i < text.length()) {
							char c = text.charAt(i);
							if (c == '"' || c == '\'' || c == '(') {
								if (hasWhitespace) {
									ParsedTitle parsedTitle = parseLinkTitle(i);
									if (parsedTitle != null) {
										title = parsedTitle.title;
										i = parsedTitle.endIndex;
										while (i < text.length() && isWhitespace(text.charAt(i)))
											i++;
									}
								}
							}
						}

						if (i < text.length() && text.charAt(i) == ')') {
							valid = true;
							i++;
						}
					} else {
						if (i < text.length() && text.charAt(i) == ')') {
							destination = "";
							valid = true;
							i++;
						}
					}
				}

				if (valid) {
					Image image = new Image(destination, title != null ? title : "");

					InlineParser parser = new InlineParser(altText, references, options);
					List<Node> children = parser.parse();
					for (Node child : children) {
						image.appendChild(child);
					}

					nodes.add(image);
					index = i;
					return;
				}
			}

			// Reference image: ![alt][label]
			String label = null;
			int refEnd = -1;

			if (afterEnd < text.length() && text.charAt(afterEnd) == '[') {
				int labelClose = findMatchingBracket(afterEnd + 1);
				if (labelClose != -1) {
					String labelContent = text.substring(afterEnd + 1, labelClose);
					if (labelContent.trim().isEmpty()) {
						label = altText;
					} else {
						label = labelContent;
					}
					refEnd = labelClose + 1;
				}
			} else {
				label = altText;
				refEnd = end + 1;
			}

			if (label != null) {
				String normalizedLabel = normalizeLabel(label);
				if (references.containsKey(normalizedLabel)) {
					LinkReference ref = references.get(normalizedLabel);
					Image image = new Image(ref.getDestination(), ref.getTitle());

					InlineParser parser = new InlineParser(altText, references, options);
					List<Node> children = parser.parse();
					for (Node child : children) {
						image.appendChild(child);
					}

					nodes.add(image);
					index = refEnd;
					return;
				}
			}

			nodes.add(new Text("!"));
			index++;
		} else {
			nodes.add(new Text("!"));
			index++;
		}
	}

	private int findMatchingBracket(int startIndex) {
		int depth = 0;
		int i = startIndex;
		while (i < text.length()) {
			char c = text.charAt(i);

			if (c == '\\') {
				i += 2;
				continue;
			}

			if (c == '<') {
				String remaining = text.substring(i);
				Matcher matcher = AUTOLINK_URI.matcher(remaining);
				if (matcher.find() && matcher.start() == 0) {
					i += matcher.end();
					continue;
				}
				matcher = AUTOLINK_EMAIL.matcher(remaining);
				if (matcher.find() && matcher.start() == 0) {
					i += matcher.end();
					continue;
				}
				matcher = HTML_TAG.matcher(remaining);
				if (matcher.find() && matcher.start() == 0) {
					i += matcher.end();
					continue;
				}
				matcher = HTML_CLOSE_TAG.matcher(remaining);
				if (matcher.find() && matcher.start() == 0) {
					i += matcher.end();
					continue;
				}
				matcher = HTML_COMMENT.matcher(remaining);
				if (matcher.find() && matcher.start() == 0) {
					i += matcher.end();
					continue;
				}
				matcher = HTML_PI.matcher(remaining);
				if (matcher.find() && matcher.start() == 0) {
					i += matcher.end();
					continue;
				}
				matcher = HTML_DECLARATION.matcher(remaining);
				if (matcher.find() && matcher.start() == 0) {
					i += matcher.end();
					continue;
				}
				matcher = HTML_CDATA.matcher(remaining);
				if (matcher.find() && matcher.start() == 0) {
					i += matcher.end();
					continue;
				}
			}

			if (c == '`') {
				int runLength = 0;
				int temp = i;
				while (temp < text.length() && text.charAt(temp) == '`') {
					runLength++;
					temp++;
				}
				int contentStart = temp;
				int closeEnd = -1;
				while (temp < text.length()) {
					if (text.charAt(temp) == '`') {
						int closeRun = 0;
						int closeStart = temp;
						while (temp < text.length() && text.charAt(temp) == '`') {
							closeRun++;
							temp++;
						}
						if (closeRun == runLength) {
							closeEnd = temp;
							break;
						}
					} else {
						temp++;
					}
				}
				if (closeEnd != -1) {
					i = closeEnd;
					continue;
				}
				i += runLength;
				continue;
			}

			if (c == '[') {
				depth++;
			} else if (c == ']') {
				if (depth == 0)
					return i;
				depth--;
			}
			i++;
		}
		return -1;
	}

	private boolean isWhitespace(char c) {
		return c == ' ' || c == '\t' || c == '\n' || c == '\u000B' || c == '\u000C' || c == '\r';
	}

	private ParsedDestination parseLinkDestination(int startIndex) {
		if (startIndex >= text.length())
			return null;
		char c = text.charAt(startIndex);

		if (c == '<') {
			// Pointy bracket destination
			StringBuilder sb = new StringBuilder();
			int i = startIndex + 1;
			while (i < text.length()) {
				char ch = text.charAt(i);
				if (ch == '\\') {
					if (i + 1 < text.length()) {
						char next = text.charAt(i + 1);
						if (isPunctuation(next) || next == '\\') {
							sb.append(next);
						} else {
							sb.append('\\').append(next);
						}
						i += 2;
						continue;
					}
				}

				if (ch == '&') {
					Matcher matcher = ENTITY.matcher(text.substring(i));
					if (matcher.find()) {
						sb.append(decodeEntity(matcher));
						i += matcher.end();
						continue;
					}
				}

				if (ch == '\n' || ch == '\r')
					return null; // No newlines
				if (ch == '>') {
					return new ParsedDestination(sb.toString(), i + 1);
				}
				if (ch == '<')
					return null; // Unescaped < not allowed
				sb.append(ch);
				i++;
			}
			return null; // Not closed
		} else {
			// Bare destination
			StringBuilder sb = new StringBuilder();
			int i = startIndex;
			int parenDepth = 0;

			while (i < text.length()) {
				char ch = text.charAt(i);
				if (Character.isISOControl(ch) && !Character.isWhitespace(ch))
					return null; // Control chars
				if (isWhitespace(ch))
					break;

				if (ch == '\\') {
					if (i + 1 < text.length()) {
						char next = text.charAt(i + 1);
						if (isPunctuation(next)) {
							sb.append(next);
						} else {
							sb.append('\\').append(next);
						}
						i += 2;
						continue;
					}
				}

				if (ch == '&') {
					Matcher matcher = ENTITY.matcher(text.substring(i));
					if (matcher.find()) {
						sb.append(decodeEntity(matcher));
						i += matcher.end();
						continue;
					}
				}

				if (ch == '(') {
					parenDepth++;
				} else if (ch == ')') {
					if (parenDepth == 0)
						break; // End of destination
					parenDepth--;
				}

				sb.append(ch);
				i++;
			}

			if (parenDepth != 0)
				return null; // Unbalanced
			// Can be empty? Yes.
			return new ParsedDestination(sb.toString(), i);
		}
	}

	private ParsedTitle parseLinkTitle(int startIndex) {
		if (startIndex >= text.length())
			return null;
		char opener = text.charAt(startIndex);
		char closer;
		if (opener == '"')
			closer = '"';
		else if (opener == '\'')
			closer = '\'';
		else if (opener == '(')
			closer = ')';
		else
			return null;

		StringBuilder sb = new StringBuilder();
		int i = startIndex + 1;

		while (i < text.length()) {
			char ch = text.charAt(i);
			if (ch == '\\') {
				if (i + 1 < text.length()) {
					char next = text.charAt(i + 1);
					if (isPunctuation(next)) {
						sb.append(next); // Unescape
					} else {
						sb.append('\\').append(next);
					}
					i += 2;
					continue;
				}
			}

			if (ch == '&') {
				Matcher matcher = ENTITY.matcher(text.substring(i));
				if (matcher.find()) {
					sb.append(decodeEntity(matcher));
					i += matcher.end();
					continue;
				}
			}

			if (ch == closer) {
				return new ParsedTitle(sb.toString(), i + 1);
			}

			if (opener == '(' && ch == '(')
				return null; // Nested parens not allowed in title

			sb.append(ch);
			i++;
		}

		return null; // Not closed
	}

	private static class ParsedDestination {
		String destination;
		int endIndex;

		ParsedDestination(String destination, int endIndex) {
			this.destination = destination;
			this.endIndex = endIndex;
		}
	}

	private static class ParsedTitle {
		String title;
		int endIndex;

		ParsedTitle(String title, int endIndex) {
			this.title = title;
			this.endIndex = endIndex;
		}
	}

	private String normalizeLabel(String label) {
		String s = label.trim().replaceAll("\\s+", " ");
		s = s.replace("ẞ", "ss");
		return s.toUpperCase(java.util.Locale.ROOT).toLowerCase(java.util.Locale.ROOT);
	}

	private void handleText() {
		int start = index;
		while (index < text.length()) {
			char c = text.charAt(index);

			// Check for potential GFM Autolink start boundary
			if (options.isGfm()) {
				// If we consumed text, previous char was c-1 (which is at index-1).
				// Wait, if index > start, we have consumed at least one char.
				// We should check if we should STOP to let handleExtendedAutolink run.

				// We stop if:
				// 1. Current char could start a link (h, f, w, or email start?)
				// 2. Previous char (which we just consumed or passed) was a boundary
				// (whitespace, etc.)

				// But handleText loop consumes c.
				// If c is ' ', we consume it.
				// Next iteration: c is 'h'. index points to 'h'. index-1 points to ' '.
				// Boundary condition met.
				// So we should break IF current char 'h' starts a link.

				if (index > start) {
					char prev = text.charAt(index - 1);
					if (isWhitespace(prev) || prev == '*' || prev == '_' || prev == '~' || prev == '(') {
						// Check if current char starts a link
						break;
					}
				}
			}

			if (c == '\n' || c == '\\' || c == '<' || c == '`' || c == '&' || c == '*' || c == '_' || c == '['
					|| c == '!' || c == '~' || customParsers.containsKey(c)) {
				break;
			}
			index++;
		}
		if (index > start) {
			String content = text.substring(start, index);
			nodes.add(new Text(content));
		}
	}

	private static final Pattern EXTENDED_AUTOLINK_URI = Pattern.compile(
			"(?:(https?|ftp)://|www\\.)[a-zA-Z0-9.+-]+(?:/[a-zA-Z0-9._+/?#@!$&'()*+,;=-]*)?", Pattern.CASE_INSENSITIVE);

	private static final Pattern EXTENDED_AUTOLINK_EMAIL = Pattern
			.compile("[a-zA-Z0-9._+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{1,}", Pattern.CASE_INSENSITIVE);

	private boolean handleExtendedAutolink() {
		if (!options.isGfm())
			return false;

		char c = text.charAt(index);
		if (!(Character.isLetterOrDigit(c) || c == '.' || c == '_' || c == '+' || c == '-')) {
			return false;
		}

		Matcher matcher = EXTENDED_AUTOLINK_URI.matcher(text);
		matcher.region(index, text.length());

		if (matcher.lookingAt()) {
			// Check boundary
			boolean boundary = false;
			if (index == 0)
				boundary = true;
			else {
				char prev = text.charAt(index - 1);
				if (isWhitespace(prev) || prev == '*' || prev == '_' || prev == '~' || prev == '(') {
					boundary = true;
				}
			}

			if (boundary) {
				String match = matcher.group();
				String linkDest;

				// Trim trailing punctuation
				match = trimTrailingPunctuation(match);

				// Check for trailing entity-like pattern (e.g. &hl;)
				if (match.endsWith(";")) {
					int amp = match.lastIndexOf('&');
					if (amp != -1) {
						String potentialEntity = match.substring(amp);
						if (potentialEntity.matches("&[a-zA-Z0-9]+;")) {
							match = match.substring(0, amp);
						}
					}
				}

				// If trimmed match is empty or invalid?
				if (match.isEmpty())
					return false;

				if (match.toLowerCase(java.util.Locale.ROOT).startsWith("www.")) {
					linkDest = "http://" + match;
				} else {
					linkDest = match;
				}

				Link link = new Link(linkDest, "");
				link.appendChild(new Text(match));
				nodes.add(link);
				index += match.length();
				return true;
			}
		}

		// Email
		matcher = EXTENDED_AUTOLINK_EMAIL.matcher(text);
		matcher.region(index, text.length());
		if (matcher.lookingAt()) {
			boolean boundary = false;
			if (index == 0)
				boundary = true;
			else {
				char prev = text.charAt(index - 1);
				if (isWhitespace(prev) || prev == '*' || prev == '_' || prev == '~' || prev == '(') {
					boundary = true;
				}
			}

			if (boundary) {
				String match = matcher.group();
				if (match.endsWith("-") || match.endsWith("_"))
					return false;

				// Check what follows
				int end = matcher.end();
				if (end < text.length()) {
					char next = text.charAt(end);
					if (Character.isLetterOrDigit(next) || next == '-' || next == '_' || next == '+') {
						return false;
					}
				}

				match = trimTrailingPunctuation(match);
				if (match.endsWith("."))
					match = match.substring(0, match.length() - 1);

				String linkDest = "mailto:" + match;
				Link link = new Link(linkDest, "");
				link.appendChild(new Text(match));
				nodes.add(link);
				index += match.length();
				return true;
			}
		}

		return false;
	}

	private String trimTrailingPunctuation(String s) {
		int end = s.length();
		while (end > 0) {
			char c = s.charAt(end - 1);
			if (isGfmTrailingPunctuation(c)) {
				if (c == ')') {
					// Check for balanced parens
					int open = 0;
					int close = 0;
					for (int i = 0; i < end; i++) {
						if (s.charAt(i) == '(')
							open++;
						if (s.charAt(i) == ')')
							close++;
					}
					if (open >= close)
						break; // Balanced or more open, keep it
				}
				// Strip
				end--;
			} else {
				break;
			}
		}
		return s.substring(0, end);
	}

	private boolean isGfmTrailingPunctuation(char c) {
		return c == '?' || c == '!' || c == '.' || c == ',' || c == ':' || c == '*' || c == '_' || c == '~' || c == ')';
	}

	private boolean isUnicodeWhitespace(char c) {
		return Character.getType(c) == Character.SPACE_SEPARATOR || c == '\t' || c == '\n' || c == '\u000B'
				|| c == '\u000C' || c == '\r';
	}

	private boolean isUnicodePunctuation(char c) {
		if (isPunctuation(c))
			return true;
		int type = Character.getType(c);
		return type == Character.DASH_PUNCTUATION || type == Character.START_PUNCTUATION
				|| type == Character.END_PUNCTUATION || type == Character.CONNECTOR_PUNCTUATION
				|| type == Character.OTHER_PUNCTUATION || type == Character.INITIAL_QUOTE_PUNCTUATION
				|| type == Character.FINAL_QUOTE_PUNCTUATION || type == Character.CURRENCY_SYMBOL
				|| type == Character.MATH_SYMBOL || type == Character.MODIFIER_SYMBOL || type == Character.OTHER_SYMBOL;
	}

	private void handleEmphasis(char c) {
		int start = index;
		int runLength = 0;
		while (index < text.length() && text.charAt(index) == c) {
			runLength++;
			index++;
		}

		Text textNode = new Text(text.substring(start, index));
		nodes.add(textNode);

		// Flanking checks
		char before = (start > 0) ? text.charAt(start - 1) : '\n';
		char after = (index < text.length()) ? text.charAt(index) : '\n';

		boolean leftFlanking = !isUnicodeWhitespace(after)
				&& (!isUnicodePunctuation(after) || isUnicodeWhitespace(before) || isUnicodePunctuation(before));

		boolean rightFlanking = !isUnicodeWhitespace(before)
				&& (!isUnicodePunctuation(before) || isUnicodeWhitespace(after) || isUnicodePunctuation(after));

		boolean canOpen;
		boolean canClose;

		if (c == '_') {
			canOpen = leftFlanking && (!rightFlanking || isUnicodePunctuation(before));
			canClose = rightFlanking && (!leftFlanking || isUnicodePunctuation(after));
		} else {
			// For * and ~
			canOpen = leftFlanking;
			canClose = rightFlanking;
		}

		// Add to delimiter stack
		Delimiter delim = new Delimiter(textNode, c, runLength, start, canOpen, canClose, lastDelimiter);
		if (lastDelimiter != null) {
			lastDelimiter.next = delim;
		}
		lastDelimiter = delim;
	}

	private void processEmphasis() {
		// Find the first delimiter
		Delimiter current = lastDelimiter;
		while (current != null && current.previous != null) {
			current = current.previous;
		}

		while (current != null) {
			Delimiter d = current;
			if (!d.canClose) {
				current = current.next;
				continue;
			}

			// d is a closer. Look back for opener.
			boolean found = false;
			Delimiter opener = d.previous;

			while (opener != null) {
				if (opener.c == d.c && opener.canOpen) {
					// Check Rule of 3
					if ((opener.canClose || d.canOpen) && (opener.length + d.length) % 3 == 0
							&& (opener.length % 3 != 0) && (d.length % 3 != 0)) {
						opener = opener.previous;
						continue;
					}

					// GFM Strikethrough check
					if (d.c == '~') {
						if (opener.length < 2 || d.length < 2) {
							// Must have at least 2 tildes
							opener = opener.previous;
							continue;
						}
					}

					found = true;
					break;
				}
				opener = opener.previous;
			}

			if (found) {
				// Match found!

				// Determine usage
				int useDelims = (d.length >= 2 && opener.length >= 2) ? 2 : 1;

				if (d.c == '~') {
					useDelims = 2; // Always use 2 for strikethrough
				}

				// Create emphasis node
				Node openerNode = opener.node;
				Node closerNode = d.node;

				Node emphasis;
				if (d.c == '~') {
					emphasis = new Strikethrough();
				} else {
					emphasis = (useDelims == 2) ? new StrongEmphasis() : new Emphasis();
				}

				int openerIndex = nodes.indexOf(openerNode);
				int closerIndex = nodes.indexOf(closerNode);

				if (openerIndex != -1 && closerIndex != -1 && openerIndex < closerIndex) {
					List<Node> children = new ArrayList<>();
					for (int i = openerIndex + 1; i < closerIndex; i++) {
						children.add(nodes.get(i));
					}

					// Remove children from main list
					nodes.subList(openerIndex + 1, closerIndex).clear();

					// Add children to emphasis
					for (Node child : children) {
						emphasis.appendChild(child);
					}

					// Update opener/closer
					String openerText = ((Text) openerNode).getLiteral();
					String closerText = ((Text) closerNode).getLiteral();

					// Remove intervening delimiters
					Delimiter temp = d.previous;
					while (temp != null && temp != opener) {
						Delimiter prev = temp.previous;
						removeDelimiter(temp);
						temp = prev;
					}

					// Handle Opener
					if (opener.length == useDelims) {
						nodes.set(openerIndex, emphasis);
						removeDelimiter(opener);
					} else {
						// Reduce opener
						opener.length -= useDelims;
						((Text) openerNode).setLiteral(openerText.substring(0, openerText.length() - useDelims));

						nodes.add(openerIndex + 1, emphasis);
					}

					// Handle Closer
					if (d.length == useDelims) {
						int currentCloserIndex = nodes.indexOf(closerNode);
						nodes.remove(currentCloserIndex);
						removeDelimiter(d);
						current = d.next;
					} else {
						// Reduce closer
						d.length -= useDelims;
						((Text) closerNode).setLiteral(closerText.substring(0, closerText.length() - useDelims));

						// Closer stays.
						// We continue with the SAME closer 'd' to see if it can close more.
						current = d;
					}

				} else {
					// Indices not valid? Should not happen if logic is correct.
					current = current.next;
				}

			} else {
				// No match found
				current = current.next;
			}
		}
	}

	private void removeDelimiter(Delimiter d) {
		if (d.previous != null) {
			d.previous.next = d.next;
		}
		if (d.next != null) {
			d.next.previous = d.previous;
		}
		// If d was lastDelimiter?
		if (d == lastDelimiter) {
			lastDelimiter = d.previous;
		}
	}

	private static class Delimiter {
		Node node;
		final char c;
		int length;
		int index;
		boolean canOpen;
		boolean canClose;
		Delimiter previous;
		Delimiter next;

		public Delimiter(Node node, char c, int length, int index, boolean canOpen, boolean canClose,
				Delimiter previous) {
			this.node = node;
			this.c = c;
			this.length = length;
			this.index = index;
			this.canOpen = canOpen;
			this.canClose = canClose;
			this.previous = previous;
		}
	}
}
