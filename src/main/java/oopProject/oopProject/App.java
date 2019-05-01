package oopProject.oopProject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

//Burada client olacak
public class App  extends Application {
	private static Socket socket;
	private static DataInputStream input;
	private static DataOutputStream output;
	private static TextField msg;
	private static TextArea log;
    public static void main( String[] args ){
        launch(args);
    }

	@Override
	public void start(Stage stage) throws Exception {
		stage.setTitle("client");		
		log = new TextArea();
		log.setEditable(false);
		
		msg = new TextField();
		Button send = new Button("send");
		send.addEventHandler(ActionEvent.ACTION,new sendListener());
		
		BorderPane foot = new BorderPane();
		foot.setRight(send);
		foot.setCenter(msg);
		
		BorderPane spane = new BorderPane();
		spane.setCenter(log);
		spane.setBottom(foot);
		
		
		Scene scene = new Scene(spane,400,400);
		stage.setScene(scene);
		stage.show();
		
		log.appendText("Client started.\n");
		
		try {
			socket = new Socket("127.0.0.1", 2023);
			input = new DataInputStream(socket.getInputStream());
			output = new DataOutputStream(socket.getOutputStream());
			log.appendText("Connected.\n");
		}catch(Exception e) {
			log.appendText("Connection Failed.\n");
		}
		
	}
	
	private class sendListener implements EventHandler<ActionEvent>{
		private String res;
		@Override
		public void handle(ActionEvent e) {
			try {
				output.writeUTF(msg.getText());
				output.flush();
				
				log.setText("");
				
				res = input.readUTF();
				
				Platform.runLater(()->{
					log.appendText(res);
				});
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
			msg.setText("");
		}
	}
}
