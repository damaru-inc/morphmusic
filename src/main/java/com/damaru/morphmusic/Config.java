package com.damaru.morphmusic;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Component
public class Config {

    @Value("${morpher.generateReport}")
    private boolean generateReport;

    @Value("${morpher.randomizeOrder}")
    private boolean randomizeOrder = false;

    @Value("${morpher.snapToGrid}")
    private boolean snapToGrid = true;

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
