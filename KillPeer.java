import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;

public class KillPeer implements Runnable {
	private final int PORT = 50000;
	private Socket connectionSocket;
	private int peerKilled;
	private Peer peer;

	public KillPeer(Socket socket, int i, Peer p) {
		connectionSocket = socket;
		peerKilled = i;
		peer = p;
		// TODO Auto-generated constructor stub
	}

	@Override
	public void run() {	
		try {
			BufferedReader inFromSuc = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
			DataOutputStream outToSuc = new DataOutputStream(connectionSocket.getOutputStream());
			
			
			//if peer killed is its first successor send message to its second successor
			if(peerKilled == peer.getSuccessor1()) {
				
				outToSuc.writeBytes("kill1 " + peerKilled + " " + "\n");
				String newSucMessage = inFromSuc.readLine();
				//System.out.println(newSucMessage);
				String[] sucMessageArray = newSucMessage.split(" ");
				
				//move its second successors as now its first successors and the received successor is its second successor
				int suc2 = peer.getSuccessor2();
				peer.setSuccessor1(suc2);
				peer.setSuccessor2(Integer.parseInt(sucMessageArray[1]));
				
				Socket clientSocket = new Socket(InetAddress.getByName("localhost"), PORT + peer.getSuccessor1());
				
				//create new output stream for socket
				System.out.println("settng up new tcp connections with " + peer.getSuccessor1());
				//peer.outToSuccessor.close();
				
				DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
				peer.setOutToSuccessor(out);

			//if peer killed is its second successor send message to first successor for its second successor
			}else if (peerKilled == peer.getSuccessor2()) {
				int currentSuccessor2 = peer.getSuccessor2();
				
				while (peerKilled == currentSuccessor2) {
					//System.out.println("THIS SHOULD ONLY BE SHOWN ONCE");
					
					outToSuc.writeBytes("kill2 " + peerKilled + " " + "\n");
					String newMessage = inFromSuc.readLine();
					String[] newMessageArray = newMessage.split(" ");
					if(newMessageArray[1] !=  null) {
						
						//allocate a new second successor
						currentSuccessor2 = Integer.parseInt(newMessageArray[1]);
						//System.out.println("My new second Successor is " + currentSuccessor2 );
					}else {
						
						//THIS SHOULD NOT PRINT
						System.out.println("newMessageArray[1] is null");
						
					}
				}
				peer.setSuccessor2(currentSuccessor2);
			
	
			}else {
				
				System.out.println("ERROR: Peer " + peerKilled + " killed is not a successor"); 
			}
			System.out.println("Peer " + peerKilled + " is no longer alive");
			System.out.println("My first successor is now Peer " + peer.getSuccessor1());
			System.out.println("My second successors is now Peer " + peer.getSuccessor2());
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
