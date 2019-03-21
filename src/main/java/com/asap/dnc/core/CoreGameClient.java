package com.asap.dnc.core;

import java.io.IOException;

public interface CoreGameClient {
    public void sendServerRequest(String address, int port, GameMessage msg) throws IOException;
    public void receiveServerResponse() throws IOException, ClassNotFoundException;
    public void executeGridOperation(GameMessage msg);
}
