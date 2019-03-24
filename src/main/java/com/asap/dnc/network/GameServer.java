package com.asap.dnc.network;

import com.asap.dnc.core.GameMessage;
import com.asap.dnc.core.PenColor;
import com.asap.dnc.network.gameconfig.host.HostServer;

import java.net.*;
import java.util.PriorityQueue;
import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Enumeration;

/**
 * Should be able to receive and buffer incoming packets in a priority queue
 * based on a Message's timestamp. Deserialization of messages should be done by the
 * network in order for them to be placed in the priority queue. Additional information
 * about the clients address/port will needed to be passed into the executing thread in
 * order for the thread to respond to the client.
 */
public class GameServer {

    private static int DEFAULT_PORT = 9000;

    //private int nThreadPoolSize;
    //private List<ClientThread> clientThreads;
    private ClientInfo[] _clientInformationArr;
    private HashMap<PenColor, ClientInfo> _clientInformation;
    private DatagramSocket socket;
    private byte[] buf = new byte[2048];
    private boolean hasMessage;
    private ServerGrid grid;

    public GameServer (ClientInfo[] _clientInformationArr){
        this._clientInformationArr = _clientInformationArr;
        this._clientInformation = new HashMap<>();
        // Make Hashmap from client information array
        for (ClientInfo client: _clientInformationArr){
            this._clientInformation.put(client.getPenColor(), client);
            System.out.println("This is server..."+ client.getPenColor());
        }
    }

    // Priority queue to be shared by all players
    private static PriorityQueue<GameMessage> messages = new PriorityQueue<>();


    public void init(int gridSize) {
        //System.out.println(Arrays.asList(_clientInformation));
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

                // Todo: listening = False

            }catch (IOException e){
                e.printStackTrace();
            } catch (ClassNotFoundException e){
                e.printStackTrace();
            }
        }
    }

    // Method to update Priority Queue
    public synchronized void updateMessageQueue(GameMessage msg){
        while (hasMessage) {
            // no room for new message
            try {
                wait();  // release the lock of this object
            } catch (InterruptedException e) { }
        }
        // acquire the lock and continue
        hasMessage = true;
        System.out.println("updating message queue");
        //add packet to messages queue with priority on timestamp
        messages.add(msg);

        // notify processing Priority queue thread to take over
        notify();
    }

    // // Method to process Priority Queue
    public synchronized void processMessageQueue(){
        while (!hasMessage){
            // no new message
            try {
                wait();  // release the lock of this object
            } catch (InterruptedException e) { }
        }
        // acquire the lock and continue
        hasMessage = false;
        System.out.println("--- Popping message queue ---");
        while (!messages.isEmpty()){
            GameMessage msg = messages.remove();
            System.out.println("\nmsg....: "+msg+"\n");
//            if (msg.getType() == MessageType.CELL_ACQUIRE){
//                // Attempt to acquire cell
//                if (this.grid.acquireCell(msg.getRow(), msg.getCol()) != null) {
//                    //System.out.println(" successfully acquired Cell[" + msg.getRow() + "][" + msg.getCol() + "]");
//
//
//                } else {
//                    //System.out.println("* " + " failed to acquire Cell[" + row + "][" + col + "], trying another cell...");
//                }
//            } else if (msg.getType() == MessageType.CELL_RELEASE){
//
//            }

            if (msg.getType() == MessageType.CELL_ACQUIRE){
                System.out.println("Trying to acquire cell..");
                // Attempt to acquire cell
                if (this.grid.acquireCell(msg.getRow(), msg.getCol()) != null) {
                    System.out.println(" successfully acquired Cell[" + msg.getRow() + "][" + msg.getCol() + "]");
                    try{
                        msg.setIsValid(true);
                        // Send unicast udp packet to each player
                        for (ClientInfo player: _clientInformationArr){
                            Thread playerMsg = new ServerUdpUnicast(player.getAddress(), 8000, msg);
                            playerMsg.start();
                        }
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                // failed to acquire cell
                } else {
                    System.out.println("* " + " failed to acquire Cell[" + msg.getRow() + "][" + msg.getCol() + "], trying another cell...");
                    try {
                        // Send unicast udp packet to player who sent the message
                        msg.setIsValid(false);
                        ClientInfo player = _clientInformation.get(msg.getPenColor());
                        Thread playerMsg = new ServerUdpUnicast(player.getAddress(), player.getPort(), msg);
                        playerMsg.start();

                    } catch (Exception e){
                        e.printStackTrace();
                    }

                }
            } else if (msg.getType() == MessageType.CELL_RELEASE){
                if (msg.getIsOwned()){   // player has successfully filled cell
                    this.grid.setCellOwner(msg.getRow(), msg.getCol(), msg.getPenColor());
                } else {     // player was not able to fill cell above threshold
                    this.grid.freeCell(msg.getRow(), msg.getCol());
                }
                try{
                    // Send unicast udp packet to each player with release update
                    for (ClientInfo player: _clientInformationArr){
                        Thread playerMsg = new ServerUdpUnicast(player.getAddress(), 8080, msg);
                        playerMsg.start();
                    }
                } catch (Exception e){
                    e.printStackTrace();
                }
            }

        }
        // notify updating Priority queue (main) thread to take over
        notify();
    }

    public class ClientThread extends Thread {

        //int gridSize;

        public ClientThread() {
            //this.gridSize = gridSize;
        }

        public void run() {
            System.out.println("Starting processing PQ thread....");
            try {
                while (true) {
                    processMessageQueue();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    // Main method to test concurrently acquiring cells in the server
//    public static void main(String[] args) {
//        ClientInfo c1 = new ClientInfo("127.0.0.1", 8000);
//        ClientInfo c2 = new ClientInfo("123.32.122.17", 8000);
//        ClientInfo c3 = new ClientInfo("123.32.122.18", 8000);
//        ClientInfo c4 = new ClientInfo("123.32.122.19", 8000);
//        c1.setPenColor(PenColor.BLUE);
//        c2.setPenColor(PenColor.RED);
//        c3.setPenColor(PenColor.GREEN);
//        c4.setPenColor(PenColor.YELLOW);
//        ClientInfo[] _clientInformation = new ClientInfo[4];
//        _clientInformation[0] = c1;
//        _clientInformation[1] = c2;
//        _clientInformation[2] = c3;
//        _clientInformation[3] = c4;
//        GameServer gameServer = new GameServer(_clientInformation);
//        gameServer.init(5);
//
//    }
}
