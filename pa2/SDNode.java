import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Calendar;
import java.util.HashMap;

/**
 * 
 */

/**
 * @author wgf2104
 *
 */
public class SDNode {


	private static String getTimestamp(){
		return "[" + Calendar.getInstance().getTimeInMillis() + "]";
	}

	private static void printRoutingTable(){
		System.out.println(getTimestamp() + " Node " + sourcePort + " - Routing Table");
		for(int n : neighbors.keySet()){
			if(n != Integer.parseInt(neighbors.get(n)[0]))
				System.out.println("Node " + n + " [next " + neighbors.get(n)[0] + "] -> (" + neighbors.get(n)[1] + ")");
			else
				System.out.println("Node " + n + " -> (" + neighbors.get(n)[1] + ")");
		}
	}

	/**
	 * 
	 * @param neighbors
	 * @param sourcePort
	 * @return string whose first element is the sender's (RT owner's) port, then "node-next-wt,...,..."
	 */
	private static String rtPayload(boolean last){
		//last => 0
		String rt = last ? "0" : "1";
		for(int n : neighbors.keySet()){
			rt += "," + n + "-" + neighbors.get(n)[0] + "-" + neighbors.get(n)[1];
		}
		return rt;
	}

	private static void broadcast(boolean last){
		String payload = rtPayload(last);
		sendBuffer = payload.getBytes();
		for(int n : neighbors.keySet()){
			if(n == Integer.parseInt(neighbors.get(n)[0])){
				try {
					new SDSR(payload, sourcePort, n, Double.parseDouble(neighbors.get(n)[1]));
					System.out.println(getTimestamp() + " Message sent from Node " + sourcePort + " to Node " + n);
				} catch (IOException e) {
					System.out.println("Sending RT packet failed; please check your connections and socket/port configurations");
				}
			}
		}//close send RT to all neighbors
	}


	//static variables:
	static int sourcePort;
	static volatile boolean updated;
	static DatagramSocket sendSocket;
	static HashMap<Integer, String[]> neighbors;
	static byte[] sendBuffer = new byte[1024];
	static byte[] receiveBuffer = new byte[1024];


	/**
	 * MAIN
	 * @param args
	 * @throws SocketException 
	 */
	public static void main(String[] args) throws SocketException {
		//check command line arguments
		if(args.length < 3){
			System.out.println("DVNode <port-number> <neighbor_1-port> <neighbor_1-loss-rate> .... <neighbor_i-port> <neighbor_i-loss-rate> [last]");
			return;
		}
		
		(new Thread((new SDNode()).new ChangeThread())).start();	

		//start the receiver
		new SRReceiver(sourcePort, 0, 10).start();
		
		sourcePort = Integer.parseInt(args[0]);
		sendSocket = new DatagramSocket(sourcePort);

		neighbors = new HashMap<Integer, String[]>();

		//get # of neighbors (ie node density)
		int density = args.length;
		if(args[args.length - 1].equals("last"))
			density -= 1;

		for(int i = 1; i < density; i+=2)
			neighbors.put(Integer.parseInt(args[i]), new String[]{args[i], args[i+1]});

		//		printRoutingTable();

		if(args[args.length - 1].equals("last")){
			broadcast(true);
		}//sentinel
		while(true){
			//receive and update
			try {
				DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
				sendSocket.receive(receivePacket);
				String receivePayload = new String(receiveBuffer, 0, receivePacket.getLength());
				String[] rcvRT = receivePayload.split(",");
				int last = Integer.parseInt(rcvRT[0]);
				int senderPort = receivePacket.getPort();
				System.out.println(getTimestamp() + " Message received at Node " + sourcePort + " from Node " + senderPort);
				for(int i = 1; i < rcvRT.length; i++){
					//					System.out.println(rcvRT[i]);
					String[] node = rcvRT[i].split("-");
					//					for(String s : node)
					//						System.out.println(s);
					if(Integer.parseInt(node[0]) != sourcePort){
						if(neighbors.containsKey(Integer.parseInt(node[0]))){
							if(Double.parseDouble(neighbors.get(Integer.parseInt(node[0]))[1]) > Double.parseDouble(neighbors.get(senderPort)[1]) + Double.parseDouble(node[2])){
								neighbors.put(Integer.parseInt(node[0]), new String[]{Integer.toString(senderPort), Double.toString(Double.parseDouble(node[2]) + Double.parseDouble(neighbors.get(Integer.parseInt(node[0]))[1]))});
								updated = true;
							}
						}
						else{
							neighbors.put(Integer.parseInt(node[0]), new String[]{Integer.toString(senderPort), node[2]});
							updated = true;
						}
					}
				}
				if(updated || last == 0)
					//send updates
					broadcast(false);

			} catch(IOException e) {
				System.out.println("Error receiving RT; please check your connections and socket/port configurations");
			}

			if(updated){
				//print newly updated rt
				printRoutingTable();
				updated = false;
			}

		}//close infinite listen/update/broadcast loop

	}//close main


	public class ChangeThread implements Runnable {

		BufferedReader input;

		public ChangeThread() {
			input = new BufferedReader(new InputStreamReader(System.in));
		}

		public void run() {
			while (true) {
				try {
					String inputString = input.readLine();
					String[] changes = inputString.split(" ");
					for(int i = 1; i < changes.length; i+=2){
						if(neighbors.containsKey(Integer.parseInt(changes[i]))){
							neighbors.put(Integer.parseInt(changes[i]), new String[]{changes[i], changes[i+1]});
						}
						else{
							neighbors.put(Integer.parseInt(changes[i]), new String[]{changes[i], changes[i+1]});
						}
					}
					broadcast(false);
				} catch (IOException e) {
					System.out.println("Error interpreting input, please try again");
				}
			}//end infinite run loop
		}//end run method
	}//close inner class









}//close class
