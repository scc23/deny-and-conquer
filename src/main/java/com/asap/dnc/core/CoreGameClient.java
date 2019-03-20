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
        DatagramSocket socket = new DatagramSocket();

        // Send message
        ByteArrayOutputStream outputByteStream = new ByteArrayOutputStream(5000);
        ObjectOutputStream os = new ObjectOutputStream(new BufferedOutputStream(outputByteStream));

        os.flush();
        os.writeObject(msg);
        os.flush();

        byte[] sendBuf = outputByteStream.toByteArray();
        DatagramPacket sendPacket = new DatagramPacket(sendBuf, sendBuf.length, address, port);
        socket.send(sendPacket);

        os.close();
    }

    void executeGridOperation() {

    }

    void receiveServerResponse() throws IOException, ClassNotFoundException {
        DatagramSocket socket = new DatagramSocket();
        // Receive message
        byte[] recvBuf = new byte[5000];
        DatagramPacket recvPacket = new DatagramPacket(recvBuf, recvBuf.length);
        socket.receive(recvPacket);
        ByteArrayInputStream inputByteStream = new ByteArrayInputStream(recvBuf);
        ObjectInputStream is = new ObjectInputStream(new BufferedInputStream(inputByteStream));
        Grid gridObject = is.readObject();
        is.close();

        // TODO: Receive response from server via multicast and perform operation on own grid
    }


}
