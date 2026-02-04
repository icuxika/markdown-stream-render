package com.icuxika.markdown.stream.render.core.ast;

public interface Visitor {
    void visit(Document document);

    void visit(Paragraph paragraph);

    void visit(Heading heading);

    void visit(Text text);

    void visit(SoftBreak softBreak);

    void visit(HardBreak hardBreak);

    void visit(Emphasis emphasis);

    void visit(StrongEmphasis strongEmphasis);

    void visit(BlockQuote blockQuote);

    void visit(BulletList bulletList);

    void visit(OrderedList orderedList);

    void visit(ListItem listItem);

    void visit(Code code);

    void visit(ThematicBreak thematicBreak);

    void visit(CodeBlock codeBlock);

    void visit(HtmlBlock htmlBlock);

    void visit(HtmlInline htmlInline);

    void visit(Link link);

    void visit(Image image);

    void visit(Table table);

    void visit(TableHead tableHead);

    void visit(TableBody tableBody);

    void visit(TableRow tableRow);

    void visit(TableCell tableCell);

    void visit(Strikethrough strikethrough);
}
