package message;

public class ClientMessage extends MessageImpl{
	
	private static final long serialVersionUID = -5538753691791418822L;
	private byte[] content;
	private int clientPort;
	private String clientAddress;
	private int clientID;
	private String existingView;
	private long time;

	public int getClientID() {
		return clientID;
	}

	public void setClientID(int clientID) {
		this.clientID = clientID;
	}

	public int getClientPort() {
		return clientPort;
	}

	public void setClientPort(int clientPort) {
		this.clientPort = clientPort;
	}

	public String getClientAddress() {
		return clientAddress;
	}

	public void setClientAddress(String clientAddress) {
		this.clientAddress = clientAddress;
	}

	public byte[] getContent() {
		return content;
	}

	public void setContent(byte[] encoded) {
		this.content = encoded;
	}

	public String getExistingView() {
		return existingView;
	}

	public void setExistingView(String existingView) {
		this.existingView = existingView;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}	

}
