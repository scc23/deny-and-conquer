package com.asap.dnc.gameconfig.controls;

import com.asap.dnc.gameconfig.GameConfig;
import com.asap.dnc.network.gameconfig.ConnectionResponseHandler;
import com.asap.dnc.network.gameconfig.client.ClientConnection;
import com.asap.dnc.network.gameconfig.host.HostServer;

import java.net.InetAddress;

public class MenuControllerImpl implements MenuController {

    private GameConfig gameConfig;
    private ConnectionResponseHandler connectionResponseHandler;
    private ClientConnection clientConnection;

    @Override
    public boolean onGameHost() {
        try {
            InetAddress hostAddress = ClientConnection.getPublicIPV4Address();
            HostServerThread serverThread = new HostServerThread(hostAddress);
            serverThread.start();
            clientConnection = ClientConnection.connectToHostServer(hostAddress.getHostAddress(), HostServer.DEFAULT_PORT, true, connectionResponseHandler);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    @Override
    public boolean onGameJoin(String hostAddress) {
        try {
            clientConnection = ClientConnection.connectToHostServer(hostAddress, HostServer.DEFAULT_PORT, false, connectionResponseHandler);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * Sets appropriate handler to update UI on new connection
     */
    @Override
    public void setConnectionResponseHandler(ConnectionResponseHandler connectionResponseHandler) {
        this.connectionResponseHandler = connectionResponseHandler;
    }

    /**
     * Sets required config for game (e.g., grid size, pen thickness), used later when starting game.
     */
    @Override
    public void setGameConfig(GameConfig gameConfig) {
        this.gameConfig = gameConfig;
    }

    @Override
    public void startGame() {

    }

    private class HostServerThread extends Thread {

        private InetAddress hostAddress;

        public HostServerThread(InetAddress hostAddress) {
            this.hostAddress = hostAddress;
        }

        @Override
        public void run() {
            try {
                HostServer hostServer = HostServer.getHostServer();
                hostServer.clear();
                hostServer.init(hostAddress, HostServer.DEFAULT_PORT, 4);
                hostServer.listenClientConnections();
                hostServer.clear();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
