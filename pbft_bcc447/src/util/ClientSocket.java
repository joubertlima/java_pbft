package util;

import java.net.Socket;

public class ClientSocket {
	
	public static Socket getSocket(String ip, int port) {
		try {
			Socket s = new Socket(ip, port);
			s.setKeepAlive(false);
			s.setTcpNoDelay(false);
						
			return s;
		}catch (Exception e) {
			return getSocket(ip, port);
		}
	}
	

}
