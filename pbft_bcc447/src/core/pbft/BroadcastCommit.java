package core.pbft;

import java.io.BufferedOutputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.SecureRandom;

import message.PbftMessage;
import message.ResponseForClientMessage;
import util.ClientSocket;
import util.Constants;
import util.NodeInfo;

public class BroadcastCommit {
	
	public static void broadcast(PbftMessage message, NodeInfo myself, boolean isMeshBased) {
		
				
		if(myself.faulty) {
			
			//decides if omission or faulty msg
			boolean flag = Constants.random.nextBoolean();
			if(flag) {
									
				System.err.println("FAULTY msg --- Node " + myself.ip+myself.port
						+ " propagates FAULTY CLIENT msg to the client. Client: " + message.getClientMsg().getClientID()
						+ " VIEW: " + message.getView());				
				
				PublicKey pub=null;
				
				//generating public and private keys from the client content		
				try {
					byte[] messageContent = Files.readAllBytes(Paths.get(Constants.faultyMessageContentFile));
					
					SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
					random.setSeed(messageContent.length);   //related with client message
					KeyPairGenerator keyGen=KeyPairGenerator.getInstance("RSA");
					keyGen.initialize(1024, random);
					KeyPair pair=keyGen.generateKeyPair();
					pub=pair.getPublic();				
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				try{
					
					Socket s = ClientSocket.getSocket(message.getClientMsg().getClientAddress(),
							 message.getClientMsg().getClientPort());			
								
					ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(s.getOutputStream()));
								
					out.writeObject(mountClientMsg(message, pub, myself, isMeshBased));
					out.flush();
					
					out.close();			
					s.close();			
								
				}catch(Exception e){
					e.printStackTrace();
				}					
				
			}else 
				System.err.println("OMISSION FAULTY --- Node " + myself.ip+myself.port
						+ " does not propagate CLIENT msg to the client. Client: " + message.getClientMsg().getClientID()
						+ " VIEW: " + message.getView());
				
			
		}else {
			
			System.out.println("Correct msg --- Node " + myself.ip+myself.port
					+ " propagates Correct CLIENT msg to the client. Client: " + message.getClientMsg().getClientID()
					+ " VIEW: " + message.getView());
			
			
			try{
				
				Socket s = ClientSocket.getSocket(message.getClientMsg().getClientAddress(),
						 message.getClientMsg().getClientPort());			
							
				ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(s.getOutputStream()));
							
				out.writeObject(mountClientMsg(message, null, myself, isMeshBased));
				out.flush();
				
				out.close();			
				s.close();			
							
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
	}


	private static ResponseForClientMessage mountClientMsg(PbftMessage message, PublicKey pub, NodeInfo myself, boolean isMeshBased) {
		ResponseForClientMessage msg = new ResponseForClientMessage();
		if(!isMeshBased)
			msg.setType(Constants.clientResponsesCommandType);
		else msg.setType(Constants.clientResponsesMeshCommandType);
		
		msg.setVote("commit");
		msg.setHostID(myself);
		msg.setNodes(message.getNodes());
		msg.setView(message.getView());
		msg.setClientID(message.getClientMsg().getClientID());
		msg.setTime(message.getClientMsg().getTime());
		if(pub==null)
			msg.setPubKey(message.getPubKey().hashCode());
		else msg.setPubKey(pub.hashCode());
		
		return msg;
	}
	
	public static void faultyBroadcast(PbftMessage message, NodeInfo myself, boolean isMeshBased) {
		
		System.err.println("FAULTY msg to force the client to restart the procedure --- Node " + myself.ip+myself.port
				+ " propagates FAULTY CLIENT msg to the client. Client: " + message.getClientMsg().getClientID()
				+ " VIEW: " + message.getView());				
		
		PublicKey pub=null;
		
		//generating public and private keys from the client content		
		try {
			byte[] messageContent = Files.readAllBytes(Paths.get(Constants.faultyMessageContentFile));
			
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
			random.setSeed(messageContent.length);   //related with client message
			KeyPairGenerator keyGen=KeyPairGenerator.getInstance("RSA");
			keyGen.initialize(1024, random);
			KeyPair pair=keyGen.generateKeyPair();
			pub=pair.getPublic();				
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try{
			
			Socket s = ClientSocket.getSocket(message.getClientMsg().getClientAddress(),
					 message.getClientMsg().getClientPort());			
						
			ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(s.getOutputStream()));
						
			out.writeObject(mountClientMsg(message, pub, myself, isMeshBased));
			out.flush();
			
			out.close();			
			s.close();			
						
		}catch(Exception e){
			e.printStackTrace();
		}
		
		
	}

}
