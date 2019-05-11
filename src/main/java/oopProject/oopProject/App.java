package oopProject.oopProject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

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
		msg.addEventHandler(ActionEvent.ACTION,new sendListener());
		
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
				
				String buf[] = msg.getText().split(" ");
				
				if(buf[0].equals("write")) {
					if(buf.length == 3) {
						FileInputStream f = new FileInputStream(new File(buf[1]));
						System.out.println("yollanan boyut "+f.available());
						int size = f.available();
						byte b[] = new byte[f.available()];
						f.read(b);
						f.close();
						
						output.writeUTF("write "+buf[2]);
						output.writeInt(size);
						output.write(b);
						output.flush();
					}else {
						Platform.runLater(()->{
							log.appendText("Command used wrong");
						});
					}
				}else if(buf[0].equals("read")) {
					if(buf.length == 3) {
						output.writeUTF("read "+buf[1]);
						output.flush();
						
						int totalSize = input.readInt();
						int readed = 0;
						FileOutputStream f =new FileOutputStream(new File(buf[2]));
						while(readed < totalSize) {
							while(input.available()==0);
							
							byte b[];
							if(input.available()+readed <= totalSize) {
								b = new byte[input.available()];
								readed += input.available();
							}else{
								b = new byte[totalSize-readed];
								readed += totalSize-readed;
							}
							System.out.println("alınan boyut "+b.length+" toplam alınan "+readed+"/"+totalSize);
							input.read(b);
							f.write(b);
						}
						f.flush();
						f.close();
						
					}else {
						Platform.runLater(()->{
							log.appendText("Command used wrong");
						});
					}
				}else {
					output.writeUTF(msg.getText());
					output.flush();
				}
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
