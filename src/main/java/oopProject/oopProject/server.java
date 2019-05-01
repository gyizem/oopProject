package oopProject.oopProject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

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
					
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		clientAccept.start();
	}
	
	private class client implements Runnable{
		private Socket socket;
		private DataInputStream input;
		private DataOutputStream output;
		private String pos = "\\";
		private String send;
		client(Socket s){
			socket = s;
			try {
				input = new DataInputStream(socket.getInputStream());
				output = new DataOutputStream(socket.getOutputStream());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		private void ls() {
			Path path = Paths.get("files"+pos);
			Stream<Path> list;
			try {
				list = Files.list(path);
				send += "Path : "+pos+"\n";
				list.forEach((e)->{
					send += e.getFileName()+(Files.isDirectory(e) ? "\t(dir)" : "")+"\n";
				});
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		
		@Override
		public void run() {
			Platform.runLater(() -> {
				log.appendText("Client connected "+socket.getInetAddress()+"\n");
			});
			try {
				while(true) {
					String msg = input.readUTF();
					Platform.runLater(() -> {
						log.appendText("Client("+socket.getInetAddress()+") : "+msg+"\n");
					});
					
					String cs[] = msg.split(" ");
					send = "";
					if(cs[0].equals("ls")){
						ls();
					}else if(cs[0].equals("cd")){
						String buf = pos;
						String dirs[] = cs[1].split("\\\\");
						for(String c:dirs) {
							if(!c.isEmpty())
								if(c.equals("..")) {
									if(Paths.get(buf).getParent() == null) {
										buf="notvalid";
										break;
									}else {
										buf = Paths.get(buf).getParent().toString();
										if(buf.length()>1) {
											buf += "\\";
										}
									}
								}else {
									buf += c+"\\";
								}
						}
						System.out.println("files"+buf);
						if(!buf.equals("notvalid") && Files.exists(Paths.get("files"+buf), LinkOption.NOFOLLOW_LINKS)) {
							pos = buf;
							ls();
						}else {
							send = msg.substring(3, msg.length())+" is not exist! \n";
						}
						System.out.println("Pos : files"+pos);
					}else {
						output.writeUTF("Bu ne?\n");
					}
					
					output.writeUTF(send);
					output.flush();
				}
			} catch (IOException e) {
				Platform.runLater(() -> {
					log.appendText("Client("+socket.getInetAddress()+") connection lost\n");
				});
			}
		}
		
	}

}
