package core;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import core.pbft.BroadcastCommit;
import core.pbft.BroadcastPrePrepare;
import core.pbft.BroadcastPrepare;
import core.pbft.BroadcastPrimary;
import message.ClientMessage;
import message.Message;
import message.PbftMessage;
import message.ResponseForClientMessage;
import util.Constants;
import util.NodeInfo;

public class AsyncResponse extends Thread{
	
	private Socket s;
	private Message m;
	private Object[] control;
	private AtomicInteger view;
	private NodeInfo myself;
	
	public AsyncResponse(Socket s, AtomicInteger view, NodeInfo myself, Object[] control) {
		this.s=s;
		this.view = view;
		this.control=control;
		this.myself = myself;
	}
	
	public void run() {
		try{
			
			ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(s.getInputStream()));
			
			m = (Message) in.readObject();			
			doSomething();
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings({ "unchecked", "unused" })
	private void doSomething() {
		
		Map<String, PublicKey> prePreparePubKey = (Map<String, PublicKey>) control[0];
		Map<String, PublicKey> preparePubKey = (Map<String, PublicKey>) control[1];
		Map<String,Set<String>> prepareContext = (Map<String, Set<String>>) control[2];
		Map<String,Set<String>> commitContext = (Map<String, Set<String>>) control[3];
		Map<String, List<ResponseForClientMessage>> clientContext = (Map<String, List<ResponseForClientMessage>>) control[4];
		
		Map<String, AtomicInteger> clientContextSubmit = (Map<String, AtomicInteger>) control[5];
		Map<String, Boolean> prepareContextSubmit = (Map<String, Boolean>) control[6];
		Map<String, Boolean> commitContextSubmit = (Map<String, Boolean>) control[7];
		
		switch(m.getType()) {
		//primary step
		case Constants.primaryCommandType:{
			
			//firstly we increment view;
			synchronized (view) {
				System.out.println("Primary increments the view: " + view.incrementAndGet());
			}
			
			ClientMessage msg = (ClientMessage)m;
			
			System.out.println("Primary received a msg from client " + msg.getClientID() + " and the msg content size is: " + msg.getContent().length);
			
			PbftMessage pbftMessage = new PbftMessage();
			pbftMessage.setClientMsg(msg);
			pbftMessage.setView(String.valueOf(view.get()));
			
			//generating public and private keys from the client content		
			try {
				SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
				random.setSeed(msg.getContent().length);   //related with client message
				KeyPairGenerator keyGen=KeyPairGenerator.getInstance("RSA");
				keyGen.initialize(1024, random);
				KeyPair pair=keyGen.generateKeyPair();
				PublicKey pub=pair.getPublic();			
							
				pbftMessage.setPubKey(pub);
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
							
			for(int i=0; i<Constants.quorumAddresses.length; i++) 
				pbftMessage.addNode(Constants.quorumAddresses[i], Constants.quorumPorts[i]);			
					
			BroadcastPrimary.broadcast(pbftMessage);
			
			break;
		}
		//pre-prepare step
		case Constants.prePrepareCommandType:{
			
			PbftMessage pbftMessage = (PbftMessage) m;
			
			//primary gives the identification to each node
			myself.ip = pbftMessage.getNodeID().ip;
			myself.port = pbftMessage.getNodeID().port;
			
			boolean flag = false;
			PublicKey pub=null;
			
			//generating public and private keys from the client content		
			try {
				ClientMessage clientMessage = pbftMessage.getClientMsg();
				SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
				random.setSeed(clientMessage.getContent().length);   //related with client message
				KeyPairGenerator keyGen=KeyPairGenerator.getInstance("RSA");
				keyGen.initialize(1024, random);
				KeyPair pair=keyGen.generateKeyPair();
				pub=pair.getPublic();	
				
				
				if(pub.equals(pbftMessage.getPubKey())) {
					flag=true;
					pbftMessage.setPubKey(pub);
				}
				
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if(flag) {
				
				if(pbftMessage.getFaultyNodes().contains(myself)) myself.faulty=true;
				else myself.faulty=false;
				
				synchronized (prePreparePubKey) {
					PublicKey pk = prePreparePubKey.get(pbftMessage.getView());
					if(pk==null) 
						prePreparePubKey.put(pbftMessage.getView(), pbftMessage.getPubKey());						
				}
						
				System.out.println("PBFT step:" + pbftMessage.getBftStep() + " - Node: " + myself.ip+myself.port 
						+ " - Client: " + pbftMessage.getClientMsg().getClientID() + " VIEW: "+ pbftMessage.getView());
				
				BroadcastPrePrepare.broadcast(pbftMessage,myself);	
			}else 
				System.err.println("Public keys different at pre-prepare step. Node: " + myself.ip+myself.port
						+ " VIEW " + pbftMessage.getView() + " client: " + pbftMessage.getClientMsg().getClientID());
						
			break;
		}
		//prepare step
		case Constants.prepareCommandType:{
			
			boolean flag = false;
			PublicKey pub=null;
			
			PbftMessage message = (PbftMessage)m;
			
			//generating public and private keys from the client content		
			try {
				ClientMessage clientMessage = message.getClientMsg();
				SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
				random.setSeed(clientMessage.getContent().length);   //related with client message
				KeyPairGenerator keyGen=KeyPairGenerator.getInstance("RSA");
				keyGen.initialize(1024, random);
				KeyPair pair=keyGen.generateKeyPair();
				pub=pair.getPublic();	
												
				if(pub.equals(message.getPubKey()) && 
						pub.equals(prePreparePubKey.get(message.getView()))) {
					flag=true;
					message.setPubKey(pub);
				}
				
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}				
			
			if(flag) {
									
				synchronized (preparePubKey) {
					PublicKey pk = preparePubKey.get(message.getView());
					if(pk==null) 
						preparePubKey.put(message.getView(), message.getPubKey());						
				}			
				
				synchronized (prepareContext) {
					if(prepareContext.get(message.getView()) == null) {
						Set<String> current = new TreeSet<String>();
						current.add(message.getNodeID().ip+message.getNodeID().port);
						prepareContext.put(message.getView(), current);
					}else prepareContext.get(message.getView()).add(message.getNodeID().ip+message.getNodeID().port);				
											
				}
				
				synchronized (prepareContextSubmit) {
					flag = false;
					
					if(prepareContextSubmit.get(message.getView())==null &&
							prepareContext.get(message.getView()).size() >= (2*Constants.maxFaultyNodes)+1) {
						prepareContextSubmit.put(message.getView(), true);
						flag=true;
					}
				}				
				
				if(flag) {
					System.out.println("PBFT step:" + message.getBftStep() + " - Node: " + myself.ip+myself.port 
							+ " - Client: " + message.getClientMsg().getClientID() + " VIEW: "+ message.getView());
					
					message.setType(Constants.commitCommandType);
					message.setBftStep("COMMIT");
					BroadcastPrepare.broadcast(message, myself);
					
				}else if(prepareContextSubmit.get(message.getView())==null){
					//needs timeout to minimize the impact of faulty nodes not responding
					int count =0;
					while(prepareContext.get(message.getView()).size() < (2*Constants.maxFaultyNodes)+1
							&& count<=Constants.maxWaits) {
						try {
							Thread.sleep(Constants.delayBetweenClientMessages);
						} catch (InterruptedException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						count++;
					}						
					
					synchronized (prepareContextSubmit) {
						flag = false;
						
						if(prepareContextSubmit.get(message.getView())==null &&
								prepareContext.get(message.getView()).size() >= (2*Constants.maxFaultyNodes)+1) {
							prepareContextSubmit.put(message.getView(), true);
							flag=true;
						}
					}	
					
					if(flag) {
						System.out.println("After waiting.... PBFT step:" + message.getBftStep() + " - Node: " + myself.ip+myself.port 
								+ " - Client: " + message.getClientMsg().getClientID() + " VIEW: "+ message.getView());
						
						message.setType(Constants.commitCommandType);
						message.setBftStep("COMMIT");
						BroadcastPrepare.broadcast(message, myself);
						
					}else if(prepareContext.get(message.getView()).size() < (2*Constants.maxFaultyNodes)+1){
						System.err.println("Node: " + myself.ip+myself.port +
								" does not broadcast COMMIT msgs to the quorum because a consensus was not reached. Msgs received:"
								+ prepareContext.get(message.getView()).size() + " VIEW " + message.getView());
												
					}
					
				}				
				
			}else 
				System.err.println("Public keys different at prepare step. Node: " + myself.ip+myself.port
						+ " Previous Node: "+ message.getNodeID().ip+message.getNodeID().port 
						+ " VIEW " + message.getView() + " client: " + message.getClientMsg().getClientID());
						
			break;
		}
		//commit step
		case Constants.commitCommandType:{
			
			PbftMessage message = (PbftMessage) m;
			
			PublicKey pub=null;
			boolean flag = false;
			
			//generating public and private keys from the client content		
			try {
				ClientMessage clientMessage = message.getClientMsg();
				SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
				random.setSeed(clientMessage.getContent().length);   //related with client message
				KeyPairGenerator keyGen=KeyPairGenerator.getInstance("RSA");
				keyGen.initialize(1024, random);
				KeyPair pair=keyGen.generateKeyPair();
				pub=pair.getPublic();				
				
				if(pub.equals(message.getPubKey()) && 
						pub.equals(prePreparePubKey.get(message.getView())) &&
						pub.equals(preparePubKey.get(message.getView()))) {
					flag=true;
					message.setPubKey(pub);
				}
				
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if(flag) {
				
				synchronized (commitContext) {
					
					if(commitContext.get(message.getView()) == null) {
						Set<String> current = new TreeSet<String>();
						commitContext.put(message.getView(), current);
					}else commitContext.get(message.getView()).add(message.getNodeID().ip+message.getNodeID().port);
											
				}
				
				synchronized (commitContextSubmit) {
					flag = false;
					
					if(commitContextSubmit.get(message.getView())==null &&
							commitContext.get(message.getView()).size() >= (2*Constants.maxFaultyNodes)+1) {
						commitContextSubmit.put(message.getView(), true);
						flag=true;
					}
				}						
				
				if(flag) {
					System.out.println("PBFT step:" + message.getBftStep() + " - Node: " + myself.ip+myself.port 
							+ " - Client: " + message.getClientMsg().getClientID() + " VIEW: "+ message.getView());
					
					BroadcastCommit.broadcast(message, myself, false);
					
				}else if(commitContextSubmit.get(message.getView())==null) {
					
					//needs timeout to minimize the impact of faulty nodes not responding
					int count =0;
					while(commitContext.get(message.getView()).size() < (2*Constants.maxFaultyNodes)+1
							&& count<=Constants.maxWaits) {
						try {
							Thread.sleep(Constants.delayBetweenClientMessages);
						} catch (InterruptedException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						count++;
					}
					
					synchronized (commitContextSubmit) {
						flag = false;
						
						if(commitContextSubmit.get(message.getView())==null &&
								commitContext.get(message.getView()).size() >= (2*Constants.maxFaultyNodes)+1) {
							commitContextSubmit.put(message.getView(), true);
							flag=true;
						}
					}
					
					if(flag) {
						System.out.println("After waiting.... PBFT step:" + message.getBftStep() + " - Node: " + myself.ip+myself.port 
								+ " - Client: " + message.getClientMsg().getClientID() + " VIEW: "+ message.getView());
						
						BroadcastCommit.broadcast(message, myself, false);
						
					}else if(commitContext.get(message.getView()).size() < (2*Constants.maxFaultyNodes)+1) {						
						System.err.println("Node: " + myself.ip+myself.port +
								" does not notify the client " + message.getClientMsg().getClientID() + 
								"because a consensus was not reached. Msgs received:"
								+ commitContext.get(message.getView()).size() + " VIEW " + message.getView());
						
						//propagating a wrong msg to force a client notification about such situation
						if(!myself.faulty) {
							BroadcastCommit.faultyBroadcast(message, myself, false);
						}
					}
				}				
				
			}else 
				System.err.println("Public keys different at commit step. Node: " + myself.ip+myself.port
						+ " Previous Node: "+ message.getNodeID().ip+message.getNodeID().port 
						+ " VIEW " + message.getView() + " client: " + message.getClientMsg().getClientID());
		
			
			break;
		}
		//the final step - the client evaluation
		case Constants.clientResponsesCommandType:{
			
			ResponseForClientMessage message = (ResponseForClientMessage) m;
			
			List<ResponseForClientMessage> response = null;
			boolean flag = false;
			int maxEqualVotes =0;
			
			synchronized (clientContext) {
						
				response = clientContext.get(message.getView());
				
				if(response == null) {
					response = new LinkedList<ResponseForClientMessage>();				
					clientContext.put(message.getView(), response);			
				}
				response.add(message);		
				
				if(response.size()>=Constants.minCorrectNodes) 
					//test the number of equal pub keys
					maxEqualVotes = getNumEqualPubKeys(response);
			}						
				
			synchronized (clientContextSubmit) {
				flag = false;
				if(clientContextSubmit.get(message.getView())==null &&
						maxEqualVotes >= Constants.maxFaultyNodes+1) {
					clientContextSubmit.put(message.getView(), new AtomicInteger(1));
					flag=true;
				}
			}			
			
			if(flag) {
				System.out.println("Client " + message.getClientID() + " reached a consensus --- "+response.size() + " votes " +  message.getVote() + " received in the view : " 
						+ message.getView() + ". The last vote received from node: " + message.getHostID().ip+message.getHostID().port
						+ " --- Elapsed time: " + (System.currentTimeMillis() - message.getTime()));
				
			}else {
				//needs timeout to minimize the impact of faulty nodes not responding
				int count =0;
				while(response.size() < Constants.maxFaultyNodes+1
						&& count<=Constants.maxWaits) {
					try {
						Thread.sleep(2*Constants.delayBetweenClientMessages);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					count++;
				}
							
				synchronized (clientContext) {				
					maxEqualVotes =0;
					flag=false;
					if(response.size()>=Constants.minCorrectNodes) 
						//test the number of equal pub keys
						maxEqualVotes = getNumEqualPubKeys(response);
				}		
				synchronized (clientContextSubmit) {
					flag = false;
					if(clientContextSubmit.get(message.getView())==null &&
							maxEqualVotes >= Constants.maxFaultyNodes+1) {
						clientContextSubmit.put(message.getView(), new AtomicInteger(1));
						flag=true;
					}
				}
						
				if(flag) {
					System.out.println("Client " + message.getClientID() + " reached a consensus --- "+response.size() + " votes " +  message.getVote() + " received in the view : " 
							+ message.getView() + ". The last vote received from node: " + message.getHostID().ip+message.getHostID().port
							+ " --- Elapsed time: " + (System.currentTimeMillis() - message.getTime()));
					flag=false;
					
				}else if(maxEqualVotes < Constants.maxFaultyNodes+1) {
						flag=true;
						System.err.println("Client " + message.getClientID() + " received " +
						maxEqualVotes + " equal votes and it will retry....");						
				}					
					
				if(flag) {
					
					synchronized (clientContextSubmit) {
						AtomicInteger at = clientContextSubmit.get(message.getView());
						if(at==null)
							clientContextSubmit.put(message.getView(), new AtomicInteger(1));
						else at.incrementAndGet();
					}		
							
					if(clientContextSubmit.get(message.getView()).get()<Constants.maxNumAttempts) {
						System.err.println("Client " + message.getClientID() + " aborted. View: " + message.getView());
						System.err.println("Client " + message.getClientID() + " can try again....");
						System.err.println("current attempt: " + clientContextSubmit.get(message.getView()).get() + " --- trying again....");
						try {
							byte[] messageContent = Files.readAllBytes(Paths.get(Constants.messageContentFile));
									
							
							Clients.send(Constants.primaryAddress, Constants.primaryPort, 
									Constants.primaryCommandType, messageContent, 
									message.getClientID(), message.getTime());
							
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}	
					}else System.err.println("Client " + message.getClientID() + " ABORTED AND CANNOT try again because number of attempts exceeds.... " + message.getView());
				}
										
			}
			
			break;
		}
		default:{
			System.out.println("Message with type " + m.getType() + " is not mapped into this testbed (pbft traditional). You need to map it before you use it !!!");
			break;
		}
		}
	}
	
	
	private int getNumEqualPubKeys(List<ResponseForClientMessage> response) {
		int maxEqualVotes=0;
		//test the number of equal pub keys
		Map<Integer, Integer> votes = new TreeMap<Integer, Integer>();
		boolean found=false;
		for(ResponseForClientMessage vote:response) {
			for(Integer pk:votes.keySet())
				if(pk.equals(vote.getPubKey())) {
					int numVotes = votes.get(vote.getPubKey());
					numVotes++;
					votes.put(vote.getPubKey(), numVotes);
					found=true;
					break;
				}
			
			if(!found) votes.put(vote.getPubKey(), 1);
		}				
		
		for(Entry<Integer, Integer> entry: votes.entrySet())
			if(entry.getValue()>maxEqualVotes)
				maxEqualVotes = entry.getValue();
		
		votes.clear();
		votes=null;
		
		return maxEqualVotes;
	}
	
	
}
