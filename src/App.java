import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class App extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("app_files/App.fxml"));
        primaryStage.setTitle("Inverse search engine for PDF files");
        primaryStage.setScene(new Scene(root, 600, 600));

        primaryStage.setMaxWidth(600);
        primaryStage.setMinWidth(600);
        primaryStage.setMaxHeight(730);
        primaryStage.setMinHeight(730);

        primaryStage.getIcons().add(new Image(App.class.getResourceAsStream("app_files/icon.png")));

        primaryStage.show();
    }
}
