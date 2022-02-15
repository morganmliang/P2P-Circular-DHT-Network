

import java.io.*;
import java.net.*;
import java.util.concurrent.TimeUnit;

public class TCPServer implements Runnable {
	private final int PORT = 50000;

	private Peer peer;
	ServerSocket welcomeSocket;

	//private ServerSocket welcomeSocket;

	
	public TCPServer (Peer p) {
		this.peer = p;
	}
	
	

	//Fork tcp connections into threads 
	@Override
	public void run() {
		try {
			//System.out.println("My peer id is " + (PORT + peer.getId()));
			this.welcomeSocket = new ServerSocket(PORT + peer.getId());
			
			while (true){
				
				// accept connection from connection queue
				Socket connectionSocket = welcomeSocket.accept();
				Thread t = new Thread(new serverHandler(connectionSocket, peer));
				t.start();
				
				if(peer.isDeparted()) {
					break;
				}
				
			}
			welcomeSocket.close();
		}catch (IOException e) {
			System.out.println("ioexception");
			e.printStackTrace();
		}

	}
	
}
