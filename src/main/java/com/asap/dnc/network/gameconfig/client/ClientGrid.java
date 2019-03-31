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
import java.time.Clock;

public class ClientGrid extends Grid {
    private int gridSize;
    private int penThickness;
    private CoreGameClient operations;
    private InetAddress serverAddress;
    private ClientCell[][] cells;
    private PenColor clientColor;
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
        this.init();
        System.out.println("Creating client grid...");
        Thread listenerThread = new clientGridListener(this);
        listenerThread.start();
    }

    // Initialize the client grid
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

    /**
     * @return the gridSize
     */
    public int getGridSize() {
        return this.gridSize;
    }

    /**
     * @return the gridpane
     */
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
            if (msg.getIsOwned()){ // declare owner
                this.cells[row][col].setOwner(penColor);
            } else { // release cell
                this.cells[row][col].setAcquiredRights(null);
            }
            break;
        default:
            System.out.println("Invalid move!");
        }
    }

    public static void main(String[] args) throws Exception {
//        GameConfig gameConfig = new GameConfig(1, 4, 3);
//        ClientInfo clientInfo = new ClientInfo("127.0.0.1", 5000);
//        clientInfo.setPenColor(PenColor.RED);
//        ClientGrid grid = new ClientGrid(gameConfig, InetAddress.getByName("localhost"), );
    }
}
