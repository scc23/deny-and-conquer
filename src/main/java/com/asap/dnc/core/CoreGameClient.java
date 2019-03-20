package com.asap.dnc.core;

import com.asap.dnc.network.ClientInfo;
import com.asap.dnc.network.MessageType;

import java.io.*;
import java.net.*;

public class CoreGameClient implements CoreGameClientInterface {
    private DatagramSocket socket;
//    private ClientInfo hostServer;
//    private Grid clientGrid;

    private CoreGameClient() throws SocketException {
        socket = new DatagramSocket();
    }

    // Send message to server to validate grid operation
    public void sendServerRequest(String address, int port, GameMessage msg) throws IOException {
        InetAddress serverAddress = InetAddress.getByName(address);

        ByteArrayOutputStream bStream = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bStream);

        oos.writeObject(msg);
        oos.flush();

        byte[] buf = bStream.toByteArray();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, serverAddress, port);
        socket.send(packet);
    }

    // Receive response from server
    public void receiveServerResponse() throws IOException, ClassNotFoundException {
        // Timeout if no response from server
        socket.setSoTimeout(2000);

        while(true) {
            try {
                byte[] buf = new byte[5000];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                ByteArrayInputStream inputByteStream = new ByteArrayInputStream(buf);
                ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(inputByteStream));
                GameMessage msg = (GameMessage)ois.readObject();
            } catch (SocketTimeoutException e) {
                System.out.println("Timeout reached: " + e);
            }
        }
    }

    // Execute grid operation
    public void executeGridOperation(MessageType type) {

    }
}
