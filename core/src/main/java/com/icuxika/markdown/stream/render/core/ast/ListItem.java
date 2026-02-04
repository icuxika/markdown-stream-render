package com.icuxika.markdown.stream.render.core.ast;

public class ListItem extends Block {
    private boolean endsWithBlankLine = false;
    private boolean isTask = false;
    private boolean isChecked = false;

    public boolean isEndsWithBlankLine() {
        return endsWithBlankLine;
    }

    public void setEndsWithBlankLine(boolean endsWithBlankLine) {
        this.endsWithBlankLine = endsWithBlankLine;
    }
    
    public boolean isTask() {
        return isTask;
    }

    public void setTask(boolean task) {
        isTask = task;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
