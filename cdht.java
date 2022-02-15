import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class cdht {
	private final static int PORT = 50000;
	
	static int id;
	static int successor1;
	static int successor2;
	static long startTime;
	static float dropRate = 0;
	static String fileName = null;
	static int MSS;
	static Peer peer;

	

	public static void main(String[] args) throws Exception {
		
		startTime = System.currentTimeMillis();
		id = Integer.parseInt(args[0]);
		successor1 = Integer.parseInt(args[1]);
		successor2 = Integer.parseInt(args[2]);
		MSS = Integer.parseInt(args[3]);
		dropRate = Float.parseFloat(args[4]);
		
		peer = new Peer(id, successor1, successor2, startTime, dropRate, MSS);
		System.out.println("This peer id is " + peer.getId());
		
		//Set up peer's tcp server side for receiving tcp connections 
		TCPServer tcpServer = new TCPServer(peer);
		Thread t3 = new Thread(tcpServer);
		t3.start();

		
		//Set up peer's server side for receiving UDP packets 
		Server server = new Server(peer);
		Thread t1 = new Thread(server); 
		t1.start();
		Thread.sleep(1000);
		
		//Set up peer's client side for sending UDP packets specifically ping requests
		Client client = new Client(peer);
		Thread t2 = new Thread(client);
		t2.start();
		
		
	
		
		//Set up peer's tcp client side for setting up tcp connections 
		TCPClient tcpClient1 = new TCPClient(peer);
		Thread t4 = new Thread(tcpClient1);
		t4.start();

	}
	
}







