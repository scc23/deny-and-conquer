package com.asap.dnc.network.gameconfig;


import com.asap.dnc.gameconfig.GameConfig;

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

    public  void setConnectionResponseHandler(ConnectionResponseHandler connectionResponseHandler);

}
