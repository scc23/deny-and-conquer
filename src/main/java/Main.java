import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.control.Button;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        VBox vbox = new VBox(15);
        vbox.getChildren().addAll(new Text("Welcome to Deny & Conquer!"), new Button("Host a Game"), new Button("Join a Game"));
        vbox.setAlignment(Pos.CENTER);

        primaryStage.setTitle("Deny & Conquer");
        primaryStage.setScene(new Scene(vbox, 300, 300));
        primaryStage.show();
    }

}
