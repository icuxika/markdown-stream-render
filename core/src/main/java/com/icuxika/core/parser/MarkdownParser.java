package com.icuxika.core.parser;

import com.icuxika.core.ast.*;
import com.icuxika.core.renderer.IMarkdownRenderer;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MarkdownParser {

    private static final Pattern ENTITY = Pattern.compile("^&(?:([a-zA-Z0-9]+)|#([0-9]{1,7})|#(?i:x)([0-9a-fA-F]{1,6}));");

    public void parse(Reader reader, IMarkdownRenderer renderer) throws IOException {
        StringBuilder sb = new StringBuilder();
        int c;
        while ((c = reader.read()) != -1) {
            sb.append((char) c);
        }
        Document doc = parse(sb.toString());
        doc.accept(renderer);
    }

    public Document parse(String input) {
        Document doc = new Document();
        if (input == null) return doc;

        // Split lines preserving empty lines
        String[] lines = input.split("\r\n|\r|\n", -1);
        int length = lines.length;
        if (input.endsWith("\n") || input.endsWith("\r")) {
             if (length > 0 && lines[length - 1].isEmpty()) {
                 length--;
             }
        }
        
        BlockParserState state = new BlockParserState();

        for (int i = 0; i < length; i++) {
            state.processLine(doc, expandTabs(lines[i]), lines[i]);
        }
        state.finalizeBlock(doc);

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

    private static class BlockParserState {
        List<Node> openContainers = new ArrayList<>();
        List<Integer> openContainerBlockIndents = new ArrayList<>();
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

        BlockParserState() {
            // Document is added lazily or handled as root
        }

        void processLine(Document doc, String line, String originalLine) {
            int currentContentDepth = 0;
            boolean inImplicitMode = false;

            if (openContainers.isEmpty()) {
                openContainers.add(doc);
                openContainerBlockIndents.add(0);
            }

            int i = 0;
            int matches = 0;
            boolean isContainerMarkerLine = false;
            boolean isListMarkerLine = false;
            boolean lastMatchedContainerHadMarker = false;

            // 1. Check open containers (BlockQuote)
            // Skip root (index 0)
            for (int k = 1; k < openContainers.size(); k++) {
                Node container = openContainers.get(k);
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
                if (matches < openContainers.size() && lastContainer instanceof Block) { // Should check if it's a block that allows lazy continuation?
                    // Actually, if we haven't matched all containers, we might be in a lazy continuation of a paragraph
                    // inside the last matched container.
                    // But standard says lazy continuation applies to paragraphs.
                    
                    // We need to check if this line is a block starter.
                    boolean isBlockStarter = isAtxHeading(contentLine) 
                        || isThematicBreak(contentLine) 
                        || parseFencedCodeStart(contentLine) != null
                        || isBlockQuoteStart(contentLine);

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
                    
                    // System.out.println("Lazy check: line='" + line + "', matches=" + matches + ", size=" + openContainers.size() + ", isStarter=" + isBlockStarter);

                    if (!isBlockStarter) {
                        lazyContinuation = true;
                    }
                }
            }

            if (!lazyContinuation) {
                while (openContainers.size() > matches + 1) {
                    finalizeCurrentLeaf();
                    openContainers.remove(openContainers.size() - 1);
                    openContainerBlockIndents.remove(openContainerBlockIndents.size() - 1);
                }
            }

            // Check if Indented Code Block ends
            if (inIndentedCodeBlock && countIndent(contentLine) < 4 && !contentLine.trim().isEmpty()) {
                finalizeCurrentLeaf();
            }

            // 3. Parse new containers (BlockQuote, List)
            // Only if we are not in a Fenced Code Block (Leaf)
            // Or if currentLeaf is null
            if (!inFencedCodeBlock && !inIndentedCodeBlock && !lazyContinuation) {
                 while (true) {
                     int indent = 0;
                     int startI = i;
                     
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
                         finalizeCurrentLeaf();
                         BlockQuote quote = new BlockQuote();
                         checkLooseList(openContainers.get(openContainers.size() - 1));
                         openContainers.get(openContainers.size() - 1).appendChild(quote);
                         openContainers.add(quote);
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
                         finalizeCurrentLeaf();
                         
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
                                openContainers.remove(openContainers.size() - 1);
                                openContainerBlockIndents.remove(openContainerBlockIndents.size() - 1);
                            }

                            Node newList;
                             if (marker.isOrdered) {
                                 OrderedList ol = new OrderedList();
                                 ol.setStartNumber(marker.startNumber);
                                 ol.setDelimiter(marker.delimiter);
                                 newList = ol;
                             } else {
                                 BulletList bl = new BulletList();
                                 bl.setBulletChar(marker.bulletChar);
                                 newList = bl;
                             }
                             checkLooseList(openContainers.get(openContainers.size() - 1));
                             openContainers.get(openContainers.size() - 1).appendChild(newList);
                             openContainers.add(newList);
                             openContainerBlockIndents.add(0);
                         }
                         
                         ListItem li = new ListItem();
                         Node listParent = openContainers.get(openContainers.size() - 1);
                         checkLooseList(listParent);
                         listParent.appendChild(li);
                         openContainers.add(li);
                         int contentIndent = marker.indent + marker.markerLength + marker.padding;
                         System.out.println("Adding ListItem: contentIndent=" + contentIndent + ", markerIndent=" + marker.indent + ", len=" + marker.markerLength + ", pad=" + marker.padding);
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
                     openContainers.remove(openContainers.size() - 1);
                     openContainerBlockIndents.remove(openContainerBlockIndents.size() - 1);
                }
            }
            
            // 4. Handle Leaf Blocks
            
            // Fenced Code Block (Continuation)
            if (inFencedCodeBlock) {
                // Check for closing fence
                // The indent of the closing fence is relative to the current container.
                // We already stripped container markers.
                // But we need to check indent < 4 relative to start of contentLine?
                // Yes.
                
                int currentIndent = countIndent(contentLine);
                if (currentIndent < 4 && isClosingFence(contentLine, fenceChar, fenceLength)) {
                    if (currentLeaf instanceof CodeBlock) {
                        ((CodeBlock) currentLeaf).setLiteral(currentLeafContent.toString());
                    }
                    currentLeaf = null;
                    currentLeafContent.setLength(0);
                    inFencedCodeBlock = false;
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
                    ((HtmlBlock) currentLeaf).setLiteral(currentLeafContent.toString());
                    currentLeaf = null;
                    currentLeafContent.setLength(0);
                    inHtmlBlock = false;
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
                    finalizeCurrentLeaf();
                    CodeBlock codeBlock = new CodeBlock("");
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
                        finalizeCurrentLeaf();
                        HtmlBlock htmlBlock = new HtmlBlock("");
                        checkLooseList(openContainers.get(openContainers.size() - 1));
                        openContainers.get(openContainers.size() - 1).appendChild(htmlBlock);
                        currentLeaf = htmlBlock;
                        inHtmlBlock = true;
                        htmlBlockCondition = condition;
                        
                        if (isHtmlBlockEnd(contentLine, condition)) {
                            currentLeafContent.append(getSubstringForColumn(originalLine, i)).append("\n");
                            htmlBlock.setLiteral(currentLeafContent.toString());
                            currentLeaf = null;
                            currentLeafContent.setLength(0);
                            inHtmlBlock = false;
                        } else {
                            currentLeafContent.append(getSubstringForColumn(originalLine, i)).append("\n");
                        }
                        lastLineContentDepth = Integer.MAX_VALUE;
                        return;
                    }
                }
            }

            // Indented Code Block
            // If indent >= 4
            // But we must check if we are already in Indented Code Block
            if (indent >= 4) {
                if (inIndentedCodeBlock) {
                     currentLeafContent.append(getSubstringForColumn(originalLine, i + 4)).append("\n");
                     lastLineContentDepth = Integer.MAX_VALUE;
                     return;
                } else if ((lastLineContentDepth == Integer.MAX_VALUE || currentLeaf == null) && !(currentLeaf instanceof Paragraph)) {
                     // Start new indented code block
                     if (!contentLine.trim().isEmpty()) {
                         finalizeCurrentLeaf();
                         CodeBlock codeBlock = new CodeBlock("");
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
                     finalizeCurrentLeaf();
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
                
                Node parent = openContainers.get(openContainers.size() - 1);
                currentLeaf.unlink(); // Remove paragraph
                parent.appendChild(heading);
                
                currentLeaf = null;
                currentLeafContent.setLength(0);
                lastLineContentDepth = Integer.MAX_VALUE;
                return;
            }

            // Thematic Break
            if (indent < 4 && isThematicBreak(contentLine)) {
                finalizeCurrentLeaf();
                checkLooseList(openContainers.get(openContainers.size() - 1));
                openContainers.get(openContainers.size() - 1).appendChild(new ThematicBreak());
                lastLineContentDepth = Integer.MAX_VALUE;
                return;
            }

            // ATX Heading
            if (indent < 4 && isAtxHeading(contentLine)) {
                finalizeCurrentLeaf();
                Node heading = parseAtxHeading(contentLine);
                checkLooseList(openContainers.get(openContainers.size() - 1));
                openContainers.get(openContainers.size() - 1).appendChild(heading);
                lastLineContentDepth = Integer.MAX_VALUE;
                return;
            }

            // Blank Line
            if (contentLine.trim().isEmpty()) {
                // If we are in a List Item that is empty (no children), and we hit a blank line,
                // it means we have two blank lines at the start (one from marker line, one here).
                // So we close the list item.
                if (!openContainers.isEmpty()) {
                    Node last = openContainers.get(openContainers.size() - 1);
                    if (!isListMarkerLine && last instanceof ListItem && last.getFirstChild() == null) {
                         openContainers.remove(openContainers.size() - 1);
                         openContainerBlockIndents.remove(openContainerBlockIndents.size() - 1);
                    }
                }

                if (currentLeaf instanceof Paragraph) {
                    finalizeCurrentLeaf();
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
                finalizeCurrentLeaf();
                Paragraph p = new Paragraph();
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

        void finalizeCurrentLeaf() {
            if (currentLeaf != null) {
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
                    ((HtmlBlock) currentLeaf).setLiteral(currentLeafContent.toString());
                }
                currentLeaf = null;
                currentLeafContent.setLength(0);
                inFencedCodeBlock = false;
                inIndentedCodeBlock = false;
                inHtmlBlock = false;
            }
        }
        
        void finalizeBlock(Document doc) {
            finalizeCurrentLeaf();
            // Close all containers? 
            // They are already in the tree.
            openContainers.clear();
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
            if (lower.startsWith("<script") || lower.startsWith("<pre") || lower.startsWith("<style") || lower.startsWith("<textarea")) return 1;
            if (s.startsWith("<!--")) return 2;
            if (s.startsWith("<?")) return 3;
            if (s.startsWith("<!") && s.length() >= 3 && Character.isUpperCase(s.charAt(2))) return 4;
            if (s.startsWith("<![CDATA[")) return 5;
            
            if (s.startsWith("<")) {
                 if (s.length() > 1 && s.charAt(1) != ' ') {
                     int i = 1;
                     if (s.charAt(i) == '/') i++;
                     int startName = i;
                     while(i < s.length() && Character.isLetterOrDigit(s.charAt(i))) i++;
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
            while(i < line.length() && (Character.isLetterOrDigit(line.charAt(i)) || line.charAt(i) == '-')) i++;
            
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
                        if (valStart == '"' || valStart == '\'' || valStart == '=' || valStart == '<' || valStart == '>' || valStart == '`') return false;
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
                    // Actually, we consumed whitespace after name.
                    // If no =, we stay at i.
                    // The loop continues, next iteration checks for whitespace or >.
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

    private static boolean isLinkReferenceDefinitions(String text) {
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
                if (c == '\\') { index += 2; continue; }
                if (c == '[') return false; // Nested bracket in label? (Wait, label can contain brackets if escaped? No.)
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
                            if (text.charAt(p) == '\\') { p += 2; continue; }
                            if (text.charAt(p) == close) { foundClose = true; break; }
                            p++;
                        }
                        if (foundClose) {
                            // Check trailing
                            int afterTitle = p + 1;
                            boolean valid = true;
                            while (afterTitle < text.length()) {
                                char c = text.charAt(afterTitle);
                                if (c == '\n' || c == '\r') break;
                                if (!Character.isWhitespace(c)) { valid = false; break; }
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
                if (!Character.isWhitespace(c)) { validLineEnd = false; break; }
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
            Node first = p.getFirstChild();
            if (first instanceof Text && first.getNext() == null) {
                String content = ((Text) first).getLiteral();
                first.unlink();
                
                InlineParser parser = new InlineParser(content, doc.getLinkReferences());
                List<Node> inlines = parser.parse();
                for (Node inline : inlines) {
                    p.appendChild(inline);
                }
            }
        } else if (node instanceof Heading) {
            Heading h = (Heading) node;
            Node first = h.getFirstChild();
            if (first instanceof Text && first.getNext() == null) {
                String content = ((Text) first).getLiteral();
                first.unlink();
                
                InlineParser parser = new InlineParser(content, doc.getLinkReferences());
                List<Node> inlines = parser.parse();
                for (Node inline : inlines) {
                    h.appendChild(inline);
                }
            }
        }
    }

    private int parseLinkReferenceDefinitions(String text, Node node) {
        Document doc = null;
        Node parent = node;
        while (parent != null) {
            if (parent instanceof Document) {
                doc = (Document) parent;
                break;
            }
            parent = parent.getParent();
        }
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
                if (index >= text.length()) { index = start; break; }
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
            label = label.trim().replaceAll("\\s+", " ").toUpperCase(java.util.Locale.ROOT).toLowerCase(java.util.Locale.ROOT);
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
