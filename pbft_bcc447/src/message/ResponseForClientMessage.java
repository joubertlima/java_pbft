package message;

import java.util.List;

import util.NodeInfo;

public class ResponseForClientMessage extends MessageImpl{
	
	private static final long serialVersionUID = 4344222423568301701L;
	private String vote;
	private NodeInfo hostID;
	private List<NodeInfo> nodes;
	private String view;
	private int clientID;
	private long time;
	private int numAttempts;
	private int pubKey;
	
	public NodeInfo getHostID() {
		return hostID;
	}
	public void setHostID(NodeInfo hostID) {
		this.hostID = hostID;
	}
	public String getVote() {
		return vote;
	}
	public void setVote(String vote) {
		this.vote = vote;
	}
	public List<NodeInfo> getNodes() {
		return nodes;
	}
	public void setNodes(List<NodeInfo> nodes) {
		this.nodes = nodes;
	}
	public String getView() {
		return view;
	}
	public void setView(String view) {
		this.view = view;
	}
	public int getClientID() {
		return clientID;
	}
	public void setClientID(int clientID) {
		this.clientID = clientID;
	}
	public long getTime() {
		return time;
	}
	public void setTime(long time) {
		this.time = time;
	}
	public int getNumAttempts() {
		return numAttempts;
	}
	public void setNumAttempts(int numAttempts) {
		this.numAttempts = numAttempts;
	}
	
	public int getPubKey() {
		return pubKey;
	}
	public void setPubKey(int pubKey) {
		this.pubKey = pubKey;
	}
	

}
