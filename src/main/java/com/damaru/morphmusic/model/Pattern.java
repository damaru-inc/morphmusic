package com.damaru.morphmusic.model;

import java.util.List;

public class Pattern {
    private String name;
    private int duration;
    private List<Note> notes;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public List<Note> getNotes() {
        return notes;
    }

    public void setNotes(List<Note> notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return "Pattern [name=" + name + ", duration=" + duration + "]";
    }
    
    
}
