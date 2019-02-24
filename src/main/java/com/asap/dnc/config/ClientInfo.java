package com.asap.dnc.config;

import java.io.Serializable;

public class ClientInfo implements Serializable {

    private String address;
    private int port;
    private boolean isHost;

    private PenColor color;

    public ClientInfo(String address, int port) {
        this.address = address;
        this.port = port;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ClientInfo)) {
            return false;
        }
        ClientInfo clientInfo = (ClientInfo) o;
        return address.equals(clientInfo.getAddress()) && port == clientInfo.port;
    }

    public void setPenColor(PenColor color) {
        this.color = color;
    }

    public PenColor getPenColor() {
        return color;
    }

    public void isHost(boolean b) {
        isHost = b;
    }

    public boolean isHost() {
        return isHost;
    }

    public String getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }
}
