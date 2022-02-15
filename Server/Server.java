

import java.io.*;
import java.util.*;
import java.net.*;


public class Server implements Runnable {
	private final static String LOCALHOST = "localhost";
	private final static int PORT = 50000;
	Peer peer;
	private int responseSeq = 0;
	private int pred1 = 0;
	private int pred2 = 0;
	private long logTime = 0;
	private FileWriter fr;
	
	
	public Server(Peer p){
		this.peer = p;

	}

	@Override
	public void run() {
		try {
			int peerPort = PORT + peer.getId(); 
			
			File file = new File("received_file.pdf");
		    
		    if(file.createNewFile()) {
		    	//System.out.println("Success!");
		    }else {
//		    	int n = 0;
//		    	boolean newFile = false;
//		    	while(!newFile) {
//		    		
//		    		file = new File("received_file"+n+".pdf");
//		    		newFile = file.createNewFile();
//		    		n++;
//		    	}
		    	//System.out.println ("Error, file already exists.");
		    }
		    
	      
			
			File filelog = new File("requesting_log.txt");
	        
	        if(filelog.createNewFile()) {
	        	//System.out.println("File is created");
	        }else {
	        	filelog.delete();
            	filelog.createNewFile();
	        	//System.out.println("File already exists");	        	
	        }
	        
	        
		    FileOutputStream outToFile = new FileOutputStream(file,true);
			//listen to peerPort
			DatagramSocket fromFileHolder = new DatagramSocket(peerPort);
			peer.setUdpSocket(fromFileHolder);
			DatagramSocket sendDatagram = new DatagramSocket();
			while (true) {
		         // Create a datagram packet to hold incomming UDP packet.
				DatagramPacket filePacket = new DatagramPacket(new byte[peer.getMSS() + 1000],peer.getMSS() + 1000);
				
				fromFileHolder.receive(filePacket);
				byte[] packetBytes = filePacket.getData();
				String fileMessage = new String(packetBytes);
				
				String[] fileArray = fileMessage.split(" ");
				
				
				if(fileArray[0].equals("acknowledge")) {
					//received acknowledgement
					
					
					peer.getQueue().put("acknowledge");
					
			    
			    //A ping request was received
				}else if(fileArray[0].equals("Ping")){
					int prePeer = Integer.parseInt(fileArray[1]);
					
					
					System.out.println("A ping request message was received from Peer " + prePeer);
					if(pred1 == 0) {
						pred1 = prePeer;
					}else if (pred1 != prePeer && pred2 == 0 ) {
						pred2 = prePeer;
					}else {
						//System.out.println("ERROR: SAME PREDECESSORS");
					}
					if (pred1 != 0 && pred2 != 0) {
						arrangePredecessors(peer, pred1, pred2);
						pred1 = 0;
						pred2 = 0;
					}
					
					//System.out.println("my first predecessor is " + peer.getPredecessor1() + " and my second predecessor is " + peer.getPredecessor2());
					//System.out.println("my first successor is " + peer.getSuccessor1() + " and my second predecessor is " + peer.getSuccessor2());
					
					
					
					int predecessorPort = PORT + prePeer;
					

					//Send ping response
					String respondToPing = "respondPing" + " " + peer.getId() + " " + responseSeq + " ";
					byte[] respondToPingBytes = respondToPing.getBytes();
					DatagramPacket pingResponse = new DatagramPacket(respondToPingBytes, 0, respondToPingBytes.length, InetAddress.getByName(LOCALHOST), predecessorPort);
					sendDatagram.send(pingResponse);
					
					//Part 5 - Add a count for number of missed pings if higher than 3 then 
					responseSeq++;
					
				//Receiving ping response from successor
				}else if (fileArray[0].equals("respondPing")) {
					int respondingPeer = Integer.parseInt(fileArray[1]);
					System.out.println("A ping response message was received from Peer " + respondingPeer);
					peer.getPingQueue().put(fileArray);
					
					
				//redefine predecessor1
				}else if (fileArray[0].equals("newPing1")) {
					//int newPre1 = peer.getPredecessor2();
					int newPre1 = Integer.parseInt(fileArray[1]);
					if(newPre1 != peer.getPredecessor1()) {
						peer.setPredecessor1(newPre1);
					}
					//peer.setPredecessor2(0);
					
					
					//redefine predecessor2
				}else if (fileArray[0].equals("newPing2")) {
					int newPre2 = Integer.parseInt(fileArray[1]);
					peer.setPredecessor2(newPre2);
					
				}else if (fileArray[0].equals("PingPre1")) {
					int newPre1 = peer.getPredecessor2();
					peer.setPredecessor1(newPre1);
					
					System.out.println("My predecessors are now Peer " + peer.getPredecessor1() + " and Peer "  + peer.getPredecessor2());
				}else if (fileArray[0].equals("PingPre2")) {
					int newPre2 = Integer.parseInt(fileArray[1]);
					peer.setPredecessor2(newPre2);
					System.out.println("My predecessors are now Peer " + peer.getPredecessor1() + " and Peer "  + peer.getPredecessor2());
				}else{
					
					if(!(fileArray[0].equals("EOF"))){
						//System.out.println("ERROR: fileArray[0] is " + fileArray[0]);
						int acknowledgePort = PORT + Integer.parseInt(fileArray[0]);
						
					
					
					//FINISH RECEIVING LOG
					//fileHolderID + sequenceNum + ackNum + MSS
					fr = new FileWriter(filelog, true);
					int sequenceNum = Integer.parseInt(fileArray[1]);
					int packetSize = Integer.parseInt(fileArray[3]);
					logTime = System.currentTimeMillis() - peer.getStartTime();
			        fr.write("rcv" + "  " + logTime + "  " + sequenceNum + "  " + packetSize + "  0" + '\n');
			        fr.close();
			        
			        sequenceNum += packetSize;
				
					
					outToFile.write(packetBytes, 100, peer.getMSS());
					fr = new FileWriter(filelog, true);
			        fr.write("snd" + "  " + logTime + "  " + "0" + "  " + packetSize + "  " + sequenceNum + '\n');
			        fr.close();
//				
				//String packetHeader = "";
				//byte[] HeaderArray = packetHeader.getBytes();
//				ByteArrayOutputStream output = new ByteArrayOutputStream();
//
//				output.write(HeaderArray);
//				output.write(packetBytes);
//
//				byte[] dataPacket = output.toByteArray();
//				System.out.println("The  total size of the packet is " + dataPacket.length);
				
				
						sendAcknowledge(sendDatagram,acknowledgePort);
						
					//File Transfer Finishsr
					}else {
						System.out.println("The file is received");
					}
					
				}
				if(peer.isDeparted()) {
					break;
				}
			}
//			outToFile.close();
//			fromFileHolder.close();
//			sendDatagram.close();
			
		
		} catch (SocketException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (IOException e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void printData(DatagramPacket request) throws Exception
	{
	   // Obtain references to the packet's array of bytes.
	   byte[] buf = request.getData();
	
	   // Wrap the bytes in a byte array input stream,
	   // so that you can read the data as a stream of bytes.
	   ByteArrayInputStream bais = new ByteArrayInputStream(buf);
	
	   // Wrap the byte array output stream in an input stream reader,
	   // so you can read the data as a stream of characters.
	   InputStreamReader isr = new InputStreamReader(bais);
	
	   // Wrap the input stream reader in a bufferred reader,
	   // so you can read the character data a line at a time.
	   // (A line is a sequence of chars terminated by any combination of \r and \n.) 
	   BufferedReader br = new BufferedReader(isr);
	
	   // The message data is contained in a single line, so read this line.
	   String line = br.readLine();
	
	   // Print host address and data received from it.
	   System.out.println(
	      "Received from " + 
	      request.getAddress().getHostAddress() + 
	      ": " +
	      new String(line) );
	}
	private static void sendAcknowledge(DatagramSocket socket, int port) throws IOException {
        // send acknowledgement
		String acknowledge = "acknowledge packet";
        byte[] bArray = acknowledge.getBytes();
        
        // the datagram packet to be sent
        DatagramPacket ackPacket = new DatagramPacket(bArray, bArray.length, InetAddress.getByName(LOCALHOST), port);
        socket.send(ackPacket);
        
    }
	
	private static void arrangePredecessors(Peer peer, int pre1, int pre2) {

		
		//if predecessors are the same as before do nothing
//		if((pre1 == peer.getPredecessor1() && pre2 == peer.getPredecessor2()) ||
//				(pre1 == peer.getPredecessor2() && pre2 == peer.getPredecessor1())) {
//			return;
//		}
		
		//peer to larger than both predecessors
		if(peer.getId() > pre1 && peer.getId() > pre2) {
			if(pre2 > pre1) {
				peer.setPredecessor1(pre2);
				peer.setPredecessor2(pre1);
			}else {
				peer.setPredecessor1(pre1);
				peer.setPredecessor2(pre2);
			}
		//peer is at the start of circular DHT
		}else if (peer.getId() < pre1 && peer.getId() < pre2){
			if(pre2 > pre1) {
				peer.setPredecessor1(pre2);
				peer.setPredecessor2(pre1);
			}else {
				peer.setPredecessor1(pre1);
				peer.setPredecessor2(pre2);
			}
		//peer is the second peer from start
		}else {
			if(pre2 < pre1) {
				peer.setPredecessor1(pre2);
				peer.setPredecessor2(pre1);
			}else {
				peer.setPredecessor1(pre1);
				peer.setPredecessor2(pre2);
			}
			
		}
	}
}


