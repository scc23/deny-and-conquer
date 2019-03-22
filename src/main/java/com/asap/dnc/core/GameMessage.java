package com.asap.dnc.core;

import com.asap.dnc.network.Message;
import com.asap.dnc.network.MessageType;

import java.sql.Timestamp;

public class GameMessage extends Message {
    private boolean isValid;    // Server will set this field in response message
    private int row, col;       // Used for cell index
    private PenColor penColor;  // Used to identify player
    private boolean isOwned = false;    // Used to determine if cell is owned by player
    // TODO: finalFillPercentage and threshold is fixed for now, needs to be updated to reflect actual changes
    private double fillPercentage = 80;
    private int threshold = 60;

    public GameMessage(MessageType type, Timestamp timestamp){
        super(type, timestamp);
    }

    // Setter for row
    public void setRow(int row) {
        this.row = row;
    }

    // Setter for col
    public void setCol(int col) {
        this.col = col;
    }

    // Getter for row
    public int getRow() {
        return row;
    }

    // Getter for col
    public int getCol() {
        return col;
    }

    // Setter for pen color
    public void setPenColor(PenColor penColor){
        this.penColor = penColor;
    }

    // Getter for pen color
    public PenColor getPenColor() {
        return this.penColor;
    }

    // Setter isOwned
    public void setIsOwned() {
        this.isOwned = true;
    }

    // Getter for isOwned
    public boolean getIsOwned() {
        return this.isOwned;
    }

    public void setFillPercentage(int fillPercentage) {
        this.fillPercentage = fillPercentage;
        // Set ownership if filled percentage exceeds threshold
        if (fillPercentage >= this.threshold) {
            this.isOwned = true;
        }
    }

    // Getter for final fill percentage
    public double getFillPercentage() {
        return this.fillPercentage;
    }

    // Getter for isValid
    public boolean getIsValid() {
        return this.isValid;
    }

    // Setter for isValid
    public void setIsValid(boolean isValid){
        this.isValid = isValid;
    }

    @Override
    public String toString() {
        return "{\n" +
                " Type: " + this.getType() + ",\n" +
                " Valid: " + this.getIsValid() + ",\n" +
                " Pen Color: " + this.getPenColor() + ",\n" +
                " Cell: [" + this.getRow() + "][" + this.getCol() +  "],\n" +
                " Timestamp: " + this.getTimestamp() + ",\n" +
                " Fill Percentage: " + this.getFillPercentage() + "\n" +
                " Owned: " + this.getIsOwned() + ",\n" +
                "}";
    }
}
