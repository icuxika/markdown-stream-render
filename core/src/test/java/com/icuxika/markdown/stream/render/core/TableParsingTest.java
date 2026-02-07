package com.icuxika.markdown.stream.render.core;

import static org.junit.jupiter.api.Assertions.*;

import com.icuxika.markdown.stream.render.core.ast.*;
import com.icuxika.markdown.stream.render.core.parser.MarkdownParser;
import org.junit.jupiter.api.Test;

class TableParsingTest {

    @Test
    void testBasicTable() {
        String markdown = "" + "| Header 1 | Header 2 |\n" + "| --- | --- |\n" + "| Cell 1   | Cell 2   |";

        MarkdownParser parser = MarkdownParser.builder().build();
        Document doc = parser.parse(markdown);

        // Expect: Document -> Table -> (TableHead -> TableRow -> Cell...), (TableBody
        // -> TableRow -> Cell...)
        Node table = doc.getFirstChild();
        assertTrue(table instanceof Table, "First child should be Table");

        Node head = table.getFirstChild();
        assertTrue(head instanceof TableHead, "Table should have TableHead");

        Node body = head.getNext();
        assertTrue(body instanceof TableBody, "Table should have TableBody");

        // Check Header
        TableRow headerRow = (TableRow) head.getFirstChild();
        assertNotNull(headerRow);
        assertEquals(2, countChildren(headerRow));
        TableCell h1 = (TableCell) headerRow.getFirstChild();
        assertTrue(h1.isHeader());
        assertEquals("Header 1", ((Text) h1.getFirstChild()).getLiteral());

        // Check Body
        TableRow bodyRow = (TableRow) body.getFirstChild();
        assertNotNull(bodyRow);
        assertEquals(2, countChildren(bodyRow));
        TableCell c1 = (TableCell) bodyRow.getFirstChild();
        assertFalse(c1.isHeader());
        assertEquals("Cell 1", ((Text) c1.getFirstChild()).getLiteral());
    }

    @Test
    void testTableAlignment() {
        String markdown = "" + "| Left | Center | Right |\n" + "| :--- | :----: | ----: |\n"
                + "| L    | C      | R     |";

        MarkdownParser parser = MarkdownParser.builder().build();
        Document doc = parser.parse(markdown);
        Table table = (Table) doc.getFirstChild();
        TableHead head = (TableHead) table.getFirstChild();
        TableRow row = (TableRow) head.getFirstChild();

        TableCell c1 = (TableCell) row.getFirstChild();
        assertEquals(TableCell.Alignment.LEFT, c1.getAlignment());

        TableCell c2 = (TableCell) c1.getNext();
        assertEquals(TableCell.Alignment.CENTER, c2.getAlignment());

        TableCell c3 = (TableCell) c2.getNext();
        assertEquals(TableCell.Alignment.RIGHT, c3.getAlignment());
    }

    @Test
    void testComplexTableCells() {
        // Test escaped pipes and code spans with pipes
        String markdown = "| `a|b` | \\| |\n| --- | --- |";

        MarkdownParser parser = MarkdownParser.builder().build();
        Document doc = parser.parse(markdown);
        Table table = (Table) doc.getFirstChild();
        TableHead head = (TableHead) table.getFirstChild();
        TableRow row = (TableRow) head.getFirstChild();

        TableCell c1 = (TableCell) row.getFirstChild();
        // Content should be Code node with literal "a|b"
        Node n1 = c1.getFirstChild();
        assertTrue(n1 instanceof Code);
        assertEquals("a|b", ((Code) n1).getLiteral());

        TableCell c2 = (TableCell) c1.getNext();
        // Content should be Text node with literal "|" (unescaped)
        Node n2 = c2.getFirstChild();
        assertTrue(n2 instanceof Text);
        assertEquals("|", ((Text) n2).getLiteral());
    }

    private int countChildren(Node parent) {
        int count = 0;
        Node child = parent.getFirstChild();
        while (child != null) {
            count++;
            child = child.getNext();
        }
        return count;
    }
}