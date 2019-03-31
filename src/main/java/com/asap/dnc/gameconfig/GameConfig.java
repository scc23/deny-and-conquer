package com.asap.dnc.gameconfig;

import java.io.Serializable;

public class GameConfig implements Serializable {

    private int numberPlayers;
    private int penThickness;
    private int gridSize;

    public GameConfig(int numberPlayers, int penThickness, int gridSize) {
        this.numberPlayers = numberPlayers;
        this.penThickness = penThickness;
        this.gridSize = gridSize;
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
}
