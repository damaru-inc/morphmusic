package com.damaru.morphmusic.model;

public class Note {
	private String id;
	private int midiNum;
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
		midiNum = note.getMidiNum();
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

	@Override
	public String toString() {
		return String.format("Note [id=%12s m=%2d st=%3d %f dur=%3d %f in=%3d out=%d]", id, midiNum, start,
				proportionalStart, duration, proportionalDuration, orderIn, orderOut);
	}

}
