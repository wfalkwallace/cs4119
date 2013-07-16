import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Calendar;


/**
 * 
 */

/**
 * @author wgf2104
 *
 */
public class SRNode {

	static int sourcePort;
	static int destinationPort;
	static int windowSize;
	static int timeOut;
	static double lossRate;

	static volatile int windowBase;
	static volatile int nextWindowBase;

	static DatagramSocket sendSocket;

	static volatile boolean[] acked;
	
	static volatile boolean ackThreadKill = false;
	

	private static boolean allACKed(){
		for(boolean b : acked)
			if(!b)
				return false;
		return true;
	}

	private static String getTimestamp(){
		return "[" + Calendar.getInstance().getTimeInMillis() + "]";
	}

	private static long getLongTimestamp(){
		return Calendar.getInstance().getTimeInMillis();
	}

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		//check command line arguments
		if(args.length < 5){
			System.out.println("Usage: SRNode <source-port> <destination-port> <window-size> <time-out> <loss-rate>");
			return;
		}
		//command args
		sourcePort = Integer.parseInt(args[0]);
		destinationPort = Integer.parseInt(args[1]);
		windowSize = Integer.parseInt(args[2]);
		timeOut = Integer.parseInt(args[3]);
		lossRate = Double.parseDouble(args[4]);	


		//start the receiver
		new SRReceiver(sourcePort, lossRate, windowSize).start();

		//make reader
		BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

		//make sending socket
		sendSocket = new DatagramSocket();	

		//start command+send
		while(true){
			String inputString = input.readLine();
			if(inputString.equals("quit"))
				return;
			else if(!inputString.startsWith("send "))
				System.out.println("Invalid Usage\n Usage: send <message>\n");
			else{
				windowBase = 0;
				nextWindowBase = 1;
				char[] messages = inputString.substring(5).toCharArray();

				//start ack listener
				ackThreadKill = false;
				(new Thread((new SRNode()).new ACKThread(sendSocket, messages.length))).start();	

				//make send buffer
				byte[] sendBuffer = new byte[1024];

				acked = new boolean[messages.length];
				long[] sent = new long[messages.length];
				while(!allACKed()){
					for(int packetID = windowBase; packetID < ((messages.length < (windowBase+windowSize)) ? messages.length : (windowBase+windowSize)); packetID++){
						if(!acked[packetID]){
							if(getLongTimestamp() - sent[packetID] > timeOut || sent[packetID] == 0){
								if(sent[packetID] != 0)
									System.out.println(getTimestamp() + " packet-" + packetID + " timeout");
								String payload = packetID + "," + messages[packetID];
								//								System.out.println(payload);
								sendBuffer = payload.getBytes();
								DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, InetAddress.getLocalHost(), destinationPort);
								sendSocket.send(sendPacket);
								sent[packetID] = getLongTimestamp();
								System.out.println(getTimestamp() + " packet-" + packetID + " " + messages[packetID] + " sent");
							}
						}
					}
				}//close for entire-input-message loop
				ackThreadKill = true;
			}//close user input else => send command
		}//close infinite user input loop
	}

	public class ACKThread implements Runnable {

		DatagramSocket ackSocket;
		int messageLength;

		public ACKThread(DatagramSocket sendSocket, int ml) {
			ackSocket = sendSocket;
			messageLength = ml;
		}

		public void run() {
			while (!ackThreadKill) {
				try {
					byte[] returnBuffer = new byte[1024];
					DatagramPacket ackPacket = new DatagramPacket(returnBuffer, returnBuffer.length);
					ackSocket.receive(ackPacket);
					String ack = new String(returnBuffer, 0, ackPacket.getLength());
					int ackID = Integer.parseInt(ack.substring(4));
					acked[ackID] = true;	
					if(ackID != windowBase){
						System.out.println(getTimestamp() + " " + ack + " received");
						//get next window base
						for(int i = nextWindowBase; i < messageLength; i++){
							if(i >= messageLength)
								i = messageLength;
							if(!acked[i]){
								nextWindowBase = i;
								break;
							}
						}

//						System.out.println("wb = " + windowBase + " | nwb = " + nextWindowBase);
					}
					else{
						windowBase = nextWindowBase;
						System.out.println(getTimestamp() + " " + ack + " received; " +
								"window = [" + windowBase + "," + (windowBase+windowSize) + "]");
						//get next window base
						for(int i = nextWindowBase + 1; i < messageLength; i++)
							if(i >= messageLength)
								i = messageLength;
							else if(!acked[i]){
								nextWindowBase = i;
								break;
							}
//						System.out.println("wb = " + windowBase + " | nwb = " + nextWindowBase);
					}
				} catch (IOException e) {
					System.out.println("Error in ACK receiver thread");
				}

			}
		}
	}//close ACK thread class










}//close class
