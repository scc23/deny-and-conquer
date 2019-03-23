package com.asap.dnc.core;

import com.asap.dnc.network.ClientInfo;
import com.asap.dnc.network.MessageType;
import com.asap.dnc.network.gameconfig.client.ClientGrid;

import java.io.*;
import java.net.*;
import java.sql.Timestamp;

public class CoreGameClient {
    private ClientGrid grid;
    private ClientInfo clientInfo;

    // Constructor to set client grid
    public CoreGameClient(ClientGrid grid, ClientInfo clientInfo) {
        this.grid = grid;
        this.clientInfo = clientInfo;
    }

    public void sendAcquireMessage(String address, int port, PenColor penColor, int row, int col) throws IOException {
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
        sendServerRequest(address, port, msg);
    }

    // Create game message to release cell to be sent to server
    public void sendReleaseMessage(String address, int port, PenColor penColor, int row, int col, int fillPercentage) throws IOException {
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
        sendServerRequest(address, port, msg);
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
                this.executeGridOperation(msg);
            } catch (SocketTimeoutException e) {
                System.out.println("Timeout reached: " + e);
                socket.close();
            }
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
                    this.grid.acquireCell(row, col);
                    System.out.println("Acquired cell[" + row + "][" + col + "]");
                }
                else {
                    System.out.println("Invalid move: Acquire cell[" + row + "][" + col + "]");
                }
                break;
            case CELL_RELEASE:
                // Release cell
                grid.freeCell(row, col);
                System.out.println("Released cell[" + row + "][" + col + "]");

                // Check if the cell is owned
                if (msg.getIsOwned()) {
                    // Set the cell to be owned
                    this.grid.setCellOwner(row, col, penColor);
                }
                break;
            default:
                System.out.println("Invalid move!");
        }
    }

}
