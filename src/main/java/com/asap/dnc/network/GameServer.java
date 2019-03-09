package com.asap.dnc.network;

import com.asap.dnc.core.Grid;

import java.util.List;

/**
 * Should be able to receive and buffer incoming packets in a priority queue
 * based on a Message's timestamp. Deserialization of messages should be done by the
 * network in order for them to be placed in the priority queue. Additional information
 * about the clients address/port will needed to be passed into the executing thread in
 * order for the thread to respond to the client.
 */
public class GameServer {

    private int nThreadPoolSize;
    private List<ClientThread> clientThreads;

    // network representation of grid
    private Grid grid;

    public void init() {

    }

    // todo: implement
    private class ClientThread extends Thread {

    }
}
