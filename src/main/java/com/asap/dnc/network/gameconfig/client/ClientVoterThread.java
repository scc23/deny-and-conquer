package com.asap.dnc.network.gameconfig.client;

import com.asap.dnc.network.ClientInfo;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Used by ClientConnection to send the local client's vote to remote clients
 * during host re-configuration.
 */
class ClientVoterThread extends Thread {

    private static int CONNECTION_TIMEOUT_MS = 5000;
    private ClientInfo remoteClientInfo; // remote client to receive vote
    private ClientInfo localVote; // local client's vote

    public ClientVoterThread(ClientInfo remoteClientInfo, ClientInfo localVote) {
        this.remoteClientInfo = remoteClientInfo;
        this.localVote = localVote;
    }

    public void run() {
        try(Socket socket = new Socket()) {
            socket.connect(remoteClientInfo, CONNECTION_TIMEOUT_MS);
            ObjectOutputStream os = new ObjectOutputStream(socket.getOutputStream());
            os.writeObject(localVote);
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
