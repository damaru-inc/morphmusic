package com.damaru.morphmusic.model;

import java.util.List;

/**
 * TODO Add a beats-per-minute property.
 * Then the note duration will be a floating point number proportional to the beat,
 * so that we can express 16th note at 100 quarter notes per second.
 * @author mike
 *
 */
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
