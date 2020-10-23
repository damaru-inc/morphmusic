package com.damaru.morphmusic.model;

import java.util.List;

/**
 * TODO Add a beats-per-minute property.
 * Then the note duration will be a floating point number proportional to the beat,
 * so that we can express 16th note at 100 quarter notes per second.
 * @author Michael Davis
 *
 */
public class Piece {
    private String name;
    private List<Part> parts;
    private int unitOfMeasurement = 16;  // division of a bar
    private int timeSignatureUnits = 4;
    private int timeSignatureDivision = 4;
    private int tempo; // beats per minute

    public List<Part> getParts() {
        return parts;
    }

    public void setParts(List<Part> parts) {
        this.parts = parts;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getUnitOfMeasurement() {
        return unitOfMeasurement;
    }

    public void setUnitOfMeasurement(int unitOfMeasurement) {
        this.unitOfMeasurement = unitOfMeasurement;
    }
    
    public boolean isUseMidiPulseAsUnitOfMeasure() {
        return unitOfMeasurement == 0;
    }

    public int getTimeSignatureUnits() {
        return timeSignatureUnits;
    }

    public void setTimeSignatureUnits(int timeSignatureUnits) {
        this.timeSignatureUnits = timeSignatureUnits;
    }

    public int getTimeSignatureDivision() {
        return timeSignatureDivision;
    }

    public void setTimeSignatureDivision(int timeSignatureDivision) {
        this.timeSignatureDivision = timeSignatureDivision;
    }

    public int getTempo() {
        return tempo;
    }

    public void setTempo(int tempo) {
        this.tempo = tempo;
    }

    @Override
    public String toString() {
        return "Piece " + name;
    }
    
    
}
