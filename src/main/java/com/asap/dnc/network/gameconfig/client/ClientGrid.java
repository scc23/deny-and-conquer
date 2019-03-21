package com.asap.dnc.network.gameconfig.client;

import com.asap.dnc.core.Cell;
import com.asap.dnc.core.Grid;
import com.asap.dnc.core.PenColor;

import java.util.Arrays;
import java.util.List;

public class ClientGrid extends Grid {
    private ClientCell[][] cells;

    public ClientGrid(int fillUnits, int length, int width) {
        super(fillUnits, length, width);
        System.out.println("Creating client grid...");
        this.cells = new ClientCell[length][width];
        for (int i = 0; i < length; i++) {
            for (int j = 0; j < length; j++) {
                this.cells[i][j] = new ClientCell(10,10,10);
            }
        }
    }

    @Override
    public Cell acquireCell(int row, int col) {
        // Return reference to cell
        return this.cells[row][col];
    }

    @Override
    public void freeCell(int row, int col) {

    }

    @Override
    public void setCellOwner(int row, int col, PenColor owner){
        this.cells[row][col].setOwner(owner);
    }

    // TODO: Implement getWinners()
    @Override
    public List<Integer> getWinners(int player) {
        return Arrays.asList(1, 2, 3, 4);
    }

}
