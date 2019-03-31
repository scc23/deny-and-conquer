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
import java.util.Collections;
import java.util.Map;
import java.util.ArrayList;
import java.util.Arrays;

public class ClientGrid extends Grid {
    private int penThickness;
    private InetAddress serverAddress;
    private GridPane gridpane;
    private CoreGameClient operations;
    private int gridSize;
    // private GameConfig gameConfig;
    private ClientCell[][] cells;
    // private ClientInfo clientInfo;
    private PenColor clientColor;
    private int clientPort;
    private int cellsWithOwner;

    public ClientGrid(GameConfig gameConfig, InetAddress serverAddress, ClientInfo clientInfo) {
        super(gameConfig.getGridSize());
        System.out.println("this is client grid talking.. " + serverAddress);
        System.out.println("client port" + clientInfo.getPort());
        this.gridSize = gameConfig.getGridSize();
        this.clientColor = clientInfo.getPenColor();
        this.clientPort = clientInfo.getPort();
        this.serverAddress = serverAddress;
        this.operations = new CoreGameClient(this.serverAddress, this.clientColor, this.clientPort);
        this.cells = new ClientCell[gameConfig.getGridSize()][gameConfig.getGridSize()];
        // this.gameConfig = gameConfig;
        this.penThickness = gameConfig.getPenThickness();
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

    /**
     * @return the gridSize
     */
    public int getGridSize() {
        return this.gridSize;
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

        ClientCell.setPenThickness(this.penThickness);
        ClientCell.setClientColor(this.clientColor);

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

    public static void main(String[] args) throws Exception {
        // ClientGrid grid = new ClientGrid(5, InetAddress.getByName("localhost"),
        // PenColor.BLUE);
    }
}
