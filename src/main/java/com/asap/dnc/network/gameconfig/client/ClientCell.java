
package com.asap.dnc.network.gameconfig.client;

import com.asap.dnc.core.Cell;
import com.asap.dnc.core.CoreGameClient;
import com.asap.dnc.core.PenColor;

import javafx.scene.canvas.Canvas;
import javafx.scene.image.WritableImage;
import javafx.scene.image.PixelReader;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.input.MouseEvent;

import java.io.IOException;
import java.sql.Timestamp;

public class ClientCell extends Cell {
    private Canvas canvas;
    private Color colorVal;
    private String hexVal;
    private static PenColor clientColor;
    private CoreGameClient operations;
    private static ClientCell[][] cellsArray;
    private static int penThickness;
    private static double fillThreshold;

    // To avoid cases where sudden click and release would not send release message to server
    private boolean dragDetected = false;

    public ClientCell(int height, int width, int col, int row, CoreGameClient operations) {
        super(height, width, col, row);
        this.operations = operations;
        this.initializeCell();
        Thread listenerThread = new ClientCell.ClientCellThread(this);
        listenerThread.start();
    }

    public void resetOperations(CoreGameClient operations) {
        this.operations = operations;
    }

    public class ClientCellThread extends Thread {
        private ClientCell cell;
        private boolean grayedOut = false;

        public ClientCellThread(ClientCell cell) {
            this.cell = cell;
        }

        public void setGrayedOut(boolean val) {
            this.grayedOut = val;
        }

        public boolean getGrayedOut() {
            return this.grayedOut;
        }

        public Timestamp getCurrentTimestamp(){ return new Timestamp(this.cell.getClock().millis());}

