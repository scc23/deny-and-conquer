package com.asap.dnc.core;

import com.asap.dnc.network.ClientInfo;
import com.asap.dnc.network.Message;
import com.asap.dnc.network.MessageType;

import java.sql.Timestamp;

public class GameMessage extends Message {
    private boolean isValid;    // Server will set this field in response message
    private int row, col;       // Used for cell index
    private PenColor color;     // Used to identify player
    private boolean isOwned = false;    // Used to determine if cell is owned by player
    // TODO: finalFillPercentage is fixed for now, needs to be updated to reflect actual changes
    private double finalFillPercentage = 80;

    public GameMessage(MessageType type, Timestamp timestamp){
        super(type, timestamp);
    }

    // Maybe we should use this instead?
//    public GameMessage(MessageType type, Timestamp timestamp, ClientInfo clientInfo){
//        super(type, timestamp);
//        this.color = clientInfo.getPenColor();
//    }

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
        return this.row;
    }

    // Getter for col
    public int getCol() {
        return this.col;
    }

    // Getter for pen color
    public PenColor getPenColor() {
        return this.color;
    }

    // Setter isOwned
    public void setIsOwned() {
        this.isOwned = true;
    }

    // Getter for isOwned
    public boolean getIsOwned() {
        return this.isOwned;
    }

    // Getter for final fill percentage
    public double getFinalFillPercentage() {
        return this.finalFillPercentage;
    }

    // Getter for isValid
    public boolean getIsValid() {
        return this.isValid;
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
