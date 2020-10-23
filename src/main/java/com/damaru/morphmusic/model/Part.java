package com.damaru.morphmusic.model;

import java.util.List;

public class Part {
    private Piece piece;
    private String name;
    private List<Pattern> patterns;
    private List<Section> sections;
    private List<Note> notes;
    private int quartersPerBar = 4;

    public Piece getPiece() {
        return piece;
    }

    public void setPiece(Piece piece) {
        this.piece = piece;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Pattern> getPatterns() {
        return patterns;
    }

    public void setPatterns(List<Pattern> patterns) {
        this.patterns = patterns;
    }

    public List<Section> getSections() {
        return sections;
    }

    public void setSections(List<Section> sections) {
        this.sections = sections;
    }
    
    /**
     * This holds the result of any generation or filtering.
     * 
     * @return The list of notes associated with this part, possibly generated.
     */
    public List<Note> getNotes() {
        return notes;
    }

    public void setNotes(List<Note> notes) {
        this.notes = notes;
    }

    public int getQuartersPerBar() {
        return quartersPerBar;
    }

    public void setQuartersPerBar(int quartersPerBar) {
        this.quartersPerBar = quartersPerBar;
    }

    @Override
    public String toString() {
        return "Part [name=" + name + ", patterns=" + patterns + ", sections=" + sections + "]";
    }

}
