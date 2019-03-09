package com.asap.dnc.network;

import com.asap.dnc.core.Cell;
import com.asap.dnc.core.Grid;

import java.util.Arrays;
import java.util.List;

public class ServerGrid extends Grid {

    private ServerCell[][] cells;

    public ServerGrid(int fillUnits, int length, int width) {
        super(fillUnits, length, width);
        System.out.println("Creating server grid...");
        this.cells = new ServerCell[length][width];
        for (int i = 0; i < length; i++) {
            for (int j = 0; j < length; j++) {
                this.cells[i][j] = new ServerCell(10,10,10);
            }
        }
    }

    @Override
    public Cell acquireCell(int row, int col) {
        // Return reference to cell if the mutex was acquired successfully
        if (this.cells[row][col].acquireCellMutex()) {
            return this.cells[row][col];
        }
        else {
            return null;
        }
    }

    @Override
    public void freeCell(int row, int col) {
        // Release the mutex on the cell
        this.cells[row][col].freeCellMutex();
    }

    // TODO: Implement getWinners()
    @Override
    public List<Integer> getWinners(int player) {
        return Arrays.asList(1, 2, 3, 4);
    }
}
