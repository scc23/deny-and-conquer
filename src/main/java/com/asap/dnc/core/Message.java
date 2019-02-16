package com.asap.dnc.core;

import java.io.Serializable;

public abstract class Message implements Serializable {
    // todo implement serialization method
    private MessageType type;
    private long timestamp;
}
