package com.asap.dnc.network.gameconfig.client;

import com.asap.dnc.core.PenColor;

import java.io.Serializable;

public class ClientConfigMessage implements Serializable {
    private boolean isHost;
    private PenColor penColor;

    public ClientConfigMessage(boolean isHost, PenColor penColor) {
        this.isHost = isHost;
        this.penColor = penColor;
    }

    public boolean isHost() {
        return isHost;
    }

    public PenColor getPenColor() {
        return penColor;
    }

}
