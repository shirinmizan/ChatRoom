package ca.sheridancollege.sync;

import java.io.*;
import java.net.*;

public class MultiThreadChatClient implements Runnable {

  private static BufferedReader inputLine = null;
  private static boolean closed = false;
  private static Socket socket = null;
  private static PrintStream out = null;
  private static DataInputStream in = null;
  
  static int PORT = 6000;
  static String host = "localhost";
  
  public static void main(String[] args) {

   if (args.length < 2) {
      System.out.println("Client connected at port: " + PORT);
    } else {
      host = args[0];
      PORT = Integer.valueOf(args[1]).intValue();
    }

    //Open a socket with host and post name. Open input and output streams.
    try {
      socket = new Socket(host, PORT);
      inputLine = new BufferedReader(new InputStreamReader(System.in));
      out = new PrintStream(socket.getOutputStream());
      in = new DataInputStream(socket.getInputStream());
    } catch (UnknownHostException e) {
      System.err.println("Host unknown " + host);
    } catch (IOException e) {
      System.err.println("No connection to the host"
          + host);
    }

    //write data 
    if (socket != null && out != null && in != null) {
      try {

        // Create a thread to read from the server. 
        new Thread(new MultiThreadChatClient()).start();
        while (!closed) {
          out.println(inputLine.readLine().trim());
        }
        
        //Close the output stream, close the input stream, close the socket.
        out.close();
        in.close();
        socket.close();
      } catch (IOException e) {
        System.err.println("IOException:  " + e);
      }
    }
  }

  public void run() {
    
     // Keep on reading from the socket till we receive "Bye" from the
     //server. Once received break
     
    String responseLine;
    try {
      while ((responseLine = in.readLine()) != null) {
        System.out.println(responseLine);
        if (responseLine.indexOf("~~~ Bye") != -1)
          break;
      }
      closed = true;
    } catch (IOException e) {
      System.err.println("IOException:  " + e);
    }
  }
}