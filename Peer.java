

import java.io.*;
import java.net.*;

import java.util.concurrent.*;

public class Peer implements Runnable {
	static int id;
	static int predecessor1 = 0;
	static int predecessor2 = 0;
	static int successor1;
	static int successor2;
	static long startTime; 
	static float dropRate;
	static int MSS;
	static DataOutputStream outToSuccessor;
	private DatagramSocket udpSocket;
	static boolean isDeparted = false;
	
	

	private BlockingQueue<String> queue;
	private BlockingQueue<String[]> pingQueue;
	
	public Peer (int i, int s1, int s2, long sTime, float dRate, int segSize) {
		this.id = i;
		this.successor1 = s1;
		this.successor2 = s2;
		this.startTime = sTime;
		this.dropRate = dRate;
		this.outToSuccessor = null;
		this.MSS = segSize;
		this.queue = new LinkedBlockingQueue<>(1);
		this.pingQueue = new LinkedBlockingQueue<>(5);
		this.isDeparted = false;
	}
	
	public static boolean isDeparted() {
		return isDeparted;
	}


	public static void setDeparted(boolean isDeparted) {
		Peer.isDeparted = isDeparted;
	}
	
	
	public static int getMSS() {
		return MSS;
	}


	public static void setMSS(int mSS) {
		MSS = mSS;
	}


	public BlockingQueue<String[]> getPingQueue() {
		return pingQueue;
	}

	public void setPingQueue(BlockingQueue<String[]> pingQueue) {
		this.pingQueue = pingQueue;
	}

	static int pingSeq = 0;

	public static int getPingSeq() {
		return pingSeq;
	}

	public static void setPingSeq(int pingSeq) {
		Peer.pingSeq = pingSeq;
	}

	public DatagramSocket getUdpSocket() {
		return udpSocket;
	}

	public void setUdpSocket(DatagramSocket udpSocket) {
		this.udpSocket = udpSocket;
	}

	
	public BlockingQueue<String> getQueue() {
		return queue;
	}

	public void setQueue(BlockingQueue<String> queue) {
		this.queue = queue;
	}

	public int getId() {
		return id;
	}

	public synchronized void setId(int id) {
		Peer.id = id;
	}
	
	public synchronized DataOutputStream getOutToSuccessor() {
		return outToSuccessor;
	}

	public synchronized void setOutToSuccessor(DataOutputStream outToSuccessor) {
		Peer.outToSuccessor = outToSuccessor;
	}
	
	public synchronized int getPredecessor1() {
		return predecessor1;
	}

	public synchronized void setPredecessor1(int predecessor1) {
	Peer.predecessor1 = predecessor1;
	}
	
	public synchronized int getPredecessor2() {
		return predecessor2;
}

	public synchronized void setPredecessor2(int predecessor2) {
	Peer.predecessor2 = predecessor2;
	}

	public synchronized int getSuccessor1() {
			return successor1;
	}

	public synchronized void setSuccessor1(int successor1) {
		Peer.successor1 = successor1;
	}

	public synchronized int getSuccessor2() {
		return successor2;
	}

	public synchronized void setSuccessor2(int successor2) {
		Peer.successor2 = successor2;
	}

	public static long getStartTime() {
		return startTime;
	}

	public static void setStartTime(long startTime) {
		Peer.startTime = startTime;
	}

	public static float getDropRate() {
		return dropRate;
	}

	public static void setDropRate(float dropRate) {
		Peer.dropRate = dropRate;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

	}

}
