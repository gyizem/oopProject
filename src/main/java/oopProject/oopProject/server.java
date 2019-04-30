package oopProject.oopProject;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

//Burada server olacak
public class server extends Application {
	private static ServerSocket server;
	private static TextArea log;
	private static Thread clientAccept;
	public static void main(String[] args) {
		launch(args);
		
	}

	@Override
	public void start(Stage stage) throws Exception {
		stage.setTitle("Server");		
		stage.setOnCloseRequest(e -> {
			System.exit(0);
		});
		log = new TextArea();
		log.setEditable(false);
		log.setText("Server logs: \n");
		server = new ServerSocket(2023);
		log.appendText("Server started.\n");
		StackPane spane = new StackPane();
		spane.getChildren().add(log);
		
		Scene scene = new Scene(spane,400,400);
		stage.setScene(scene);
		stage.show();
		clientAccept=new Thread(()->{
			while(true) {
				try {
					Socket c = server.accept();
					new Thread(new client(c)).start();
					Platform.runLater(() -> {
						log.appendText("Client bağlandı\n");
					});
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		clientAccept.start();
	}
	
	private class client implements Runnable{
		Socket socket;
		client(Socket s){
			socket = s;
		}
		@Override
		public void run() {
			
		}
		
	}

}
