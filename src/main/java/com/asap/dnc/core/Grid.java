package com.asap.dnc.core;

import java.util.List;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Abstract representation of a gameconfig board. Server/client side implementations must
 * implement the methods used to access the cells of the board and to check for winners
 */
public abstract class Grid {
    private Cell[][] cells; // internal representation of gameconfig board
    private int length; // number of columns (entries) per row of cells
    private int width; // number of rows of cells
    private Map<Integer, Set<Integer>> scoreMap; // tracks the number of cells controlled by each player

    // Subclasses should implement a factory method making use of this
    protected Grid(int fillUnits, int length, int width) {
        this.length = length;
        this.width = width;

        scoreMap = new HashMap<>();
        for (int i = 0; i <= length * width; i++) {
            scoreMap.put(i, new HashSet<>());
        }
    }

    // blocking or non-blocking method to return reference to a Cell instance to fill
    public abstract Cell acquireCell(int row, int col);

    // method to call when access to a cell instance specified by (row, col) has finished
    public abstract void freeCell(int row, int col);

    // check for players controlling the most cells
    public abstract List<Integer> getWinners(int player);

    // set owner on cell
    public abstract void setCellOwner(int row, int col, PenColor owner);
}
