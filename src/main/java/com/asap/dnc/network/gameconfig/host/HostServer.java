package com.asap.dnc.network.gameconfig.host;

import com.asap.dnc.network.ClientInfo;
import com.asap.dnc.core.PenColor;
import com.asap.dnc.network.gameconfig.ConnectionResponseHandler;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * TCP Server used to accept connections when hosting a game. Member clientInformation
 * will contain address information for each connected player. This class is implemented
 * as a singleton, and is configured/re-configured via the init and clear methods.
 */
public class HostServer {

    public static int DEFAULT_PORT = 5000;
    private static int MAX_CONNECTIONS = PenColor.values().length;

    private ClientInfo[] clientInformation;
    private InetAddress address;
    private int nConnections;
    private int port;
    private boolean isSet;

    // singleton
    private static HostServer hostServer = new HostServer();

    public static HostServer getHostServer() {
        return hostServer;
    }

    public ClientInfo[] getClientInformation() {
        return clientInformation;
    }

    public boolean isSet() {
        return isSet;
    }

    public void listenClientConnections() throws Exception {
        if (!isSet) {
            throw new Exception("Connection Server has not been initialized");
        }

        int nClients = 0;
        final PenColor[] penColors = PenColor.values();
        ConnectionThread.init(clientInformation, penColors);

        try (ServerSocket serverSocket = new ServerSocket(port, nConnections, address)) {
            ConnectionThread[] threads = new ConnectionThread[nConnections];
            while (nClients != nConnections) {
                Socket clientConnection = serverSocket.accept();
                ConnectionThread connectionThread = new ConnectionThread(clientConnection);
                connectionThread.start();
                threads[nClients++] = connectionThread;
            }
            for (int i = 0; i < threads.length; i++) {
                threads[i].join();
            }
            assert(clientInformation == ConnectionThread.getClientInformation());
            isSet = true;
        }
    }

    public void init(InetAddress address, int port, int nConnections) throws Exception {
        if (nConnections > MAX_CONNECTIONS) {
            throw new Exception("The number of target connections cannot exceed  " + MAX_CONNECTIONS);
        }
        this.address = address;
        this.port = port;
        this.nConnections = nConnections;
        this.clientInformation = new ClientInfo[nConnections];
        this.isSet = true;
    }

    public void clear() {
        this.clientInformation = null;
        this.isSet = false;
    }

    private HostServer() {
        // ensure singleton class
    }

    public static void main(String[] args) {
        HostServer server = HostServer.getHostServer();
        try {
            server.init(InetAddress.getLocalHost(), 8000, 2);
            server.listenClientConnections();
            server.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
