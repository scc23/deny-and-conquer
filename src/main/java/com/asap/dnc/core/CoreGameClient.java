package com.asap.dnc.core;

import com.asap.dnc.network.MessageType;

import java.io.IOException;
import java.net.SocketTimeoutException;

public interface CoreGameClient {
    public void sendServerRequest(String address, int port, GameMessage msg) throws IOException;
    public void receiveServerResponse() throws IOException, ClassNotFoundException;
    public void executeGridOperation(MessageType type);
}
