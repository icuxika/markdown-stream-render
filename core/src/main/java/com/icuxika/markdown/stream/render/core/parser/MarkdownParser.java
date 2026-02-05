package com.icuxika.markdown.stream.render.core.parser;

import com.icuxika.markdown.stream.render.core.ast.*;
import com.icuxika.markdown.stream.render.core.parser.block.*;
import com.icuxika.markdown.stream.render.core.parser.inline.InlineContentParserFactory;
import com.icuxika.markdown.stream.render.core.renderer.IMarkdownRenderer;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 核心 Markdown 解析器。
 * <p>
 * 负责将 Markdown 文本解析为 {@link Document} (AST 根节点)。
 * 支持 CommonMark 规范的大部分特性，以及通过插件扩展自定义块级和行内元素。
 * </p>
 */

/**
 * 核心 Markdown 解析器。
 * <p>
 * 负责将 Markdown 文本解析为 {@link Document} (AST 根节点)。
 * 支持 CommonMark 规范的大部分特性，以及通过插件扩展自定义块级和行内元素。
 * </p>
 */
public class MarkdownParser {

    private static final Pattern ENTITY = Pattern.compile("^&(?:([a-zA-Z0-9]+)|#([0-9]{1,7})|#(?i:x)([0-9a-fA-F]{1,6}));");

    private final MarkdownParserOptions options;
    private final List<BlockParserFactory> blockParserFactories;
    private final List<InlineContentParserFactory> inlineParserFactories;

    public MarkdownParser() {
        this(new Builder());
    }

    public MarkdownParser(Builder builder) {
        this.options = builder.options;
        this.blockParserFactories = builder.blockParserFactories;
        this.inlineParserFactories = builder.inlineParserFactories;
    }

    /**
     * 获取解析器构建器。
     *
     * @return 新的 Builder 实例
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * 解析器构建器，用于配置选项和注册插件。
     */
    public static class Builder {
        private MarkdownParserOptions options = new MarkdownParserOptions();
        private List<BlockParserFactory> blockParserFactories = new ArrayList<>();
        private List<InlineContentParserFactory> inlineParserFactories = new ArrayList<>();

        public Builder options(MarkdownParserOptions options) {
            this.options = options;
            return this;
        }

        /**
         * 注册自定义块级解析器工厂。
         *
         * @param factory 块解析器工厂
         */
        public Builder blockParserFactory(BlockParserFactory factory) {
            this.blockParserFactories.add(factory);
            return this;
        }

        /**
         * 注册自定义行内解析器工厂。
         *
         * @param factory 行内解析器工厂
         */
        public Builder inlineParserFactory(InlineContentParserFactory factory) {
            this.inlineParserFactories.add(factory);
            return this;
        }

        public MarkdownParser build() {
            return new MarkdownParser(this);
        }
    }

    public MarkdownParserOptions getOptions() {
        return options;
    }

    /**
     * 解析 Reader 输入，并使用指定的渲染器进行处理。
     * <p>
     * 这种方式支持边解析边渲染（流式处理），尽管目前的实现主要还是先构建完 AST。
     * </p>
     *
     * @param reader   输入流
     * @param renderer 渲染器
     * @throws IOException 如果读取失败
     */
    /**
     * 解析 Reader 输入，并使用指定的渲染器进行处理。
     * <p>
     * 这种方式支持边解析边渲染（流式处理），尽管目前的实现主要还是先构建完 AST。
     * </p>
     *
     * @param reader   输入流
     * @param renderer 渲染器
     * @throws IOException 如果读取失败
     */
    public void parse(Reader reader, IMarkdownRenderer renderer) throws IOException {
        Document doc = new Document();
        BlockParserState state = new BlockParserState(blockParserFactories, options);

        java.io.BufferedReader br = (reader instanceof java.io.BufferedReader) ? (java.io.BufferedReader) reader : new java.io.BufferedReader(reader);
        String line;
        int lineNumber = 0;
        while ((line = br.readLine()) != null) {
            state.processLine(doc, expandTabs(line), line, lineNumber++);
        }
        state.finalizeBlock(doc, lineNumber);

        extractLinkReferenceDefinitions(doc);
        parseInlines(doc, doc);

        doc.accept(renderer);
    }

    /**
     * 解析字符串输入，返回完整的 AST 文档对象。
     *
     * @param input Markdown 源码字符串
     * @return 解析后的文档根节点
     */
    public Document parse(String input) {
        Document doc = new Document();
        if (input == null) return doc;

        BlockParserState state = new BlockParserState(blockParserFactories, options);
        int lineNumber = 0;

        int len = input.length();
        if (len == 0) {
            state.processLine(doc, "", "", lineNumber++);
        } else {
            int start = 0;
            for (int i = 0; i < len; i++) {
                char c = input.charAt(i);
                if (c == '\n' || c == '\r') {
                    String line = input.substring(start, i);
                    state.processLine(doc, expandTabs(line), line, lineNumber++);

                    if (c == '\r' && i + 1 < len && input.charAt(i + 1) == '\n') {
                        i++;
                    }
                    start = i + 1;
                }
            }

            if (start < len) {
                String line = input.substring(start);
                state.processLine(doc, expandTabs(line), line, lineNumber++);
            }
        }

        state.finalizeBlock(doc, lineNumber);

        extractLinkReferenceDefinitions(doc);
        parseInlines(doc, doc);

        return doc;
    }

    static String getSubstringForColumn(String line, int column) {
        int col = 0;
        for (int i = 0; i < line.length(); i++) {
            if (col == column) return line.substring(i);

            char c = line.charAt(i);
            if (c == '\t') {
                int toAdd = 4 - (col % 4);
                if (col + toAdd > column) {
                    int spacesNeeded = (col + toAdd) - column;
                    StringBuilder sb = new StringBuilder();
                    for (int k = 0; k < spacesNeeded; k++) sb.append(' ');
                    if (i + 1 < line.length()) sb.append(line.substring(i + 1));
                    return sb.toString();
                }
                col += toAdd;
            } else {
                col++;
            }
        }
        return "";
    }

