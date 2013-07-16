import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Calendar;
import java.util.Random;

/**
 * 
 */

/**
 * @author wgf2104
 *
 */
public class SRReceiver extends Thread {

	DatagramSocket receiveSocket;
	double lossRate;
	int windowSize;
	int windowBase;
	int windowNext;

	Random random = new Random();
	byte[] receiveBuffer = new byte[1024];
	byte[] ackBuffer = new byte[1024];

	private static String getTimestamp(){
		return "[" + Calendar.getInstance().getTimeInMillis() + "]";
	}

	/**
	 * Constructor
	 * @param sp the source port on which the Receiving thread listens
	 * @param lr the loss rate
	 * @throws SocketException
	 */
	public SRReceiver(int sp, double lr, int ws) {
		try {
			receiveSocket = new DatagramSocket(sp);
			lossRate = lr;
			windowSize = ws;
			windowBase = 0;
			windowNext = 1;
		} catch (SocketException e) {
			System.out.println("Could not construct Receiver Thread; socket construction failed.");
		}
	}

	/**
	 * 
	 */
	public void run() {
		while (true) {
			//receive packet and split content
			DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
			try {
				receiveSocket.receive(receivePacket);
				String receivePayload = new String(receiveBuffer, 0, receivePacket.getLength());
				int packetID = Integer.parseInt(receivePayload.split(",")[0]);
				String message = receivePayload.split(",")[1];
				//drop some junk
				if(random.nextDouble() < lossRate){
					System.out.println(getTimestamp() + " packet-" + packetID + " " + message + " discarded");
					continue;
				}
				if(packetID == windowBase){
					//set new window base
					windowBase = windowNext;
					//print receipt to log
					System.out.println(getTimestamp() + " packet-" + packetID + " " + message + " received; " +
							"window = [" + windowBase + "," + (windowBase+windowSize) + "]");
				}
				else{
					//set new window next
					windowNext = packetID + 1;
					//print receipt to log
					System.out.println(getTimestamp() + " packet-" + packetID + " " + message + " received");
				}
				ack(packetID, receivePacket.getAddress(), receivePacket.getPort());
			} catch (IOException e) {
				System.out.println("Receiving/parsing packet failed; " +
						"please check your connections and socket/port configurations");
			}
		}//infinite run loop end
	}//run method end

	private void ack(int pid, InetAddress returnAddress, int returnPort){
		//prepare and send ack
		String ackPayload = "ACK-" + pid;
		ackBuffer = ackPayload.getBytes();
		DatagramPacket ackPacket = new DatagramPacket(ackBuffer, ackBuffer.length, returnAddress, returnPort);
		try {
			receiveSocket.send(ackPacket);
		} catch (IOException e) {
			System.out.println("Sending ACK packet failed; please check your connections and socket/port configurations");
			return;
		}
		//print ack to log
		System.out.println(getTimestamp() + " " + ackPayload + " sent");
	}

}//receiver class end
