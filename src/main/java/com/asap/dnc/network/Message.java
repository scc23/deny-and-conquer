package com.asap.dnc.network;

import java.io.*;
import java.sql.Timestamp;

public abstract class Message implements Comparable<Message>, Serializable {
    private MessageType type;
    private Timestamp timestamp;

    public Message(MessageType type, Timestamp timestamp){
        this.type = type;
        this.timestamp = timestamp;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
    }

    public Timestamp getTimestamp(){
        return timestamp;
    }

    public MessageType getType(){
        return type;
    }

    @Override
    public int compareTo(Message msg){
        if (this.getTimestamp().before(msg.getTimestamp())){
            return 1;
        } else if (this.getTimestamp().after(msg.getTimestamp())){
            return -1;
        } else {
            return 0;
        }
    }
}
