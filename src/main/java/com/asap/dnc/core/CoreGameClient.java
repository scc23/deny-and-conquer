package com.asap.dnc.core;

import com.asap.dnc.network.MessageType;

import java.io.*;
import java.net.*;
import java.sql.Timestamp;
import java.time.Clock;

public class CoreGameClient {
    private InetAddress serverAddress;
    private PenColor clientColor;
    private int serverPort;
    private Clock clock;
    private DatagramSocket socket;

    // Constructor to set client grid
    public CoreGameClient(InetAddress serverAddress, PenColor clientColor, int clientPort) {
        System.out.println(serverPort);
        System.out.println(serverAddress);
        this.serverAddress = serverAddress;
        this.serverPort = 9000;
        this.clientColor = clientColor;

        try{
            // for testing set port manually to 8000
            this.socket = new DatagramSocket(clientPort);
        } catch (BindException e){
            System.out.println(e);
            e.printStackTrace();
        } catch (SocketException e){
            e.printStackTrace();
        }

    }

    public void setClock(Clock clock) {
        this.clock = clock;
    }

    // Reset server address on fault tolerance
    public void setServerAddress(InetAddress serverAddress) {
        this.serverAddress = serverAddress;
    }

    public void sendAcquireMessage(int row, int col) throws IOException {
        System.out.println("Sending acquire message to server...");

        // Get timestamp
        Timestamp timestamp = new Timestamp(clock.millis());

        // Create acquire game message
        GameMessage msg = new GameMessage(MessageType.CELL_ACQUIRE, timestamp);
        // Set the pen color
        msg.setPenColor(this.clientColor);
        // Set the cell index to acquire
        msg.setRow(row);
        msg.setCol(col);

        // Call function to send message to server
        sendServerRequest(msg);
    }

    // Create game message to release cell to be sent to server
    public void sendReleaseMessage(int row, int col, double fillPercentage, PenColor owner) throws IOException {
        System.out.println("Sending release message to server...");

        // Get timestamp
        Timestamp timestamp = new Timestamp(clock.millis());

        // Create release game message
        GameMessage msg = new GameMessage(MessageType.CELL_RELEASE, timestamp);
        // Set the pen color
        msg.setPenColor(this.clientColor);
        // Set the cell index to release
        msg.setRow(row);
        msg.setCol(col);
        // Set fill percentage
        msg.setFillPercentage(fillPercentage);
        if (owner != null) {
            msg.setIsOwned();
        }

        // Call function to send message to server
        sendServerRequest(msg);
    }

    // Send message to server to validate grid operation
    public void sendServerRequest(GameMessage msg) throws IOException {
        DatagramSocket socket = new DatagramSocket();

        ByteArrayOutputStream bStream = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bStream);

        oos.writeObject(msg);
        oos.flush();

        byte[] buf = bStream.toByteArray();
        System.out.println("Server address: " + this.serverAddress);
        System.out.println("Server port: " + this.serverPort);
        DatagramPacket packet = new DatagramPacket(buf, buf.length, this.serverAddress, this.serverPort);
        System.out.println("Client message " +msg);
        socket.send(packet);

        oos.close();
    }

    // Receive response from server
    public GameMessage receiveServerResponse() throws IOException, ClassNotFoundException {

        socket.setReuseAddress(true);
        byte[] buf = new byte[5000];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);

        // Timeout if no response from server
        // socket.setSoTimeout(2000);

        while (true) {
            try {
                socket.receive(packet);
                ByteArrayInputStream inputByteStream = new ByteArrayInputStream(buf);
                ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(inputByteStream));
                GameMessage msg = (GameMessage) ois.readObject();
                System.out.println("Received uni-cast message from server...");
                System.out.println(msg);
                return msg;
                // this.executeGridOperation(msg);
            } catch (SocketTimeoutException e) {
                System.out.println("Timeout reached: " + e);
                socket.close();
            }
        }
    }

    public void sendUpdateStateMessage(int row, int col) throws IOException{
        System.out.println("Sending Update state message to server...");

        // Get timestamp
        Timestamp timestamp = new Timestamp(clock.millis());

        // Create acquire game message
        GameMessage msg = new GameMessage(MessageType.GET_CELL_STATE, timestamp);
        // Set the pen color
        msg.setPenColor(this.clientColor);
        // Set the cell index to acquire
        msg.setRow(row);
        msg.setCol(col);

        // Call function to send message to server
        sendServerRequest(msg);
    }

}
