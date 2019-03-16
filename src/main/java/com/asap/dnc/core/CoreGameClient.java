package com.asap.dnc.core;

import com.asap.dnc.network.ClientInfo;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class CoreGameClient {
    private ClientInfo hostServer;
//    private Grid clientGrid;

    // Send message to server for grid operation
    void sendServerRequest(String address, int port, GameMessage msg) throws IOException {
    }

    void receiveServerResponse() throws IOException, ClassNotFoundException {
        // TODO: Receive response from server via multicast and perform operation on own grid
    }
}
