package util;

import java.io.Serializable;

public class NodeInfo implements Comparable<NodeInfo>, Serializable{
	private static final long serialVersionUID = 7940463756852194728L;
	public String ip;
	public int port;
	public boolean faulty;

	@Override
	public int compareTo(NodeInfo o) {
		if((this.ip+this.port).hashCode()>(o.ip+o.port).hashCode()) return 1;
		else if ((this.ip+this.port).hashCode()<(o.ip+o.port).hashCode()) return -1;
		// otherwise
		return 0;
	}
	
	@Override
	public boolean equals(Object obj) {
		NodeInfo aux = (NodeInfo)obj;
		
        return (this.ip+this.port).equals(aux.ip+aux.port);
    }
		
}
