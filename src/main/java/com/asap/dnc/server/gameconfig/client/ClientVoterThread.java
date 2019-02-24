package com.asap.dnc.server.gameconfig.client;

import com.asap.dnc.config.ClientInfo;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Used by ClientConnection to send the local client's vote to remote clients
 * during host re-configuration.
 */
class ClientVoterThread extends Thread {

    private ClientInfo remoteClientInfo; // remote client to receive vote
    private ClientInfo localVote; // local client's vote

    public ClientVoterThread(ClientInfo remoteClientInfo, ClientInfo localVote) {
        this.remoteClientInfo = remoteClientInfo;
        this.localVote = localVote;
    }

    public void run() {
        try(Socket socket = new Socket(remoteClientInfo.getAddress(), remoteClientInfo.getPort());
            ObjectOutputStream os = new ObjectOutputStream(socket.getOutputStream())
        ) {
            os.writeObject(localVote);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
