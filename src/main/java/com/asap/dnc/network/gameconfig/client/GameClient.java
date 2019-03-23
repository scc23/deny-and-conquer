package com.asap.dnc.network.gameconfig.client;

import java.util.Random;

public class GameClient {

//    // Client representation of grid
//    private ClientGrid grid;
//
//    public void init() {
//        this.grid = new ClientGrid(10, 3, 3);
//        Thread player = new PlayerThread();
//        player.setName("Player");
//        player.start();
//    }
//
//    public class PlayerThread extends Thread {
//        public void run() {
//            int gridSize = 2;
//            int minSleepTime = 2000, maxSleepTime = 5000;
//            int row, col, sleepTime;
//            Random random = new Random();
//
//            System.out.println(getName() + " is running");
//
//            try {
//                while(true) {
//                    // Randomly select a cell to acquire
//                    row = random.nextInt(gridSize + 1);
//                    col = random.nextInt(gridSize + 1);
//
//                    // Attempt to acquire cell
//                    if (grid.acquireCell(row, col) != null) {
//                        System.out.println(getName() + " successfully acquired Cell[" + row + "][" + col + "]");
//
//                        // Sleep for random amount of time
//                        sleepTime = random.nextInt(maxSleepTime - minSleepTime + 1) + minSleepTime;
//                        Thread.sleep(sleepTime);
//
//                        // Free cell
//                        System.out.println(getName() + " freeing Cell[" + row + "][" + col + "]");
//                        grid.freeCell(row, col);
//                    }
//                    else {
//                        System.out.println("* " + getName() + " failed to acquire Cell[" + row + "][" + col + "], trying another cell...");
//                    }
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    // Main method to test acquiring cells in the client
//    public static void main(String[] args) {
//        GameClient gameClient = new GameClient();
//        gameClient.init();
//    }
}
