package com.asap.dnc.core;

import com.asap.dnc.network.Message;
import com.asap.dnc.network.MessageType;

import java.sql.Timestamp;

public class GameMessage extends Message {
    private int row, col;
    // TODO: finalFillPercentage is fixed for now, needs to be updated to reflect actual changes
    private double finalFillPercentage = 80;

    public GameMessage(MessageType type, Timestamp timestamp){
        super(type, timestamp);
    }

    // Setters and getters for row and col
    public void setRow(int row) {
        this.row = row;
    }

    public void setCol(int col) {
        this.col = col;
    }

    public int getRow() {
        return this.row;
    }

    public int getCol() {
        return this.col;
    }

    // Getter for final fill percentage
    public double getFinalFillPercentage() {
        return finalFillPercentage;
    }

    @Override
    public String toString() {
        return "{\n" +
                " Type: " + getType() + ",\n" +
                " timestamp: " + getTimestamp() + ",\n" +
                " Percentage: " + getFinalFillPercentage() + "\n" +
                "}";
    }
}
