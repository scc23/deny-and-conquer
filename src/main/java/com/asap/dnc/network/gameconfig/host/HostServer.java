package com.asap.dnc.network.gameconfig.host;

import com.asap.dnc.gameconfig.GameConfig;
import com.asap.dnc.network.ClientInfo;
import com.asap.dnc.core.PenColor;

import java.io.IOException;
import java.net.*;

/**
 * TCP Server used to accept connections when hosting a game. Member clientInformation
 * will contain address information for each connected player. This class is implemented
 * as a singleton, and is configured/re-configured via the init and clear methods.
 */
public class HostServer {

    public static int DEFAULT_PORT = 5000;
    private static int MAX_CONNECTIONS = PenColor.values().length;

    private ClientInfo[] clientInformation;
    private Thread[] connectionThreads;
    private InetAddress address;
    private ServerSocket serverSocket;
    private GameConfig config;
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

    public void listenClientConnections(boolean hasTimeOut) throws Exception {
        if (!isSet) {
            throw new Exception("Connection Server has not been initialized");
        }

        int nClients = 0;
        final PenColor[] penColors = PenColor.values();
        ConnectionThread.init(clientInformation, penColors, config);

        int nConnections = config.getNumberPlayers();
        serverSocket = new ServerSocket(port, nConnections, address);
        if (hasTimeOut) {
            serverSocket.setSoTimeout(5000); // 5 second timeout
        }

        while (nClients != config.getNumberPlayers()) {
            try {
                Socket clientConnection = serverSocket.accept();
                clientConnection.setKeepAlive(true);
                ConnectionThread connectionThread = new ConnectionThread(clientConnection);
                connectionThread.start();
                connectionThreads[nClients++] = connectionThread;
            } catch (SocketTimeoutException e) {
                int nPlayers = config.getNumberPlayers() - 1;
                System.out.println("It appears one of the connected clients have disconnected...");
                System.out.println("Reducing the number of players to " + nPlayers);
                if (nPlayers == 0) {
                    serverSocket.close();
                    throw e;
                }
                config.setNumberPlayers(nPlayers);
            }
        }
        assert(clientInformation == ConnectionThread.getClientInformation());
        isSet = true;
    }

    public void init(InetAddress address, int port, GameConfig config) throws Exception {
        if (config.getNumberPlayers() > MAX_CONNECTIONS) {
            throw new Exception("The number of target connections cannot exceed  " + MAX_CONNECTIONS);
        } else if (this.isSet) {
            throw new Exception("Host server configuration must be cleared before reinitialization");
        }
        this.address = address;
        this.port = port;
        this.config = config;
        this.clientInformation = new ClientInfo[config.getNumberPlayers()];
        this.connectionThreads = new ConnectionThread[config.getNumberPlayers()];
        this.isSet = true;
    }

    public void clear() {
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        terminateConnectionThreads();
        this.isSet = false;
    }

    private void terminateConnectionThreads() {
        if (connectionThreads != null) {
            for (Thread conn : connectionThreads) {
                conn.interrupt();
            }
        }
    }

    private HostServer() {
        // ensure singleton class
    }

    public static void main(String[] args) {

    }

}
