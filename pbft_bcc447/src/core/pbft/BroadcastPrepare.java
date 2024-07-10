package core.pbft;

import java.io.BufferedOutputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;

import message.PbftMessage;
import util.ClientSocket;
import util.Constants;
import util.NodeInfo;

public class BroadcastPrepare {
	
	public static void broadcast(PbftMessage message, NodeInfo myself) {
				
		for(int i=0; i<message.getNodes().size(); i++) {
			try{
				NodeInfo nf = message.getNodes().get(i);
				
				message.setNodeID(myself.ip,myself.port);
								
				if(myself.faulty) {
						
					//decides if omission or faulty msg
					boolean flag = Constants.random.nextBoolean();
					if(flag) {
						Socket s = ClientSocket.getSocket(nf.ip, nf.port);
						ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(s.getOutputStream()));
						
						System.err.println("FAULTY msg --- Node " + myself.ip+myself.port
								+ " propagates FAULTY COMMIT msg to the quorum. Client: " + message.getClientMsg().getClientID()
								+ " VIEW: " + message.getView() + " destination node: "+nf.ip+nf.port);
						
						byte[] content = message.getClientMsg().getContent();
						
						byte[] messageContent = Files.readAllBytes(Paths.get(Constants.faultyMessageContentFile));
						
						message.getClientMsg().setContent(messageContent);
						out.writeObject(message);
						out.flush();
						
						out.close();			
						s.close();
						
						message.getClientMsg().setContent(content);
						
					}else 
						System.err.println("OMISSION FAULTY --- Node " + myself.ip+myself.port
								+ " does not propagate COMMIT msg to the quorum. Client: " + message.getClientMsg().getClientID()
								+ " VIEW: " + message.getView() + " destination node: "+nf.ip+nf.port);
									
					
				}else {
					Socket s = ClientSocket.getSocket(nf.ip, nf.port);
					ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(s.getOutputStream()));
					
					System.out.println("Correct msg --- Node " + myself.ip+myself.port
							+ " propagates Correct COMMIT msg to the quorum. Client: " + message.getClientMsg().getClientID()
							+ " VIEW: " + message.getView() + " destination node: "+nf.ip+nf.port);
					
					out.writeObject(message);
					out.flush();
					
					out.close();			
					s.close();
				}
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
	}	

}
