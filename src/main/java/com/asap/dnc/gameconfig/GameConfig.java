package com.asap.dnc.gameconfig;

public class GameConfig {

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
        return gridSize;
    }

    public void setGridSize(int gridSize) {
        this.gridSize = gridSize;
    }
}
