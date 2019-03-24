package com.asap.dnc.network.gameconfig.client;

import com.asap.dnc.core.Cell;
import com.asap.dnc.core.CoreGameClient;
import com.asap.dnc.core.PenColor;

import javafx.event.EventHandler;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.WritableImage;
import javafx.scene.image.PixelReader;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.input.MouseEvent;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

// TODO: methods to implement
// acquireCell DONE
// freeCell DONE
// setCellOwner DONE
// getWinner IN PROGRESS

public class ClientCell extends Cell {
    private Canvas canvas;
    private Color colorVal;
    private String hexVal;
    private static PenColor clientColor;
    private CoreGameClient operations;
    private static ClientCell[][] cellsArray;
    private static int penThickness;

    public ClientCell(int height, int width, int col, int row, CoreGameClient operations) {
        super(height, width, col, row);
        this.operations = operations;
        this.initializeCell();
        System.out.println("Creating cell in client grid...");
    }

    public static void setPenThickness(int thickness) {
        penThickness = thickness;
    }

    // setter for static cells array
    public static void setCellsArray(ClientCell[][] cells) {
        cellsArray = cells;
    }

    // setter for client color shared by all cells
    public static void setClientColor(PenColor color) {
        clientColor = color;
    }

    private void initializeCell() {
        this.getColor(clientColor);
        this.canvas = new Canvas(this.getHeight(), this.getWidth());

        final Canvas currentCanvas = this.canvas;
        final int col = this.getCol();
        final int row = this.getRow();
        final GraphicsContext graphicsContext = this.canvas.getGraphicsContext2D();
        final double THRESH_HOLD = 60.0;

        // initializes each cell as a drawable canvas
        this.initDraw(graphicsContext);

        // sends request to server to gain ownership of cell[i][j]
        this.canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                System.out.println(String.format("ACQUIRE: %d, %d", row, col));
                try {
                    if (cellsArray[row][col].getAcuiredRights() == null) {
                        operations.sendAcquireMessage(row, col);
                    } else {
                        System.out.println("This cell is already acquired by client of color "
                                + cellsArray[row][col].getAcuiredRights());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                while (cellsArray[row][col].getAcuiredRights() != null) {
                    if (cellsArray[row][col].getAcuiredRights() == clientColor) {
                        graphicsContext.beginPath();
                        graphicsContext.moveTo(event.getX(), event.getY());
                        graphicsContext.stroke();
                        break;
                    } else {
                        System.out.println("This cell is already acquired");
                        break;
                    }
                }
            }
        });

        // starts coloring cell
        this.canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                graphicsContext.lineTo(event.getX(), event.getY());
                graphicsContext.stroke();
            }
        });

        // informs server that client is coloring cell[i][j], disable it for others
        this.canvas.addEventHandler(MouseEvent.DRAG_DETECTED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                System.out.println(String.format("START_COLOR: %d, %d", row, col));
            }
        });

        // computes colored area and sends it to the server
        this.canvas.addEventHandler(MouseEvent.MOUSE_RELEASED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                System.out.println(String.format("RELEASE: %d, %d\n", row, col));

                // converts canvas cell to a writable image
                WritableImage snap = graphicsContext.getCanvas().snapshot(null, null);

                // computes colored area percentage
                double fillPercentage = computeFillPercentage(snap);

                // checks if threshold is reached
                if (fillPercentage > THRESH_HOLD) {
                    ClientCell.super.setOwner(clientColor);
                    // StackPane holder = new StackPane();

                    // holder.getChildren().add(currentCanvas);

                    // holder.setStyle("-fx-background-color: red");
                    graphicsContext.setFill(colorVal);
                    System.out.println("THRESHOLD_REACHED");
                } else {
                    System.out.println("THRESHOLD_NOT_REACHED");
                    // currentCanvas.setStyle("-fx-background-color: none");
                }
                try {
                    operations.sendReleaseMessage(row, col, fillPercentage, ClientCell.super.getOwner());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                System.out.println(String.format("REGION_COLORED: %.2f%%", fillPercentage));
            }
        });
    }

    /**
     * @param snap image of the cell
     * @return the colored region
     */
    private double computeFillPercentage(WritableImage snap) {
        // obtains PixelReader from the snap
        PixelReader pixelReader = snap.getPixelReader();
        double coloredPixels = 0;
        double totalColorablePixels = (snap.getHeight() * snap.getWidth());

        // determines the number of colored pixels
        for (int readY = 0; readY < snap.getHeight(); readY++) {
            for (int readX = 0; readX < snap.getWidth(); readX++) {
                Color color = pixelReader.getColor(readX, readY);

                // checks if a pixel is colored with PenColor
                if (color.toString().equals(this.hexVal)) {
                    coloredPixels += 1;
                }
            }
        }

        // computes colored area percentage
        double fillPercentage = (coloredPixels / totalColorablePixels) * 100.0;
        return fillPercentage;
    }

    /**
     * @param color PenColor
     * @return the hex value of PenColor
     */
    private void getColor(PenColor color) {
        switch (color) {
        case BLUE: {
            this.hexVal = "0x0000ffff";
            this.colorVal = Color.BLUE;
            break;
        }

        case GREEN: {
            this.hexVal = "0x00ff00ff";
            this.colorVal = Color.GREEN;
            break;
        }

        case RED: {
            this.hexVal = "0xff0000ff";
            this.colorVal = Color.RED;
            break;
        }

        case YELLOW: {
            this.hexVal = "0x#ffff00ff";
            this.colorVal = Color.YELLOW;
            break;
        }

        default:
            break;
        }
    }

    /**
     * @return the canvas
     */
    public Canvas getCanvas() {
        return canvas;
    }

    private void initDraw(GraphicsContext graphicsContext) {
        double canvasWidth = graphicsContext.getCanvas().getWidth();
        double canvasHeight = graphicsContext.getCanvas().getHeight();

        graphicsContext.setFill(Color.LIGHTGRAY);
        graphicsContext.setStroke(Color.BLACK);
        graphicsContext.setLineWidth(5);

        graphicsContext.fill();
        graphicsContext.strokeRect(0, 0, canvasWidth, canvasHeight);
        graphicsContext.setFill(Color.RED);

        graphicsContext.setStroke(this.colorVal);
        graphicsContext.setLineWidth(penThickness);
    }
}
