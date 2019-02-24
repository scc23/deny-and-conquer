package com.asap.dnc.server.gameconfig.client;

import com.asap.dnc.config.ClientInfo;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.*;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Map;

/**
 * Represents a connection to a host server. Instances are created when connecting
 * to a specified host server. Private fields will be set after the connection has
 * been set up.
 */
public class ClientConnection {

    // instance fields set dynamically when connecting to host server
    private String localAddress;
    private int localPort;
    private long localTime;
    private ClientInfo[] connectedClients;

    private ClientConnection() {
        // instantiate through connectToHostServer static factory method
    }

    /**
     * Joins a hosted game by connecting and synchronizing with the specified
     * connection server. The server connection is enapsulated within a ClientConnection
     * instance which contains the network info of the other connected clients and the server time.
     */
    public static ClientConnection connectToHostServer(String address, int port, boolean isHost) throws IOException, ClassNotFoundException {
        ClientConnection connection = new ClientConnection();
        final Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
        InetAddress siteLocalAddress = null;

        while (siteLocalAddress == null && networkInterfaces.hasMoreElements()) {
            NetworkInterface nwi = networkInterfaces.nextElement();
            final Enumeration<InetAddress> addresses = nwi.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress nwiAddr = addresses.nextElement();
                if (nwiAddr.isSiteLocalAddress()) {
                    siteLocalAddress = nwiAddr;
                    break;
                }
            }
        }
        connection.localAddress = siteLocalAddress.getHostAddress();

        try (Socket socket = new Socket(address, port, siteLocalAddress, 0);
             OutputStream os = socket.getOutputStream();
             ObjectInputStream is = new ObjectInputStream(socket.getInputStream())
        ) {
            // save system assigned local port
            connection.localAddress = socket.getLocalAddress().getHostAddress();

            // write to server whether client instance is hosting
            os.write((byte) (isHost ? 1 : 0));

            // block until all clients connected and server sends client information
            connection.connectedClients = (ClientInfo[]) is.readObject();
            connection.localTime = is.readLong();
        }

        return connection;
    }

    /**
     * Implements the protocol to establish a new host upon the previous host disconnecting.
     */
    public ClientInfo reconfigureHost() throws UnknownHostException, IOException {
        ClientInfo localClientInfo = new ClientInfo(localAddress, localPort);

        // generate vote and list of clients to send vote to
        ClientInfo localVote = generateRandomVote();
        ClientInfo[] remoteClients = (ClientInfo[]) Arrays.stream(connectedClients)
                .filter(ci -> !ci.equals(localClientInfo) && !ci.isHost())
                .toArray();

        // start thread listen to listen to votes from other clients
        ClientVoteListenerThread.init(remoteClients, localVote);
        ClientVoteListenerThread voteListenerThread = new ClientVoteListenerThread(localPort);
        voteListenerThread.start();

        // start threads to send local vote to other clients
        ClientVoterThread[] voterThreads = new ClientVoterThread[remoteClients.length];
        for (int i = 0; i < remoteClients.length; i++) {
            ClientInfo ci = remoteClients[i];
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
        int voteIdx = (int) Math.floor(Math.random() * connectedClients.length);
        return connectedClients[voteIdx];
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

    public ClientInfo[] getConnectedClients() {
        return this.connectedClients;
    }

    public static void main(String[] args) {
        try {
            ClientConnection client = ClientConnection.connectToHostServer("localhost", 8000, true);
            ClientInfo[] clientInformation = client.getConnectedClients();

            for(ClientInfo ci : clientInformation) {
                System.out.println(ci.getAddress());
                System.out.println(ci.getPort());
            }
        } catch (Exception e) {
                e.printStackTrace();
        }
    }

}
