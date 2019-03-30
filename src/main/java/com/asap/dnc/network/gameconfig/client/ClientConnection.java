package com.asap.dnc.network.gameconfig.client;

import com.asap.dnc.core.PenColor;
import com.asap.dnc.gameconfig.GameConfig;
import com.asap.dnc.network.ClientInfo;
import com.asap.dnc.network.gameconfig.ConnectionResponseHandler;

import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Represents a connection to a host network. Instances are created when connecting
 * to a specified host network. Private fields will be set after the connection has
 * been set up.
 */
public class ClientConnection {

    // instance fields set dynamically when connecting to host network
    private ClientInfo clientInfo;
    private ClientInfo[] connectedClients;
    private GameConfig config;
    private Socket socket;

    private static ClientConnection clientConnection = new ClientConnection(); // singleton

    private ClientConnection() {
        // instantiate through connectToHostServer static factory method
    }

    /**
     * Returns a public point-to-point ipv4 address that can be used to communicate with other clients.
     */
    public static Inet4Address getPublicIPV4Address() throws SocketException {
        final Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
        Inet4Address pppIP4Addr = null;

        // scan through the network interfaces for a public point-to-point IPv4 address
        while (pppIP4Addr == null && networkInterfaces.hasMoreElements()) {
            NetworkInterface nwi = networkInterfaces.nextElement();
            final Enumeration<InetAddress> addresses = nwi.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress nwiAddr = addresses.nextElement();
                if (nwiAddr instanceof Inet4Address &&
                        !nwiAddr.isSiteLocalAddress() &&
                        !nwiAddr.isLinkLocalAddress() &&
                        !nwiAddr.isAnyLocalAddress() &&
                        !nwiAddr.isMulticastAddress()
                ) {
                    pppIP4Addr = (Inet4Address) nwiAddr;
                    break;
                }
            }
        }
        return pppIP4Addr;
    }

    /**
     * Joins a hosted gameconfig by connecting and synchronizing with the specified
     * connection network. The network connection is enapsulated within a ClientConnection
     * instance which contains the network info of the other connected clients and the network time.
     */
    public static ClientConnection connectToHostServer(String address, int port, boolean isHost, ConnectionResponseHandler connectionResponseHandler)
            throws IOException, ClassNotFoundException {

        if (clientConnection.socket != null && !clientConnection.socket.isClosed()) {
            clientConnection.socket.close();
        }

        Inet4Address pppIP4Addr = getPublicIPV4Address();

        Socket socket = new Socket(address, port, pppIP4Addr, 0);
        socket.setKeepAlive(true);

        ObjectOutputStream os = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream is = new ObjectInputStream(socket.getInputStream());

        // save generated site local address and port
        clientConnection.clientInfo = new ClientInfo(pppIP4Addr.getHostAddress(), socket.getLocalPort());
        clientConnection.clientInfo.isHost(isHost);

        // write to network whether client instance is hosting
        os.writeObject(new ClientConfigMessage(isHost, null));
        os.flush();

        // fetch remaining number of clients that need to join
        int numClients = is.readInt();
        connectionResponseHandler.updateRemaining(numClients);

        if (numClients == 0) { // read extra data sent by connection thread
            is.readInt();
        }
        while (numClients > 0) {
           numClients = is.readInt();
           if (connectionResponseHandler != null) {
               connectionResponseHandler.updateRemaining(numClients);
           }
        }

        // block until all clients connected and network sends client information
        clientConnection.config = (GameConfig) is.readObject();
        clientConnection.connectedClients = (ClientInfo[]) is.readObject();
        clientConnection.clientInfo.setPenColor((PenColor) is.readObject());
        clientConnection.clientInfo.setTime(is.readLong());
        clientConnection.socket = socket;

        return clientConnection;
    }

    /**
     * Pings the host server to verify if connection is still alive
     */
    public boolean sendKeepAlive() {
        if (clientInfo.isHost()) {
            return true;
        }
        try {
            OutputStream os = socket.getOutputStream();
            os.write(0);
            os.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Connect to a new host server
     */
    public void reconfigureHost(ClientInfo hostInfo, int serverPort) throws IOException, ClassNotFoundException {
        if (!socket.isClosed()) {
            socket.close();
        }

        int count = 0;
        while (true) {
            try {
                socket = new Socket(
                        hostInfo.getAddress(),
                        serverPort,
                        clientInfo.getAddress(),
                        clientInfo.getPort());
                break;
            } catch (IOException e) {
                if (count++ < 5) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e1) {
                        throw e;
                    }
                } else {
                    throw e;
                }
            }
        }

        ObjectOutputStream os = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream is = new ObjectInputStream(socket.getInputStream());

        clientInfo.isHost(hostInfo.equals(clientInfo));
        System.out.println(clientInfo);

        ClientConfigMessage msg = new ClientConfigMessage(clientInfo.isHost(), clientInfo.getPenColor());
        os.writeObject(msg);
        os.flush();

        int nRemainingConnections = is.readInt();
        if (nRemainingConnections == 0) { // read extra data sent by connection thread
            is.readInt();
        }

        config = (GameConfig) is.readObject();
        connectedClients = (ClientInfo[]) is.readObject();
        clientInfo.setPenColor((PenColor) is.readObject());
        clientInfo.setTime(is.readLong());
    }

    /**
     * Implements the protocol to establish a new host upon the previous host disconnecting.
     */
    public ClientInfo generateNewHost() throws Exception {
        // generate vote and list of clients to send vote to
        ClientInfo localVote = generateRandomVote();
        List<ClientInfo> remoteClients = Arrays.stream(connectedClients)
                .filter(ci -> ci.equals(clientInfo) || ci.isHost() ? false : true)
                .collect(Collectors.toList());

        System.out.println("remote clients:");
        for (ClientInfo ci : remoteClients) {
            System.out.println(ci.toString());
        }

        if (remoteClients.isEmpty()) {
            throw new Exception("Not enough clients are connected");
        }

        // start thread listen to listen to votes from other clients
        ClientVoteListenerThread voteListenerThread = null;
        ClientVoteListenerThread.init(remoteClients, localVote);
        voteListenerThread = new ClientVoteListenerThread(clientInfo);
        voteListenerThread.start();

        // wait for listener's thread socket to be up before creating voter threads
        while (!voteListenerThread.isServerSocketListening()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // start threads to send local vote to other clients
        ClientVoterThread[] voterThreads = new ClientVoterThread[remoteClients.size()];
        for (int i = 0; i < remoteClients.size(); i++) {
            ClientInfo ci = remoteClients.get(i);
            ClientVoterThread voterThread = new ClientVoterThread(ci, localVote);
            voterThread.start();
            voterThreads[i] = voterThread;
        }

        // wait for voter threads and listener thread to finish
        try {
            for (ClientVoterThread thread : voterThreads) {
                thread.join();
            }
            voteListenerThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return getNewHostFromVotes(voteListenerThread.getVoteCountMap());
    }

    private ClientInfo generateRandomVote() {
        List<ClientInfo> remClients = Arrays.stream(connectedClients)
                .filter(ci -> !ci.isHost() ? true : false)
                .collect(Collectors.toList());
        int voteIdx = (int) Math.floor(Math.random() * remClients.size());
        return remClients.get(voteIdx);
    }

    private ClientInfo getNewHostFromVotes(Map<ClientInfo, Integer> voteCountMap) {
        ClientInfo host = null;
        int maxVotes = -1;
        for (ClientInfo ci : voteCountMap.keySet()) {
            int numVotes = voteCountMap.get(ci);
            if (numVotes > maxVotes) {
                maxVotes = numVotes;
                host = ci;
            }
        }
        return host;
    }

    public ClientInfo getClientInfo() {
        return this.clientInfo;
    }

    public ClientInfo[] getConnectedClients() {
        return this.connectedClients;
    }

    public GameConfig getConfiguration() {
        return this.config;
    }

    public static void main(String[] args) {
        try {
            // connect to host network
            ClientConnection client = ClientConnection.connectToHostServer("localhost", 8000, false, null);
            ClientInfo[] clientInformation = client.getConnectedClients();

            // output connected clients
            for(ClientInfo ci : clientInformation) {
                System.out.println("Connected client: ");
                System.out.println(ci.toString());
                System.out.println();
            }

            // reconfigure host network
            System.out.println("Reconfiguring host: ");
            ClientInfo newHost = client.generateNewHost();
            System.out.println("New host: ");
            System.out.println(newHost.toString());
        } catch (Exception e) {
                e.printStackTrace();
        }
    }

}
