package com.asap.dnc.network;

import java.io.Serializable;

public abstract class Message implements Serializable {
    private MessageType type;
    private long timestamp;
}
