package oopProject.oopProject;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

//Burada client olacak
public class App  extends Application {
	private static Socket socket;
    public static void main( String[] args ){
        launch(args);
    }

	@Override
	public void start(Stage stage) throws Exception {
		stage.setTitle("client");		
		TextArea log = new TextArea();
		log.setEditable(false);
		
		log.appendText("Client started.\n");
		try
        { 
            socket = new Socket("127.0.0.1", 2023); 
            log.appendText("Connected.\n");
        } 
        catch(Exception u) 
        { 
        	log.appendText("Connection Failed.\n");
        }
		StackPane spane = new StackPane();
		spane.getChildren().add(log);
		
		Scene scene = new Scene(spane,400,400);
		stage.setScene(scene);
		stage.show();
	}
}
