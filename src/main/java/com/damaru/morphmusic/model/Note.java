package com.damaru.morphmusic.model;

import com.damaru.midi.MidiUtil;

/**
 * TODO add the capability to express notes like C#3 instead of midinum.
 * @author mike
 *
 */
public class Note {

	private String id;
	private int midiNoteNum;
	private int start;
	private double proportionalStart;
	private int duration;
	private double proportionalDuration;
	private int dynamic;
	private int expression;
	private int orderIn;
	private int orderOut;

	public Note() {

	}

	public Note(Note note) {
		id = note.getId();
		midiNoteNum = note.getMidiNoteNum();
		start = note.getStart();
		proportionalStart = note.getProportionalStart();
		duration = note.getDuration();
		proportionalDuration = note.getProportionalDuration();
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

	public int getMidiNoteNum() {
		return midiNoteNum;
	}

	public void setMidiNoteNum(int midiNum) {
		this.midiNoteNum = midiNum;
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public double getProportionalStart() {
		return proportionalStart;
	}

	public void setProportionalStart(double proportionalStart) {
		this.proportionalStart = proportionalStart;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public double getProportionalDuration() {
		return proportionalDuration;
	}

	public void setProportionalDuration(double proportionateDuration) {
		this.proportionalDuration = proportionateDuration;
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
	
    public int getVelocity() {
        int dynamic = getDynamic();

        if (dynamic > 8) {
            dynamic = 8;
        }
        return dynamic * 16 - 1; // max value: 127 for midi.
    }
    
    /**
     * TODO Where do these get set?
     */
    public int getMidiStart() {
        return MidiUtil.PULSES_PER_SIXTEENTH_NOTE * start;
    }    
    
    public int getMidiDuration() {
        return MidiUtil.PULSES_PER_SIXTEENTH_NOTE * duration;
    }


	@Override
	public String toString() {
		return String.format("Note [id=%12s m=%2d st=%3d %6.03f dur=%3d %6.03f in=%3d out=%d]", id, midiNoteNum, start,
				proportionalStart, duration, proportionalDuration, orderIn, orderOut);
	}

}
