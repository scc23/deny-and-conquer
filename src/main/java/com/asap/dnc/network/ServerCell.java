package com.asap.dnc.network;

import com.asap.dnc.core.Cell;
import com.asap.dnc.core.PenColor;

import java.util.concurrent.Semaphore;

public class ServerCell extends Cell {
    // Use a mutex to ensure no more than one thread is acquiring the same cell at a time
    private Semaphore mutex = new Semaphore(1);
    private boolean isLocked;
    private PenColor playerColor;

    public ServerCell(int height, int width, int col, int row) {
        super(height, width, col, row);
        System.out.println("Creating cell in server grid...");
        this.isLocked = false;
        this.playerColor = null;
    }

    public boolean acquireCellMutex() {
        return mutex.tryAcquire();
    }

    public void freeCellMutex() {
        mutex.release();
    }

    public void setIsLocked(boolean val, PenColor color) {
        this.isLocked = val;
        this.playerColor = color;
    }

    public boolean getIsLocked(){ return this.isLocked;}

    public PenColor getAcquiredOwner(){ return this.playerColor;}
}
