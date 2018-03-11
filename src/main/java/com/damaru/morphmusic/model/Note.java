package com.damaru.morphmusic.model;

public class Note {
    private String id;
    private int midiNum;
    private int start;
    private int duration;
    private int dynamic;
    private int expression;
    private int orderIn;
    private int orderOut;

    public Note() {

    }

    public Note(Note note) {
        midiNum = note.getMidiNum();
        start = note.getStart();
        duration = note.getDuration();
        dynamic = note.getDynamic();
        expression = note.getExpression();
        orderIn = note.getOrderIn();
        orderOut = note.getOrderOut();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public int getOrderIn() {
        return orderIn;
    }

    public void setOrderIn(int orderIn) {
        this.orderIn = orderIn;
    }

    public int getOrderOut() {
        return orderOut;
    }

    public void setOrderOut(int orderOut) {
        this.orderOut = orderOut;
    }

    /**
     * This assumes we have dynamic values from 1 to 8.
     * 
     * @return
     */
    public int getVelocity() {
        int dynamic = getDynamic();

        if (dynamic > 8) {
            dynamic = 8;
        }
        return dynamic * 16 - 1; // max value: 127 for midi.
    }
    
    public int getMidiDuration() {
        // assume for just today:
        // duration is 16 notes
        // speed is 66 bpm, * 4 = 264
        return 264 * duration;
    }

    @Override
    public String toString() {
        return "Note [id=" + id + ", midiNum=" + midiNum + ", start=" + start + ", duration=" + duration + ", orderIn="
                + orderIn + ", orderOut=" + orderOut + "]";
    }

}
