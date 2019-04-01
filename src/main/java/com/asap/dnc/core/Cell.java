package com.asap.dnc.core;

import java.sql.Timestamp;

/**
 * Abstract representation of a cell within a gameconfig board. Server/client side implementations must
 * implement the method to fill a specified pixel as belonging to a specified player.
 */
public abstract class Cell {
    private double coloredPercentage = 0.0; // number of pixels filled by the player with access
    private PenColor owner = null; // set once ctrlReq has been met

    // Subclasses should implement a factory method making use of this
    private int height;
    private int width;
    private int col;
    private int row;
    private PenColor acquiredRights;
    private Timestamp acquiredCellTimestamp;
    
    protected Cell(int height, int width, int col, int row) {
        this.height = height;
        this.width = width;
        this.col = col;
        this.row = row;
        this.acquiredRights = null;
    }

    public void clearCell() {
        this.owner = null;
    }

    public PenColor getOwner() {
        return owner;
    }

    public void setOwner(PenColor owner) {
        this.owner = owner;
    }

    public void setAcquiredRights(PenColor color){ this.acquiredRights = color;}

    public PenColor getAcquiredRights(){ return this.acquiredRights;}

    public void setColoredPercentage(double coloredPercentage) {
        this.coloredPercentage = coloredPercentage;
    }

    public double getColoredPercentage() {
        return this.coloredPercentage;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public int getCol() {
        return col;
    }

    public int getRow() {
        return row;
    }

    public void setAcquiredCellTimestamp(){ this.acquiredCellTimestamp = new Timestamp(System.currentTimeMillis());}

    public Timestamp getAcquiredCellTimestamp(){ return this.acquiredCellTimestamp;}
}
