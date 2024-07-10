package message;

public class MessageImpl implements Message{

	private static final long serialVersionUID = -1298874117877687170L;
	private String type;
	
	public MessageImpl(){
		
	}
	
	@Override
	public String getType() {
		// TODO Auto-generated method stub
		return type;
	}

	@Override
	public void setType(String type) {
		this.type = type;		
	}	

}
