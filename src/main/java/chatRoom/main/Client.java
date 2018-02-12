package main;

import view.ClientViewController;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * Class responsible for running the client view that responsible for handling
 * Client's side of chat. It creates ClientViewController base on .fxml file
 * 
 * @author Michal
 */
public class Client extends Application {

	@Override
	public void start(final Stage primaryStage) throws Exception {
		Parent root;
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/client.fxml"));
		root = fxmlLoader.load();
		ClientViewController viewController = fxmlLoader.getController();
		viewController.init();
		final Scene scene = new Scene(root);

		primaryStage.setTitle("Client");
		primaryStage.setScene(scene);
		primaryStage.show();
		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent e) {
				Platform.exit();
				System.exit(0);
			}
		});
	}
	
	public static void main(String[] args) {
		launch(args);

	}
}
