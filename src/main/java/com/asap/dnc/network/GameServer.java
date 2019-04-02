package com.asap.dnc.network;

import com.asap.dnc.core.Cell;
import com.asap.dnc.core.GameMessage;
import com.asap.dnc.core.PenColor;

import java.net.*;
import java.util.PriorityQueue;
import java.io.*;
import java.util.HashMap;

/**
 * Should be able to receive and buffer incoming packets in a priority queue
 * based on a Message's timestamp. Deserialization of messages should be done by the
 * network in order for them to be placed in the priority queue. Additional information
 * about the clients address/port will needed to be passed into the executing thread in
 * order for the thread to respond to the client.
 */
public class GameServer {

    private static final int DEFAULT_PORT = 9000;
    private final ClientInfo[] _clientInformationArr;
    private HashMap<PenColor, ClientInfo> _clientInformation;
    private DatagramSocket socket;
    private final byte[] buf = new byte[2048];
    private boolean hasMessage;
    private ServerGrid grid;

    public GameServer (ClientInfo[] _clientInformationArr){
        this._clientInformationArr = _clientInformationArr;
        this._clientInformation = new HashMap<>();
        for (ClientInfo client: _clientInformationArr){
            this._clientInformation.put(client.getPenColor(), client);
            System.out.println("This is server..."+ client.getPenColor());
        }
    }

    /**
     * Priority queue to be shared by all players
     * This priority queue holds udp messages with priority on timestamp
     * Threads share this PQ so should be accessed in synchronized manner
     */
    private static PriorityQueue<GameMessage> messages = new PriorityQueue<>();

