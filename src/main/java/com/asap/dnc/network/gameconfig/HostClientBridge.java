package com.asap.dnc.network.gameconfig;

import com.asap.dnc.gameconfig.GameConfig;

import java.time.Clock;

/**
 * Encapsulates and abstracts communication between client and host server
 * during game establishment phase. Used by MenuFX to separate networking
 * details from game logic
 */
public interface HostClientBridge {

    public boolean connectLocalHostServer(GameConfig config);

    public void closeLocalHostServer();

    public boolean isLocalHostServer();

    public boolean connectRemoteHostServer(String hostAddress);

    public boolean reconfigRemoteHostServer();

    public boolean checkHostAlive();

    public Object getHostServerInfo();

    public Object[] getAllClients();

    public GameConfig getHostClientConfiguration();

    public Object getClientInfo();

    public Clock getHostClientClock();

    public  void setConnectionResponseHandler(ConnectionResponseHandler connectionResponseHandler);

}
