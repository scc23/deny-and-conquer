package com.asap.dnc.network.gameconfig.client;

import com.asap.dnc.core.CoreGameClient;
import com.asap.dnc.core.Grid;

import com.asap.dnc.gameconfig.GameConfig;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.RowConstraints;

public class ClientGrid extends Grid {
    private int penThickness;
    private GridPane gridpane;
    private CoreGameClient clientOperations;

    public ClientGrid(GameConfig gameConfig) {
        super(gameConfig.getGridSize());
        this.penThickness = gameConfig.getPenThickness();
        this.init();
        System.out.println("Creating client grid...");
    }

    /**
     * @return the gridSize
     */
    public int getGridSize() {
        return this.getGridSize();
    }

    /**
     * @return the gridpane
     */
    public GridPane getGridpane() {
        return this.gridpane;
    }

    private void init() {
        // creates grid
        this.gridpane = new GridPane();

        // sets rows and column sizes of the grid
        for (int i = 0; i < this.getGridSize(); i++) {
            this.gridpane.getColumnConstraints().add(new ColumnConstraints(100));
            this.gridpane.getRowConstraints().add(new RowConstraints(100));
        }

        // adds cells to the grid
        for (int row = 0; row < this.getGridSize(); row++) {
            for (int col = 0; col < this.getGridSize(); col++) {
                ClientCell cell = new ClientCell(75, 75, col, row);
                this.gridpane.add(cell.getCanvas(), col, row);
            }
        }
    }
}
