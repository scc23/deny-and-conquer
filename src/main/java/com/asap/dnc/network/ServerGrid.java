package com.asap.dnc.network;

import com.asap.dnc.core.Cell;
import com.asap.dnc.core.Grid;
import com.asap.dnc.core.PenColor;
import com.asap.dnc.network.gameconfig.client.ClientCell;
//import com.sun.security.ntlm.Server;

import java.util.Arrays;
import java.util.List;

public class ServerGrid extends Grid {

    private ServerCell[][] cells;

    // Constructor that creates empty cells at the start of a game
    public ServerGrid(int gridSize) {
        super(gridSize);
        this.cells = new ServerCell[gridSize][gridSize];
        System.out.println("Creating server grid...");
        this.init();

    }

    // Constructor that takes in existing cells as a parameter for fault tolerance
    public ServerGrid(int gridSize, Cell[][] cells) {
        super(gridSize);
        this.init();
        this.reconfigCells(cells);
    }

    private void init(){
        // adds cells to the grid
        for (int row = 0; row < this.getGridSize(); row++) {
            for (int col = 0; col < this.getGridSize(); col++) {
                ServerCell cell = new ServerCell(75, 75, col, row);
                this.cells[row][col] = cell;
            }
        }
    }

    // Set owned cells from existing state
    private void reconfigCells(Cell[][] cells) {
        System.out.println("Reconfiguring server grid cells...");
        for (int row = 0; row < this.getGridSize(); row++) {
            for (int col = 0; col < this.getGridSize(); col++) {
                System.out.println("Cell[" + row + "][" + col + "]: " + cells[row][col].getOwner());
                this.cells[row][col].setOwner(cells[row][col].getOwner());
            }
        }
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
