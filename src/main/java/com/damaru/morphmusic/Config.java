package com.damaru.morphmusic;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("morpher")
public class Config {

    private int defaultTempo = 60;

    private boolean generateReport = false;

    private boolean generateTempo = true;

    private boolean randomizeOrder = false;

    private boolean snapToGrid = true;

    public int getDefaultTempo() {
        return defaultTempo;
    }

    public void setDefaultTempo(int defaultTempo) {
        this.defaultTempo = defaultTempo;
    }

    public boolean isGenerateTempo() {
        return generateTempo;
    }

    public void setGenerateTempo(boolean generateTempo) {
        this.generateTempo = generateTempo;
    }

    public boolean isGenerateReport() {
        return generateReport;
    }

    public void setGenerateReport(boolean generateReport) {
        this.generateReport = generateReport;
    }

    public boolean isRandomizeOrder() { return randomizeOrder; }

    public void setRandomizeOrder(boolean randomizeOrder) { this.randomizeOrder = randomizeOrder; }

    public boolean isSnapToGrid() {
        return snapToGrid;
    }

    public void setSnapToGrid(boolean snapToGrid) {
        this.snapToGrid = snapToGrid;
    }

    public String toString() {
        return String.format("Config:\ngenerateReport: %s \nrandomizeOrder: %s \nsnapToGrid: %s", generateReport, randomizeOrder,
                snapToGrid);
    }

}
