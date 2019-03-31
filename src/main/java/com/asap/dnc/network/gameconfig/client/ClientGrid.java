package com.asap.dnc.network.gameconfig.client;

import com.asap.dnc.core.*;

import com.asap.dnc.gameconfig.GameConfig;
import com.asap.dnc.network.ClientInfo;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Collections;
import java.util.Map;
import java.util.ArrayList;
import java.time.Clock;

public class ClientGrid extends Grid {
    private int gridSize;
    private int penThickness;
    private CoreGameClient operations;
    private InetAddress serverAddress;
    private ClientCell[][] cells;
    private PenColor clientColor;
    private double fillThreshold;
    private int cellsWithOwner;
    private GridPane gridpane;

    // Constructor
    public ClientGrid(GameConfig gameConfig, InetAddress serverAddress, ClientInfo clientInfo, Clock clock) {
        super(gameConfig.getGridSize());
        System.out.println("this is client grid talking.. " + serverAddress);
        System.out.println("client port" + clientInfo.getPort());
        this.gridSize = gameConfig.getGridSize();
        this.clientColor = clientInfo.getPenColor();
        this.serverAddress = serverAddress;
        this.operations = new CoreGameClient(this.serverAddress, this.clientColor, clientInfo.getPort());
        this.operations.setClock(clock);
        this.cells = new ClientCell[gameConfig.getGridSize()][gameConfig.getGridSize()];
        this.penThickness = gameConfig.getPenThickness();
        this.fillThreshold = gameConfig.getThreshold();
        this.init();
        System.out.println("Creating client grid...");

        // start listener thread for server messages
        Thread listenerThread = new clientGridListener(this);
        listenerThread.start();

        // Initialize cells with owner
        cellsWithOwner = 0;

        // start thread for checking winner
        Thread checkWinner = new GetWinner(this);
        checkWinner.start();
    }

    public int getGridSize() {
        return this.gridSize;
    }

    private void init() {
        // creates grid
        this.gridpane = new GridPane();

        // sets rows and column sizes of the grid
        for (int i = 0; i < this.getGridSize(); i++) {
            this.gridpane.getColumnConstraints().add(new ColumnConstraints());
            this.gridpane.getRowConstraints().add(new RowConstraints());
        }

        this.gridpane.setHgap(1); //horizontal gap in pixels => that's what you are asking for
        this.gridpane.setVgap(1); //vertical gap in pixels
        this.gridpane.setPadding(new Insets(1));

        ClientCell.setPenThickness(this.penThickness);
        ClientCell.setClientColor(this.clientColor);
        ClientCell.setFillThreshold(this.fillThreshold);

        // adds cells to the grid
        for (int row = 0; row < this.getGridSize(); row++) {
            for (int col = 0; col < this.getGridSize(); col++) {
                ClientCell cell = new ClientCell(75, 75, col, row, operations);
                this.gridpane.add(cell.getCanvas(), col, row);
                this.cells[row][col] = cell;
            }
        }

        ClientCell.setCellsArray(this.cells);
    }

    public Scene getGridpane() {
        VBox root = new VBox();
        root.getChildren().addAll(this.gridpane);
        return new Scene(root);
    }

    // Get the client cells for fault tolerance
    public Cell[][] getCells() {
        return this.cells;
    }

    // Reset the grid on fault tolerance
    public void setClientConfig(InetAddress serverAddress) {
        // Set new server address
        this.serverAddress = serverAddress;
        // Set new server address to perform operations
        this.operations.setServerAddress(serverAddress);
    }

    // Listener to listen for server messages
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

    public class GetWinner extends Thread {
        private ClientGrid grid;
        private int gridsize;
        private boolean noWinner;

        public GetWinner (ClientGrid grid) {
            this.grid = grid;
            this.noWinner = true;
            this.gridsize = grid.gridSize;
        }

        @Override
        public void run() {
            while (noWinner) {
                try{
                    if (grid.cellsWithOwner == (gridSize * gridSize)){
                        System.out.println(" ---- We have winner -----");
                        noWinner = false;
                        findWinner();
                    }
                    Thread.sleep(10);
                } catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        }

        public ArrayList<PenColor> findWinner(){
            ArrayList<PenColor> winner = new ArrayList<>();
            Map<PenColor, Integer> scoreMap = grid.getScoreMap();
            int maxValueInMap=(Collections.max(scoreMap.values()));  // This will return max value in the Hashmap

            // check for max equalizer
            for (Map.Entry<PenColor, Integer> entry : scoreMap.entrySet()) {
                if (entry.getValue() == maxValueInMap) {
                    winner.add(entry.getKey());
                }
            }
            return winner;
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
                this.cells[row][col].setAcquiredCellTimestamp();
            } else {
                System.out.println("Invalid move: Acquire cell[" + row + "][" + col + "]");
            }
            break;
        case CELL_RELEASE:
            // Release cell
            System.out.println("Released cell[" + row + "][" + col + "]");
            if (msg.getIsOwned()){ // declare owner
                this.cells[row][col].setOwner(penColor);
                setScoreMap(penColor, 1);   // update scoreMap
                cellsWithOwner ++;
            } else { // release cell
                this.cells[row][col].setAcquiredRights(null);
            }
            break;
        default:
            System.out.println("Invalid move!");
        }
    }
}
