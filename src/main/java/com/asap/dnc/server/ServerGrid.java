package com.asap.dnc.server;

import com.asap.dnc.core.Cell;
import com.asap.dnc.core.Grid;

import java.util.Arrays;
import java.util.List;

public class ServerGrid extends Grid {

    private ServerCell[][] cells;

    public ServerGrid(int fillUnits, int length, int width) {
        super(fillUnits, length, width);
        this.cells = new ServerCell[length][width];
    }

    @Override
    public Cell acquireCell(int row, int col) {
        // Return reference to cell if the mutex was acquired successfully
        // TODO: Figure out how to properly acquire a semaphore from a class object
        if (this.cells[row-1][col-1].mutex.tryAcquire()) {
            return this.cells[row-1][col-1];
        }
        else {
            return null;
        }
    }

    @Override
    public void freeCell(int row, int col) {
        // Release the mutex on the cell
        this.cells[row-1][col-1].mutex.release();
    }

    // TODO: Implement getWinners()
    @Override
    public List<Integer> getWinners(int player) {
        return Arrays.asList(1, 2, 3, 4);
    }
}
