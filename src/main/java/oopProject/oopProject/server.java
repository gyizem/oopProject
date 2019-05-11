package oopProject.oopProject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.DirectoryNotEmptyException;
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
					if(cs[0].equals("ls") && cs.length == 1){
						ls();
					}else if(cs[0].equals("cd") && cs.length == 2){
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
						if(!buf.equals("notvalid") && Files.exists(Paths.get("files"+buf), LinkOption.NOFOLLOW_LINKS)) {
							if(Files.isDirectory(Paths.get("files"+buf))) {
								pos = buf;
								ls();
							}else {
								send = cs[1]+" is not a directory.\n";
							}
							
						}else {
							send = msg.substring(3, msg.length())+" is not exist! \n";
						}
					}else if(cs[0].equals("pwd") && cs.length == 1){
						send = "You are in \""+pos+"\" \n";
					}else if(cs[0].equals("rm") && cs.length == 2){
						Path rmD = Paths.get("files\\"+pos+cs[1]);
						if(!Files.exists(rmD)){
							send = "\""+cs[1]+"\" is not exists.\n";
						}else {
							try {
								Files.delete(rmD);
								send = "\""+cs[1]+"\" removed.\n";
							}catch(DirectoryNotEmptyException e) {
								send = "Removing failed, directory is not empty";
							}catch(Exception e) {
								e.printStackTrace();
							}
							
						}
					}else if(cs[0].equals("mkdir") && cs.length == 2){
						Path newD = Paths.get("files\\"+pos+cs[1]);
						if(Files.exists(newD)){
							send = "\""+cs[1]+"\" already created\n";
						}else {
							send = "\""+cs[1]+"\" created.\n";
							Files.createDirectories(newD);
						}
						
						
					}else if(cs[0].equals("help") && cs.length == 1){
						send = "cd <dest> - open the directory \n"+
								"rm <file> - remove the file \n"+
								"mkdir <file> - remove the file \n"+
								"ls - show all files \n"+
								"pwd -  print the path to the directory \n";
					}else if(cs[0].equals("write") && cs.length == 2){
						try {
							int totalSize = input.readInt();
							int readed = 0;
							FileOutputStream f =new FileOutputStream(new File("files"+pos+cs[1]));
							while(readed < totalSize) {
								while(input.available()==0);
								readed += input.available();
								byte b[] = new byte[input.available()];
								System.out.println("alınan boyut "+input.available()+" toplam alınan "+readed+"/"+totalSize);
								input.read(b);
								f.write(b);
							}
							f.flush();
							f.close();
							send = "File uploaded\n";
						}catch(Exception e) {
							send = "Error happend\n";
						}
					}else if(cs[0].equals("read") && cs.length == 2){
						try {
							FileInputStream f = new FileInputStream("files"+pos+new File(cs[1]));
							System.out.println("yollanan boyut "+f.available());
							int size = f.available();
							byte b[] = new byte[f.available()];
							f.read(b);
							f.close();
							
							output.writeInt(size);
							output.write(b);
							output.flush();
							send = "File downloaded\n";
						}catch(Exception e) {
							send = "Error happend\n";
						}
					}else {
						send = msg+" is not valid command\nTo show all commands write \"help\"";
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
