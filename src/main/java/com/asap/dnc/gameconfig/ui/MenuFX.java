package com.asap.dnc.gameconfig.ui;

import com.asap.dnc.core.Cell;
import com.asap.dnc.core.EndGameHandler;
import com.asap.dnc.core.PenColor;
import com.asap.dnc.gameconfig.GameConfig;
import com.asap.dnc.network.ClientInfo;
import com.asap.dnc.network.GameServer;
import com.asap.dnc.network.gameconfig.HostClientBridge;
import com.asap.dnc.network.gameconfig.HostClientBridgeImpl;
import com.asap.dnc.network.gameconfig.ConnectionResponseHandler;
import com.asap.dnc.network.gameconfig.client.ClientConnection;
import com.asap.dnc.network.gameconfig.client.ClientGrid;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

import java.net.SocketException;
import java.util.*;

public class MenuFX extends Application {

    private GameConfig gameConfig;
    private Stage stage;
    private HostClientBridge hostClientBridge;
    private StringProperty remainingConnectionsText = new SimpleStringProperty("Waiting for 4 more players to join...");
    private ClientGrid clientGrid;

    @Override
    public void init() {
        hostClientBridge = new HostClientBridgeImpl();
        hostClientBridge.setConnectionResponseHandler(new ConnectionHandler());
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;

        // Display the start menu at the beginning
        Scene scene = startMenuScene();

        primaryStage.setTitle("Deny & Conquer");
        primaryStage.setScene(scene);
        stage.getScene().getWindow().addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, this::closeWindowEvent);
        primaryStage.show();
    }

    private void closeWindowEvent(WindowEvent e) {
        hostClientBridge.closeLocalHostServer();
    }

    // Start menu
    private Scene startMenuScene() {
        Text text = new Text("Welcome to Deny & Conquer!");
        VBox root = new VBox(15);

        // Host gameconfig button to display network menu
        Button hostGameBtn = new Button("Host Game");
        hostGameBtn.setOnAction(
              event ->  stage.setScene(serverMenuScene())
        );

        // Join gameconfig button to display client menu
        Button joinGameBtn = new Button("Join Game");
        joinGameBtn.setOnAction(
                event -> stage.setScene(clientMenuScene())
        );

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
        backBtn.setStyle(
                "-fx-border-color: transparent; -fx-border-width: 0; -fx-background-radius: 0; -fx-background-color: transparent; -fx-cursor: hand;");

        // Return to the start menu when backBtn is clicked
        backBtn.setOnAction(
                event -> stage.setScene(startMenuScene())
        );

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

        final ComboBox comboBox = new ComboBox<>(options);
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

        // Create dropdown menu for threshold configuration
        Label labelThreshold = new Label("Select threshold: ");
        ObservableList<String> thresholdOptions = FXCollections.observableArrayList();
        for (int i = 30; i < 100; i+=10) thresholdOptions.add(String.valueOf(i));
        HBox thresholdConfig = createComboBox(labelThreshold, thresholdOptions);

        // Start gameconfig/wait for more players
        Button startGameBtn = new Button("Start");
        startGameBtn.setMaxSize(100, 200);

        startGameBtn.setOnAction(
                event -> {
                    // Get network ip address
                    ComboBox penComboBox = (ComboBox) penConfig.getChildren().get(1);
                    int penThickness = Integer.parseInt((String) penComboBox.getValue());

                    ComboBox boardComboBox = (ComboBox) gameBoardConfig.getChildren().get(1);
                    int gridSize = Integer.parseInt(((String) boardComboBox.getValue()).substring(0, 1));

                    ComboBox thresholdComboBox = (ComboBox) thresholdConfig.getChildren().get(1);
                    double threshold = Integer.parseInt(((String) thresholdComboBox.getValue()));

                    gameConfig = new GameConfig(2, penThickness, gridSize, threshold);

                    System.out.println("Starting gameconfig...");

                    Thread hostThread = new Thread(() -> {
                        hostClientBridge.connectLocalHostServer(gameConfig);
                        startGame();
                    });
                    hostThread.start();
                    try {
                        stage.setScene(waitMenuScene(ClientConnection.getPublicIPV4Address().getHostAddress()));
                    } catch (SocketException e) {
                        e.printStackTrace();
                        System.exit(-1);
                    }
                }
        );

        vbox.getChildren().addAll(penConfig, gameBoardConfig, thresholdConfig, startGameBtn);

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
        startGameBtn.setOnAction(
                event -> {
                    // TODO: Check if we can successfully connect to the network

                    // Check if inputted ip address is in valid format
                    if (field.getText().matches("\\b\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\b")) {
                        System.out.println("Joining host ip address: " + field.getText());
                        String hostIpAddr = field.getText();

                        Thread joinThread = new Thread(() -> {
                            hostClientBridge.connectRemoteHostServer(hostIpAddr);
                            startGame();
                        });
                        joinThread.start();
                        stage.setScene(waitMenuScene(hostIpAddr));
                    } else {
                        // Display alert message
                        Alert a = new Alert(Alert.AlertType.ERROR, "Invalid IP address.");
                        a.show();
                        System.out.println("Invalid ip address");
                    }
                }
        );

        vbox.getChildren().addAll(label, field, startGameBtn);

        return setupScene(hbTitle, vbox);
    }

    private Scene inGameScene() {
        // Display game grid
        return this.clientGrid.getGridpane();
    }

    private Scene reconfigMenuScene() {
        VBox root = new VBox(15);

        Text reconfigMsg = new Text();
        StringProperty reconfigStringProperty = new SimpleStringProperty(
                "Host server unexpectedly dropped, performing reconfiguration...");
        reconfigMsg.textProperty().bind(reconfigStringProperty);

        Text connectionsMsg = new Text();
        int remainingConnections = hostClientBridge.getHostClientConfiguration().getNumberPlayers() - 1;
        remainingConnectionsText.set("Waiting for " + remainingConnections + " players to join...");
        connectionsMsg.textProperty().bind(remainingConnectionsText);

        Thread thread = new Thread(() -> {
            if (!hostClientBridge.reconfigRemoteHostServer()) {
                Platform.runLater(() -> {
                    stage.setScene(gameEndScene( null));
                });
                return;
            }
            Platform.runLater(() -> {
                reconfigStringProperty.set("Game has been successfully reconfigured, reloading game state...");
                // Get host server info
                ClientInfo hostServerInfo = (ClientInfo) hostClientBridge.getHostServerInfo();
                // set new address in CoreGameClient in ClientGrid
                this.clientGrid.setClientConfig(hostServerInfo.getAddress());
                // Start game reconfiguration
                startGameReconfig();
            });
        });
        thread.start();

        root.getChildren().addAll(reconfigMsg, connectionsMsg);
        root.setAlignment(Pos.CENTER);
        return new Scene(root, 300, 300);
    }

    private Scene waitMenuScene(String hostAddress) {
        // TODO: If all players have joined, begin gameconfig

        VBox root = new VBox(30);
        Text ip = new Text("Host IP address: " + hostAddress);
        Text waitMsg = new Text();
        waitMsg.textProperty().bind(remainingConnectionsText);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setOnAction(
                event -> {
                    System.out.println("Game cancelled, returning to start menu");
                    stage.setScene(startMenuScene());
                }
        );

        root.getChildren().addAll(ip, waitMsg, cancelBtn);
        root.setAlignment(Pos.CENTER);

        return new Scene(root, 300, 300);
    }

    private Scene gameEndScene(Map<PenColor, Integer> sortedScoreMap) {
        VBox root = new VBox(30);
        root.setAlignment(Pos.CENTER);
        Text gameEndText = new Text("Game Over.");
        root.getChildren().add(gameEndText);

        Button mainMenuBtn = new Button("Main menu");
        Button exitBtn = new Button("Exit");

        mainMenuBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                stage.setScene(startMenuScene());
            }
        });

        exitBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                System.exit(0);
            }
        });

        if (sortedScoreMap == null) {
            Text noClientsRemainingText = new Text("All other players have disconnected.");
            root.getChildren().add(noClientsRemainingText);
            root.getChildren().addAll(mainMenuBtn, exitBtn);
            return new Scene(root, 300, 300);
        }

        ArrayList<Text> rankingTexts = new ArrayList<>();
        int i = 0;
        for (Map.Entry<PenColor, Integer> pc :  sortedScoreMap.entrySet()) {
            Text ranking = new Text((i+1) + ". " + pc.getKey() + "\t\t\t\t\t" + pc.getValue());
            rankingTexts.add(ranking);
            i++;
        }

        root.getChildren().addAll(rankingTexts);
        root.getChildren().addAll(mainMenuBtn, exitBtn);
        return new Scene(root, 300, 300);
    }

    private void startGame() { // todo: pass cleanUpHandler into grid
        CleanUpHandler cleanUpHandler = new CleanUpHandler();
        Thread backgroundThread = new Thread(new BackgroundTask());
        cleanUpHandler.addThread(backgroundThread);

        if (hostClientBridge.isLocalHostServer()) {
            Thread gameServerThread = new Thread(new GameServerTask());
            cleanUpHandler.addThread(gameServerThread);
            gameServerThread.start();
        }

        backgroundThread.start();
        Platform.runLater(() -> {
            // Get host server info
            ClientInfo hostServerInfo = (ClientInfo) hostClientBridge.getHostServerInfo();

            // Get client info
            ClientInfo clientInfo = (ClientInfo) hostClientBridge.getClientInfo();
            System.out.println("host" + hostServerInfo);
            System.out.println("client" + clientInfo);
            // Pass in game config info, host server address, and client info
            this.clientGrid = new ClientGrid(hostClientBridge.getHostClientConfiguration(),
                    hostServerInfo.getAddress(), clientInfo, hostClientBridge.getHostClientClock(), cleanUpHandler);

            stage.setScene(inGameScene());
        });
    }

    private void startGameReconfig() {
        CleanUpHandler cleanUpHandler = new CleanUpHandler();
        Thread backgroundThread = new Thread(new BackgroundTask());
        cleanUpHandler.addThread(backgroundThread);

        if (hostClientBridge.isLocalHostServer()) {
            // Create reconfigured server grid with existing cells
            Thread gameServerThread = new Thread(new GameServerTaskReconfig(this.clientGrid.getCells()));
            cleanUpHandler.addThread(gameServerThread);
            gameServerThread.start();
        }

        backgroundThread.start();
        Platform.runLater(() -> {
            // Set scene with existing client grid, but with new configurations
            System.out.println("Setting new grid to scene...");

            // Get host server info
            ClientInfo hostServerInfo = (ClientInfo) hostClientBridge.getHostServerInfo();
            // set new address in CoreGameClient in ClientGrid
            this.clientGrid.setClientConfig(hostServerInfo.getAddress());
            // Display grid
            stage.setScene(inGameScene());
        });
    }

    private class ConnectionHandler implements ConnectionResponseHandler {
        @Override
        public void updateRemaining(int remainingConnections) {
            Platform.runLater(() -> {
                remainingConnectionsText.set("Waiting for " + remainingConnections + " players to join...");
            });
        }
    }

    private class CleanUpHandler implements EndGameHandler {
        List<Thread> cleanThreads = new ArrayList<>();

        public void addThread(Thread thread) {
            cleanThreads.add(thread);
        }

        @Override
        public void onGameEnd(Map<PenColor, Integer> cellMap) {
            System.out.println(" cell map ->" + cellMap);
            cleanThreads();

            List<Map.Entry<PenColor, Integer>> scoreList = new LinkedList<Map.Entry<PenColor, Integer>>(cellMap.entrySet());

            // sort the list
            Collections.sort(scoreList, new Comparator<Map.Entry<PenColor, Integer>>() {
                @Override
                public int compare(Map.Entry<PenColor, Integer> o1, Map.Entry<PenColor, Integer> o2) {
                    return (o1.getValue()).compareTo(o2.getValue());
                }
            });

            Map<PenColor, Integer> sortedScoreMap = new LinkedHashMap<>();
            for (Map.Entry<PenColor, Integer> pc : scoreList){
                sortedScoreMap.put(pc.getKey(), pc.getValue());
            }

            Platform.runLater(() -> {
                stage.setScene(gameEndScene(sortedScoreMap));
            });
        }

        private void cleanThreads() {
            hostClientBridge.closeLocalHostServer();
            for (Thread t : cleanThreads) {
                if (t != null && t.isAlive()) {
                    t.interrupt();
                }

            }
        }
    }

    private class BackgroundTask implements Runnable {
        @Override
        public void run() {
            while (true) {
                //System.out.println("Sending keep alive...");
                if (!hostClientBridge.checkHostAlive()) {
                    Platform.runLater(() -> {
                        stage.setScene(reconfigMenuScene());
                    });
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }
    }

    private class GameServerTask implements Runnable {

        private GameServer gameServer;

        @Override
        public void run() {
            Thread child = null;

            gameServer = null;
            try {
                System.out.println("Starting Gameserver..");
                gameServer = new GameServer((ClientInfo[]) hostClientBridge.getAllClients());
                child = new Thread(() -> {
                    gameServer.init(gameConfig.getGridSize());
                });
                child.start();
                Thread.sleep(600000);
            } catch (InterruptedException e) {
                child.interrupt();
                gameServer.clear();
                e.printStackTrace();
            } finally {

            }
        }
    }

    private class GameServerTaskReconfig implements Runnable {
        private Cell[][] existingState;

        public GameServerTaskReconfig(Cell[][] existingState) {
            this.existingState = existingState;
        }
        @Override
        public void run() {
            try {
                System.out.println("Starting reconfigured Gameserver..");
                GameServer gameServer = new GameServer((ClientInfo[]) hostClientBridge.getAllClients());
                gameConfig = hostClientBridge.getHostClientConfiguration();
                gameServer.initReconfig(gameConfig.getGridSize(), existingState);
            } finally {
                // cleanup
            }
        }
    }

}
