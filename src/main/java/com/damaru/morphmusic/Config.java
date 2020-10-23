package com.damaru.morphmusic;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Config {

    @Value("${morpher.generateReport}")
    private boolean generateReport;

    @Value("${morpher.snapToGrid}")
    private boolean snapToGrid = true;

    public boolean isGenerateReport() {
        return generateReport;
    }

    public void setGenerateReport(boolean generateReport) {
        this.generateReport = generateReport;
    }

    public boolean isSnapToGrid() {
        return snapToGrid;
    }

    public void setSnapToGrid(boolean snapToGrid) {
        this.snapToGrid = snapToGrid;
    }
    
    public String toString() {
        return String.format("Config:\ngenerateReport: %s \nsnapToGrid: %s", generateReport, snapToGrid);
    }

}