    /**
     * Initialize grid, spawn thread for priority queue processing,
     * and open socket for listening UDP messges in main thread
     * @param gridSize
     */
    public void init(int gridSize) {
        // network representation of grid
        this.grid = new ServerGrid(gridSize);

        // Start a Priority queue processing thread
        Thread t1 = new ClientThread();
        t1.setName("Processing PQ messages");
        t1.start();

        // open a UDP socket for getting udp packets from all players
        try{
            socket = new DatagramSocket(DEFAULT_PORT);
        }catch (SocketException e){
            System.out.println("Could not open UDP socket on PORT - " + DEFAULT_PORT);
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        boolean listening = true;   // keep listening for client udp messages until threads are killed
        DatagramPacket packet = new DatagramPacket(buf, buf.length);

        // keep listening until game is over
        while(listening){
            System.out.println("Listening for UDP packets....");
            try{
                this.socket.receive(packet);
                ByteArrayInputStream byteStream = new ByteArrayInputStream(buf);
                ObjectInputStream iStream = new ObjectInputStream(new BufferedInputStream(byteStream));
                GameMessage msg = (GameMessage) iStream.readObject();
                iStream.close();
                System.out.println("Received message\n");
                updateMessageQueue(msg);

            }catch (IOException e){
                e.printStackTrace();
                break;
            } catch (ClassNotFoundException e){
                e.printStackTrace();
            }
        }
    }

    /**
     * Closes udp socket
     */
    public void clear() {
        if (this.socket != null && !this.socket.isClosed()) {
            this.socket.close();
        }
    }

    /**
     * Performs reconfiguration of gamer server
     * @param gridSize
     * @param cells
     */
    public void initReconfig(int gridSize, Cell[][] cells) {
        // network representation of grid
        this.grid = new ServerGrid(gridSize, cells);

        // Start a Priority queue processing thread
        Thread t1 = new ClientThread();
        t1.setName("Processing PQ messages");
        t1.start();

        // open a UDP socket for getting udp packets from all players
        try{
            socket = new DatagramSocket(DEFAULT_PORT);
        }catch (SocketException e){
            System.out.println("Could not open UDP socket on PORT - " + DEFAULT_PORT);
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        boolean listening = true;
        DatagramPacket packet = new DatagramPacket(buf, buf.length);

        // keep listening until game is over
        while(listening){
            System.out.println("Listening for UDP packets....");
            try{
                this.socket.receive(packet);
                ByteArrayInputStream byteStream = new ByteArrayInputStream(buf);
                ObjectInputStream iStream = new ObjectInputStream(new BufferedInputStream(byteStream));
                GameMessage msg = (GameMessage) iStream.readObject();
                iStream.close();
                System.out.println("Received message\n");
                updateMessageQueue(msg);

            }catch (IOException | ClassNotFoundException e){
                e.printStackTrace();
            }
        }
    }

    /**
     * synchronized method to update Priority Queue
     * @param msg
     */
    private synchronized void updateMessageQueue(GameMessage msg){
        while (hasMessage) {
            // no room for new message
            try {
                wait();  // release the lock of this object
            } catch (InterruptedException ignored) { }
        }
        // acquire the lock and continue
        hasMessage = true;
        System.out.println("updating message queue");
        //add packet to messages queue with priority on timestamp
        messages.add(msg);

        // notify processing Priority queue thread to take over
        notify();
    }

    /**
     * synchronized method to process Priority Queue
     */
    private synchronized void processMessageQueue(){
        while (!hasMessage){
            // no new message
            try {
                wait();  // release the lock of this object
            } catch (InterruptedException ignored) { }
        }
        // acquire the lock and continue
        hasMessage = false;
        System.out.println("--- Popping message from queue ---");
        while (!messages.isEmpty()){
            GameMessage msg = messages.remove();
            System.out.println("\nServer reading message from PO....: "+msg+"\n");

            int row = msg.getRow();
            int col = msg.getCol();
            PenColor playerColor = msg.getPenColor();

            if (msg.getType() == MessageType.CELL_ACQUIRE){
                System.out.println("Trying to acquire cell..");
                // Attempt to acquire cell
                if (this.grid.acquireCell(row, col, playerColor) != null) {
                    System.out.println(" successfully acquired Cell[" +row + "][" +col + "] for player: " + playerColor);
                    try{
                        msg.setIsValid(true);
                        // Send unicast udp packet to each player
                        for (ClientInfo player: _clientInformationArr){
                            Thread playerMsg = new ServerUdpUnicast(player.getAddress(), player.getPort(), msg);
                            playerMsg.start();
                        }
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                // failed to acquire cell
                } else {
                    System.out.println("Server" + " failed to acquire Cell[" + row + "][" + col + "]");
                    try {
                        // Send uni-cast udp packet to player who sent the message
                        msg.setIsValid(false);
                        ClientInfo player = _clientInformation.get(playerColor);
                        Thread playerMsg = new ServerUdpUnicast(player.getAddress(), player.getPort(), msg);
                        playerMsg.start();

                    } catch (Exception e){
                        e.printStackTrace();
                    }

                }
            } else if (msg.getType() == MessageType.CELL_RELEASE){
                if (msg.getIsOwned()){   // player has successfully filled cell
                    this.grid.setCellOwner(row, col, playerColor);
                } else {     // player was not able to fill cell above threshold
                    this.grid.freeCell(row,col);
                }
                try{
                    // Send uni-cast udp packet to each player with release update
                    for (ClientInfo player: _clientInformationArr){
                        Thread playerMsg = new ServerUdpUnicast(player.getAddress(), player.getPort(), msg);
                        playerMsg.start();
                    }
                } catch (Exception e){
                    e.printStackTrace();
                }
            } else if (msg.getType() == MessageType.GET_CELL_STATE){
                if (this.grid.getIsLocked(row, col)) {  // acquired by some player
                    msg.setIsValid(true);
                    msg.setAcquiredOwner(this.grid.getAcquiredOwner(row, col)); // get player who acquired

                    if (this.grid.getCellOwner(row, col) != null){  // check if cell owned by player
                        msg.setIsOwned();
                    }
                } else { // Not acquired by any player
                    msg.setIsValid(false);
                }

                try {
                    // Send unicast udp packet to player who sent the message
                    ClientInfo player = _clientInformation.get(playerColor);
                    Thread playerMsg = new ServerUdpUnicast(player.getAddress(), player.getPort(), msg);
                    playerMsg.start();

                } catch (Exception e){
                    e.printStackTrace();
                }
            }

        }
        // notify main thread (that updates Priority queue) to take over
        notify();
    }

    /**
     * This class starts a thread to process messages from Priority queue,
     * with priority based on timestamp
     */
    public class ClientThread extends Thread {

        @Override
        public void run() {
            System.out.println("Starting thread for processing Priority queue....");
            try {
                while (true) {
                    // call synchronized method
                    processMessageQueue();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
