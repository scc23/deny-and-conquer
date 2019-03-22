package com.asap.dnc.core;

import java.io.IOException;

public interface CoreGameClient {
    void sendAcquireMessage(String address, int port, PenColor penColor, int row, int col) throws IOException;
    void sendReleaseMessage(String address, int port, PenColor penColor, int row, int col, int fillPercentage) throws IOException;
    void sendServerRequest(String address, int port, GameMessage msg) throws IOException;
    void receiveServerResponse() throws IOException, ClassNotFoundException;
    void executeGridOperation(GameMessage msg);
}
