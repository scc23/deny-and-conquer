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
    public void setRow(int cellRow) {
        row = cellRow;
    }

    // Setter for col
    public void setCol(int cellCol) {
        col = cellCol;
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
    public void setPenColor(PenColor color){
        this.color = color;
    }

    // Getter for pen color
    public PenColor getPenColor() {
        return color;
    }

    // Setter isOwned
    public void setIsOwned() {
        isOwned = true;
    }

    // Getter for isOwned
    public boolean getIsOwned() {
        return isOwned;
    }

    // Getter for final fill percentage
    public double getFinalFillPercentage() {
        return finalFillPercentage;
    }

    // Getter for isValid
    public boolean getIsValid() {
        return isValid;
    }

    // Setter for isValid
    public void setIsValid(boolean isValid){
        this.isValid = isValid;
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
