package com.asap.dnc.core;

import com.asap.dnc.network.MessageType;
import com.asap.dnc.network.gameconfig.client.ClientGrid;

import java.io.*;
import java.net.*;
import java.sql.Timestamp;

public class CoreGameClient implements CoreGameClient {
    private ClientGrid grid;

    // Constructor to set client grid
    public CoreGameClient(ClientGrid grid) {
        this.grid = grid;
    }

    public void sendAcquireMessage(String address, PenColor penColor, int row, int col) throws IOException {
        System.out.println("Sending acquire message to server...");

        // Get timestamp
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        // Create acquire game message
        GameMessage msg = new GameMessage(MessageType.CELL_ACQUIRE, timestamp);
        // Set the pen color
        msg.setPenColor(penColor);
        // Set the cell index to acquire
        msg.setRow(row);
        msg.setCol(col);

        // Call function to send message to server
        sendServerRequest(address, 5000, msg);
    }

    // Create game message to release cell to be sent to server
    public void sendReleaseMessage(String address, PenColor penColor, int row, int col, double fillPercentage) throws IOException {
        System.out.println("Sending release message to server...");

        // Get timestamp
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        // Create release game message
        GameMessage msg = new GameMessage(MessageType.CELL_RELEASE, timestamp);
        // Set the pen color
        msg.setPenColor(penColor);
        // Set the cell index to release
        msg.setRow(row);
        msg.setCol(col);
        // Set fill percentage
        msg.setFillPercentage(fillPercentage);

        // Call function to send message to server
        sendServerRequest(address, 5000, msg);
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
                System.out.println("Received uni-cast message..");
                System.out.println(msg);
                this.executeGridOperation(msg);
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

            this.executeGridOperation(msg);

            System.out.println("Received Multi-cast message");
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
        PenColor penColor = msg.getPenColor();

        switch(msg.getType()) {
            case CELL_ACQUIRE:
                // Lock cell
                if (msg.getIsValid()) {
                    System.out.println("Acquired cell[" + row + "][" + col + "]");
                }
                else {
                    System.out.println("Invalid move: Acquire cell[" + row + "][" + col + "]");
                }
                break;
            case CELL_RELEASE:
                // Release cell
                System.out.println("Released cell[" + row + "][" + col + "]");

                break;
            default:
                System.out.println("Invalid move!");
        }
    }

}
