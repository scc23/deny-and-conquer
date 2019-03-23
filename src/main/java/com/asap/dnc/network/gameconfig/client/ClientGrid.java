package com.asap.dnc.network.gameconfig.client;

import com.asap.dnc.core.CoreGameClient;
import com.asap.dnc.core.GameMessage;
import com.asap.dnc.core.Grid;

import com.asap.dnc.core.PenColor;
import com.asap.dnc.gameconfig.GameConfig;
import com.asap.dnc.network.ClientInfo;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.RowConstraints;

import java.io.IOException;
import java.net.InetAddress;

public class ClientGrid extends Grid {
    private int penThickness;
    private InetAddress serverAddress;
    private GridPane gridpane;
    private CoreGameClient operations;
    // private GameConfig gameConfig;
    private ClientCell[][] cells;
    // private ClientInfo clientInfo;
    private PenColor clientColor;

    public ClientGrid(GameConfig gameConfig, InetAddress serverAddress, PenColor color) {
        super(gameConfig.getGridSize());
        this.clientColor = color;
        this.operations = new CoreGameClient(this.serverAddress, this.clientColor);
        this.cells = new ClientCell[gameConfig.getGridSize()][gameConfig.getGridSize()];
        // this.gameConfig = gameConfig;
        this.penThickness = gameConfig.getPenThickness();
        this.serverAddress = serverAddress;
        this.init();
        System.out.println("Creating client grid...");
        Thread listenerThread = new clientGridListener(this);
        listenerThread.start();
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
                ClientCell cell = new ClientCell(75, 75, col, row, operations);
                this.gridpane.add(cell.getCanvas(), col, row);
                this.cells[row][col] = cell;
            }
        }
        ClientCell.setPenThickness(this.penThickness);
        ClientCell.setCellsArray(this.cells);
        ClientCell.setClientColor(this.clientColor);
    }

    public class clientGridListener extends Thread {
        private ClientGrid grid;

        public clientGridListener(ClientGrid grid) {
            this.grid = grid;
        }

        @Override
        public void run() {
            System.out.println("client thread listening for server messages...");
            while (true) {
                try {
                    GameMessage msg = grid.operations.receiveServerResponse();
                    executeGridOperation(msg);
                    Thread.sleep(10);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    // Execute grid operation
    public void executeGridOperation(GameMessage msg) {
        int row = msg.getRow();
        int col = msg.getCol();
        PenColor penColor = msg.getPenColor();

        switch (msg.getType()) {
        case CELL_ACQUIRE:
            // Lock cell
            if (msg.getIsValid()) {
                System.out.println("Acquired cell[" + row + "][" + col + "] for color " + penColor);
                this.cells[row][col].setAcquiredRights(penColor);
            } else {
                System.out.println("Invalid move: Acquire cell[" + row + "][" + col + "]");
            }
            break;
        case CELL_RELEASE:
            // Release cell
            System.out.println("Released cell[" + row + "][" + col + "]");

            break;
        default:
            System.out.println("Invalid move!");
        }
    }

    public static void main(String[] args) throws Exception {
        // ClientGrid grid = new ClientGrid(5, InetAddress.getByName("localhost"),
        // PenColor.BLUE);
    }
}
