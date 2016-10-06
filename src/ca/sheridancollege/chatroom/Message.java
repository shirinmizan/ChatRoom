package ca.sheridancollege.chatroom;

import java.io.Serializable;

public class Message implements Serializable {
	 
	
	private static final long serialVersionUID = 1L;
		static final int SHOWUSER = 0, CHATMESSAGE = 1, QUIT = 2;
		private int type;
		private String message;
	 
		// constructor
		Message(int type, String message){
				this.type = type;
				this.message = message;
		}
	 
	 	// getters
		 int getType() {
			 return type;
		 }
		 
		 String getMessage() {
		  return message;
		 }
}
