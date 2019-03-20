package com.asap.dnc.core;

import com.asap.dnc.network.Message;
import com.asap.dnc.network.MessageType;

import java.sql.Timestamp;

public class GameMessage extends Message {
    private Cell cell;
    private double finalFillPercentage = 80.2;

    public GameMessage(MessageType type, Timestamp timestamp){
        super(type, timestamp);
    }
    public Cell getCell() {
        return cell;
    }

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
