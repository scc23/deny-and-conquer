package com.asap.dnc.server.gameconfig.client;

import com.asap.dnc.config.ClientInfo;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.*;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Represents a connection to a host server. Instances are created when connecting
 * to a specified host server. Private fields will be set after the connection has
 * been set up.
 */
public class ClientConnection {

    // instance fields set dynamically when connecting to host server
    private ClientInfo clientInfo;
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

        try (Socket socket = new Socket(address, port, pppIP4Addr, 0);
             OutputStream os = socket.getOutputStream();
             ObjectInputStream is = new ObjectInputStream(socket.getInputStream())
        ) {
            // save generated site local address and port
            connection.clientInfo = new ClientInfo(pppIP4Addr.getHostAddress(), socket.getLocalPort());

            // write to server whether client instance is hosting
            os.write((byte) (isHost ? 1 : 0));

            // block until all clients connected and server sends client information
            connection.connectedClients = (ClientInfo[]) is.readObject();
            connection.clientInfo.setTime(is.readLong());
        }

        return connection;
    }

    /**
     * Implements the protocol to establish a new host upon the previous host disconnecting.
     */
    public ClientInfo reconfigureHost() throws NullPointerException {
        // generate vote and list of clients to send vote to
        ClientInfo localVote = generateRandomVote();
        List<ClientInfo> remoteClients = Arrays.stream(connectedClients)
                .filter(ci -> !ci.equals(clientInfo) && !ci.isHost())
                .collect(Collectors.toList());

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
            // connect to host server
            ClientConnection client = ClientConnection.connectToHostServer("localhost", 8000, false);
            ClientInfo[] clientInformation = client.getConnectedClients();

            // output connected clients
            for(ClientInfo ci : clientInformation) {
                System.out.println("Connected client: ");
                System.out.println(ci.toString());
                System.out.println();
            }

            // reconfigure host server
            System.out.println("Reconfiguring host: ");
            ClientInfo newHost = client.reconfigureHost();
            System.out.println("New host: ");
            System.out.println(newHost.toString());
        } catch (Exception e) {
                e.printStackTrace();
        }
    }

}