        @Override
        public void run() {
            while (true) {
                try {
                    int row = cell.getRow();
                    int col = cell.getCol();
                    if (cell.getCellsArray() != null) {
                        ClientCell currentCell = cell.getCellsArray()[row][col];

                        double cellWidth = currentCell.getWidth();
                        double cellHeight = currentCell.getHeight();

                        GraphicsContext currentCellGC = currentCell.getCanvas().getGraphicsContext2D();

                        if (currentCell.getOwner() == null && currentCell.getAcquiredRights() != null && currentCell.getAcquiredRights() != clientColor  && !this.getGrayedOut()) {
                            switch (currentCell.getAcquiredRights()) {
                                case BLUE: {
                                    currentCellGC.setFill(Color.rgb(0, 0, 255, 0.3));
                                    break;
                                }
                                case GREEN: {
                                    currentCellGC.setFill(Color.rgb(0, 255, 0, 0.3));
                                    break;
                                }
                                case RED: {
                                    currentCellGC.setFill(Color.rgb(255, 0, 0, 0.3));
                                    break;
                                }
                                case YELLOW: {
                                    currentCellGC.setFill(Color.rgb(255, 255, 0, 0.3));
                                    break;
                                }
                                default:
                                    break;
                            }
                            currentCellGC.fillRect(0, 0, cellWidth, cellHeight);
                            currentCellGC.setLineWidth(penThickness);
                            setGrayedOut(true);
                            System.out.println("Cell [" + row + "] [" + col +"] is grayed out!");
                        }

                        if (this.getGrayedOut() && currentCell.getAcquiredRights() == null) {
                            // clear cell
                            currentCellGC.clearRect(0,0, cellWidth, cellHeight);

                            // draw border
                            currentCellGC.setStroke(Color.BLACK);
                            currentCellGC.setLineWidth(5);
                            currentCellGC.strokeRect(0, 0, cellWidth, cellHeight);

                            // begin path
                            currentCellGC.setStroke(colorVal);
                            currentCellGC.setLineWidth(penThickness);
                            setGrayedOut(false);
                        }

                         //checking acquired time out
                        if ((this.getGrayedOut() && (this.getCurrentTimestamp().getTime() - currentCell.getAcquiredCellTimestamp().getTime()) > 10000)){
                            try {
                                cell.operations.sendUpdateStateMessage(row,col);
                            } catch (IOException e){
                                e.printStackTrace();
                            }
                        }

                        if (currentCell.getOwner() != null && currentCell.getOwner() != clientColor
                                && currentCell.colorVal != null && getGrayedOut()) {
                            switch (currentCell.getOwner()) {
                                case BLUE: {
                                    currentCellGC.setFill(Color.BLUE);
                                    break;
                                }
                                case GREEN: {
                                    currentCellGC.setFill(Color.GREEN);
                                    break;
                                }
                                case RED: {
                                    currentCellGC.setFill(Color.RED);
                                    break;
                                }
                                case YELLOW: {
                                    currentCellGC.setFill(Color.YELLOW);
                                    break;
                                }
                                default:
                                    break;
                            }
                            currentCellGC.fillRect(0, 0, cellWidth, cellHeight);
                            System.out.println("Cell [" + row + "][" + col +"] is completely filled with color: " + currentCell.getOwner());
                            setGrayedOut(false);
                        }
                    }
                     Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void setPenThickness(int thickness) {
        penThickness = thickness;
    }

    public static void setCellsArray(ClientCell[][] cells) {
        cellsArray = cells;
    }

    public static ClientCell[][]  getCellsArray() {
        return cellsArray;
    }

    public static void setClientColor(PenColor color) {
        clientColor = color;
    }

    public static void setFillThreshold(double threshold) {
        fillThreshold = threshold;
    }

    private void initializeCell() {
        this.initClientColor(clientColor);
        this.canvas = new Canvas(this.getHeight(), this.getWidth());

        final Canvas currentCanvas = this.canvas;
        final int col = this.getCol();
        final int row = this.getRow();
        final GraphicsContext graphicsContext = this.canvas.getGraphicsContext2D();

        // initializes each cell as a drawable canvas
        this.initDraw(graphicsContext);

        // sends request to server to gain ownership of cell[i][j]
        this.canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            System.out.println(String.format("ACQUIRE: %d, %d", row, col));
            try {
                if (cellsArray[row][col].getAcquiredRights() == null) {
                    operations.sendAcquireMessage(row, col);
                    System.out.println("CURRENT_ACQUIRED_RIGHTS: " + cellsArray[row][col].getAcquiredRights());
                    while (cellsArray[row][col].getAcquiredRights() != null) {
                        if (cellsArray[row][col].getAcquiredRights() == clientColor) {
                            graphicsContext.beginPath();
                            graphicsContext.moveTo(event.getX(), event.getY());
                            graphicsContext.stroke();
                            break;
                        } else {
                            System.out.println("CELL_PREOCCUPIED");
                            break;
                        }
                    }
                } else {
                    System.out.println("CELL_ALREADY_ACQUIRED_BY: "  + cellsArray[row][col].getAcquiredRights());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        // starts coloring cell
        this.canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
            if (cellsArray[row][col].getAcquiredRights() == clientColor){
                graphicsContext.lineTo(event.getX(), event.getY());
                graphicsContext.stroke();
            }
        });

        // Client is coloring cell[i][j]
        this.canvas.addEventHandler(MouseEvent.DRAG_DETECTED, event -> {
            System.out.println(String.format("START_COLOR: %d, %d", row, col));
            dragDetected = true;
        });

        // computes colored area and sends it to the server
        this.canvas.addEventHandler(MouseEvent.MOUSE_RELEASED, event -> {
            System.out.println("\n--------- Mouse released -----------\n");
            System.out.println(cellsArray[row][col].getAcquiredRights());
            if (cellsArray[row][col].getAcquiredRights() == clientColor && ClientCell.super.getOwner() == null){
                System.out.println(String.format("RELEASE: %d, %d\n", row, col));

                // converts canvas cell to a writable image
                WritableImage snap = graphicsContext.getCanvas().snapshot(null, null);

                // computes colored area percentage
                double fillPercentage = computeFillPercentage(snap);
                double canvasWidth = currentCanvas.getWidth();
                double canvasHeight = currentCanvas.getHeight();

                // checks if threshold is reached
                if (fillPercentage > fillThreshold) {
                    ClientCell.super.setOwner(clientColor);
                    graphicsContext.setFill(colorVal);
                    graphicsContext.fillRect(0, 0, canvasWidth, canvasHeight);
                    System.out.println("THRESHOLD_REACHED");
                } else {
                    System.out.println("THRESHOLD_NOT_REACHED");

                    // clear cell
                    graphicsContext.clearRect(0,0, canvasWidth, canvasHeight);

                    // draw border
                    graphicsContext.setStroke(Color.BLACK);
                    graphicsContext.setLineWidth(5);
                    graphicsContext.strokeRect(0, 0, canvasWidth, canvasHeight);

                    // re-begin path and reset pen thickness
                    graphicsContext.setStroke(colorVal);
                    graphicsContext.setLineWidth(penThickness);
                    graphicsContext.beginPath();
                    graphicsContext.moveTo(event.getX(), event.getY());
                    graphicsContext.stroke();
                }
                try {
                    operations.sendReleaseMessage(row, col, fillPercentage, ClientCell.super.getOwner());
                    dragDetected = false;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                System.out.println(String.format("REGION_COLORED: %.2f%%", fillPercentage));
            }

            if (!dragDetected && cellsArray[row][col].getAcquiredRights() == null){
                try {
                    operations.sendReleaseMessage(row, col, 0, ClientCell.super.getOwner());
                } catch (IOException e){
                    e.printStackTrace();
                }
            }
        });
    }

    private double computeFillPercentage(WritableImage snap) {
        // obtains PixelReader from the snap
        PixelReader pixelReader = snap.getPixelReader();

        double snapHeight = snap.getHeight();
        double snapWidth = snap.getWidth();
        double coloredPixels = 0;
        double totalPixels = (snapHeight * snapWidth);

        String hexColor = this.hexVal;

        // computes the number of colored pixels
        for (int readY = 0; readY < snapHeight; readY++) {
            for (int readX = 0; readX < snapWidth; readX++) {
                Color color = pixelReader.getColor(readX, readY);

                // checks if a pixel is colored with PenColor
                if (color.toString().equals(hexColor)) {
                    coloredPixels += 1;
                }
            }
        }

        // computes colored area percentage
        double fillPercentage = (coloredPixels / totalPixels) * 100.0;
        return fillPercentage;
    }

    private void initClientColor(PenColor color) {
        switch (color) {
        case BLUE: {
            this.hexVal = "0x0000ffff";
            this.colorVal = Color.BLUE;
            break;
        }

        case GREEN: {
            this.hexVal = "0x008000ff";
            this.colorVal = Color.GREEN;
            break;
        }

        case RED: {
            this.hexVal = "0xff0000ff";
            this.colorVal = Color.RED;
            break;
        }

        case YELLOW: {
            this.hexVal = "0xffff00ff";
            this.colorVal = Color.YELLOW;
            break;
        }

        default:
            break;
        }
    }

    public Canvas getCanvas() {
        return canvas;
    }

    private void initDraw(GraphicsContext graphicsContext) {
        double canvasWidth = graphicsContext.getCanvas().getWidth();
        double canvasHeight = graphicsContext.getCanvas().getHeight();

        graphicsContext.setStroke(Color.BLACK);
        graphicsContext.setLineWidth(5);

        graphicsContext.fill();
        graphicsContext.strokeRect(0, 0, canvasWidth, canvasHeight);

        graphicsContext.setLineWidth(penThickness);
        graphicsContext.setStroke(this.colorVal);
    }
}
