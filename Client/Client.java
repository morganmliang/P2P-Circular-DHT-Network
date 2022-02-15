

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.net.*;


public class Client implements Runnable {
	
	private final int PORT = 50000;
	private Peer peer;
	private int successor1Port;
	private int successor2Port;
	private int idPort;
	
	
	private int pingSuc1Count = 0;
	private int pingSuc2Count = 0;

	private InetAddress serverIPAddress;
	
	private DatagramSocket socket;
	
	
	public Client(Peer p){
		peer = p;
	}

	@Override
	public void run() {

		try {
			idPort = PORT + peer.getId();

			serverIPAddress = InetAddress.getByName("localhost");
			socket = new DatagramSocket();
			
			while(true) {
				try {
					
					pingSuccessors(peer);
					
					Thread.sleep(30000);
					
					pingSuc1Count++;
					pingSuc2Count++;

					//wait for
					String[] message1Array = peer.getPingQueue().poll(5, TimeUnit.SECONDS);
					
					if(message1Array == null) {
						//System.out.println("message was not received from blocking queue");
					}else {
						//System.out.println("getting response from blocking queue from peer" + message1Array[1]);
						
						if(Integer.parseInt(message1Array[1]) == peer.getSuccessor1()) {
							pingSuc1Count = 0;
						}else if (Integer.parseInt(message1Array[1]) == peer.getSuccessor2()) {
							pingSuc2Count = 0;
						}else {
							//System.out.println("This message is not from a successor");
						}
					}
					
					String[] message2Array = peer.getPingQueue().poll(0, TimeUnit.SECONDS); 
					if(message2Array == null) {
						//System.out.println("message was not received from blocking queue");
					}else {
						//System.out.println("getting response from blocking queue from peer" + message2Array[1]);
						
						if(Integer.parseInt(message2Array[1]) == peer.getSuccessor1()) {
							pingSuc1Count = 0;
						}else if (Integer.parseInt(message2Array[1]) == peer.getSuccessor2()) {
							pingSuc2Count = 0;
						}else {
							System.out.println("This message is not from a successor");
						}
					}
					
					//successor1 is killed
					if(pingSuc1Count >= 4) {
						
						
						
						
						//check with successor2 for new successor2
						int peerKilled = peer.getSuccessor1();
						//peer.setSuccessor2(0);
						//System.out.println("The peer successor1 is now peer " + peer.getSuccessor1());
						//System.out.println("successor 1 ping duplicate count is at 4 ");
						
						//send message to successor 2 using TCP
						Socket toSuc2Socket = new Socket(InetAddress.getByName("localhost"), PORT + peer.getSuccessor2());
						
						KillPeer kPeer = new KillPeer(toSuc2Socket, peerKilled, peer);
						Thread t1 = new Thread(kPeer);
						t1.start();
						t1.join();
						
						//ping new successors
						pingnewSuccessors(peer);
						
						//reset ping count to 0
						pingSuc1Count = 0;
						
					}
					
					//successor2 is killed
					if(pingSuc2Count >= 4) {
						int peerKilled = peer.getSuccessor2();
						
						//connect to successor1
						Socket toSuc1socket = new Socket(InetAddress.getByName("localhost"), PORT + peer.getSuccessor1());
						KillPeer kPeer = new KillPeer(toSuc1socket, peerKilled, peer);
						Thread t2 = new Thread(kPeer);
						t2.start();
						t2.join();
						
						//ping new Successors
						pingnewSuccessors(peer);
						
						//reset ping count to 0
						pingSuc2Count = 0;
						
					}
					//System.out.println("ping count for successor1: " + pingSuc1Count);
					//System.out.println("ping count for successor2: " + pingSuc2Count);
					
					//Thread.sleep(30000);
					
					//System.out.println("The two successors are now Peer " + peer.getSuccessor1() + " and Peer " + peer.getSuccessor2());
					//System.out.println("The two predecessors are now Peer " + peer.getPredecessor1() + " and Peer " + peer.getPredecessor2());
					
					if(peer.isDeparted()) {
						break;
					}
					
					
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			//socket.close();
			
		}catch (UnknownHostException e1) {
			e1.printStackTrace();
		}catch (SocketException e2) {
			e2.printStackTrace();
		} 
	}
	
	private void pingSuccessors(Peer peer){
		String pingMessage1 = "Ping " + peer.getId() + " " + "peer";
		byte[] pingMessageBytes = pingMessage1.getBytes();
		
		
		
		successor1Port = PORT + peer.getSuccessor1();
		successor2Port = PORT + peer.getSuccessor2();
		
		
		DatagramPacket pingRequest1 = new DatagramPacket(pingMessageBytes, 0, pingMessageBytes.length, serverIPAddress, successor1Port);
		DatagramPacket pingRequest2 = new DatagramPacket(pingMessageBytes, 0, pingMessageBytes.length, serverIPAddress, successor2Port);
		
		try {
			socket.send(pingRequest1);
			socket.send(pingRequest2);
		} catch (IOException e) {
			System.out.println("ERROR: pinging requests cannot be sent");
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private void pingnewSuccessors(Peer peer) {
		
		byte[] newPre1 = ("newPing1 " + peer.getId() + " " + peer.getPredecessor1() + " \n").getBytes();
		byte[] newPre2 = ("newPing2 " + peer.getId() + " \n").getBytes();
		
		DatagramPacket newPre1Ping = new DatagramPacket(newPre1, 0, newPre1.length, serverIPAddress, PORT + peer.getSuccessor1());
		DatagramPacket newPre2Ping = new DatagramPacket(newPre2, 0, newPre2.length, serverIPAddress, PORT +peer.getSuccessor2());
		
		try {
			socket.send(newPre1Ping);
			socket.send(newPre2Ping);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	
	
	
}










