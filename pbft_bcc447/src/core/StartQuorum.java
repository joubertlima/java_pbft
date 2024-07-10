package core;

import util.Constants;

public class StartQuorum {
	
	public StartQuorum(int type) {
		for(int i=0; i<Constants.quorumPorts.length; i++) {
			new StartOneNode(Constants.quorumPorts[i], type).start();
		}
	}
	
	public static void main(String[] args) {		
			
		new StartQuorum(1);
	}
	
	private class StartOneNode extends Thread{
		int port;
		int type;
		public StartOneNode(int port, int type) {
			this.port = port;
			this.type = type;
		}
		
		public void run() {
			if(type==1)
				new Node(port);
			else System.err.println("YOU MUST USE TYPE: 1 for traditional BFT ");
		}
	}

}
