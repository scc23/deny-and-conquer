package com.asap.dnc.network;

import com.asap.dnc.core.GameMessage;
import com.asap.dnc.core.PenColor;
import com.asap.dnc.network.gameconfig.host.HostServer;

import java.lang.reflect.Array;
import java.net.InetAddress;
import java.util.List;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.util.Random;
import java.util.PriorityQueue;
import java.util.Comparator;
import java.io.*;
//import java.sql.Timestamp;
//import java.util.concurrent.TimeoutException;
import java.net.SocketException;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Should be able to receive and buffer incoming packets in a priority queue
 * based on a Message's timestamp. Deserialization of messages should be done by the
 * network in order for them to be placed in the priority queue. Additional information
 * about the clients address/port will needed to be passed into the executing thread in
 * order for the thread to respond to the client.
 */
public class GameServer {

    //private int nThreadPoolSize;
    //private List<ClientThread> clientThreads;
    //private ClientInfo[] _clientInformation;
    private HashMap<PenColor, ClientInfo> _clientInformation;
    private DatagramSocket socket;
    private byte[] buf = new byte[2048];
    private boolean hasMessage;
    private ServerGrid grid;

    private GameServer (ClientInfo[] _clientInformationArr){
        this._clientInformation = new HashMap<>();
        for (ClientInfo client: _clientInformationArr){
            this._clientInformation.put(client.getPenColor(), client);
        }
    }


    private static PriorityQueue<GameMessage> messages = new PriorityQueue<>();


    public void init(int fillUnits, int length, int width) {
        System.out.println(Arrays.asList(_clientInformation));
        // network representation of grid
        this.grid = new ServerGrid(fillUnits, length, width);
        Thread t1 = new ClientThread();
        t1.setName("Processing PQ messages");
        t1.start();

        try{
            socket = new DatagramSocket(HostServer.DEFAULT_PORT);
        }catch (SocketException e){
            System.out.println("Could not open UDP socket on PORT - " + HostServer.DEFAULT_PORT);
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        boolean listening = true;
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        while(listening){
            System.out.println("Listening for UDP packets....");
            try{
                this.socket.receive(packet);
                ByteArrayInputStream byteStream = new ByteArrayInputStream(buf);
                ObjectInputStream iStream = new ObjectInputStream(new BufferedInputStream(byteStream));
                GameMessage msg = (GameMessage) iStream.readObject();
                iStream.close();
                System.out.println("Received message\n");
                //System.out.println(msg);
                updateMessageQueue(msg);

//                if (received.equals("end")) {
//                    listening = false;
//                    continue;
//                }


            }catch (IOException e){
                e.printStackTrace();
            } catch (ClassNotFoundException e){
                e.printStackTrace();
            }

        }
    }

    public class MulticastPublisher {
        private DatagramSocket socket;
        private InetAddress group;
        private byte[] buf;

        public void sendMulticast(GameMessage multicastMessage) throws IOException {
            socket = new DatagramSocket();
            group = InetAddress.getByName("224.0.0.0");

            ByteArrayOutputStream bStream = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bStream);
            oos.writeObject(multicastMessage);
            oos.flush();
            buf = bStream.toByteArray();

            DatagramPacket packet = new DatagramPacket(buf, buf.length, group, 9000);
            System.out.println("Sending multicast message to group" + group + " on port "+ 9000);
            socket.send(packet);
            socket.close();
        }
    }

    public class UdpUnicast {
        private DatagramSocket socket;
        private InetAddress address;
        private int port;

        public UdpUnicast(InetAddress address, int port) throws Exception{
            this.socket = new DatagramSocket();
            this.address = address;
            this.port = port;
        }

