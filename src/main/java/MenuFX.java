import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.control.Button;

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

    public Scene startMenuScene() {
        Label label = new Label("Welcome to Deny & Conquer!");

        VBox root = new VBox(15);

        Button hostGameBtn = new Button("Host a Game");
        Button joinGameBtn = new Button("Join a Game");

        // Display the server menu when hostGameBtn is clicked
        hostGameBtn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                stage.setScene(serverMenuScene());
            }
        });

        // Display the client menu when joinGameBtn is clicked
        joinGameBtn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                stage.setScene(clientMenuScene());
            }
        });

        root.getChildren().addAll(label, hostGameBtn, joinGameBtn);
        root.setAlignment(Pos.CENTER);

        return new Scene(root, 300, 300);
    }

    protected Scene setupScene(HBox hbTitle, VBox vbox) {
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
        root.setPadding(new Insets(35, 0, 0, 0));
        root.setTop(hbTitle);
        root.setCenter(vbox);
        root.setBottom(hbButtons);

        return new Scene(root, 300, 300);
    }

    protected Scene serverMenuScene() {
        HBox hbTitle = new HBox();
        Label labelTitle = new Label("Host a Game");
        hbTitle.getChildren().add(labelTitle);

        VBox vbox = new VBox(15);
        Label labelPenThickness = new Label("Thickness of pen:");
        Label labelGameBoardSize = new Label("Size of game board:");

        Button startGameBtn = new Button("Start");
        startGameBtn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                System.out.println("Starting game...");
            }
        });

        vbox.getChildren().addAll(labelPenThickness, labelGameBoardSize, startGameBtn);

        return setupScene(hbTitle, vbox);
    }

    protected Scene clientMenuScene() {
        HBox hbTitle = new HBox();
        Label labelTitle = new Label("Join a Game");
        hbTitle.getChildren().add(labelTitle);

        VBox vbox = new VBox(15);
        Label label = new Label("Please enter host IP address:");
        final TextField field = new TextField();
        field.setMaxWidth(150);

        Button startGameBtn = new Button("Start");

        // Display waiting scene when joinGameBtn is clicked
        startGameBtn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                System.out.println("Joining host ip address: " + field.getText());
            }
        });

        vbox.getChildren().addAll(label, field, startGameBtn);

        return setupScene(hbTitle, vbox);
    }

}
