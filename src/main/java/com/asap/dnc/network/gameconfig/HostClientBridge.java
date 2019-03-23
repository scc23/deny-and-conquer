package com.asap.dnc.network.gameconfig;


public interface HostClientBridge {

    public boolean connectLocalHostServer();

    public void closeLocalHostServer();

    public boolean isLocalHostServer();

    public boolean connectRemoteHostServer(String hostAddress);

    public boolean reconfigRemoteHostServer();

    public boolean checkHostAlive();

    public Object getHostServerInfo();

    public Object[] getAllClients();

    public  void setConnectionResponseHandler(ConnectionResponseHandler connectionResponseHandler);

}
