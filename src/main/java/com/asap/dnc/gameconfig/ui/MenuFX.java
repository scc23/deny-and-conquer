package com.asap.dnc.gameconfig.ui;

import com.asap.dnc.gameconfig.GameConfig;
import com.asap.dnc.gameconfig.controls.MenuController;
import com.asap.dnc.gameconfig.controls.MenuControllerImpl;
import com.asap.dnc.network.gameconfig.ConnectionResponseHandler;
import com.asap.dnc.network.gameconfig.client.ClientConnection;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.net.SocketException;

public class MenuFX extends Application {

    private Stage stage;
    private MenuController controller;
    private StringProperty remainingConnectionsText = new SimpleStringProperty("Waiting for 3 more players to join...");

    @Override
    public void init() {
        controller = new MenuControllerImpl();
        controller.setConnectionResponseHandler(new ConnectionHandler());
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        stage = primaryStage;

        // Display the start menu at the beginning
        Scene scene = startMenuScene();

        primaryStage.setTitle("Deny & Conquer");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // Start menu
    private Scene startMenuScene() {
        Text text = new Text("Welcome to Deny & Conquer!");
        VBox root = new VBox(15);

        // Host gameconfig button to display network menu
        Button hostGameBtn = new Button("Host Game");
        hostGameBtn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                stage.setScene(serverMenuScene());
            }
        });

        // Join gameconfig button to display client menu
        Button joinGameBtn = new Button("Join Game");
        joinGameBtn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                stage.setScene(clientMenuScene());
            }
        });

        root.getChildren().addAll(text, hostGameBtn, joinGameBtn);
        root.setAlignment(Pos.CENTER);

        return new Scene(root, 300, 300);
    }

    // Function to include the necessary contents inside a scene
    private Scene setupScene(HBox hbTitle, VBox vbox) {
        // Create back button to return to start menu
        HBox hbButtons = new HBox();
        Button backBtn = new Button("← Menu");

        // Edit default button styling
        backBtn.setStyle("-fx-border-color: transparent; -fx-border-width: 0; -fx-background-radius: 0; -fx-background-color: transparent; -fx-cursor: hand;");

        // Return to the start menu when backBtn is clicked
        backBtn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                stage.setScene(startMenuScene());
            }
        });

        hbButtons.getChildren().add(backBtn);
        hbButtons.setAlignment(Pos.CENTER_RIGHT);
        hbTitle.setAlignment(Pos.CENTER);
        vbox.setAlignment(Pos.CENTER);

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(50, 0, 0, 0));
        root.setTop(hbTitle);
        root.setCenter(vbox);
        root.setBottom(hbButtons);

        return new Scene(root, 300, 300);
    }

    // Function to create a dropdown menu
    private HBox createComboBox(Label label, ObservableList<String> options) {
        HBox hbox = new HBox();

        final ComboBox comboBox = new ComboBox<String>(options);
        comboBox.getSelectionModel().selectFirst();

        hbox.getChildren().addAll(label, comboBox);
        hbox.setAlignment(Pos.CENTER);

        return hbox;
    }

    // Server menu
    private Scene serverMenuScene() {
        HBox hbTitle = new HBox();
        Text text = new Text("Host Game");
        hbTitle.getChildren().add(text);

        VBox vbox = new VBox(15);

        // TODO: Save configuration values to set up the gameconfig
        // Create dropdown menu for pen thickness configuration
        Label labelPenThickness = new Label("Pen thickness: ");
        ObservableList<String> penThicknessOptions = FXCollections.observableArrayList("1", "2", "3", "4", "5", "6", "7", "8", "9", "10");
        HBox penConfig = createComboBox(labelPenThickness, penThicknessOptions);

        // Create dropdown menu for pen gameconfig board size configuration
        Label labelGameBoardSize = new Label("Game board size: ");
        ObservableList<String> gameBoardSizeOptions = FXCollections.observableArrayList("2x2", "3x3", "4x4", "5x5", "6x6", "7x7", "8x8", "9x9", "10x10");
        HBox gameBoardConfig = createComboBox(labelGameBoardSize, gameBoardSizeOptions);

        // Start gameconfig/wait for more players
        Button startGameBtn = new Button("Start");
        startGameBtn.setMaxSize(100, 200);
        startGameBtn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                // Get network ip address
                ComboBox penComboBox = (ComboBox) penConfig.getChildren().get(1);
                int penThickness = Integer.parseInt((String) penComboBox.getValue());

                ComboBox boardComboBox = (ComboBox) gameBoardConfig.getChildren().get(1);
                int gridSize = Integer.parseInt(((String) boardComboBox.getValue()).substring(0, 1));

                GameConfig gameConfig = new GameConfig(4, penThickness, gridSize);
                controller.setGameConfig(gameConfig);

                System.out.println("Starting gameconfig...");

                GameHostThread hostThread = new GameHostThread();
                hostThread.start();
                try {
                    stage.setScene(waitMenuScene(ClientConnection.getPublicIPV4Address().getHostAddress()));
                } catch (SocketException e) {
                    e.printStackTrace();
                    System.exit(-1);
                }
            }
        });

        vbox.getChildren().addAll(penConfig, gameBoardConfig, startGameBtn);

        return setupScene(hbTitle, vbox);
    }

    // Client menu
    private Scene clientMenuScene() {
        HBox hbTitle = new HBox();
        Text text = new Text("Join Game");
        hbTitle.getChildren().add(text);

        VBox vbox = new VBox(15);
        Label label = new Label("Please enter host IP address:");
        final TextField field = new TextField();
        field.setMaxWidth(150);

        // Start gameconfig/wait for more players
        Button startGameBtn = new Button("Start");
        startGameBtn.setMaxSize(100, 200);
        startGameBtn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                // TODO: Check if we can successfully connect to the network

                // Check if inputted ip address is in valid format
                if (field.getText().matches("\\b\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\b")) {
                    System.out.println("Joining host ip address: " + field.getText());
                    String hostIpAddr = field.getText();

                    GameJoinThread joinThread = new GameJoinThread(hostIpAddr);
                    joinThread.start();
                    stage.setScene(waitMenuScene(hostIpAddr));
                }
                else {
                    // Display alert message
                    Alert a = new Alert(Alert.AlertType.ERROR, "Invalid IP address.");
                    a.show();
                    System.out.println("Invalid ip address");
                }
            }
        });

        vbox.getChildren().addAll(label, field, startGameBtn);

        return setupScene(hbTitle, vbox);
    }

    private Scene waitMenuScene(String hostAddress) {
        // TODO: If all players have joined, begin gameconfig

        VBox root = new VBox(15);
        Text ip = new Text("Host IP address: " + hostAddress);
        Text waitMsg = new Text();
        waitMsg.textProperty().bind(remainingConnectionsText);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                System.out.println("Game cancelled, returning to start menu");
                stage.setScene(startMenuScene());
            }
        });

        root.getChildren().addAll(ip, waitMsg, cancelBtn);
        root.setAlignment(Pos.CENTER);

        return new Scene(root, 300, 300);
    }

    private class ConnectionHandler implements ConnectionResponseHandler {
        @Override
        public void updateRemaining(int remainingConnections) {
            Platform.runLater(() -> {
                remainingConnectionsText.set("Waiting for " + remainingConnections + " players to join...");
            });
        }
    }

    private class GameHostThread extends Thread {
        @Override
        public void run() {
            boolean success = controller.onGameHost();
            Platform.runLater(() -> {
                stage.close();
            });
            System.exit(success ? 0 : 1);
        }
    }

    private class GameJoinThread extends Thread {
        String hostAddress;

        public GameJoinThread(String hostAddress) {
            this.hostAddress = hostAddress;
        }

        @Override
        public void run() {
            boolean success = controller.onGameJoin(hostAddress);
            Platform.runLater(() -> {
                stage.close();
            });
            System.exit(success ? 0 : 1);
        }
    }

}
