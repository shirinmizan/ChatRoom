package ca.sheridancollege.sync;

import java.io.*;
import java.net.*;


public class MultiThreadChatServerSync {
	private static ServerSocket serverSocket = null;
	private static Socket socket = null;
	private static final int maxClientsCount = 10;
	private static final clientThread[] threads = new clientThread[maxClientsCount];
	static int PORT = 6000;

	  public static void main(String args[]) {
		  
		  if (args.length < 1) {
	      System.out.println("Server connected at port: " + PORT +" \nWaiting for client...");
	    } else {
	      PORT = Integer.valueOf(args[0]).intValue();
	    }

	    /*
	     * Open a server socket on the portNumber (default 2222). Note that we can
	     * not choose a port less than 1023 if we are not privileged users (root).
	     */
	    try {
	      serverSocket = new ServerSocket(PORT);
	    } catch (IOException e) {
	      System.out.println(e);
	    }

	    /*
	     * Create a client socket for each connection and pass it to a new client
	     * thread.
	     */
	    while (true) {
	      try {
	        socket = serverSocket.accept();
	        int i = 0;
	        for (i = 0; i < maxClientsCount; i++) {
	          if (threads[i] == null) {
	            (threads[i] = new clientThread(socket, threads)).start();
	            break;
	          }
	        }
	        if (i == maxClientsCount) {
	          PrintStream os = new PrintStream(socket.getOutputStream());
	          os.println("Server too busy. Try later.");
	          os.close();
	          socket.close();
	        }
	      } catch (IOException e) {
	        System.out.println(e);
	      }
	    }
	  }
	}

	/*
	 * The chat client thread. This client thread opens the input and the output
	 * streams for a particular client, ask the client's name, informs all the
	 * clients connected to the server about the fact that a new client has joined
	 * the chat room, and as long as it receive data, echos that data back to all
	 * other clients. The thread broadcast the incoming messages to all clients and
	 * routes the private message to the particular client. When a client leaves the
	 * chat room this thread informs also all the clients about that and terminates.
	 */
	class clientThread extends Thread {

	  private String clientName = null;
	  private DataInputStream in = null;
	  private PrintStream out = null;
	  private Socket socket = null;
	  private final clientThread[] threads;
	  private int maxClientsCount;

	  public clientThread(Socket clientSocket, clientThread[] threads) {
	    this.socket = clientSocket;
	    this.threads = threads;
	    maxClientsCount = threads.length;
	  }

	  public void run() {
	    int maxClientsCount = this.maxClientsCount;
	    clientThread[] threads = this.threads;

	    try {
	      /*
	       * Create input and output streams for this client.
	       */
	      in = new DataInputStream(socket.getInputStream());
	      out = new PrintStream(socket.getOutputStream());
	      String name;
	      while (true) {
	        out.println("Please Enter a username: ");
	        name = in.readLine().trim();
	        if (name.indexOf('@') == -1) {
	          break;
	        } else {
	          out.println("The name should not contain '@' character.");
	        }
	      }

	      /* Welcome the new the client. */
	      out.println("Welcome " + name
	          + " hope you enjoy your chat today.\n**To leave type 'quit' \n**To start a private chat type '@' before the user name");
	      synchronized (this) {
	        for (int i = 0; i < maxClientsCount; i++) {
	          if (threads[i] != null && threads[i] == this) {
	            clientName = "@" + name;
	            break;
	          }
	        }
	        for (int i = 0; i < maxClientsCount; i++) {
	          if (threads[i] != null && threads[i] != this) {
	            threads[i].out.println("~~ A new user " + name
	                + " entered the chat room !!! ~~");
	          }
	        }
	      }
	      /* Start the conversation. */
	      while (true) {
	        String line = in.readLine();
	        if (line.equalsIgnoreCase("QUIT")) {
	          break;
	        }
	        //for private chat
	        if (line.startsWith("@")) {
	          String[] words = line.split("\\s", 2);
	          if (words.length > 1 && words[1] != null) {
	            words[1] = words[1].trim();
	            if (!words[1].isEmpty()) {
	              synchronized (this) {
	                for (int i = 0; i < maxClientsCount; i++) {
	                  if (threads[i] != null && threads[i] != this
	                      && threads[i].clientName != null
	                      && threads[i].clientName.equals(words[0])) {
	                    threads[i].out.println("::" + name + ":: " + words[1]);
	                    
	                   //will show in the private client
	                    this.out.println(":" + name + ": " + words[1]);
	                    break;
	                  }
	                }
	              }
	            }
	          }
	        } else {
	          //public chat messages
	          synchronized (this) {
	            for (int i = 0; i < maxClientsCount; i++) {
	              if (threads[i] != null && threads[i].clientName != null) {
	                threads[i].out.println("<<" + name + ">> " + line);
	              }
	            }
	          }
	        }
	      }
	      //user disconnecting 
	      synchronized (this) {
	        for (int i = 0; i < maxClientsCount; i++) {
	          if (threads[i] != null && threads[i] != this
	              && threads[i].clientName != null) {
	            threads[i].out.println("~~ " + name
	                + " is leaving the chat room !!! ~~");
	          }
	        }
	      }
	      
	      out.println("Closing Connection... Goodbye " +name);

	      
	       //Setting the current thread variable to null so that a new client
	       //could be accepted by the server.
	       synchronized (this) {
	        for (int i = 0; i < maxClientsCount; i++) {
	          if (threads[i] == this) {
	            threads[i] = null;
	          }
	        }
	      }
	      in.close();
	      out.close();
	      socket.close();
	    } catch (IOException e) {
	    }
	  }
	}


