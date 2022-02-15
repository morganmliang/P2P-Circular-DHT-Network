
import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class FileTransfer implements Runnable {
	private final int PORT = 50000;
	private final String LOCALHOST = "localhost";
	Peer peer;
	Socket toRequester;
	BufferedReader getResponse;
	DataOutputStream sendResponse;
	String[] messageArray;
	int sequenceNum = 1;
	int ackNum = 0;
	int requestorPort;
	int reTransmitted = 0;
	long logTime = 0;
	
	String event = "snd";
	
	
	public FileTransfer (Peer p, String[] m) {
		peer = p;
		messageArray = m;
		requestorPort = PORT + Integer.parseInt(messageArray[2]);
	}
	@Override
	
	// message Format = status + fileName + requestID;
	  
	public void run() {
		try {

			//Start file Transfer using UDP
			System.out.println("Now will start sending the file .....");
			
			//create filelog
			File filelog = new File("responding_log.txt");
			if(filelog.createNewFile()) {
            	//System.out.println("File is created");
            }else {
            	filelog.delete();
            	filelog.createNewFile();
            	//System.out.println("File already exists");
            	
            }
			
			FileWriter fr = new FileWriter(filelog, true);
			sequenceNum = 1;
			
			
			//get max segment size
			int maxSegmentSize = peer.getMSS();
			byte b[]=new byte[maxSegmentSize];
			DatagramSocket socket = new DatagramSocket();
			
			
			try {
				
				FileInputStream fStream = new FileInputStream(messageArray[1] + ".pdf");
				while(fStream.available()!= 0) {
					int i = fStream.read(b);

//					//fileHolderID + sequenceNum + ackNum + MSS
					String packetHeader = peer.getId() + " " + sequenceNum + " " + ackNum + " " + i + " ";
					byte[] HeaderArray = new byte[100];
					HeaderArray = packetHeader.getBytes();
					
					//Header length is always 100 bytes
					int totalLength = HeaderArray.length + b.length;
			        byte[] dataPacket = new byte[100 + b.length];
			        System.arraycopy(HeaderArray, 0, dataPacket, 0, HeaderArray.length);
			        System.arraycopy(b, 0, dataPacket, 100, i);

//			        System.out.println("THe size of i is " + i);
//			        System.out.println("total length of headerarray and b is " + totalLength);
//					System.out.println("The  total size of the b is " + b.length);
//					System.out.println("header length is "  + HeaderArray.length);
//					System.out.println("The  total size of the packet is " + dataPacket.length);
//					
					DatagramPacket sendPacket = new DatagramPacket(dataPacket, dataPacket.length,InetAddress.getByName(LOCALHOST), requestorPort);
					
		            boolean ackNotReceived = true;
		            
		            // The acknowledgment is not correct
		            while (ackNotReceived) {
		            	
		            	//Check if this packet is dropped 
						double randomValue = Math.random();

						if (randomValue >= peer.getDropRate()) {
						    socket.send(sendPacket); 
						    if(reTransmitted == 0) {
						    	event = "snd";
						    }else {
						    	event = "RTX";
						    }
						    
			            }else {
			            	
			            	if(reTransmitted > 0) {
			            		event = "RTX/Drop";
			            	}else {
			            		event = "Drop";
			            	}
			            }
			            
			            logTime = System.currentTimeMillis() - peer.getStartTime();
			            
				        //write to log file
			            fr = new FileWriter(filelog, true);
				        fr.write(event + "  " + logTime + "  " + sequenceNum + "  " + i + "  0" + '\n');
				        fr.close();
						
		                //check for acknowledgement
		            	String acknowledgeString = null;
			            try {
			            	
			            	//Check for acknowledge of file packet, timeout after 1 second
			            	acknowledgeString = peer.getQueue().poll(1, TimeUnit.SECONDS);
			            	
						} catch (Exception e1) {
							// TODO Auto-generated catch block
							System.out.println("ERROR: QUEUE POLLING ERROR");
							e1.printStackTrace();
						}
			            
			            //Polling has timed out
			            if(acknowledgeString == null) {
			            	reTransmitted++;
			            	
			            //acknowledgement has been received, sending the next packet
			            }else {
			            	ackNotReceived = false;
			            	 
			            	//add rcv entry to log file
			            	sequenceNum += i;
			            	event = "rcv";
			            	logTime = System.currentTimeMillis() - peer.getStartTime();
			            	fr = new FileWriter(filelog, true);
					        fr.write(event + "  " + logTime + "  " + 0 + "  " + i + "  " + sequenceNum + '\n');
					        fr.close();
			            }
		            }
		            reTransmitted = 0;
				}
				
				//send EOF message to indicate end of file transfer
				byte[] endofFile = ("EOF " + '\n').getBytes();
				DatagramPacket endPacket = new DatagramPacket(endofFile, endofFile.length,InetAddress.getByName(LOCALHOST), requestorPort);
				socket.send(endPacket);
				socket.close();
				System.out.println("Finished sending file....");
				
			}catch (FileNotFoundException e) {
				System.out.println("File is not found");
			}
				
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//creates new serverHandler
		
		// TODO Auto-generated method stub

	}
	
	
	public static int[] combine(byte[] a, byte[] b){
        int length = a.length + b.length;
        int[] result = new int[length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }
}
