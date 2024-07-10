package core.pbft;

import java.io.BufferedOutputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Set;
import java.util.TreeSet;

import message.PbftMessage;
import util.ClientSocket;
import util.Constants;
import util.NodeInfo;

public class BroadcastPrimary {
	
	
	public static void broadcast(PbftMessage pbftMessage) {
		
		pbftMessage.setType(Constants.prePrepareCommandType);
		pbftMessage.setBftStep("PRE-PREPARE");
		
		Set<NodeInfo> faultyNodes = new TreeSet<NodeInfo>();
		if(Constants.nodeFaulty) 			
			while(faultyNodes.size()<Constants.maxFaultyNodes) {
				int index = Constants.random.nextInt(Constants.quorumAddresses.length);
				faultyNodes.add(pbftMessage.getNodes().get(index));
			}
				
		//it is not part of BFT, but we decided that the primary selects the faulty nodes
		//this way, each node knows if it is faulty or not		
		System.err.println("client: " + pbftMessage.getClientMsg().getClientID() 
				+ " - Number of faulty nodes: " + faultyNodes.size());
		pbftMessage.setFaultyNodes(faultyNodes);
				
		for(int i=0; i<Constants.quorumAddresses.length; i++) {
			try{
				Socket s = ClientSocket.getSocket(pbftMessage.getNodes().get(i).ip, pbftMessage.getNodes().get(i).port);
				
				ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(s.getOutputStream()));
				pbftMessage.setNodeID(pbftMessage.getNodes().get(i).ip,pbftMessage.getNodes().get(i).port);
							
				out.writeObject(pbftMessage);
				out.flush();
				
				out.close();			
				s.close();
			}catch (Exception e) {
				e.printStackTrace();
			}
		}	
	}
}
