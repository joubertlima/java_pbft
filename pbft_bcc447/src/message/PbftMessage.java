package message;

import java.security.PublicKey;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import util.NodeInfo;

public class PbftMessage extends MessageImpl implements Comparable<PbftMessage>{

	private static final long serialVersionUID = 7878010073671492379L;
	private ClientMessage clientMsg;
	private List<NodeInfo> nodes;
	private Set<NodeInfo> faultyNodes;
	private String view;
	private NodeInfo id;
	private String bftStep;
	private PublicKey pubKey;
		
	public PublicKey getPubKey() {
		return pubKey;
	}
	
	public void setPubKey(PublicKey pubKey) {
		this.pubKey = pubKey;
	}
	
	public String getView() {
		return view;
	}
	
	public void setView(String view) {
		this.view = view;
	}
		
	public boolean addNode(String host, int port) {
		NodeInfo n = new NodeInfo();
		n.ip = host;
		n.port = port;
		if(nodes == null) nodes = new LinkedList<NodeInfo>();
		return nodes.add(n);
	}
		
	public List<NodeInfo> getNodes(){
		return nodes;
	}
	
	public Set<NodeInfo> getFaultyNodes() {
		return faultyNodes;
	}
	
	public void setFaultyNodes(Set<NodeInfo> faultyNodes) {
		this.faultyNodes = faultyNodes;
	}
		
	public ClientMessage getClientMsg() {
		return clientMsg;
	}
	public void setClientMsg(ClientMessage clientMsg) {
		this.clientMsg = clientMsg;
	}

	public NodeInfo getNodeID() {
		return id;
	}
	public void setNodeID(String host, int port) {
		id = new NodeInfo();
		id.port = port;
		id.ip = host;
	}

	public String getBftStep() {
		return bftStep;
	}
	
	public void setBftStep(String bftStep) {
		this.bftStep = bftStep;
	}
	
	@Override
	public int compareTo(PbftMessage o) {
		if((this.getNodeID().ip+this.getNodeID().port+this.getView()+this.getType()).hashCode()>
		(o.getNodeID().ip+o.getNodeID().port+o.getView()+o.getType()).hashCode()) return 1;
		else if((this.getNodeID().ip+this.getNodeID().port+this.getView()+this.getType()).hashCode()<
		(o.getNodeID().ip+o.getNodeID().port+o.getView()+o.getType()).hashCode()) return -1;
		// otherwise
		return 0;
	}
	
		
}
