package com.damaru.morphmusic.model;

import java.util.List;

public class Part {
    private String name;
    private List<Pattern> patterns;
    private List<Section> sections;

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

    @Override
    public String toString() {
        return "Part [name=" + name + ", patterns=" + patterns + ", sections=" + sections + "]";
    }

}
