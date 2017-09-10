package com.damaru.morphmusic.model;

public class Section {

    private String name;
    private String startPattern;
    private String endPattern;
    private int steps;
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getStartPattern() {
        return startPattern;
    }
    public void setStartPattern(String startPattern) {
        this.startPattern = startPattern;
    }
    public String getEndPattern() {
        return endPattern;
    }
    public void setEndPattern(String endPattern) {
        this.endPattern = endPattern;
    }
    public int getSteps() {
        return steps;
    }
    public void setSteps(int steps) {
        this.steps = steps;
    }
    @Override
    public String toString() {
        return "Section [name=" + name + ", startPattern=" + startPattern + ", endPattern=" + endPattern + ", steps="
                + steps + "]";
    }
    
    
}
