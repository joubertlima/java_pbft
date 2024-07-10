package core;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.PublicKey;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import message.ResponseForClientMessage;
import util.NodeInfo;


public class Node {
	
	private static Scanner input;
	
	//variables do maintain the control of BFT protocols
	private Map<String, PublicKey> prePreparePubKey;
	private Map<String, PublicKey> preparePubKey;
	private Map<String,Set<String>> prepareContext;
	private Map<String,Set<String>> commitContext;
	private Map<String, List<ResponseForClientMessage>> clientContext;
	
	private Map<String, AtomicInteger> clientContextSubmit;
	private Map<String, Boolean> prepareContextSubmit;
	private Map<String, Boolean> commitContextSubmit;
	private AtomicInteger view;
	private NodeInfo myself;
	
	public static void main(String[] args) {
		input = new Scanner(System.in);
		System.out.print("Enter a node port: ");
		int port = input.nextInt();
		System.out.println("You entered: " + port);
		
		new Node(port);		
	}
	
	public Node(int port) {
		ServerSocket serverSocket = null;
				
		prePreparePubKey = new TreeMap<String,PublicKey>();
		preparePubKey = new TreeMap<String,PublicKey>();
		prepareContext = new TreeMap<String,Set<String>>();
		commitContext = new TreeMap<String,Set<String>>();
		clientContext = new TreeMap<String, List<ResponseForClientMessage>>();		
		
		clientContextSubmit = new TreeMap<String, AtomicInteger>();
		prepareContextSubmit = new TreeMap<String, Boolean>();
		commitContextSubmit = new TreeMap<String, Boolean>();
		view = new AtomicInteger(1);
		myself = new NodeInfo();
		
		Object[] control = {prePreparePubKey, preparePubKey, prepareContext, commitContext,
				clientContext, clientContextSubmit, prepareContextSubmit, commitContextSubmit};
		
		
		try {
			serverSocket = new ServerSocket(port);
			
			System.out.println("Node is listening on port " + port);
			
			while (true) {
	            Socket socket = serverSocket.accept();
	            new AsyncResponse(socket, view, myself, control).start();
			}
		}catch (Exception e) {
			e.printStackTrace();
			try {
				serverSocket.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
	

}
