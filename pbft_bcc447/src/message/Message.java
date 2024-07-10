/**
 * 
 */
package message;

import java.io.Serializable;

/**
 * @author Joubert
 * @version 1.0
 * 
 */
public interface Message extends Serializable{
	
	public abstract String getType();
	public abstract void setType(String type);
	
}
