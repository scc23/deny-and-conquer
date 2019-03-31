package com.asap.dnc.gameconfig;

import java.io.Serializable;

public class GameConfig implements Serializable {

    private int numberPlayers;
    private int penThickness;
    private int gridSize;
    private double threshold;

    public GameConfig(int numberPlayers, int penThickness, int gridSize, double threshold) {
        this.numberPlayers = numberPlayers;
        this.penThickness = penThickness;
        this.gridSize = gridSize;
        this.threshold = threshold;
    }

    public int getNumberPlayers() {
        return numberPlayers;
    }

    public void setNumberPlayers(int numberPlayers) {
        this.numberPlayers = numberPlayers;
    }

    public  int getPenThickness() {
        return penThickness;
    }

    public void setPenThickness(int penThickness) {
        this.penThickness = penThickness;
    }

    public int getGridSize() {
        System.out.println("In GameConfig (gridSize): " + gridSize);
        return gridSize;
    }

    public void setGridSize(int gridSize) {
        this.gridSize = gridSize;
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }
}
