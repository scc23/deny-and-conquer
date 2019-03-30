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
    private int gridSize;
    private Map<PenColor, Integer> scoreMap; // tracks the number of cells controlled by each player

    // Subclasses should implement a factory method making use of this
    protected Grid(int gridSize) {
        this.gridSize = gridSize;
        scoreMap = new HashMap<>();
//        for (int i = 0; i <= gridSize * gridSize; i++) {
//            scoreMap.put(i, new HashSet<>());
//        }
    }

    public int getGridSize() {
        return gridSize;
    }

    public void setScoreMap(PenColor player, int newScore){
        if (scoreMap.containsKey(player)){
            int oldScore = scoreMap.get(player);
            int currScore = oldScore + newScore;
            scoreMap.put(player, currScore);
        } else {
            scoreMap.put(player, newScore);
        }
    }
}
