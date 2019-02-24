package com.asap.dnc.server.gameconfig.client;

import com.asap.dnc.config.ClientInfo;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;

/**
 * Used by ClientConnection to listen and tally votes from remote clients
 * during host re-configuration.
 */
class ClientVoteListenerThread extends Thread {

    private static Map<ClientInfo, Integer> voteCountMap = new HashMap<>();
    private static Set<ClientInfo> voterSet = new HashSet<>();
    private static Semaphore voteLock = new Semaphore(1);

    private int port; // listens for voters on specified port

    /**
     * remoteClients contains connection info on all clients excluding local client.
     * The local client's vote is tallied first as _localVote.
     */
    public static void init(ClientInfo[] remoteClients, ClientInfo _localVote) {
        for (ClientInfo ci : remoteClients) {
            voteCountMap.put(ci, 0);
            voterSet.add(ci);
        }
        voteCountMap.put(_localVote, 1);
    }

    public static Map<ClientInfo, Integer> getVoteCountMap() {
        return voteCountMap;
    }

    public ClientVoteListenerThread(int port) {
        this.port = port;
    }

    public void run() {
        try(ServerSocket serverSocket = new ServerSocket(this.port)) {
            while (!voterSet.isEmpty()) {
                Socket remoteClientConnection = serverSocket.accept();
                HelperThread helperThread = new HelperThread(remoteClientConnection);
                helperThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static class HelperThread extends Thread {

        private Socket socket;

        public HelperThread(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try(ObjectInputStream is = new ObjectInputStream(this.socket.getInputStream())) {
                try {
                    ClientInfo client = new ClientInfo(
                            this.socket.getLocalAddress().getHostAddress(),
                            this.socket.getLocalPort());
                    ClientInfo clientVote = (ClientInfo) is.readObject();

                    try {
                        voteLock.acquire();
                        int count = voteCountMap.get(clientVote);
                        voteCountMap.put(clientVote, count + 1);
                        voterSet.remove(client);
                        voteLock.release();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } catch(ClassNotFoundException e) {
                    e.printStackTrace();
                }

                try {
                    this.socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
