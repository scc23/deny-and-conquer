package com.asap.dnc.network;

import com.asap.dnc.core.Cell;
import com.asap.dnc.core.Grid;
import com.asap.dnc.core.PenColor;

import java.util.Arrays;
import java.util.List;

public class ServerGrid extends Grid {

    private ServerCell[][] cells;

    public ServerGrid(int gridSize) {
        super(gridSize);
        System.out.println("Creating server grid...");
    }

    public Cell acquireCell(int row, int col) {
        // Return reference to cell if the mutex was acquired successfully
        if (this.cells[row][col].acquireCellMutex()) {
            return this.cells[row][col];
        }
        else {
            return null;
        }
    }

    public void freeCell(int row, int col) {
        // Release the mutex on the cell
        this.cells[row][col].freeCellMutex();
    }

    public void setCellOwner(int row, int col, PenColor owner){
        this.cells[row][col].setOwner(owner);
    }

    // TODO: Implement getWinner()
    public List<Integer> getWinner(int player) {
        return Arrays.asList(1, 2, 3, 4);
    }
}
