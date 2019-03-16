package com.asap.dnc.network;

import java.io.Serializable;

public abstract class Message implements Comparable<Message>, Serializable {
    private MessageType type;
    private long timestamp;

    public long getTimestamp(){
        return timestamp;
    }

    @Override
    public int compareTo(Message msg){
        if (this.getTimestamp() > msg.getTimestamp()){
            return 1;
        } else if (this.getTimestamp() < msg.getTimestamp()){
            return -1;
        } else {
            return 0;
        }
    }
}
