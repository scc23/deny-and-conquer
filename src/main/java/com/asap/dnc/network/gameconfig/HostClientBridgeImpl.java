package com.asap.dnc.network.gameconfig;

import com.asap.dnc.gameconfig.GameConfig;
import com.asap.dnc.network.ClientInfo;
import com.asap.dnc.network.gameconfig.client.ClientConnection;
import com.asap.dnc.network.gameconfig.host.HostServer;

import java.net.InetAddress;
import java.time.Clock;

public class HostClientBridgeImpl implements HostClientBridge {

    private ConnectionResponseHandler connectionResponseHandler;
    private ClientConnection clientConnection;
    private HostServerThread serverThread;
    private ClientInfo hostInfo;

    @Override
    public boolean connectLocalHostServer(GameConfig config) {
        try {
            InetAddress hostAddress = ClientConnection.getPublicIPV4Address();
            System.out.println(hostAddress.getHostAddress());
            if (serverThread != null) {
                serverThread.interrupt();
            }
            serverThread = new HostServerThread(hostAddress, config);
            serverThread.start();
            clientConnection = ClientConnection.connectToHostServer(hostAddress.getHostAddress(), HostServer.DEFAULT_PORT, true, connectionResponseHandler);
            hostInfo = new ClientInfo(hostAddress.getHostAddress(), HostServer.DEFAULT_PORT);
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
    public GameConfig getHostClientConfiguration() {
        return clientConnection.getConfiguration();
    }

    @Override
    public Object getClientInfo() {
        return clientConnection.getClientInfo();
    }

    @Override
    public Clock getHostClientClock() {
        return clientConnection.getClock();
    }

    @Override
    public boolean reconfigRemoteHostServer() {
        while (true) {
            ClientInfo newHost;
            try {
                newHost = clientConnection.determineNewHost();
            } catch (Exception e) {
                return false;
            }

            hostInfo = new ClientInfo(newHost.getHostName(), HostServer.DEFAULT_PORT);
            if (newHost.equals(clientConnection.getClientInfo())) {
                GameConfig config = getHostClientConfiguration();
                config.setNumberPlayers(config.getNumberPlayers() - 1);
                serverThread = new HostServerThread(newHost.getAddress(), config);
                //serverThread.setHasTimeOut(true);
                serverThread.start();
            }

            try {
                clientConnection.reconfigureHost(newHost, HostServer.DEFAULT_PORT, connectionResponseHandler);
                break;
            } catch (Exception e) {
                e.printStackTrace();
                newHost.isHost(true);
            }
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
        private GameConfig config;
        private boolean hasTimeOut;

        public HostServerThread(InetAddress hostAddress, GameConfig config) {
            this.hostAddress = hostAddress;
            this.config = config;
        }

        public void setHasTimeOut(boolean b) {
            hasTimeOut = b;
        }

        @Override
        public void run() {
            try {
                HostServer hostServer = HostServer.getHostServer();
                hostServer.clear();
                hostServer.init(hostAddress, HostServer.DEFAULT_PORT, config);
                hostServer.listenClientConnections(hasTimeOut);
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
