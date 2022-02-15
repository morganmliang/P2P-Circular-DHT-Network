

import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class TCPClient implements Runnable  {
	
	
	private final int PORT = 50000;
	private int successorPort1;
	private Socket clientSocket;

	private DataOutputStream out;
	private BufferedReader inFromUser;
	Peer peer;


	public TCPClient (Peer p) {
	  this.peer = p;
  
	}
	
	
	@Override
	//set up tcp connection with successor1
	public void run()  {
		successorPort1 = PORT + peer.getSuccessor1();
		// TODO Auto-generated method stub
		try {
			
			boolean connectionSuccess = false;
			while(!connectionSuccess) {
				try {
					clientSocket = new Socket(InetAddress.getByName("localhost"), successorPort1);
					inFromUser = new BufferedReader(new InputStreamReader(System.in)); 
					
					out = new DataOutputStream(clientSocket.getOutputStream());
					peer.setOutToSuccessor(out);

					connectionSuccess = true;
				}catch (IOException e) {
				}	
				
			}
			

			while(true) {
				
				//read from user input
				String message1 = inFromUser.readLine();
				if(message1 != null) {
					String[] message1Array = message1.split(" ");
					String status = message1Array[0];
					
					
					//peer requests file
					// message Format 
					// [0] - status + 
					// [1] - fileName 
					// [2] - file requesting ID;
					if(status.equals("request")) {
		
						//send to successor
						System.out.println("File request message for " + message1Array[1] 
								+ " has been sent to my successor");
						
						peer.getOutToSuccessor().writeBytes( message1 + " " + peer.getId() + '\n');
					
					//peer quits network
					}else if (status.equals("quit")) {
						
						
						int predecessorPort1 = PORT + peer.getPredecessor1();
						int predecessorPort2 = PORT + peer.getPredecessor2();
						
						//send TCP message to predecessors
						Socket predecessor1Socket = new Socket(InetAddress.getByName("localhost"), predecessorPort1);
						Socket predecessor2Socket = new Socket(InetAddress.getByName("localhost"), predecessorPort2);
	
						DataOutputStream outToPre1 = new DataOutputStream(predecessor1Socket.getOutputStream());
						DataOutputStream outToPre2 = new DataOutputStream(predecessor2Socket.getOutputStream());
	
						int suc1 = peer.getSuccessor1();
						int suc2 = peer.getSuccessor2();
						
						String sendToPre1 = "depart " + peer.getId() + " " + suc2 + " ";
						String sendToPre2 = "depart " + peer.getId() + " " + suc1 + " ";
	
						outToPre1.writeBytes (sendToPre1 + '\n');
						outToPre2.writeBytes (sendToPre2 + '\n');
	
						
						//read from predecessors connection
						BufferedReader inFromPre1 =
								new BufferedReader(new
								InputStreamReader(predecessor1Socket.getInputStream()));
						BufferedReader inFromPre2 =
								new BufferedReader(new
								InputStreamReader(predecessor2Socket.getInputStream()));
	
						String dupAck1 = inFromPre1.readLine();
						String dupAck2 = inFromPre2.readLine();
	
						System.out.println(dupAck1);
						System.out.println(dupAck2);
						
						System.out.println("Peer is now quitting the network");
	
						
						//close all tcp connections to predecessors 
						predecessor1Socket.close();
						predecessor2Socket.close();
						
						peer.setDeparted(true);
						break;
						
						
					}else {
						System.out.println("User input is an invalid request");
					}
				}	
			}
			
//			inFromUser.close();
//			clientSocket.close();
			
			
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

}
