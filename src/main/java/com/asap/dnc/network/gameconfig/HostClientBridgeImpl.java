package com.asap.dnc.network.gameconfig;

import com.asap.dnc.network.ClientInfo;
import com.asap.dnc.network.gameconfig.client.ClientConnection;
import com.asap.dnc.network.gameconfig.host.HostServer;

import java.net.InetAddress;

public class HostClientBridgeImpl implements HostClientBridge {

    private ConnectionResponseHandler connectionResponseHandler;
    private ClientConnection clientConnection;
    private HostServerThread serverThread;
    private ClientInfo hostInfo;

    @Override
    public boolean connectLocalHostServer() {
        try {
            InetAddress hostAddress = ClientConnection.getPublicIPV4Address();
            if (serverThread != null) {
                serverThread.interrupt();
            }
            serverThread = new HostServerThread(hostAddress, 3);
            serverThread.start();
            clientConnection = ClientConnection.connectToHostServer(hostAddress.getHostAddress(), HostServer.DEFAULT_PORT, true, connectionResponseHandler);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public boolean connectRemoteHostServer(String hostAddress) {
        try {
            clientConnection = ClientConnection.connectToHostServer(hostAddress, HostServer.DEFAULT_PORT, false, connectionResponseHandler);
            hostInfo = new ClientInfo(hostAddress, HostServer.DEFAULT_PORT);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public boolean isLocalHostServer() {
        return clientConnection.getClientInfo().isHost();
    }

    @Override
    public ClientInfo getHostServerInfo() {
        return hostInfo;
    }

    @Override
    public ClientInfo[] getAllClients() {
        return clientConnection.getConnectedClients();
    }

    @Override
    public boolean reconfigRemoteHostServer() {
        try {
            ClientInfo newHost = clientConnection.generateNewHost();
            System.out.println("new host: " + newHost.toString());
            if (newHost.equals(clientConnection.getClientInfo())) {
                serverThread = new HostServerThread(newHost.getAddress(), clientConnection.getConnectedClients().length - 1);
                serverThread.start();
            }
            clientConnection.reconfigureHost(newHost, HostServer.DEFAULT_PORT);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public boolean checkHostAlive() {
        return clientConnection.sendKeepAlive();
    }

    /**
     * Sets appropriate handler to update UI on new connection
     */
    @Override
    public void setConnectionResponseHandler(ConnectionResponseHandler connectionResponseHandler) {
        this.connectionResponseHandler = connectionResponseHandler;
    }

    @Override
    public void closeLocalHostServer() {
        if (serverThread != null) {
            serverThread.interrupt();
        }
    }

    private class HostServerThread extends Thread {

        private InetAddress hostAddress;
        int nConnections;

        public HostServerThread(InetAddress hostAddress, int nConnections) {
            this.hostAddress = hostAddress;
            this.nConnections = nConnections;
        }

        @Override
        public void run() {
            try {
                HostServer hostServer = HostServer.getHostServer();
                hostServer.clear();
                hostServer.init(hostAddress, HostServer.DEFAULT_PORT, nConnections);
                hostServer.listenClientConnections();
                while (true) {
                    try {
                        sleep(60000);
                    } catch (InterruptedException e) {
                        hostServer.clear();
                        break;
                    }
                }
                hostServer.clear();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
