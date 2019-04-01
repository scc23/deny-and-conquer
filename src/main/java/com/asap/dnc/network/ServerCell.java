package com.asap.dnc.network;

import com.asap.dnc.core.Cell;

import java.util.concurrent.Semaphore;

public class ServerCell extends Cell {
    // Use a mutex to ensure no more than one thread is acquiring the same cell at a time
    private Semaphore mutex = new Semaphore(1);

    public ServerCell(int height, int width, int col, int row) {
        super(height, width, col, row);
        System.out.println("Creating cell in server grid...");
    }

    public boolean acquireCellMutex() {
        return mutex.tryAcquire();
    }

    public void freeCellMutex() {
        mutex.release();
    }
}
