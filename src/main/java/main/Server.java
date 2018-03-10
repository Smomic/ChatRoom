package main;

import view.ServerViewController;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * Class responsible for running the server view. It creates
 * ServerViewController base on .fxml file
 *
 * @author Michal
 */
public class Server extends Application {

    @Override
    public void start(final Stage primaryStage) throws Exception {
        Parent root;
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/server.fxml"));
        root = fxmlLoader.load();
        ServerViewController viewController = fxmlLoader.getController();
        viewController.init();
        final Scene scene = new Scene(root);

        primaryStage.setTitle("Server");
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });
    }

    public static void main(String[] args) {
        launch(args);

    }
}