        private void sendMessage(GameMessage msg) throws Exception{
            ByteArrayOutputStream bStream = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bStream);
            oos.writeObject(msg);
            oos.flush();
            byte[] buf = bStream.toByteArray();
            DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);
            System.out.println("Sending unicast message to " + this.address + " on port "+ this.port);
            socket.send(packet);
        }
    }

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
        notify();
    }

    public synchronized void processMessageQueue(){
        System.out.println("\nSize: "+messages.size());
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
            MulticastPublisher mp = new MulticastPublisher();
            if (msg.getType() == MessageType.CELL_ACQUIRE){
                System.out.println("Trying to acquire cell..");
                // Attempt to acquire cell
                if (this.grid.acquireCell(msg.getRow(), msg.getCol()) != null) {
                    System.out.println(" successfully acquired Cell[" + msg.getRow() + "][" + msg.getCol() + "]");
                    try{
                        msg.setIsValid(true);
                        mp.sendMulticast(msg);
                    } catch (Exception e){
                        e.printStackTrace();
                    }

                } else {
                    System.out.println("* " + " failed to acquire Cell[" + msg.getRow() + "][" + msg.getCol() + "], trying another cell...");
                    try {
                        msg.setIsValid(false);
                        ClientInfo player = _clientInformation.get(msg.getPenColor());
                        UdpUnicast unicastMsg = new UdpUnicast(player.getAddress(), player.getPort());
                        unicastMsg.sendMessage(msg);
                    } catch (Exception e){
                        e.printStackTrace();
                    }

                }
            } else if (msg.getType() == MessageType.CELL_RELEASE){
                if (msg.getIsOwned()){
                    this.grid.setCellOwner(msg.getRow(), msg.getCol(), msg.getPenColor());
                } else {
                    this.grid.freeCell(msg.getRow(), msg.getCol());
                }
                try{
                    mp.sendMulticast(msg);
                } catch (Exception e){
                    e.printStackTrace();
                }
            }

        }
        notify();
    }

    // todo: implement
    public class ClientThread extends Thread {

        //int gridSize;

        public ClientThread() {
            //this.gridSize = gridSize;
        }

        public void run() {
            System.out.println("Starting thread....");
            try {
                while(true) {
                        processMessageQueue();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

//            int minSleepTime = 2000, maxSleepTime = 5000;
//            int row, col, sleepTime;
//            Random random = new Random();
//
//            System.out.println(getName() + " is running");
//
//            try {
//                while(true) {
//                    // Randomly select a cell to acquire
//                    row = random.nextInt(this.gridSize);
//                    col = random.nextInt(this.gridSize);
//
//                    // Attempt to acquire cell
//                    if (grid.acquireCell(row, col) != null) {
//                        System.out.println(getName() + " successfully acquired Cell[" + row + "][" + col + "]");
//
//                        // Sleep for random amount of time
//                        sleepTime = random.nextInt(maxSleepTime - minSleepTime + 1) + minSleepTime;
//                        Thread.sleep(sleepTime);
//
//                        // Free cell
//                        System.out.println(getName() + " freeing Cell[" + row + "][" + col + "]");
//                        grid.freeCell(row, col);
//                    }
//                    else {
//                        System.out.println("* " + getName() + " failed to acquire Cell[" + row + "][" + col + "], trying another cell...");
//                    }
//                }
//            } catch (Exception e) {
//                System.out.println(e);
//                e.printStackTrace();
//            }
        }

    }

    // Main method to test concurrently acquiring cells in the server
    public static void main(String[] args) {
        ClientInfo c1 = new ClientInfo("127.0.0.1", 8000);
        ClientInfo c2 = new ClientInfo("123.32.122.17", 8000);
        ClientInfo c3 = new ClientInfo("123.32.122.18", 8000);
        ClientInfo c4 = new ClientInfo("123.32.122.19", 8000);
        c1.setPenColor(PenColor.BLUE);
        ClientInfo[] _clientInformation = new ClientInfo[4];
        _clientInformation[0] = c1;
        _clientInformation[1] = c2;
        _clientInformation[2] = c3;
        _clientInformation[3] = c4;
        GameServer gameServer = new GameServer(_clientInformation);
        gameServer.init(10, 3, 3);

    }
}
