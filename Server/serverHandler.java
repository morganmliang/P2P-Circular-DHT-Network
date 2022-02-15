

import java.io.*;
import java.net.*;

public class serverHandler implements Runnable {
	private final int PORT = 50000;
	private Socket connectSocket;
	Peer peer;
	String connectingPeer;
	DataInputStream in;
	DataOutputStream outToSuccessor;
	DataOutputStream outToPeer = null;
	BufferedReader inFromPeer;
	DataOutputStream sendResponse;

	Socket sendingSocket;
	
	
	//write - to successorPort
	// read - from the sending peer
	
	public serverHandler(Socket sock, Peer p) {
		this.connectSocket = sock;
		this.peer = p;
		
	}
	
	@Override
	public void run() {
		try {
		
			inFromPeer = new BufferedReader(new InputStreamReader(connectSocket.getInputStream()));
			
			while (true) {
				String message = inFromPeer.readLine();
				
				outToSuccessor = peer.getOutToSuccessor();
				
				
				if( message != null) {
					String[] messageArray = message.split(" ");
					
					
					//peer receives file request from another peer
					if( messageArray[0].equals("request")) {
						System.out.println("Peer is requesting file " + messageArray[1]);
						System.out.println("Checking if file is contained here...");
						
						
						if(peerhasFile(messageArray)) {
							System.out.println("File " + messageArray[1] +  " is here");
						
							System.out.println("A response message, destined for peer " + messageArray[2] 
									+ ", has been sent.");
							
							sendFileResponse(messageArray);
							
							FileTransfer response = new FileTransfer(peer, messageArray);
							Thread fileTransferT = new Thread(response);
							fileTransferT.start();
							
						//if entire network has been traversed without anyone holding file - SHOULD NOT HAPPEN
						}else if(Integer.parseInt(messageArray[2]) == peer.getId()) {
							System.out.println("request has returned to its requester peer");
							
						// pass request to its successor
						}else{
							System.out.println("File " + messageArray[1] + " is not stored here.");
							System.out.println("File request has been forwarded to my successor");
							outToSuccessor.writeBytes(message + '\n');
							
						}
	
						
					//received file response message from file holder
					}else if (messageArray[0].equals("response")){
						String senderPeer = messageArray[1];
						String fileName = messageArray[2];
						System.out.println("Received a response message from peer " + senderPeer 
								+ ", which has the file " + fileName);
						
						System.out.println("Will now start receiving the file .....");
						
					}else if (messageArray[0].equals("acknowledge")){
						System.out.println("acknowledgement of response has been received ");
					
					//Server receiving departing message from successor
					}else if (messageArray[0].equals("depart")) {
						System.out.println("Peer " + messageArray[1] + " will depart from the network.");
						
						int successor1Peer = peer.getSuccessor1();
						int successor2Peer = peer.getSuccessor2();
						
						int departingPeer = Integer.parseInt(messageArray[1]);
						int newSuccessor = Integer.parseInt(messageArray[2]);
						
						
						//allocate new successors 
						if(successor1Peer == departingPeer) {
							int suc2 = peer.getSuccessor2();
							
							peer.setSuccessor2(newSuccessor);
							peer.setSuccessor1(suc2);
							//System.out.println("I am here with " + peer.getSuccessor1());
						}else if (successor2Peer == departingPeer) {
							peer.setSuccessor2(newSuccessor);
						}else {
							System.out.println("ERROR: departing peer is not a successor");
						}
						
						orderSuccessors(peer);
						Socket clientSocket = new Socket(InetAddress.getByName("localhost"), PORT + peer.getSuccessor1());
						DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
						peer.setOutToSuccessor(out);
						System.out.println("My first successor is now Peer " + peer.getSuccessor1());
						System.out.println("My second successor is now Peer " + peer.getSuccessor2());
						
						DataOutputStream toDepartingPeer = new DataOutputStream(connectSocket.getOutputStream());
						toDepartingPeer.writeBytes("depart received from Peer " + peer.getId() + '\n');
						
						//immediately ping new successors 
						pingnewSuccessors(peer);
						
						
						
						//need to change predecessor information as well
						//send received depart message to departing peer
						//departing peer will close all sockets 
					}else if (messageArray[0].equals("Suc")) {
						System.out.println("gracefully exit message received as successor");
						int predecessor1Peer = peer.getPredecessor1();
						int predecessor2Peer = peer.getPredecessor2();
						int departingPeer = Integer.parseInt(messageArray[1]);
						int newPredecessor = Integer.parseInt(messageArray[2]);
						
						if(predecessor1Peer == departingPeer) {
							peer.setPredecessor1(newPredecessor);
						}else if (predecessor2Peer == departingPeer) {
							peer.setSuccessor2(newPredecessor);
						}
						orderPredecessors(peer);
						System.out.println("The new predecessors after the graceful exit of Peer " + departingPeer );
						System.out.println("My first predecessor is now Peer " + peer.getPredecessor1());
						System.out.println("My second predecessor is now Peer " + peer.getPredecessor2());
						
						DataOutputStream toDepartingPeer = new DataOutputStream(connectSocket.getOutputStream());
						toDepartingPeer.writeBytes("depart received from Peer " + peer.getId() + '\n');
							
					//sender's first sucessors is killed
					// message from second predecessor -> sends its first successor
					}else if (messageArray[0].equals("kill1")) {
						
						DataOutputStream toFindingSuc = new DataOutputStream(connectSocket.getOutputStream());
						toFindingSuc.writeBytes("kill1Received" + " " + peer.getSuccessor1() + '\n');
							
						
					//sender is its first predecessor
					// sends its first successor or second successor depending on which kill message is sent first
					// passed successor is the predecessor's second successor
					}else if (messageArray[0].equals("kill2")) {
						int currentSuccessor2;
						int killedPeer = Integer.parseInt(messageArray[1]);
						//System.out.println("successor2 has been killed sending to my successor 2 as its successor2");
						synchronized (peer) {
							if(killedPeer == peer.getSuccessor1()) {
								currentSuccessor2 = peer.getSuccessor2();
							}else {
								currentSuccessor2 = peer.getSuccessor1();
							}
						}
						//peer.successors is guaranteed to be new successor
						
						//System.out.println("The this peer's successor is now" + peer.getSuccessor1());
						DataOutputStream toFindingSuc = new DataOutputStream(connectSocket.getOutputStream());
						toFindingSuc.writeBytes("kill2Received" + " " + currentSuccessor2 + '\n');
							
					}else {
	
						System.out.println("Peer sent a invalid command");
					}
				}
				
				//check if peer is dead
				if(peer.isDeparted()) {
					break;
				}
			}
			//close all connections 
			//inFromPeer.close();
			//outToSuccessor.close();
			//connectSocket.close();
			
			
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
			
			//clientSocket.close();
			//in.close();
			//out.close();
//		}catch (Exception e) {
//			e.printStackTrace();
//		}
		//clientSocket.close();
		// TODO Auto-generated method stub
	
	// message Format = status + fileName + requestID;
	boolean peerhasFile(String[] messageArray) {
		
		boolean containsFile = false;
		int hashValue = hashFunction(Integer.parseInt(messageArray[1]));
	
		//If file hash is between peer Id and its predecessor value
		if(peer.getId() >= hashValue && hashValue > peer.getPredecessor1()) { 
			containsFile = true;

		//If file is near the end of the CHT, then file is contained in first peer in network
		}else if(peer.getPredecessor1() > peer.getId() && (hashValue > peer.getPredecessor1() || hashValue == peer.getId())) {
			containsFile = true;
			
		//file is not contained in peer
		}else { 
			containsFile =false;
		}

		return containsFile;
	}
	
	public int hashFunction(int fileName){
		int hashValue = 0;
		
		hashValue = fileName % 256;
		
		return hashValue;
	}
	
	public void sendFileResponse(String[] messageArray) {
		//create a tcp connection with requesting peer to send response
		
		try {
			Socket responseSocket = new Socket(InetAddress.getByName("localhost"), PORT + Integer.parseInt(messageArray[2]));
			DataOutputStream sendfResponse = new DataOutputStream(responseSocket.getOutputStream());
			String sendMessage = "response " + peer.getId() + " " + messageArray[1];
			sendfResponse.writeBytes(sendMessage + '\n');
			responseSocket.close();
		} catch (IOException e) {
			System.out.println("ERROR: file Response message cannot be sent");
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	//successor1 is closest to current peer
	public void orderSuccessors(Peer p) {
		int sPeer1 = p.getSuccessor1();
		int sPeer2 = p.getSuccessor2();
		int peerId = p.getId();
		if (peerId > sPeer1 && peerId > sPeer2) {
			if(sPeer1 > sPeer2) {
				p.setSuccessor1(sPeer2);
				p.setSuccessor2(sPeer1);
			}
		}else if (peerId < sPeer1 && peerId < sPeer2) {
			if(sPeer1 > sPeer2) {
				p.setSuccessor1(sPeer2);
				p.setSuccessor2(sPeer1);
			}
		}else {
			if(sPeer2 > sPeer1) {
				p.setSuccessor1(sPeer2);
				p.setSuccessor2(sPeer1);
			}
		}	
	}
	//predecessor 1 is closest to current peer
	public void orderPredecessors(Peer p) {
		int pPeer1 = p.getPredecessor1();
		int pPeer2 = p.getPredecessor2();
		int peerId = p.getId();
		
		if (peerId > pPeer1 && peerId > pPeer2) {
			if(pPeer1 < pPeer2) {
				p.setPredecessor1(pPeer2);
				p.setPredecessor2(pPeer1);
			}
		}else if (peerId < pPeer1 && peerId < pPeer2) {
			if(pPeer1 < pPeer2) {
				p.setPredecessor1(pPeer2);
				p.setPredecessor2(pPeer1);
			}
		}else {
			if(pPeer2 < pPeer1) {
				p.setPredecessor1(pPeer2);
				p.setPredecessor2(pPeer1);
			}
		}	
	}
	
	private void pingnewSuccessors(Peer peer){
		
		
		String pingMessage1 = "newPing1" + " " + peer.getId() + " " + '\n';
		String pingMessage2 = "newPing2" + " " + peer.getId() + " " + '\n';
		byte[] pingMessage1Bytes = pingMessage1.getBytes();
		byte[] pingMessage2Bytes = pingMessage2.getBytes();
		
		
		
		int successor1Port = PORT + peer.getSuccessor1();
		int successor2Port = PORT + peer.getSuccessor2();
		

		try {
			DatagramSocket socket = new DatagramSocket();
			DatagramPacket pingRequest1 = new DatagramPacket(pingMessage1Bytes, 0, pingMessage1Bytes.length, InetAddress.getByName("localhost"), successor1Port);
			DatagramPacket pingRequest2 = new DatagramPacket(pingMessage2Bytes, 0, pingMessage2Bytes.length, InetAddress.getByName("localhost"), successor2Port);
			socket.send(pingRequest1);
			socket.send(pingRequest2);
			
		} catch (IOException e) {
			System.out.println("ERROR: pinging requests cannot be sent");
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
}
