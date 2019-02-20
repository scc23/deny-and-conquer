import javafx.application.Application;
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
import javafx.stage.Stage;

public class MenuFX extends Application {

    private Stage stage;

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
        Label label = new Label("Welcome to Deny & Conquer!");
        VBox root = new VBox(15);

        // Host game button to display server menu
        Button hostGameBtn = new Button("Host Game");
        hostGameBtn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                stage.setScene(serverMenuScene());
            }
        });

        // Join game button to display client menu
        Button joinGameBtn = new Button("Join Game");
        joinGameBtn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                stage.setScene(clientMenuScene());
            }
        });

        root.getChildren().addAll(label, hostGameBtn, joinGameBtn);
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
        Label labelTitle = new Label("Host Game");
        hbTitle.getChildren().add(labelTitle);

        VBox vbox = new VBox(15);

        // Create dropdown menu for pen thickness configuration
        Label labelPenThickness = new Label("Pen thickness: ");
        ObservableList<String> penThicknessOptions = FXCollections.observableArrayList("1", "2", "3", "4", "5", "6", "7", "8", "9", "10");
        HBox penConfig = createComboBox(labelPenThickness, penThicknessOptions);

        // Create dropdown menu for pen game board size configuration
        Label labelGameBoardSize = new Label("Game board size: ");
        ObservableList<String> gameBoardSizeOptions = FXCollections.observableArrayList("2x2", "3x3", "4x4", "5x5", "6x6", "7x7", "8x8", "9x9", "10x10");
        HBox gameBoardConfig = createComboBox(labelGameBoardSize, gameBoardSizeOptions);

        // Start game/wait for more players
        Button startGameBtn = new Button("Start");
        startGameBtn.setMaxSize(100,200);
        startGameBtn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                System.out.println("Starting game...");
            }
        });

        vbox.getChildren().addAll(penConfig, gameBoardConfig, startGameBtn);

        return setupScene(hbTitle, vbox);
    }

    // Client menu
    private Scene clientMenuScene() {
        HBox hbTitle = new HBox();
        Label labelTitle = new Label("Join Game");
        hbTitle.getChildren().add(labelTitle);

        VBox vbox = new VBox(15);
        Label label = new Label("Please enter host IP address:");
        final TextField field = new TextField();
        field.setMaxWidth(150);

        // Start game/wait for more players
        Button startGameBtn = new Button("Start");
        startGameBtn.setMaxSize(100,200);
        startGameBtn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                System.out.println("Joining host ip address: " + field.getText());
            }
        });

        vbox.getChildren().addAll(label, field, startGameBtn);

        return setupScene(hbTitle, vbox);
    }

}
