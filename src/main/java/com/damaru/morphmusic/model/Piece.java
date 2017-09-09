package com.damaru.morphmusic.model;

import java.util.List;

public class Piece {
    private List<Part> parts;

    public List<Part> getParts() {
        return parts;
    }

    public void setParts(List<Part> parts) {
        this.parts = parts;
    }

    @Override
    public String toString() {
        return "Piece [parts=" + parts + "]";
    }
    
    
}
