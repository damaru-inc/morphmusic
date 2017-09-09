package com.damaru.morphmusic.model;

import java.util.List;

public class Part {
    private List<Pattern> patterns;

    public List<Pattern> getPatterns() {
        return patterns;
    }

    public void setPatterns(List<Pattern> patterns) {
        this.patterns = patterns;
    }

    @Override
    public String toString() {
        return "Part [patterns=" + patterns + "]";
    }

}
