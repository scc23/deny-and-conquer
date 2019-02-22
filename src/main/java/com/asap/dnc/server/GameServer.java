package com.asap.dnc.server;

import java.util.List;
import java.util.Random;

/**
 * Should be able to receive and buffer incoming packets in a priority queue
 * based on a Message's timestamp. Deserialization of messages should be done by the
 * server in order for them to be placed in the priority queue. Additional information
 * about the clients address/port will needed to be passed into the executing thread in
 * order for the thread to respond to the client.
 */
public class GameServer {

    private int nThreadPoolSize;
    private List<ClientThread> clientThreads;

    // server representation of grid
//    private Grid grid;
    private ServerGrid grid = new ServerGrid(10, 3, 3);

    public void init() {
//        this.grid = new ServerGrid(fillUnits, length, width);
        // Start 4 threads to continuously acquire and free cells
        Thread t1 = new ClientThread();
        t1.setName("Player 1");
        Thread t2 = new ClientThread();
        t2.setName("Player 2");
        Thread t3 = new ClientThread();
        t3.setName("Player 3");
        Thread t4 = new ClientThread();
        t4.setName("Player 4");

        t1.start();
//        t2.start();
//        t3.start();
//        t4.start();
    }

    // todo: implement
    public class ClientThread extends Thread {
        public void run() {
            int gridSize = 3;
            int minSleepTime = 2000, maxSleepTime = 5000;
            int row, col, sleepTime;
            Random random = new Random();

            System.out.println(getName() + " is running");

            try {
                while(true) {
                    // Randomly select a cell to acquire
                    row = random.nextInt(gridSize + 1);
                    col = random.nextInt(gridSize + 1);

                    // Attempt to acquire cell
                    System.out.println(getName() + " attempting to acquire Cell" + row + "][" + col + "]");
                    if (grid.acquireCell(row, col) != null) {
                        System.out.println(getName() + " successfully acquired Cell" + row + "][" + col + "]");

                        // Sleep for random amount of time
                        sleepTime = random.nextInt(maxSleepTime - minSleepTime + 1) + minSleepTime;
                        Thread.sleep(sleepTime);

                        // Free cell
                        System.out.println(getName() + " freeing Cell" + row + "][" + col + "]");
                        grid.freeCell(row, col);
                    }

//                    System.out.println(getName() + " acquiring Cell" + row + "][" + col + "]");
//                    grid.acquireCell(row, col);
                }
            } catch (Exception e) {
                System.out.println();
                e.printStackTrace();
            }
        }
    }

    // Main method to test concurrently acquiring cells in the server
    public static void main(String[] args) {
        GameServer gameServer = new GameServer();
        gameServer.init();
    }

}