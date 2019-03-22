package com.asap.dnc.core;

import com.asap.dnc.network.MessageType;
import com.asap.dnc.network.gameconfig.client.ClientGrid;

import java.io.*;
import java.net.*;

public class CoreGameClientImpl implements CoreGameClient {
    private ClientGrid grid;
//    private ClientInfo hostServer;
//    private Grid clientGrid;

    // TODO: Add constructor to set client grid
    public CoreGameClientImpl(ClientGrid clientGrid) {
        grid = clientGrid;
    }

    // Send message to server to validate grid operation
    public void sendServerRequest(String address, int port, GameMessage msg) throws IOException {
        DatagramSocket socket = new DatagramSocket();
        InetAddress serverAddress = InetAddress.getByName(address);

        ByteArrayOutputStream bStream = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bStream);

        oos.writeObject(msg);
        oos.flush();

        byte[] buf = bStream.toByteArray();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, serverAddress, port);
        socket.send(packet);

        oos.close();
    }

    // Receive response from server
    public void receiveServerResponse() throws IOException, ClassNotFoundException {
        // for testing set port manually to 8000
        DatagramSocket socket = new DatagramSocket(8000);

        // Timeout if no response from server
        //socket.setSoTimeout(2000);

        while(true) {
            try {
                byte[] buf = new byte[5000];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                ByteArrayInputStream inputByteStream = new ByteArrayInputStream(buf);
                ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(inputByteStream));
                GameMessage msg = (GameMessage)ois.readObject();
                System.out.println("Recieved unicast message..");
                System.out.println(msg);
                executeGridOperation(msg);
            } catch (SocketTimeoutException e) {
                System.out.println("Timeout reached: " + e);
                socket.close();
            }
        }
    }

    // Recieve multicast messages
    public void recieveMulticast() throws IOException, ClassNotFoundException {
        byte[] buf = new byte[5000];
        MulticastSocket socket = new MulticastSocket(9000);
        InetAddress group = InetAddress.getByName("230.0.0.0");
        socket.joinGroup(group);
        while (true) {
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            socket.receive(packet);
            ByteArrayInputStream inputByteStream = new ByteArrayInputStream(buf);
            ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(inputByteStream));
            GameMessage msg = (GameMessage)ois.readObject();

            executeGridOperation(msg);

            System.out.println("Recieved Multicast message");
            System.out.println(msg);
            System.out.println(socket.getInterface());
            System.out.println(socket.getNetworkInterface());
            System.out.println(socket.getTimeToLive());
        }
    }

    // Execute grid operation
    public void executeGridOperation(GameMessage msg) {
        int row = msg.getRow();
        int col = msg.getCol();
        switch(msg.getType()) {
            case CELL_ACQUIRE:
                // Lock cell
                if (msg.getIsValid()) {
                    grid.acquireCell(row, col);
                    System.out.println("Acquired cell[" + row + "][" + col + "]");
                }
                else {
                    System.out.println("Invalid move!");
                }
                break;
            case CELL_RELEASE:
                // Release cell
                grid.freeCell(row, col);
                System.out.println("Released cell[" + row + "][" + col + "]");
                // Fill owned cell

                break;
            default:
                System.out.println("Invalid move!");
        }
    }
}
