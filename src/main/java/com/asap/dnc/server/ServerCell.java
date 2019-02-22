package com.asap.dnc.server;

import com.asap.dnc.core.Cell;

import java.util.concurrent.Semaphore;

public class ServerCell extends Cell {
    // Use a mutex to ensure no more than one thread is acquiring the same cell at a time
    public Semaphore mutex = new Semaphore(1);

    public ServerCell(int nRows, int nCols, int ctrlReq) {
        super(nRows, nCols, ctrlReq);
    }

    // TODO: Implement fillCell()
    @Override
    public boolean fillCell(int rowIdx, int colIdx, int player) {
        return true;
    }
}
