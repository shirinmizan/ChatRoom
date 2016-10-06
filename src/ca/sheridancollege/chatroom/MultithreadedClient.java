package ca.sheridancollege.chatroom;

import java.net.*;
import java.io.*;
import java.util.*;

public class MultithreadedClient {
	// for input output
	private ObjectInputStream sInput;       // to read from the socket
	private ObjectOutputStream sOutput;     // to write on the socket
	private Socket socket;
	
	// GUI
	private ClientGUI cg;
	
	// port, server, username
	private String server;
	private String username;
	private int port;
	
	/*
	*  Constructor called by console mode
	*  server: the server address
	*  port: the port number
	*  username: the username
	*/
	
	MultithreadedClient(String server, int port, String username) {
	
		// which calls the common constructor with the GUI set to null
		this(server, port, username, null);
	
	}
	/*
	* Constructor call when used from a GUI
	* in console mode the ClienGUI parameter is null
	*/
	MultithreadedClient(String server, int port, String username, ClientGUI cg) {
		this.server = server;
		this.port = port;
		this.username = username;
		// GUI
		this.cg = cg;
	}
	
	//to start dialog
	public boolean start() {
		
		//connect to server
		try {
			socket = new Socket(server, port);
		}
		//failed show message
		catch(Exception ec) {
			display("Error connectiong to server:" + ec);
			return false;
		}
	
		String msg = "Connection accepted " + socket.getInetAddress() + ":" + socket.getPort();
		display(msg);
	
		//Creating Data Stream 
		try
			{
				sInput  = new ObjectInputStream(socket.getInputStream());
				sOutput = new ObjectOutputStream(socket.getOutputStream());
			}
			catch (IOException eIO) {
				display("Exception creating new Input/output Streams: " + eIO);
				return false;
			}
	
		 //creating Thread to listening from server
		new ListenFromServer().start();
		
		// Send our username to the server this is the only message that we
		// will send as a String. All other messages will be Message objects
		try
			{
				sOutput.writeObject(username);
			}
			catch (IOException eIO) {
				display("Exception doing login : " + eIO);
			    disconnect();
				return false;
			}
			// success we inform the caller that it worked
				return true;
	}
	 
	
	//Send message to console or GUI
	private void display(String msg) {
		
		if(cg == null){
			System.out.println(msg);      //print in console mode
		}
		else{
			cg.append(msg + "\n");      //Show in GUI
		}
	}
	
	//Send message to server
	void sendMessage(Message msg) {
		
		try {
			sOutput.writeObject(msg);
		}
		catch(IOException e) {
			display("Exception writing to server: " + e);
		}
	}

	// if something goes wrong close the I/O stream and disconnect
	private void disconnect() {
		
		try {
			
			if(sInput != null) sInput.close();
		}
		catch(Exception e) {} 
		
		try {

			if(sOutput != null) sOutput.close();
		}
		catch(Exception e) {} 
	
		try{
	
			if(socket != null) socket.close();
	
		}
		catch(Exception e) {} 

		// let GUI know
		if(cg != null){
			cg.connectionFailed();
		}
	}
	/*
	* To start the Client in console mode use one of the following command
	* > java Client
	* > java Client username
	* * > java Client username portNumber
	* * > java Client username portNumber serverAddress
	* at the console prompt
	* If the portNumber is not specified 6000 is used
	* * If the serverAddress is not specified "localHost" is used
	* If the username is not specified "Anonymous" is used
	* > java Client
	* is equivalent to
	* > java Client Anonymous 6000 localhost
	* are eqquivalent
	*
	*In console mode, if an error occurs the program simply stops
	* when a GUI id used, the GUI is informed of the disconnection
	*/
	public static void main(String[] args) {
		// values for the server, port, and user
		int portNumber = 6000;
		String serverAddress = "localhost";
		String userName = "Anonymous";
	
		// depending of the number of arguments provided we fall through
		switch(args.length) {
		
			// > javac Client username portNumber serverAddr
			case 3:
				serverAddress = args[2];
			// > javac Client username portNumber
			case 2:
				try {
					
					portNumber = Integer.parseInt(args[1]);
				}
				catch(Exception e) {
					System.out.println("Invalid port number.");
					System.out.println("Usage is: > java Client [username] [portNumber] [serverAddress]");
				return;
				}
			// > javac Client username
			case 1:
				userName = args[0];
			// > java Client
			case 0:
				break;
			// invalid number of arguments
			default:
				System.out.println("Usage is: > java Client [username] [portNumber] {serverAddress]");
				return;
		}
		// create the Client object
		MultithreadedClient client = new MultithreadedClient(serverAddress, portNumber, userName);
		
		// test if we can start the connection to the Server
		// if it failed nothing we can do
		if(!client.start()){
			return;
		}
	
		// wait for messages from user
		Scanner scan = new Scanner(System.in);
		// loop forever for message from the user
		while(true){
			
			System.out.print("> ");
			// read message from user
			String msg = scan.nextLine();
			
			// Quit if message is QUIT
			if(msg.equalsIgnoreCase("QUIT")) {
				
				client.sendMessage(new Message(Message.QUIT, ""));
				// break to do the disconnect
				break;
			}
			//show the list of user who is connected
			else if(msg.equalsIgnoreCase("SHOWUSER")) {
				
				client.sendMessage(new Message(Message.SHOWUSER, ""));              
			}
			//show chat messages
			else { 
				
				client.sendMessage(new Message(Message.CHATMESSAGE, msg));
			}
		}
		
		//disconnect if finished
		client.disconnect();   
	}
	
	/*
	* a class that waits for the message from the server and append them to the JTextArea
	* if we have a GUI or simply System.out.println() it in console mode
	*/
	class ListenFromServer extends Thread {
		public void run(){
			while(true){
				try {
					String msg = (String) sInput.readObject();
					// if console mode print the message and add back the prompt
					if(cg == null){
						System.out.println(msg);
						System.out.print("> ");
					}
					else {
						cg.append(msg);
					}
				}
				catch(IOException e) {
					display("Server has close the connection: " + e);
					if(cg != null)
						cg.connectionFailed();
					break;
				}
				// can't happen with a String object but need the catch anyhow
				catch(ClassNotFoundException e2) {}
			}
		}
	}
}