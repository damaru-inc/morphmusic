package com.damaru.morphmusic.model;

public class Note {
    private int midiNum;
    private int start;
    private int duration;
    private int dynamic;
    private int expression;
    public int getMidiNum() {
        return midiNum;
    }
    public void setMidiNum(int midiNum) {
        this.midiNum = midiNum;
    }
    public int getStart() {
        return start;
    }
    public void setStart(int start) {
        this.start = start;
    }
    public int getDuration() {
        return duration;
    }
    public void setDuration(int duration) {
        this.duration = duration;
    }
    public int getDynamic() {
        return dynamic;
    }
    public void setDynamic(int dynamic) {
        this.dynamic = dynamic;
    }
    public int getExpression() {
        return expression;
    }
    public void setExpression(int expression) {
        this.expression = expression;
    }
    @Override
    public String toString() {
        return "Note [midiNum=" + midiNum + ", start=" + start + ", duration=" + duration + ", dynamic=" + dynamic
                + ", expression=" + expression + "]";
    }
    
    
}
