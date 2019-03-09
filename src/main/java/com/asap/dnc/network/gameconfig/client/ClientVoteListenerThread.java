package com.asap.dnc.server.gameconfig.client;

import com.asap.dnc.config.ClientInfo;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.Semaphore;

/**
 * Used by ClientConnection to listen and tally votes from remote clients
 * during host re-configuration.
 */
class ClientVoteListenerThread extends Thread {

    private static int VOTE_QUEUE_CAPACITY = 100;
    private static Map<ClientInfo, Integer> voteCountMap = new HashMap<>();
    private static Set<ClientInfo> voterSet = new HashSet<>();
    private static Semaphore voteLock = new Semaphore(1);

    private ClientInfo clientInfo; // local socket address to bind server socket to
    private boolean isServerSocketListening;

    /**
     * remoteClients contains connection info on all clients excluding local client.
     * The local client's vote is tallied first as _localVote.
     */
    public static void init(List<ClientInfo> remoteClients, ClientInfo _localVote) {
        for (ClientInfo ci : remoteClients) {
            voteCountMap.put(ci, 0);
            voterSet.add(ci);
        }
        voteCountMap.put(_localVote, 1);
    }

    public static Map<ClientInfo, Integer> getVoteCountMap() {
        return voteCountMap;
    }

    public boolean isServerSocketListening() {
        return this.isServerSocketListening;
    }

    public ClientVoteListenerThread(ClientInfo clientInfo) {
        this.clientInfo = clientInfo;
    }

    public void run() {
        try(ServerSocket serverSocket = new ServerSocket(
                this.clientInfo.getPort(),
                VOTE_QUEUE_CAPACITY,
                this.clientInfo.getAddress())
        ) {
            this.isServerSocketListening = true;
            int expectedConnections = voterSet.size();
            HelperThread[] helperThreads = new HelperThread[expectedConnections];

            for (int i = 0; i < expectedConnections; i++) {
                Socket remoteClientConnection = serverSocket.accept();
                HelperThread helperThread = new HelperThread(remoteClientConnection);
                helperThread.start();
                helperThreads[i] = helperThread;
            }

            // wait for all remote client votes to come in
            for (HelperThread thread : helperThreads) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            assert(voterSet.isEmpty());
        } catch (IOException e ) {
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
                        int count = voteCountMap.containsKey(clientVote) ? voteCountMap.get(clientVote) : 0;
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
