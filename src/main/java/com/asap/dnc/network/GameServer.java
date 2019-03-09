package com.asap.dnc.network;

import com.asap.dnc.core.Grid;

import java.util.List;
import java.util.Random;

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
//    private Grid grid;
    private ServerGrid grid;

//    public void init() {
//
//    }
    public void init(int fillUnits, int length, int width) {
        this.grid = new ServerGrid(fillUnits, length, width);
        // Start 4 threads to continuously acquire and free cells
        Thread t1 = new ClientThread(3);
        t1.setName("Player 1");
        Thread t2 = new ClientThread(3);
        t2.setName("Player 2");
        Thread t3 = new ClientThread(3);
        t3.setName("Player 3");
        Thread t4 = new ClientThread(3);
        t4.setName("Player 4");

        t1.start();
        t2.start();
        t3.start();
        t4.start();
    }


    // todo: implement
    public class ClientThread extends Thread {
        int gridSize;

        public ClientThread(int gridSize) {
            this.gridSize = gridSize;
        }

        public void run() {
            int minSleepTime = 2000, maxSleepTime = 5000;
            int row, col, sleepTime;
            Random random = new Random();

            System.out.println(getName() + " is running");

            try {
                while(true) {
                    // Randomly select a cell to acquire
                    row = random.nextInt(this.gridSize);
                    col = random.nextInt(this.gridSize);

                    // Attempt to acquire cell
                    if (grid.acquireCell(row, col) != null) {
                        System.out.println(getName() + " successfully acquired Cell[" + row + "][" + col + "]");

                        // Sleep for random amount of time
                        sleepTime = random.nextInt(maxSleepTime - minSleepTime + 1) + minSleepTime;
                        Thread.sleep(sleepTime);

                        // Free cell
                        System.out.println(getName() + " freeing Cell[" + row + "][" + col + "]");
                        grid.freeCell(row, col);
                    }
                    else {
                        System.out.println("* " + getName() + " failed to acquire Cell[" + row + "][" + col + "], trying another cell...");
                    }
                }
            } catch (Exception e) {
                System.out.println(e);
                e.printStackTrace();
            }
        }
    }

    // Main method to test concurrently acquiring cells in the server
    public static void main(String[] args) {
        GameServer gameServer = new GameServer();
        gameServer.init(10, 3, 3);
    }
}
