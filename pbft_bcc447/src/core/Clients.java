package core;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;

import message.ClientMessage;
import util.ClientSocket;
import util.Constants;

public class Clients {
		
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub		
		try {
			byte[] messageContent = Files.readAllBytes(Paths.get(Constants.messageContentFile));
					
			new Clients(Constants.primaryAddress, Constants.primaryPort, 
					Constants.primaryCommandType, messageContent);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
		
	public Clients(String host, int port, String type, byte[] encoded){
		try{
			
			ClientMessage msg = new ClientMessage();
			msg.setTime(System.currentTimeMillis());
			msg.setType(type);
			msg.setContent(encoded);			
			
			msg.setClientAddress(Constants.clientResponsesServerAddress);
			msg.setClientPort(Constants.clientResponsesServerPort);	
		
			ServerStarter clientS = new ServerStarter(Constants.clientResponsesServerPort);
			clientS.start();
						
			//we can start many client calls
			for(int i=0; i<Constants.numOfClients; i++) {
				Socket s = ClientSocket.getSocket(host, port);
				ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(s.getOutputStream()));
				msg.setClientID(i);
				out.writeObject(msg);
				out.flush();
				out.close();			
				s.close();	
				Thread.sleep(Constants.delayBetweenClientMessages);
			}
			
						
		}catch(Exception e){
			e.printStackTrace();
		}
	}	
	
			
	class ServerStarter extends Thread{
		int port;
		
		public ServerStarter(int port) {
			this.port = port;
		}
		
		public void run() {
			new Node(port);
		}
		
	}
	
	public static synchronized void send(String host, int port, String type, byte[] encoded, int clientID, long time) {
		ClientMessage msg = new ClientMessage();
		msg.setType(type);
		msg.setContent(encoded);
		msg.setTime(time);
		
		msg.setClientAddress(Constants.clientResponsesServerAddress);
		msg.setClientPort(Constants.clientResponsesServerPort);	
		try {
			
			Socket s = ClientSocket.getSocket(host, port);
			ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(s.getOutputStream()));
			msg.setClientID(clientID);
			out.writeObject(msg);
			out.flush();
			out.close();			
			s.close();	
		}catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
