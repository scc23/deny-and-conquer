package com.asap.dnc.network.gameconfig.client;

import com.asap.dnc.core.Grid;

import javafx.scene.layout.GridPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.RowConstraints;

public class ClientGrid extends Grid {
    private int gridSize = 5;
    private GridPane gridpane;

    public ClientGrid(int gridSize) {
        super(gridSize);
        this.init();
        System.out.println("Creating client grid...");
    }

    /**
     * @return the gridSize
     */
    public int getGridSize() {
        return gridSize;
    }

    /**
     * @return the gridpane
     */
    public GridPane getGridpane() {
        return gridpane;
    }

    private void init() {
        // creates grid
        this.gridpane = new GridPane();

        // sets rows and column sizes of the grid
        for (int i = 0; i < this.gridSize; i++) {
            this.gridpane.getColumnConstraints().add(new ColumnConstraints(100));
            this.gridpane.getRowConstraints().add(new RowConstraints(100));
        }

        // adds cells to the grid
        for (int row = 0; row < this.gridSize; row++) {
            for (int col = 0; col < this.gridSize; col++) {
                ClientCell cell = new ClientCell(75, 75, col, row);
                this.gridpane.add(cell.getCanvas(), col, row);
            }
        }
    }
}