    private String expandTabs(String s) {
        if (s.indexOf('\t') == -1) return s;
        StringBuilder sb = new StringBuilder();
        int col = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\t') {
                int toAdd = 4 - (col % 4);
                for (int j = 0; j < toAdd; j++) {
                    sb.append(' ');
                }
                col += toAdd;
            } else {
                sb.append(c);
                col++;
            }
        }
        return sb.toString();
    }

    // Making BlockParserState accessible for StreamMarkdownParser
    public static class BlockParserState {
        // Callback for streaming
        private java.util.function.Consumer<Node> onBlockFinalized;
        private java.util.function.Consumer<Node> onBlockStarted;
        private java.util.function.Consumer<Node> onBlockClosed;

        public void setOnBlockFinalized(java.util.function.Consumer<Node> onBlockFinalized) {
            this.onBlockFinalized = onBlockFinalized;
        }

        public void setOnBlockStarted(java.util.function.Consumer<Node> onBlockStarted) {
            this.onBlockStarted = onBlockStarted;
        }

        public void setOnBlockClosed(java.util.function.Consumer<Node> onBlockClosed) {
            this.onBlockClosed = onBlockClosed;
        }

        List<Node> openContainers = new ArrayList<>();
        List<Integer> openContainerBlockIndents = new ArrayList<>();

        // New: Support for custom Block Parsers
        Map<Node, BlockParser> activeBlockParsers = new HashMap<>();
        List<BlockParserFactory> blockParserFactories;

        Node currentLeaf = null; // Paragraph, CodeBlock, etc.
        StringBuilder currentLeafContent = new StringBuilder();
        int lastLineContentDepth = Integer.MAX_VALUE;

        // Fenced Code Block State (if currentLeaf is CodeBlock)
        boolean inFencedCodeBlock = false;
        char fenceChar;
        int fenceLength;
        int fenceIndent;

        // Indented Code Block State
        boolean inIndentedCodeBlock = false;

        // HTML Block State
        boolean inHtmlBlock = false;
        int htmlBlockCondition = 0;

        // Table State
        boolean inTable = false;
        List<TableCell.Alignment> tableAlignments = new ArrayList<>();

        private final MarkdownParserOptions options;

        BlockParserState(List<BlockParserFactory> blockParserFactories, MarkdownParserOptions options) {
            this.blockParserFactories = blockParserFactories != null ? blockParserFactories : new ArrayList<>();
            this.options = options != null ? options : new MarkdownParserOptions();
        }

        // Inner class implementation for ParserState
        private class ParserStateImpl implements ParserState {
            private final String line;
            private final int index;
            private final int indent;

            public ParserStateImpl(String line, int index, int indent) {
                this.line = line;
                this.index = index;
                this.indent = indent;
            }

            @Override
            public CharSequence getLine() {
                return line;
            }

            @Override
            public int getIndex() {
                return index;
            }

            @Override
            public int getNextNonSpaceIndex() {
                int i = index;
                while (i < line.length() && line.charAt(i) == ' ') i++;
                return i;
            }

            @Override
            public int getIndent() {
                // Approximate indent based on spaces
                // Or use pre-calculated indent passed in?
                // The indent passed in constructor is relative to... what?
                // The 'indent' passed in constructor is likely current line indent.
                return indent;
            }

            @Override
            public boolean isBlank() {
                return line.trim().isEmpty();
            }

            @Override
            public BlockParser getActiveBlockParser() {
                // Return the parser for the deepest open container if available
                if (openContainers.isEmpty()) return null;
                Node node = openContainers.get(openContainers.size() - 1);
                return activeBlockParsers.get(node);
            }
        }

        // Inner class for MatchedBlockParser
        private class MatchedBlockParserImpl implements MatchedBlockParser {
            private final BlockParser parser;

            public MatchedBlockParserImpl(BlockParser parser) {
                this.parser = parser;
            }

            @Override
            public BlockParser getBlockParser() {
                return parser;
            }

            @Override
            public CharSequence getParagraphContent() {
                if (currentLeaf instanceof Paragraph) {
                    return currentLeafContent;
                }
                return null;
            }
        }

        void processLine(Document doc, String line, String originalLine, int lineNumber) {
            int currentContentDepth = 0;
            boolean inImplicitMode = false;

            if (openContainers.isEmpty()) {
                openContainers.add(doc);
                openContainerBlockIndents.add(0);
                // Document start line is always 0
                if (doc.getStartLine() == -1) doc.setStartLine(0);
                if (onBlockStarted != null) onBlockStarted.accept(doc);
            }

            int i = 0;
            int matches = 0;
            boolean isContainerMarkerLine = false;
            boolean isListMarkerLine = false;
            boolean lastMatchedContainerHadMarker = false;

            // 1. Check open containers (BlockQuote, Custom Blocks)
            // Skip root (index 0)
            for (int k = 1; k < openContainers.size(); k++) {
                Node container = openContainers.get(k);
                // Update end line for container as we are still inside it
                container.setEndLine(lineNumber);

                // Check for custom BlockParser
                BlockParser parser = activeBlockParsers.get(container);
                if (parser != null) {
                    int indent = countIndent(line.substring(i));
                    ParserState state = new ParserStateImpl(line, i, indent);
                    BlockContinue cont = parser.tryContinue(state);
                    if (cont != null) {
                        matches++;
                        currentContentDepth = k;
                        if (cont.getNewIndex() != -1) {
                            i = cont.getNewIndex();
                        }
                        if (cont.isFinalize()) {
                            // finalize? Not supported in tryContinue normally.
                            // But we can support it.
                            // If finalized, we should close it?
                            // Usually tryContinue just says "matched".
                        }
                        lastMatchedContainerHadMarker = true; // Assume true for custom blocks? Or false?
                        // Depends on block.
                        continue;
                    } else {
                        // Mismatch
                        break;
                    }
                }

                if (container instanceof BlockQuote) {
                    int indent = 0;
                    int startI = i;
                    while (i < line.length() && line.charAt(i) == ' ' && indent < 3) {
                        i++;
                        indent++;
                    }
                    if (i < line.length() && line.charAt(i) == '>') {
                        matches++;
                        currentContentDepth = k;
                        i++;
                        if (i < line.length() && line.charAt(i) == ' ') {
                            i++;
                        }
                        isContainerMarkerLine = true;
                        lastMatchedContainerHadMarker = true;
                    } else {
                        // Mismatch - rewind i
                        i = startI;
                        break;
                    }
                } else if (container instanceof ListItem) {
                    int expectedIndent = openContainerBlockIndents.get(k);
                    int indent = 0;
                    int startI = i;
                    while (i < line.length() && line.charAt(i) == ' ' && indent < expectedIndent) {
                        i++;
                        indent++;
                    }

                    if (indent >= expectedIndent) {
                        matches++;
                        currentContentDepth = k;
                        lastMatchedContainerHadMarker = false;
                    } else if (line.substring(startI).trim().isEmpty()) {
                        matches++;
                        lastMatchedContainerHadMarker = false;
                    } else {
                        i = startI;
                        break;
                    }
                } else {
                    // Other containers (List, Document)
                    matches++;
                    currentContentDepth = k;
                    lastMatchedContainerHadMarker = false;
                }
            }

            // 2. Close unmatched containers
            String contentLine = line.substring(i);

            // Check for Lazy Continuation
            boolean lazyContinuation = false;
            if (currentLeaf instanceof Paragraph && !openContainers.isEmpty() && !contentLine.trim().isEmpty()) {
                Node lastContainer = openContainers.get(openContainers.size() - 1);
                if (matches < openContainers.size() && lastContainer instanceof Block) {

                    // We need to check if this line is a block starter.
                    boolean isBlockStarter = isAtxHeading(contentLine)
                            || isThematicBreak(contentLine)
                            || parseFencedCodeStart(contentLine) != null
                            || isBlockQuoteStart(contentLine)
                            || isTableDelimiterRow(contentLine) // Table can interrupt paragraph
                            ;

                    if (!isBlockStarter) {
                        ListMarker lm = parseListMarker(contentLine, 0);
                        if (lm != null) {
                            boolean matchesCurrent = false;
                            if (matches < openContainers.size()) {
                                Node parent = openContainers.get(matches);
                                if (parent instanceof BulletList) {
                                    BulletList bl = (BulletList) parent;
                                    if (!lm.isOrdered && bl.getBulletChar() == lm.bulletChar) matchesCurrent = true;
                                } else if (parent instanceof OrderedList) {
                                    OrderedList ol = (OrderedList) parent;
                                    if (lm.isOrdered && ol.getDelimiter() == lm.delimiter) matchesCurrent = true;
                                }
                            }

                            if (matchesCurrent) {
                                isBlockStarter = true;
                            } else {
                                boolean canInterrupt = true;
                                if (currentLeaf instanceof Paragraph) {
                                    if (lm.isOrdered && lm.startNumber != 1) canInterrupt = false;
                                    if (contentLine.substring(lm.nextIndex).trim().isEmpty()) canInterrupt = false;
                                }

                                if (!openContainers.isEmpty()) {
                                    Node lastOpenContainer = openContainers.get(openContainers.size() - 1);
                                    if (lastOpenContainer instanceof ListItem && lastOpenContainer.getParent() instanceof OrderedList) {
                                        canInterrupt = true;
                                    }
                                }
                                if (canInterrupt) isBlockStarter = true;
                            }
                        }
                    }

                    if (!isBlockStarter) {
                        lazyContinuation = true;
                    }
                }
            }

            if (!lazyContinuation) {
                while (openContainers.size() > matches + 1) {
                    finalizeCurrentLeaf(lineNumber - 1);
                    Node removed = openContainers.remove(openContainers.size() - 1);
                    if (onBlockClosed != null) onBlockClosed.accept(removed);
                    openContainerBlockIndents.remove(openContainerBlockIndents.size() - 1);
                }
            }

            // Check if Indented Code Block ends
            if (inIndentedCodeBlock && countIndent(contentLine) < 4 && !contentLine.trim().isEmpty()) {
                finalizeCurrentLeaf(lineNumber - 1);
            }

            // 3. Parse new containers (BlockQuote, List, Custom Blocks)
            // Only if we are not in a Fenced Code Block (Leaf)
            // Or if currentLeaf is null
            if (!inFencedCodeBlock && !inIndentedCodeBlock && !lazyContinuation) {
                while (true) {
                    int indent = 0;
                    int startI = i;

                    // Check Custom Block Factories
                    boolean startedCustom = false;
                    for (BlockParserFactory factory : blockParserFactories) {
                        ParserState state = new ParserStateImpl(line, i, countIndent(line.substring(i)));
                        // Determine parent parser (legacy node handling is tricky)
                        MatchedBlockParser matched = null;
                        if (!openContainers.isEmpty()) {
                            Node parentNode = openContainers.get(openContainers.size() - 1);
                            BlockParser parentParser = activeBlockParsers.get(parentNode);
                            if (parentParser != null) {
                                matched = new MatchedBlockParserImpl(parentParser);
                            }
                        }

                        BlockStart start = factory.tryStart(state, matched);
                        if (start != null) {
                            finalizeCurrentLeaf(lineNumber - 1);

                            // Handle replacement
                            if (start.isReplaceActiveBlockParser() && !openContainers.isEmpty()) {
                                Node removed = openContainers.remove(openContainers.size() - 1);
                                openContainerBlockIndents.remove(openContainerBlockIndents.size() - 1);
                                activeBlockParsers.remove(removed);
                            }

                            for (BlockParser bp : start.getBlockParsers()) {
                                Node block = bp.getBlock();
                                block.setStartLine(lineNumber);
                                checkLooseList(openContainers.get(openContainers.size() - 1));
                                openContainers.get(openContainers.size() - 1).appendChild(block);
                                openContainers.add(block);
                                if (onBlockStarted != null) onBlockStarted.accept(block);
                                activeBlockParsers.put(block, bp);

                                int newIndent = start.getNewIndent();
                                if (newIndent == -1) newIndent = 0; // Default
                                openContainerBlockIndents.add(newIndent);
                            }

                            if (start.getNewIndex() != -1) {
                                i = start.getNewIndex();
                            }
                            startedCustom = true;
                            break; // Restart loop to check for nested blocks
                        }
                    }
                    if (startedCustom) {
                        currentContentDepth = openContainers.size() - 1;
                        continue;
                    }

                    // Check BlockQuote
                    while (i < line.length() && line.charAt(i) == ' ' && indent < 3) {
                        i++;
                        indent++;
                    }
                    if (i < line.length() && line.charAt(i) == '>') {
                        i++;
                        if (i < line.length() && line.charAt(i) == ' ') {
                            i++;
                        }
                        finalizeCurrentLeaf(lineNumber - 1); // Previous line ended the leaf
                        BlockQuote quote = new BlockQuote();
                        quote.setStartLine(lineNumber);
                        checkLooseList(openContainers.get(openContainers.size() - 1));
                        openContainers.get(openContainers.size() - 1).appendChild(quote);
                        openContainers.add(quote);
                        if (onBlockStarted != null) onBlockStarted.accept(quote);
                        openContainerBlockIndents.add(0);
                        currentContentDepth = openContainers.size() - 1;
                        continue;
                    }

                    // Rewind for List check
                    i = startI;

                    // Check Thematic Break - If it IS a thematic break, it cannot be a List Item start.
                    if (isThematicBreak(line.substring(i))) {
                        break;
                    }

                    // Check List Item
                    ListMarker marker = parseListMarker(line, i);
                    if (marker != null) {
                        finalizeCurrentLeaf(lineNumber - 1);

                        boolean matchesCurrentList = false;
                        Node parent = openContainers.get(openContainers.size() - 1);
                        if (parent instanceof BulletList) {
                            BulletList bl = (BulletList) parent;
                            if (!marker.isOrdered && bl.getBulletChar() == marker.bulletChar) {
                                matchesCurrentList = true;
                            }
                        } else if (parent instanceof OrderedList) {
                            OrderedList ol = (OrderedList) parent;
                            if (marker.isOrdered && ol.getDelimiter() == marker.delimiter) {
                                matchesCurrentList = true;
                            }
                        }

                        if (!matchesCurrentList) {
                            // If parent is a List, close it!
                            Node parentNode = openContainers.get(openContainers.size() - 1);
                            if (parentNode instanceof BulletList || parentNode instanceof OrderedList) {
                                Node removed = openContainers.remove(openContainers.size() - 1);
                                if (onBlockClosed != null) onBlockClosed.accept(removed);
                                openContainerBlockIndents.remove(openContainerBlockIndents.size() - 1);
                            }

                            Node newList;
                            if (marker.isOrdered) {
                                OrderedList ol = new OrderedList();
                                ol.setStartLine(lineNumber);
                                ol.setStartNumber(marker.startNumber);
                                ol.setDelimiter(marker.delimiter);
                                newList = ol;
                            } else {
                                BulletList bl = new BulletList();
                                bl.setStartLine(lineNumber);
                                bl.setBulletChar(marker.bulletChar);
                                newList = bl;
                            }
                            checkLooseList(openContainers.get(openContainers.size() - 1));
                            openContainers.get(openContainers.size() - 1).appendChild(newList);
                            openContainers.add(newList);
                            if (onBlockStarted != null) onBlockStarted.accept(newList);
                            openContainerBlockIndents.add(0);
                        }

                        ListItem li = new ListItem();
                        li.setStartLine(lineNumber);

                        // Check for Task List Item
                        String remaining = "";
                        if (marker.nextIndex < line.length()) {
                            remaining = line.substring(marker.nextIndex);
                        }

                        if (remaining.startsWith("[ ] ") || remaining.startsWith("[x] ") || remaining.startsWith("[X] ")) {
                            li.setTask(true);
                            if (remaining.charAt(1) != ' ') {
                                li.setChecked(true);
                            }
                            marker.nextIndex += 4; // [ ] + space
                        } else if (remaining.equals("[ ]") || remaining.equals("[x]") || remaining.equals("[X]")) {
                            li.setTask(true);
                            if (remaining.charAt(1) != ' ') li.setChecked(true);
                            marker.nextIndex += 3; // No trailing space
                        }

                        Node listParent = openContainers.get(openContainers.size() - 1);
                        checkLooseList(listParent);
                        listParent.appendChild(li);
                        openContainers.add(li);
                        if (onBlockStarted != null) onBlockStarted.accept(li);
                        int contentIndent = marker.indent + marker.markerLength + marker.padding;
                        openContainerBlockIndents.add(contentIndent);

                        i = marker.nextIndex;
                        isListMarkerLine = true;
                        continue;
                    }

                    break;
                }
            }

            // Re-calculate contentLine after potential new containers
            contentLine = line.substring(i);
            int indent = countIndent(contentLine);

            // Close List if it's the tip of stack and we didn't add a new ListItem
            if (!openContainers.isEmpty()) {
                Node last = openContainers.get(openContainers.size() - 1);
                if ((last instanceof BulletList || last instanceof OrderedList) && currentLeaf == null) {
                    // We closed the last ListItem, and didn't open a new one.
                    Node removed = openContainers.remove(openContainers.size() - 1);
                    if (onBlockClosed != null) onBlockClosed.accept(removed);
                    openContainerBlockIndents.remove(openContainerBlockIndents.size() - 1);
                }
            }

            // 4. Handle Leaf Blocks

            // Table (Continuation)
            if (inTable) {
                // If line is empty or matches another block start, end table
                if (contentLine.trim().isEmpty()
                        || isBlockQuoteStart(contentLine)
                        || isAtxHeading(contentLine)
                        || isThematicBreak(contentLine)
                        || parseFencedCodeStart(contentLine) != null
                        || parseListMarker(contentLine, 0) != null
                        || getHtmlBlockStartCondition(contentLine) > 0) {

                    Node tableNode = currentLeaf.getParent(); // TableBody -> Table
                    if (tableNode == null && currentLeaf instanceof Table) {
                        // Should not happen if currentLeaf is TableBody
                        // But if currentLeaf is Table (just created)? No, we set currentLeaf to TableBody immediately.
                        // Wait, if currentLeaf is TableBody, its parent SHOULD be Table.
                        // Unless something unlinked it?

                        // Debug: NPE happens at processLine(MarkdownParser.java:740)
                        // Line 740: Node tableNode = currentLeaf.getParent();
                        // currentLeaf is not null (checked by if inTable && currentLeaf instanceof TableBody)
                        // But getParent() might be null if it's not attached?
                        // It should be attached.

                        // Ah, wait. If currentLeaf is TableBody, and we are inside `processLine`,
                        // currentLeaf should be part of the tree.
                    }

                    finalizeCurrentLeaf(lineNumber - 1);
                    inTable = false;
                    tableAlignments.clear();

                    if (onBlockFinalized != null && tableNode instanceof Table) {
                        onBlockFinalized.accept(tableNode);
                    }

                    // Fall through to process as normal block/line
                } else {
                    // Parse Table Row
                    if (currentLeaf instanceof TableBody) {
                        TableRow row = parseTableRow(contentLine, tableAlignments, false);
                        currentLeaf.appendChild(row);
                    } else if (currentLeaf instanceof TableHead) {
                        // Should not happen, we switch to TableBody immediately after parsing header
                    } else if (currentLeaf instanceof Table) {
                        // Create TableBody if not exists?
                        // Actually we should set currentLeaf to TableBody after creating Table
                    }
                    lastLineContentDepth = Integer.MAX_VALUE;
                    return;
                }
            }

            // Fenced Code Block (Continuation)
            if (inFencedCodeBlock) {
                int currentIndent = countIndent(contentLine);
                if (currentIndent < 4 && isClosingFence(contentLine, fenceChar, fenceLength)) {
                    if (currentLeaf instanceof CodeBlock) {
                        ((CodeBlock) currentLeaf).setLiteral(currentLeafContent.toString());
                    }
                    finalizeCurrentLeaf(lineNumber); // Ends on this line
                    // Wait, finalizeCurrentLeaf sets currentLeaf to null.
                    // But we already did logic.
                    // Let's use finalizeCurrentLeaf(lineNumber) but it does logic.
                    // Actually, if we call finalizeCurrentLeaf(lineNumber), it sets literal from content.
                    // But we already set literal in line 510?
                    // Line 510 in original: ((CodeBlock) currentLeaf).setLiteral(currentLeafContent.toString());
                    // My new finalizeCurrentLeaf(lineNumber) does that too.
                    // So I can just call finalizeCurrentLeaf(lineNumber) and return.

                    lastLineContentDepth = Integer.MAX_VALUE;
                    return;
                }

                // Add content
                int strip = fenceIndent;
                int spaces = 0;
                int j = 0;
                while (j < contentLine.length() && spaces < strip && contentLine.charAt(j) == ' ') {
                    spaces++;
                    j++;
                }
                currentLeafContent.append(getSubstringForColumn(originalLine, i + j)).append("\n");
                lastLineContentDepth = Integer.MAX_VALUE;
                return;
            }

            // HTML Block (Continuation)
            if (inHtmlBlock) {
                if (isHtmlBlockEnd(contentLine, htmlBlockCondition)) {
                    if (htmlBlockCondition < 6) {
                        currentLeafContent.append(getSubstringForColumn(originalLine, i)).append("\n");
                    }
                    finalizeCurrentLeaf(lineNumber); // Ends on this line
                    lastLineContentDepth = Integer.MAX_VALUE;
                } else {
                    currentLeafContent.append(getSubstringForColumn(originalLine, i)).append("\n");
                }
                lastLineContentDepth = Integer.MAX_VALUE;
                return;
            }

            // Fenced Code Block (Start)
            if (indent < 4) {
                FencedCodeStart start = parseFencedCodeStart(contentLine);
                if (start != null) {
                    finalizeCurrentLeaf(lineNumber - 1);
                    CodeBlock codeBlock = new CodeBlock("");
                    codeBlock.setStartLine(lineNumber);
                    codeBlock.setInfo(start.info);
                    checkLooseList(openContainers.get(openContainers.size() - 1));
                    openContainers.get(openContainers.size() - 1).appendChild(codeBlock);
                    currentLeaf = codeBlock;
                    inFencedCodeBlock = true;
                    fenceChar = start.fenceChar;
                    fenceLength = start.fenceLength;
                    fenceIndent = start.indent;
                    lastLineContentDepth = Integer.MAX_VALUE;
                    return;
                }
            }

            // HTML Block (Start)
            if (indent < 4) {
                int condition = getHtmlBlockStartCondition(contentLine);
                if (condition > 0) {
                    if (condition == 7 && currentLeaf instanceof Paragraph) {
                        // Type 7 cannot interrupt a paragraph
                    } else {
                        finalizeCurrentLeaf(lineNumber - 1);
                        HtmlBlock htmlBlock = new HtmlBlock("");
                        htmlBlock.setStartLine(lineNumber);
                        checkLooseList(openContainers.get(openContainers.size() - 1));
                        openContainers.get(openContainers.size() - 1).appendChild(htmlBlock);
                        currentLeaf = htmlBlock;
                        inHtmlBlock = true;
                        htmlBlockCondition = condition;

                        if (isHtmlBlockEnd(contentLine, condition)) {
                            currentLeafContent.append(getSubstringForColumn(originalLine, i)).append("\n");
                            finalizeCurrentLeaf(lineNumber);
                        } else {
                            currentLeafContent.append(getSubstringForColumn(originalLine, i)).append("\n");
                        }
                        lastLineContentDepth = Integer.MAX_VALUE;
                        return;
                    }
                }
            }

            // Indented Code Block
            if (indent >= 4) {
                if (inIndentedCodeBlock) {
                    currentLeafContent.append(getSubstringForColumn(originalLine, i + 4)).append("\n");
                    lastLineContentDepth = Integer.MAX_VALUE;
                    return;
                } else if ((lastLineContentDepth == Integer.MAX_VALUE || currentLeaf == null) && !(currentLeaf instanceof Paragraph)) {
                    // Start new indented code block
                    if (!contentLine.trim().isEmpty()) {
                        finalizeCurrentLeaf(lineNumber - 1);
                        CodeBlock codeBlock = new CodeBlock("");
                        codeBlock.setStartLine(lineNumber);
                        checkLooseList(openContainers.get(openContainers.size() - 1));
                        openContainers.get(openContainers.size() - 1).appendChild(codeBlock);
                        currentLeaf = codeBlock;
                        inIndentedCodeBlock = true;
                        currentLeafContent.append(getSubstringForColumn(originalLine, i + 4)).append("\n");
                        lastLineContentDepth = Integer.MAX_VALUE;
                        return;
                    }
                }
            } else if (inIndentedCodeBlock) {
                if (contentLine.trim().isEmpty()) {
                    currentLeafContent.append("\n");
                    lastLineContentDepth = currentContentDepth;
                    return;
                } else {
                    finalizeCurrentLeaf(lineNumber - 1);
                }
            }

            // Table (Start)
            if (indent < 4 && currentLeaf instanceof Paragraph) {
                List<TableCell.Alignment> alignments = parseTableDelimiterRow(contentLine);
                if (alignments != null) {
                    Paragraph p = (Paragraph) currentLeaf;
                    int pStartLine = p.getStartLine();

                    String paragraphContent = currentLeafContent.toString();
                    String[] lines = paragraphContent.split("\n");
                    String headerLine = lines[lines.length - 1];

                    // Verify header cell count matches delimiter cell count (GFM)
                    // We need to parse header row to count cells
                    List<String> headerCells = splitTableCells(headerLine.trim());
                    // splitTableCells handles escaped pipes and code spans correctly now.
                    // But we need to trim the header line first? splitTableCells does trimming internally?
                    // splitTableCells takes "row" string.
                    // parseTableRow does: String s = line.trim(); ... splitTableCells(s);
                    // So we should trim headerLine.

                    if (headerCells.size() != alignments.size()) {
                        // Mismatch! Not a table.
                        // Treat the delimiter row as normal text (paragraph continuation or new paragraph)
                        // Fall through to standard processing.

                        // BUT, we already consumed the line logic?
                        // No, we are in the "Start" block. If we return, we are done.
                        // If we don't return, we fall through to "Setext Heading" etc?
                        // No, "Table (Start)" is one of the blocks.
                        // If we decide it's not a table, we should let it be processed as... what?
                        // A paragraph continuation? Or a new paragraph?
                        // If it's `| --- |`, it might be a thematic break? No, we checked thematic break earlier?
                        // Wait, Thematic Break check is AFTER Table check in my code?
                        // Line 971: Thematic Break. Line 895: Table (Start).
                        // So Table check comes FIRST.

                        // If we fail here, we should fall through.
                        // BUT `alignments != null` means it looks like a table delimiter.
                        // If column count mismatch, it is NOT a table delimiter?
                        // "The header row must match the delimiter row in the number of cells."
                        // So if mismatch, this line is NOT a delimiter row in this context.
                        // So we should pretend `alignments == null`.

                    } else {
                        // Match! Proceed to create table.

                        // Finalize paragraph (remove last line)
                        if (lines.length > 1) {
                            StringBuilder remaining = new StringBuilder();
                            for (int k = 0; k < lines.length - 1; k++) {
                                remaining.append(lines[k]).append("\n");
                            }
                            currentLeafContent.setLength(0);
                            currentLeafContent.append(remaining);
                            finalizeCurrentLeaf(lineNumber - 1);
                        } else {
                            currentLeaf.unlink();
                            currentLeaf = null;
                            currentLeafContent.setLength(0);
                            // No finalizeCurrentLeaf call here because leaf is already gone.
                            // But we might have pending state?
                            // If currentLeaf is null, finalizeCurrentLeaf won't do anything.
                        }

                        // Create Table structure
                        Table table = new Table();
                        table.setStartLine(pStartLine != -1 ? pStartLine : lineNumber - 1);
                        checkLooseList(openContainers.get(openContainers.size() - 1));
                        openContainers.get(openContainers.size() - 1).appendChild(table);

                        TableHead head = new TableHead();
                        table.appendChild(head);

                        TableRow headerRow = parseTableRow(headerLine, alignments, true);
                        head.appendChild(headerRow);

                        TableBody body = new TableBody();
                        table.appendChild(body);

                        currentLeaf = body;
                        inTable = true;
                        tableAlignments = alignments;
                        lastLineContentDepth = Integer.MAX_VALUE;
                        return;
                    }
                }
            }

            // Setext Heading
            if (indent < 4 && currentLeaf instanceof Paragraph && isSetextHeading(contentLine) && matches >= openContainers.size() - 1) {
                if (isLinkReferenceDefinitions(currentLeafContent.toString())) {
                    currentLeafContent.append("\n").append(trimLeading(getSubstringForColumn(originalLine, i)));
                    lastLineContentDepth = Integer.MAX_VALUE;
                    return;
                }

                String headingContent = currentLeafContent.toString();
                int level = contentLine.trim().startsWith("=") ? 1 : 2;
                Heading heading = new Heading(level);
                heading.appendChild(new Text(headingContent.trim()));
                heading.setStartLine(currentLeaf.getStartLine());
                heading.setEndLine(lineNumber);

                Node parent = openContainers.get(openContainers.size() - 1);
                currentLeaf.unlink(); // Remove paragraph
                parent.appendChild(heading);

                if (onBlockFinalized != null) onBlockFinalized.accept(heading);

                currentLeaf = null;
                currentLeafContent.setLength(0);
                lastLineContentDepth = Integer.MAX_VALUE;
                return;
            }

            // Thematic Break
            if (indent < 4 && isThematicBreak(contentLine)) {
                finalizeCurrentLeaf(lineNumber - 1);
                ThematicBreak tb = new ThematicBreak();
                tb.setStartLine(lineNumber);
                tb.setEndLine(lineNumber);
                checkLooseList(openContainers.get(openContainers.size() - 1));
                openContainers.get(openContainers.size() - 1).appendChild(tb);

                if (onBlockFinalized != null) onBlockFinalized.accept(tb);

                lastLineContentDepth = Integer.MAX_VALUE;
                return;
            }

            // ATX Heading
            if (indent < 4 && isAtxHeading(contentLine)) {
                finalizeCurrentLeaf(lineNumber - 1);
                Node heading = parseAtxHeading(contentLine);
                heading.setStartLine(lineNumber);
                heading.setEndLine(lineNumber);
                checkLooseList(openContainers.get(openContainers.size() - 1));
                openContainers.get(openContainers.size() - 1).appendChild(heading);

                if (onBlockFinalized != null) {
                    onBlockFinalized.accept(heading);
                }

                lastLineContentDepth = Integer.MAX_VALUE;
                return;
            }

            // Blank Line
            if (contentLine.trim().isEmpty()) {
                if (!openContainers.isEmpty()) {
                    Node last = openContainers.get(openContainers.size() - 1);
                    if (!isListMarkerLine && last instanceof ListItem && last.getFirstChild() == null) {
                        openContainers.remove(openContainers.size() - 1);
                        openContainerBlockIndents.remove(openContainerBlockIndents.size() - 1);
                    }
                }

                if (currentLeaf instanceof Paragraph) {
                    finalizeCurrentLeaf(lineNumber - 1);
                }

                if (lastMatchedContainerHadMarker || isListMarkerLine) {
                    lastLineContentDepth = Integer.MAX_VALUE;
                } else {
                    lastLineContentDepth = currentContentDepth;
                }
                return;
            }

            // Paragraph
            if (currentLeaf instanceof Paragraph) {
                currentLeafContent.append("\n").append(trimLeading(getSubstringForColumn(originalLine, i)));
                lastLineContentDepth = Integer.MAX_VALUE;
            } else {
                finalizeCurrentLeaf(lineNumber - 1);
                Paragraph p = new Paragraph();
                p.setStartLine(lineNumber);
                checkLooseList(openContainers.get(openContainers.size() - 1));
                openContainers.get(openContainers.size() - 1).appendChild(p);
                currentLeaf = p;
                currentLeafContent.append(trimLeading(getSubstringForColumn(originalLine, i)));
                lastLineContentDepth = Integer.MAX_VALUE;
            }
        }

        private String trimLeading(String s) {
            int i = 0;
            while (i < s.length() && Character.isWhitespace(s.charAt(i))) {
                i++;
            }
            return s.substring(i);
        }

        void checkLooseList(Node list) {
            boolean isBlank = lastLineContentDepth != Integer.MAX_VALUE;
            if (isBlank) {
                markLoose(list);
            }
        }

        void markLoose(Node list) {
            if (list instanceof BulletList) {
                if (list.getFirstChild() != null) ((BulletList) list).setTight(false);
            } else if (list instanceof OrderedList) {
                if (list.getFirstChild() != null) ((OrderedList) list).setTight(false);
            } else if (list instanceof ListItem) {
                if (list.getFirstChild() != null) {
                    Node parent = list.getParent();
                    if (parent != null) {
                        markLoose(parent);
                    }
                }
            }
        }

        private static final Pattern DISALLOWED_TAG_PATTERN = Pattern.compile("(?i)<(/?)((?:title|textarea|style|xmp|iframe|noembed|noframes|script|plaintext)(?=[\\s/>]|$))");

        private String filterDisallowedTags(String content) {
            Matcher matcher = DISALLOWED_TAG_PATTERN.matcher(content);
            StringBuffer sb = new StringBuffer();
            while (matcher.find()) {
                matcher.appendReplacement(sb, "&lt;$1$2");
            }
            matcher.appendTail(sb);
            return sb.toString();
        }

        void finalizeCurrentLeaf(int endLine) {
            if (currentLeaf != null) {
                currentLeaf.setEndLine(endLine);
                if (currentLeaf instanceof Paragraph) {
                    ((Paragraph) currentLeaf).appendChild(new Text(currentLeafContent.toString()));
                } else if (currentLeaf instanceof CodeBlock) {
                    if (inFencedCodeBlock) {
                        ((CodeBlock) currentLeaf).setLiteral(currentLeafContent.toString());
                    } else if (inIndentedCodeBlock) {
                        // Strip trailing newlines from indented code block
                        String literal = currentLeafContent.toString();
                        int end = literal.length();
                        while (end > 0 && literal.charAt(end - 1) == '\n') {
                            end--;
                        }
                        if (end < literal.length()) {
                            literal = literal.substring(0, end) + "\n";
                        }
                        ((CodeBlock) currentLeaf).setLiteral(literal);
                    }
                } else if (currentLeaf instanceof HtmlBlock) {
                    String content = currentLeafContent.toString();
                    if (options.isGfm()) {
                        content = filterDisallowedTags(content);
                    }
                    ((HtmlBlock) currentLeaf).setLiteral(content);
                }
                Node finalized = currentLeaf;
                currentLeaf = null;
                currentLeafContent.setLength(0);
                inFencedCodeBlock = false;
                inIndentedCodeBlock = false;
                inHtmlBlock = false;
                inTable = false; // Fix: Ensure table mode is exited when leaf is finalized (e.g. by interrupt)

                if (onBlockFinalized != null) {
                    onBlockFinalized.accept(finalized);
                }
            }
        }

        void finalizeBlock(Document doc, int lastLineNumber) {
            if (inTable && currentLeaf instanceof TableBody) {
                Node tableNode = currentLeaf.getParent();
                finalizeCurrentLeaf(lastLineNumber - 1);
                if (onBlockFinalized != null && tableNode instanceof Table) {
                    onBlockFinalized.accept(tableNode);
                }
                inTable = false;
            } else {
                finalizeCurrentLeaf(lastLineNumber - 1);
            }
            // Close all containers? 
            // They are already in the tree.
            if (onBlockClosed != null) {
                for (int i = openContainers.size() - 1; i >= 0; i--) {
                    onBlockClosed.accept(openContainers.get(i));
                }
            }
            openContainers.clear();
        }

        boolean isTableDelimiterRow(String line) {
            return parseTableDelimiterRow(line) != null;
        }

        List<TableCell.Alignment> parseTableDelimiterRow(String line) {
            String s = line.trim();

            if (!s.contains("|") && s.matches("^[-=\\s]+$")) {
                return null; // Looks like Setext
            }

            // GFM Spec: A delimiter row consists of cells of dashes -, and optionally, a leading or trailing pipe,
            // and optionally a colon : at the beginning or end of each cell.

            // It MUST contain at least one pipe, OR if it doesn't, it must not look like setext.
            // But wait, GFM table rows don't strictly require leading/trailing pipes.
            // However, a delimiter row usually involves pipes.

            // Issue with Example 200: | f\|oo  | \n | ------ |
            // The delimiter row | ------ | is standard.
            // But the first row has escaped pipe.

            // Issue with Example 203: | abc | def | \n | --- | \n | bar |
            // This is valid. The second cell in delimiter is missing?
            // "The delimiter row consists of ... one or more cells"
            // "The header row must match the delimiter row in the number of cells."

            if (!s.contains("-")) return null; // Must have at least one dash

            // Remove leading/trailing pipes if present
            if (s.startsWith("|")) s = s.substring(1);
            if (s.endsWith("|") && !s.endsWith("\\|")) s = s.substring(0, s.length() - 1);

            // Use splitTableCells instead of simple split to handle escaped pipes if any (though delimiter row shouldn't have escaped pipes usually)
            // But delimiter row cells only contain -, : and space. So simple split by | is fine?
            // "Cells in the delimiter row can contain optional whitespace"

            String[] parts = s.split("\\|");
            List<TableCell.Alignment> alignments = new ArrayList<>();

            for (String part : parts) {
                String cell = part.trim();
                // Empty cell in delimiter row?
                // | --- | |
                // If a cell is empty (whitespace only), it's not a valid delimiter cell.
                // Delimiter cell must contain at least one dash.

                if (cell.isEmpty()) {
                    // If we have "||", split gives empty string.
                    // This is valid if it's meant to be an empty cell? No, delimiter cells MUST have dashes.
                    return null;
                }

                boolean left = cell.startsWith(":");
                boolean right = cell.endsWith(":");

                // Strip colons to check for dashes
                String content = cell;
                if (left) content = content.substring(1);
                if (right) content = content.substring(0, content.length() - 1);

                if (content.isEmpty()) return null;
                // Check if content consists only of dashes
                for (int i = 0; i < content.length(); i++) {
                    if (content.charAt(i) != '-') return null;
                }

                if (left && right) alignments.add(TableCell.Alignment.CENTER);
                else if (left) alignments.add(TableCell.Alignment.LEFT);
                else if (right) alignments.add(TableCell.Alignment.RIGHT);
                else alignments.add(TableCell.Alignment.NONE);
            }

            return alignments;
        }

        TableRow parseTableRow(String line, List<TableCell.Alignment> alignments, boolean isHeader) {
            TableRow row = new TableRow();
            String s = line.trim();
            // Don't strip pipes here anymore, splitTableCells handles it
            // if (s.startsWith("|")) s = s.substring(1);

            List<String> cells = splitTableCells(s);

            // GFM: If there are less cells than columns in header, fill with empty cells.
            // If there are more, ignore the extra ones.
            // Wait, spec says:
            // "The header row must match the delimiter row in the number of cells."
            // "Body rows ... excess cells are ignored, missing cells are inserted."

            // Wait, alignments size is determined by delimiter row.
            // Header row size should match delimiter row?
            // "The header row consists of the line before the delimiter row."

            int targetSize = alignments.size();

            for (int i = 0; i < targetSize; i++) {
                TableCell cell = new TableCell();
                cell.setHeader(isHeader);
                cell.setAlignment(alignments.get(i));

                String content = (i < cells.size()) ? cells.get(i).trim() : "";
                cell.appendChild(new Text(content));
                row.appendChild(cell);
            }

            return row;
        }

        int findCodeSpanEnd(String row, int start, int runLength) {
            int i = start;
            while (i < row.length()) {
                if (row.charAt(i) == '`') {
                    int s = i;
                    while (i < row.length() && row.charAt(i) == '`') i++;
                    int len = i - s;
                    if (len == runLength) return s;
                    i = s;
                } else if (row.charAt(i) == '\\') {
                    i += 2;
                } else {
                    i++;
                }
            }
            return -1;
        }

        List<String> splitTableCells(String row) {
            List<String> cells = new ArrayList<>();
            StringBuilder current = new StringBuilder();
            boolean escaped = false;

            int i = 0;
            // Trim leading/trailing pipe logic should be consistent
            if (row.startsWith("|")) i++;
            int limit = row.length();
            if (row.endsWith("|") && !row.endsWith("\\|")) limit--;

            boolean lastCharWasDelimiter = false;

            while (i < limit) {
                char c = row.charAt(i);
                lastCharWasDelimiter = false;

                if (escaped) {
                    current.append(c);
                    escaped = false;
                    i++;
                    continue;
                }

                if (c == '\\') {
                    // Check if next char is pipe, if so, escape it. 
                    // GFM spec: \| is a literal pipe.
                    if (i + 1 < limit && row.charAt(i + 1) == '|') {
                        current.append('|');
                        i += 2;
                        continue;
                    }
                    // Else, keep backslash (will be handled by inline parser)
                    current.append(c);
                    // escaped = true; // DO NOT set escaped=true here.
                    // If we set escaped=true, the next char will be appended blindly.
                    // But we want `\` to be treated as a literal backslash by THIS parser loop,
                    // and let InlineParser handle it later.
                    // UNLESS the next char is a pipe, which we handled above.
                    // Wait, if we have `\\`, we append `\` and then next char is `\`.
                    // If we set escaped=true, next `\` is appended. Result `\\`. InlineParser sees `\\` -> `\`. Correct.

                    // If we have `\a`, we append `\`. Next char `a`.
                    // If we set escaped=true, next `a` is appended. Result `\a`. InlineParser sees `\a` -> `a` (escaped a? No, only punctuation).
                    // Actually `\a` is not an escape in Markdown usually.

                    // The issue in Example 200 is `| b `\|` az |`.
                    // Here `\|` is INSIDE code span.
                    // My parser handles code span FIRST.
                    // `if (c == '`')` block comes AFTER `if (c == '\\')`.
                    // This is WRONG priority. Backslash escaping should NOT happen inside code spans.
                    // BUT, to detect code span, we need to scan for backticks.
                    // And backticks can be escaped? No, backticks in code span delimiters cannot be escaped.
                    // But inside code span, backslash is literal.

                    // So we must check for code span start BEFORE checking for backslash escape?
                    // NO. `\` escapes backticks? `\` escapes `|`?
                    // "Code spans are delimited by backticks. Backslash escapes are NOT active in code spans."

                    // So if we see a backtick, we should check if it starts a code span.
                    // BUT, `\` can escape a backtick to prevent it from starting a code span?
                    // "Backslash escapes are allowed... including escaped backticks?"
                    // Example: \` is not a code span start.

                    // So `if (c == '\\')` MUST come first to handle `\` escaping a backtick.
                    // But inside `if (c == '\\')`, if we consume the backslash and the next char, we effectively "escape" it.

                    // In Example 200: `b `\|` az`.
                    // i points to first backtick.
                    // It should enter `if (c == '`')`.
                    // Inside code span logic, it consumes `\|`.

                    // So why did my code fail?
                    // Because `if (c == '\\')` block is BEFORE `if (c == '`')`.
                    // Wait, `|` is NOT inside code span in `b `\|` az`?
                    // Markdown: `| b `\|` az |`
                    // The cell content is ` b `\|` az `.
                    // The backticks wrap `\|`.
                    // So `\|` IS inside code span.
                    // And inside code span, `\` is literal.
                    // So content is `\|`.
                    // HTML output should be `<code>\|</code>`?
                    // Example 200 Expected: `b <code>|</code> az`.
                    // Wait. `\|` in code span means literal `|`?
                    // Spec says: "Backslash escapes are never active in code spans".
                    // So `\|` should be `\|`.
                    // Why does Expected say `|`?
                    // Ah, the example markdown is: `| b `\|` az |`
                    // Wait, if I write `|` in a table cell, it ends the cell.
                    // So to put a pipe in a code span in a table cell, I MUST escape it?
                    // GFM Spec "Tables (extension)":
                    // "Pipes can be escaped with backslash"
                    // "Block-level structure... takes precedence over inline structure."
                    // "Splitting into cells... happens BEFORE inline parsing."

                    // So, `splitTableCells` MUST handle `\|` as "escaped pipe" (i.e. treat as content, not delimiter)
                    // EVEN IF it looks like it's inside a code span?
                    // "Parsing of the row into cells happens before parsing of inline content."
                    // So `splitTableCells` doesn't know about code spans?
                    // WRONG. If it doesn't know about code spans, it might split inside a code span.
                    // Example: `| `a|b` |` -> Cell 1: ` `a|b` `. Pipe inside code span should NOT split cell.
                    // So `splitTableCells` MUST know about code spans.

                    // So:
                    // 1. Scan for code spans.
                    // 2. Scan for escaped pipes `\|`.
                    // 3. Scan for delimiters `|`.

                    // In `| b `\|` az |`:
                    // We encounter first backtick. We consume until next backtick.
                    // Content `\|`.
                    // But wait, does `\|` count as escaped pipe inside code span?
                    // If I write `|` inside code span, does it split cell?
                    // `| `a|b` |` -> Pipe inside code span does NOT split.
                    // `| `a\|b` |` -> Pipe inside code span does NOT split.

                    // So my logic of handling code spans seems correct: consume everything inside backticks.
                    // So `\|` is consumed as part of code span content.
                    // So cell content becomes ` b `\|` az `.
                    // Then InlineParser parses this.
                    // Code span parsing: content is `\|`.
                    // Rendered as `<code>\|</code>`.

                    // BUT Expected is `<code>|</code>`.
                    // This implies that `\|` was unescaped to `|` BEFORE code span parsing?
                    // OR that `\|` in a table cell is ALWAYS unescaped to `|`, even inside code spans?
                    // "To include a pipe | in a cell, it must be escaped: \|."
                    // This rule applies to the raw line text, BEFORE any other processing?

                    // If so, `splitTableCells` should convert `\|` to `|`?
                    // If I convert `\|` to `|`, then ` b `|` az `.
                    // Inline parser sees ` b `|` az `. Code span `|`. Render `<code>|</code>`.
                    // THIS MATCHES EXPECTED!

                    // So: `splitTableCells` should REPLACE `\|` with `|` in the output string?
                    // Currently I do: `current.append('|'); i+=2;`
                    // This appends `|`.
                    // So `current` becomes ` b `|` az `.
                    // Wait, my code does exactly this!
                    
                    /*
                    if (i + 1 < limit && row.charAt(i + 1) == '|') {
                        current.append('|');
                        i += 2;
                        continue; 
                    }
                    */

                    // So why did Actual output show `<code>\|</code>`?
                    // Maybe because I didn't reach that block?
                    // In `| b `\|` az |`:
                    // i hits first backtick. `if (c == '`')` block runs.
                    // It consumes the code span.
                    // Inside the code span, there is `\|`.
                    // My code span logic: `current.append(row.substring(i, match));`
                    // It copies `\|` verbatim!

                    // AHA!
                    // If `\|` is inside a code span, my code span logic swallows it without unescaping.
                    // But apparently, even inside a code span (in the context of a table row), `\|` should be treated as an escaped pipe and unescaped?
                    // This is weird because "Backslash escapes are never active in code spans".
                    // But Table parsing happens BEFORE Inline parsing.
                    // So we are stripping the "Table encoding" first.
                    // The "Table encoding" says: `\|` represents a `|` character.
                    // This decoding must happen BEFORE we pass the string to InlineParser.
                    // And since we are building the cell string manually, we must do it.

                    // So, even inside `if (c == '`')` block, we must handle `\|` -> `|`?
                    // Yes!

                    escaped = true;
                    i++;
                    continue;
                }

                if (c == '`') {
                    int start = i;
                    while (i < limit && row.charAt(i) == '`') i++;
                    int runLength = i - start;

                    int match = findCodeSpanEnd(row, i, runLength);
                    if (match != -1) {
                        for (int k = 0; k < runLength; k++) current.append('`');
                        // Process code span content to unescape \| to |
                        String content = row.substring(i, match);

                        // Manually unescape \| to |
                        // We can iterate content and append.
                        // But wait, `\` only escapes `|`?
                        // "Backslash escapes are never active in code spans."
                        // BUT `\|` in table cell must be unescaped to `|`.
                        // So we replace `\|` with `|`.
                        // What about `\\`? "Backslashes are also escaped as usual".
                        // So `\\` -> `\`.

                        StringBuilder unescapedContent = new StringBuilder();
                        int p = 0;
                        while (p < content.length()) {
                            char ch = content.charAt(p);
                            if (ch == '\\' && p + 1 < content.length() && content.charAt(p + 1) == '|') {
                                unescapedContent.append('|');
                                p += 2;
                            } else {
                                unescapedContent.append(ch);
                                p++;
                            }
                        }
                        current.append(unescapedContent.toString());

                        for (int k = 0; k < runLength; k++) current.append('`');
                        i = match + runLength;
                    } else {
                        for (int k = 0; k < runLength; k++) current.append('`');
                    }
                    continue;
                }

                if (c == '|') {
                    cells.add(current.toString());
                    current.setLength(0);
                    lastCharWasDelimiter = true;
                    i++;
                    continue;
                }

                current.append(c);
                i++;
            }
            // Add the last cell, UNLESS the row was empty and we didn't add anything?
            // If row was "|", i=1, limit=0. loop skipped. cells.add("") -> size 1. Correct.
            // If row was "abc", i=0, limit=3. loop runs. cells.add("abc"). Correct.
            // If row was "", i=0, limit=0. loop skipped. cells.add(""). Correct.
            cells.add(current.toString());

            return cells;
        }

        static class ListMarker {
            boolean isOrdered;
            char bulletChar;
            char delimiter;
            int startNumber;
            int markerLength;
            int padding;
            int indent;
            int nextIndex;
        }

        ListMarker parseListMarker(String line, int index) {
            int i = index;
            int indent = 0;
            while (i < line.length() && line.charAt(i) == ' ' && indent < 3) {
                i++;
                indent++;
            }
            if (i >= line.length()) return null;

            char c = line.charAt(i);

            // Bullet
            if (c == '-' || c == '+' || c == '*') {
                if (i + 1 >= line.length() || line.charAt(i + 1) == ' ' || line.charAt(i + 1) == '\t') {
                    ListMarker m = new ListMarker();
                    m.isOrdered = false;
                    m.bulletChar = c;
                    m.markerLength = 1;
                    m.indent = indent;
                    return calculateListMarkerIndent(line, i, 1, indent, m);
                }
            }

            // Ordered
            if (Character.isDigit(c)) {
                int start = i;
                while (i < line.length() && Character.isDigit(line.charAt(i))) {
                    i++;
                    if (i - start > 9) return null;
                }
                if (i >= line.length()) return null;

                char delim = line.charAt(i);
                if (delim != '.' && delim != ')') return null;

                if (i + 1 >= line.length() || line.charAt(i + 1) == ' ' || line.charAt(i + 1) == '\t') {
                    ListMarker m = new ListMarker();
                    m.isOrdered = true;
                    m.startNumber = Integer.parseInt(line.substring(start, i));
                    m.delimiter = delim;
                    m.markerLength = (i - start) + 1;
                    m.indent = indent;
                    return calculateListMarkerIndent(line, start, m.markerLength, indent, m);
                }
            }

            return null;
        }

        ListMarker calculateListMarkerIndent(String line, int markerStart, int markerLength, int markerIndent, ListMarker m) {
            int i = markerStart + markerLength;

            if (i >= line.length()) {
                m.padding = 1;
                m.nextIndex = i;
                return m;
            }

            int spaces = 0;
            while (i < line.length() && line.charAt(i) == ' ') {
                spaces++;
                i++;
            }

            if (i >= line.length()) {
                m.padding = 1;
                m.nextIndex = i;
                return m;
            }

            if (spaces >= 1 && spaces <= 4) {
                m.padding = spaces;
                m.nextIndex = i;
            } else if (spaces > 4) {
                m.padding = 1;
                m.nextIndex = markerStart + markerLength + 1;
            } else {
                m.padding = 0;
                m.nextIndex = i;
            }

            return m;
        }

        int countIndent(String line) {
            int count = 0;
            for (int i = 0; i < line.length(); i++) {
                if (line.charAt(i) == ' ') count++;
                else break;
            }
            return count;
        }

        boolean isSetextHeading(String line) {
            String s = line.trim();
            if (s.isEmpty()) return false;
            char c = s.charAt(0);
            if (c != '=' && c != '-') return false;
            for (int i = 1; i < s.length(); i++) {
                if (s.charAt(i) != c) return false;
            }
            return true;
        }

        static class FencedCodeStart {
            char fenceChar;
            int fenceLength;
            int indent;
            String info;
        }

        FencedCodeStart parseFencedCodeStart(String line) {
            int i = 0;
            int indent = 0;
            while (i < line.length() && line.charAt(i) == ' ') {
                indent++;
                i++;
            }
            if (i >= line.length()) return null;
            char c = line.charAt(i);
            if (c != '`' && c != '~') return null;

            int start = i;
            while (i < line.length() && line.charAt(i) == c) {
                i++;
            }
            int length = i - start;
            if (length < 3) return null;

            // Info string
            String info = unescape(line.substring(i).trim());
            if (c == '`' && info.contains("`")) return null;

            FencedCodeStart res = new FencedCodeStart();
            res.fenceChar = c;
            res.fenceLength = length;
            res.indent = indent;
            res.info = info;
            return res;
        }

        boolean isClosingFence(String line, char fenceChar, int minLength) {
            int i = 0;
            while (i < line.length() && line.charAt(i) == ' ') {
                i++;
            }
            if (i >= line.length()) return false;
            if (line.charAt(i) != fenceChar) return false;

            int start = i;
            while (i < line.length() && line.charAt(i) == fenceChar) {
                i++;
            }
            int length = i - start;
            if (length < minLength) return false;

            while (i < line.length()) {
                if (line.charAt(i) != ' ') return false;
                i++;
            }
            return true;
        }

        boolean isBlockQuoteStart(String line) {
            int i = 0;
            int indent = 0;
            while (i < line.length() && line.charAt(i) == ' ' && indent < 3) {
                i++;
                indent++;
            }
            return i < line.length() && line.charAt(i) == '>';
        }

        boolean isThematicBreak(String line) {
            int i = 0;
            while (i < line.length() && line.charAt(i) == ' ') i++;
            if (i >= line.length()) return false;
            char c = line.charAt(i);
            if (c != '-' && c != '_' && c != '*') return false;

            int count = 0;
            while (i < line.length()) {
                char current = line.charAt(i);
                if (current == c) {
                    count++;
                } else if (current != ' ') {
                    return false;
                }
                i++;
            }
            return count >= 3;
        }

        boolean isAtxHeading(String line) {
            int i = 0;
            while (i < line.length() && line.charAt(i) == ' ') i++;
            if (i >= line.length() || line.charAt(i) != '#') return false;
            int start = i;
            while (i < line.length() && line.charAt(i) == '#') i++;
            int level = i - start;
            if (level < 1 || level > 6) return false;
            if (i < line.length() && line.charAt(i) != ' ') return false;
            return true;
        }

        Node parseAtxHeading(String line) {
            int i = 0;
            while (i < line.length() && line.charAt(i) == ' ') i++;
            int start = i;
            while (i < line.length() && line.charAt(i) == '#') i++;
            int level = i - start;

            String content = line.substring(i).trim();
            int end = content.length() - 1;
            while (end >= 0 && content.charAt(end) == '#') {
                end--;
            }
            if (end < content.length() - 1) {
                // Only strip if preceded by space or start of string
                if (end < 0 || content.charAt(end) == ' ') {
                    content = content.substring(0, end + 1).trim();
                }
            }

            Heading h = new Heading(level);
            h.appendChild(new Text(content));
            return h;
        }

        int getHtmlBlockStartCondition(String line) {
            String s = line.trim();
            String lower = s.toLowerCase();
            if (lower.startsWith("<script") || lower.startsWith("<pre") || lower.startsWith("<style") || lower.startsWith("<textarea"))
                return 1;
            if (s.startsWith("<!--")) return 2;
            if (s.startsWith("<?")) return 3;
            if (s.startsWith("<!") && s.length() >= 3 && Character.isUpperCase(s.charAt(2))) return 4;
            if (s.startsWith("<![CDATA[")) return 5;

            if (s.startsWith("<")) {
                if (s.length() > 1 && s.charAt(1) != ' ') {
                    int i = 1;
                    if (s.charAt(i) == '/') i++;
                    int startName = i;
                    while (i < s.length() && Character.isLetterOrDigit(s.charAt(i))) i++;
                    String name = s.substring(startName, i).toLowerCase();

                    if (name.isEmpty()) return 0;

                    if (isBlockTag(name)) {
                        return 6;
                    }

                    if (isCompleteTagAndRestWhitespace(s, startName)) {
                        return 7;
                    }
                }
            }
            return 0;
        }

        boolean isCompleteTagAndRestWhitespace(String line, int startName) {
            int i = startName;
            // Skip name
            while (i < line.length() && (Character.isLetterOrDigit(line.charAt(i)) || line.charAt(i) == '-')) i++;

            boolean isClosing = (startName > 1 && line.charAt(startName - 1) == '/');

            if (isClosing) {
                while (i < line.length() && Character.isWhitespace(line.charAt(i))) i++;
                if (i < line.length() && line.charAt(i) == '>') {
                    i++;
                    while (i < line.length()) {
                        if (!Character.isWhitespace(line.charAt(i))) return false;
                        i++;
                    }
                    return true;
                }
                return false;
            }

            // Start Tag
            boolean requireWhitespace = true; // After tag name, whitespace required before attribute
            while (i < line.length()) {
                // Consume whitespace
                boolean hasWhitespace = false;
                while (i < line.length() && Character.isWhitespace(line.charAt(i))) {
                    hasWhitespace = true;
                    i++;
                }

                if (i >= line.length()) return false;
                char c = line.charAt(i);

                if (c == '/') {
                    i++;
                    if (i < line.length() && line.charAt(i) == '>') {
                        i++;
                        while (i < line.length()) {
                            if (!Character.isWhitespace(line.charAt(i))) return false;
                            i++;
                        }
                        return true;
                    }
                    return false;
                }

                if (c == '>') {
                    i++;
                    while (i < line.length()) {
                        if (!Character.isWhitespace(line.charAt(i))) return false;
                        i++;
                    }
                    return true;
                }

                // Attribute
                if (requireWhitespace && !hasWhitespace) return false;

                // Attribute Name
                int attrStart = i;
                while (i < line.length()) {
                    char ch = line.charAt(i);
                    if (Character.isLetterOrDigit(ch) || ch == '_' || ch == '-' || ch == '.' || ch == ':') {
                        i++;
                    } else {
                        break;
                    }
                }
                if (i == attrStart) return false; // Invalid attribute start char

                // Attribute Value
                int afterName = i;
                while (i < line.length() && Character.isWhitespace(line.charAt(i))) i++;

                if (i < line.length() && line.charAt(i) == '=') {
                    i++;
                    while (i < line.length() && Character.isWhitespace(line.charAt(i))) i++;
                    if (i >= line.length()) return false;

                    char valStart = line.charAt(i);
                    if (valStart == '"' || valStart == '\'') {
                        char quote = valStart;
                        i++;
                        boolean foundClose = false;
                        while (i < line.length()) {
                            if (line.charAt(i) == quote) {
                                foundClose = true;
                                i++;
                                break;
                            }
                            i++;
                        }
                        if (!foundClose) return false;
                    } else {
                        // Unquoted
                        if (valStart == '"' || valStart == '\'' || valStart == '=' || valStart == '<' || valStart == '>' || valStart == '`')
                            return false;
                        while (i < line.length()) {
                            char ch = line.charAt(i);
                            if (Character.isWhitespace(ch) || ch == '"' || ch == '\'' || ch == '=' || ch == '<' || ch == '>' || ch == '`') {
                                break;
                            }
                            i++;
                        }
                    }
                } else {
                    // Boolean attribute (no value)
                    i = afterName; // Backtrack to just after name (and whitespace)
                }

                requireWhitespace = true;
            }

            return false;
        }

        boolean isBlockTag(String name) {
            return name.equals("address") || name.equals("article") || name.equals("aside") || name.equals("base") ||
                    name.equals("basefont") || name.equals("blockquote") || name.equals("body") || name.equals("caption") ||
                    name.equals("center") || name.equals("col") || name.equals("colgroup") || name.equals("dd") ||
                    name.equals("details") || name.equals("dialog") || name.equals("dir") || name.equals("div") ||
                    name.equals("dl") || name.equals("dt") || name.equals("fieldset") || name.equals("figcaption") ||
                    name.equals("figure") || name.equals("footer") || name.equals("form") || name.equals("frame") ||
                    name.equals("frameset") || name.equals("h1") || name.equals("h2") || name.equals("h3") ||
                    name.equals("h4") || name.equals("h5") || name.equals("h6") || name.equals("head") ||
                    name.equals("header") || name.equals("hr") || name.equals("html") || name.equals("iframe") ||
                    name.equals("legend") || name.equals("li") || name.equals("link") || name.equals("main") ||
                    name.equals("menu") || name.equals("menuitem") || name.equals("meta") || name.equals("nav") ||
                    name.equals("noframes") || name.equals("ol") || name.equals("optgroup") || name.equals("option") ||
                    name.equals("p") || name.equals("param") || name.equals("section") || name.equals("source") ||
                    name.equals("summary") || name.equals("table") || name.equals("tbody") || name.equals("td") ||
                    name.equals("tfoot") || name.equals("th") || name.equals("thead") || name.equals("title") ||
                    name.equals("tr") || name.equals("track") || name.equals("ul");
        }

        boolean isHtmlBlockEnd(String line, int condition) {
            if (condition == 1) {
                return line.toLowerCase().contains("</script>") || line.toLowerCase().contains("</pre>") || line.toLowerCase().contains("</style>") || line.toLowerCase().contains("</textarea>");
            }
            if (condition == 2) return line.contains("-->");
            if (condition == 3) return line.contains("?>");
            if (condition == 4) return line.contains(">");
            if (condition == 5) return line.contains("]]>");
            if (condition == 6 || condition == 7) return line.trim().isEmpty();
            return false;
        }
    }

    public static boolean isLinkReferenceDefinitions(String text) {
        int index = 0;
        while (index < text.length()) {
            while (index < text.length() && Character.isWhitespace(text.charAt(index))) {
                index++;
            }
            if (index >= text.length()) break;

            int start = index;
            // Label
            if (text.charAt(index) != '[') return false;
            index++;
            int labelStart = index;
            int labelEnd = -1;
            int bracketDepth = 1;
            while (index < text.length()) {
                char c = text.charAt(index);
                if (c == '\\') {
                    index += 2;
                    continue;
                }
                if (c == '[')
                    return false; // Nested bracket in label? (Wait, label can contain brackets if escaped? No.)
                if (c == ']') {
                    bracketDepth--;
                    if (bracketDepth == 0) {
                        labelEnd = index;
                        break;
                    }
                }
                index++;
            }
            if (labelEnd == -1) return false;

            String label = text.substring(labelStart, labelEnd);
            if (label.trim().isEmpty()) return false;

            index = labelEnd + 1;
            if (index >= text.length() || text.charAt(index) != ':') return false;
            index++;

            while (index < text.length() && Character.isWhitespace(text.charAt(index))) index++;

            // Destination
            boolean inAngleBrackets = false;
            if (index < text.length() && text.charAt(index) == '<') {
                inAngleBrackets = true;
                index++;
                while (index < text.length() && text.charAt(index) != '>') {
                    if (text.charAt(index) == '\\') index += 2;
                    else index++;
                }
                if (index >= text.length()) return false;
                index++;
            } else {
                int destStart = index;
                while (index < text.length() && !Character.isWhitespace(text.charAt(index))) {
                    if (text.charAt(index) == '\\') index += 2;
                    else index++;
                }
                if (index == destStart) return false; // Empty destination
            }

            // Optional Title
            if (index < text.length() && Character.isWhitespace(text.charAt(index))) {
                int lookahead = index;
                while (lookahead < text.length() && Character.isWhitespace(text.charAt(lookahead))) lookahead++;
                if (lookahead < text.length()) {
                    char open = text.charAt(lookahead);
                    if (open == '"' || open == '\'' || open == '(') {
                        char close = (open == '(') ? ')' : open;
                        int titleStart = lookahead + 1;
                        int p = titleStart;
                        boolean foundClose = false;
                        while (p < text.length()) {
                            if (text.charAt(p) == '\\') {
                                p += 2;
                                continue;
                            }
                            if (text.charAt(p) == close) {
                                foundClose = true;
                                break;
                            }
                            p++;
                        }
                        if (foundClose) {
                            // Check trailing
                            int afterTitle = p + 1;
                            boolean valid = true;
                            while (afterTitle < text.length()) {
                                char c = text.charAt(afterTitle);
                                if (c == '\n' || c == '\r') break;
                                if (!Character.isWhitespace(c)) {
                                    valid = false;
                                    break;
                                }
                                afterTitle++;
                            }
                            if (valid) {
                                String title = text.substring(titleStart, p);
                                if (!title.matches("(?s).*\\n\\s*\\n.*")) {
                                    index = afterTitle;
                                }
                            }
                        }
                    }
                }
            }

            // Trailing checks
            boolean validLineEnd = true;
            while (index < text.length()) {
                char c = text.charAt(index);
                if (c == '\n' || c == '\r') break;
                if (!Character.isWhitespace(c)) {
                    validLineEnd = false;
                    break;
                }
                index++;
            }
            if (!validLineEnd) return false;
        }
        return true;
    }

    private void extractLinkReferenceDefinitions(Document doc) {
        visitAndExtract(doc, doc);
    }

    private void visitAndExtract(Node node, Document doc) {
        if (node instanceof Paragraph) {
            Paragraph p = (Paragraph) node;
            Node firstChild = p.getFirstChild();
            if (firstChild instanceof Text) {
                Text textNode = (Text) firstChild;
                String content = textNode.getLiteral();
                if (content != null) {
                    int consumed = parseLinkReferenceDefinitions(content, doc);
                    if (consumed > 0) {
                        if (consumed >= content.length()) {
                            p.unlink();
                        } else {
                            String remaining = content.substring(consumed);
                            if (remaining.trim().isEmpty()) {
                                p.unlink();
                            } else {
                                textNode.setLiteral(remaining.trim());
                            }
                        }
                    }
                }
            }
        } else if (node instanceof Heading) {
            Heading h = (Heading) node;
            Node firstChild = h.getFirstChild();
            if (firstChild instanceof Text) {
                Text textNode = (Text) firstChild;
                String content = textNode.getLiteral();
                if (content != null) {
                    int consumed = parseLinkReferenceDefinitions(content, doc);
                    if (consumed > 0) {
                        if (consumed >= content.length()) {
                            textNode.unlink();
                        } else {
                            String remaining = content.substring(consumed);
                            if (remaining.trim().isEmpty()) {
                                textNode.unlink();
                            } else {
                                textNode.setLiteral(remaining.trim());
                            }
                        }
                    }
                }
            }
        }

        Node child = node.getFirstChild();
        while (child != null) {
            Node next = child.getNext();
            visitAndExtract(child, doc);
            child = next;
        }
    }

    private void parseInlines(Document doc, Node node) {
        Node child = node.getFirstChild();
        while (child != null) {
            Node next = child.getNext();
            parseInlines(doc, child);
            child = next;
        }

        if (node instanceof Paragraph) {
            Paragraph p = (Paragraph) node;
            processInlineContainer(doc, p);
        } else if (node instanceof Heading) {
            Heading h = (Heading) node;
            processInlineContainer(doc, h);
        } else if (node instanceof TableCell) {
            TableCell c = (TableCell) node;
            processInlineContainer(doc, c);
        }
    }

    public static void processInlineContainerStatic(Document doc, Node container, MarkdownParserOptions options, List<InlineContentParserFactory> factories) {
        Node first = container.getFirstChild();
        if (first instanceof Text) {
            StringBuilder sb = new StringBuilder();
            Node current = first;
            while (current instanceof Text) {
                sb.append(((Text) current).getLiteral());
                current = current.getNext();
            }

            if (current == null) {
                Node child = container.getFirstChild();
                while (child != null) {
                    Node next = child.getNext();
                    child.unlink();
                    child = next;
                }

                String content = sb.toString();
                InlineParser parser = new InlineParser(content, doc.getLinkReferences(), options, factories);
                List<Node> inlines = parser.parse();
                for (Node inline : inlines) {
                    container.appendChild(inline);
                }
            }
        }
    }

    private void processInlineContainer(Document doc, Node container) {
        processInlineContainerStatic(doc, container, options, inlineParserFactories);
    }

    public static int parseLinkReferenceDefinitions(String text, Document doc) {
        if (doc == null) return 0;

        int index = 0;
        while (index < text.length()) {
            // Consume whitespace/newlines between definitions
            while (index < text.length() && Character.isWhitespace(text.charAt(index))) {
                index++;
            }
            if (index >= text.length()) break;

            int start = index;

            // 1. Parse Label: [ ... ]
            if (index >= text.length() || text.charAt(index) != '[') {
                break;
            }
            index++;

            int labelStart = index;
            int labelEnd = -1;
            int bracketDepth = 1;

            while (index < text.length()) {
                char c = text.charAt(index);
                if (c == '\\') {
                    index += 2;
                    continue;
                }
                if (c == '[') {
                    index = start;
                    break;
                } else if (c == ']') {
                    bracketDepth--;
                    if (bracketDepth == 0) {
                        labelEnd = index;
                        break;
                    }
                }
                index++;
            }

            if (labelEnd == -1) {
                index = start;
                break;
            }

            String label = text.substring(labelStart, labelEnd);
            if (label.trim().isEmpty()) {
                index = start;
                break;
            }

            index = labelEnd + 1;
            if (index >= text.length() || text.charAt(index) != ':') {
                index = start;
                break;
            }
            index++;

            // Whitespace
            while (index < text.length() && Character.isWhitespace(text.charAt(index))) {
                index++;
            }

            // Destination
            String destination;
            int destStart = index;
            boolean inAngleBrackets = false;
            if (index < text.length() && text.charAt(index) == '<') {
                inAngleBrackets = true;
                index++;
                while (index < text.length() && text.charAt(index) != '>') {
                    if (text.charAt(index) == '\\') index += 2;
                    else index++;
                }
                if (index >= text.length()) {
                    index = start;
                    break;
                }
                destination = text.substring(destStart + 1, index);
                index++;
            } else {
                while (index < text.length() && !Character.isWhitespace(text.charAt(index))) {
                    if (text.charAt(index) == '\\') index += 2;
                    else index++;
                }
                destination = text.substring(destStart, index);
            }

            if (!inAngleBrackets && destination.isEmpty()) {
                index = start;
                break;
            }

            // Optional Title
            String title = null;
            int titleStartIdx = index;
            boolean hasTitle = false;

            // Check for space/newline before title
            if (index < text.length() && Character.isWhitespace(text.charAt(index))) {
                int lookahead = index;
                while (lookahead < text.length() && Character.isWhitespace(text.charAt(lookahead))) {
                    lookahead++;
                }
                if (lookahead < text.length()) {
                    char open = text.charAt(lookahead);
                    if (open == '"' || open == '\'' || open == '(') {
                        char close = (open == '(') ? ')' : open;
                        int titleStart = lookahead + 1;
                        int titleEnd = titleStart;
                        boolean foundClose = false;
                        int p = titleStart;
                        while (p < text.length()) {
                            if (text.charAt(p) == '\\') {
                                p += 2;
                                continue;
                            }
                            if (text.charAt(p) == close) {
                                foundClose = true;
                                titleEnd = p;
                                break;
                            }
                            p++;
                        }

                        if (foundClose) {
                            // Check for trailing whitespace/newline
                            int afterTitle = p + 1;
                            boolean valid = true;
                            while (afterTitle < text.length()) {
                                char c = text.charAt(afterTitle);
                                if (c == '\n' || c == '\r') break;
                                if (!Character.isWhitespace(c)) {
                                    valid = false;
                                    break;
                                }
                                afterTitle++;
                            }

                            if (valid) {
                                title = text.substring(titleStart, titleEnd);
                                // Check for blank line in title
                                Pattern blankLine = Pattern.compile("\\n\\s*\\n");
                                if (blankLine.matcher(title).find()) {
                                    index = start;
                                    break;
                                }
                                index = afterTitle;
                                hasTitle = true;
                            }
                        }
                    }
                }
            }

            if (!hasTitle) {
                // Check if we stopped at newline or EOF
                boolean validLineEnd = true;
                while (index < text.length()) {
                    char c = text.charAt(index);
                    if (c == '\n' || c == '\r') break;
                    if (!Character.isWhitespace(c)) {
                        validLineEnd = false;
                        break;
                    }
                    index++;
                }
                if (!validLineEnd) {
                    index = start;
                    break;
                }
            }

            // Normalization
            label = label.trim().replaceAll("\\s+", " ");
            // Handle Unicode case folding for sharp S (ẞ)
            label = label.replace("ẞ", "ss");
            label = label.toUpperCase(java.util.Locale.ROOT).toLowerCase(java.util.Locale.ROOT);
            doc.addLinkReference(new LinkReference(label, unescape(destination), title != null ? unescape(title) : null));
        }

        return index;
    }

    private static boolean isPunctuation(char c) {
        String punctuation = "!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~";
        return punctuation.indexOf(c) != -1;
    }

    private static String unescape(String s) {
        if (s == null) return null;
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < s.length()) {
            char c = s.charAt(i);
            if (c == '\\' && i + 1 < s.length()) {
                if (isPunctuation(s.charAt(i + 1))) {
                    i++;
                    sb.append(s.charAt(i));
                    i++;
                } else {
                    sb.append(c);
                    i++;
                }
            } else if (c == '&') {
                Matcher matcher = ENTITY.matcher(s.substring(i));
                if (matcher.find()) {
                    sb.append(decodeEntity(matcher));
                    i += matcher.end();
                } else {
                    sb.append(c);
                    i++;
                }
            } else {
                sb.append(c);
                i++;
            }
        }
        return sb.toString();
    }

    private static String decodeEntity(Matcher matcher) {
        String name = matcher.group(1);
        String decimal = matcher.group(2);
        String hex = matcher.group(3);

        if (name != null) {
            String decoded = EntityDecoder.decode(name);
            if (decoded != null) return decoded;
            return matcher.group();
        } else if (decimal != null) {
            try {
                int codePoint = Integer.parseInt(decimal);
                if (codePoint == 0) return "\uFFFD";
                return new String(Character.toChars(codePoint));
            } catch (IllegalArgumentException e) {
                return "\uFFFD";
            }
        } else if (hex != null) {
            try {
                int codePoint = Integer.parseInt(hex, 16);
                if (codePoint == 0) return "\uFFFD";
                return new String(Character.toChars(codePoint));
            } catch (IllegalArgumentException e) {
                return "\uFFFD";
            }
        }
        return matcher.group();
    }
}
