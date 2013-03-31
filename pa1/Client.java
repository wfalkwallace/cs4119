import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;


public class Client {

	private static final String receiverIP = "127.0.0.1";
	private static final int receiverPort = 4119;
	private static final int bufferSize = 1024;

	public static void main(String[] args) throws IOException {

		int packetID = 1;
		String name = null;

		// Create DatagramSocket
		DatagramSocket sendSocket = new DatagramSocket();
		DatagramSocket receiveSocket = new DatagramSocket();
		(new Thread((new Client()).new ReceiverThread(receiveSocket))).start();		

		// Begin to send
		BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
		byte[] sendBuffer = new byte[bufferSize];
		byte[] receiveBuffer = new byte[bufferSize];

		while (true) {


			/*
			 * client request
			 * 
			 * 
			 */
			String inputString = input.readLine();
			String[] command = inputString.split(" ");
			String payload = null;
			switch(command[0]){
			case "login": 
				name = command[1];
				payload = "login," + packetID + "," + name + "," + receiveSocket.getLocalPort();
				packetID++;
				break;
			case "ls": 
				payload = "list," + packetID + "," + name;
				packetID++;
				break;
			case "choose": 
				payload = "choose," + packetID + "," + name + "," + command[1];
				packetID++;
				break;
			case "accept": 
				payload = "ackchoose," + packetID + "," + name + "," + command[1] + ",A";
				packetID++;
				break;
			case "deny": 
				payload = "ackchoose," + packetID + "," + name + "," + command[1] + ",D";
				packetID++;
				break;
			case "play": 
				payload = "play," + packetID + "," + name + "," + command[1];
				packetID++;
				break;
			case "logout": 
				payload = "logout," + packetID + "," + name;
				packetID++;
				break;
			default: 
				//deal with invalid usage; also account for incorrect second args
				System.out.println("Usage: login;ls;choose;accept;deny;play;logout");
				break;
			}

			//testing
			//			System.out.println("TX: " + payload);


			/*
			 * ack loop
			 * 
			 * 
			 */
			boolean acked = false;
			while(!acked){
				sendBuffer = payload.getBytes();
				DatagramPacket requestPacket = new DatagramPacket(sendBuffer, sendBuffer.length, 
						InetAddress.getByName(receiverIP), receiverPort);
				sendSocket.send(requestPacket);
				//if time exceeds, break
				DatagramPacket responsePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
				sendSocket.receive(responsePacket);
				if(new String(receiveBuffer, 0, responsePacket.getLength()).split(",")[0].equals("ack"))
					acked = true;

			}
		}
	}

	public class ReceiverThread implements Runnable {

		DatagramSocket receiveSocket;

		public ReceiverThread(DatagramSocket receiveSocket) {
			this.receiveSocket = receiveSocket;
		}

		public void run() {
			byte[] receiveBuffer = new byte[bufferSize];
			while (true) {
				DatagramPacket ServerMessagePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
				try {
					receiveSocket.receive(ServerMessagePacket);
					String rcv = new String(receiveBuffer, 0, ServerMessagePacket.getLength());
					if(rcv.split(",")[0].equals("play")){
						String board = rcv.split(",")[1];
						System.out.println(" " + board.substring(0,1) + " || " + board.substring(1,2) + " || " + board.substring(2,3) + "\n" +
								"=============\n" +
								" " + board.substring(3,4) + " || " + board.substring(4,5) + " || " + board.substring(5,6) + "\n" +
								"=============\n" +
								" " + board.substring(6,7) + " || " + board.substring(7,8) + " || " + board.substring(8,9) + "\n");
					}
					else if(rcv.split(",")[0].equals("ackchoose")){
						if(rcv.split(",")[2].equals("A"))
							System.out.println("User Accepted");
						else if(rcv.split(",")[2].equals("D"))
							System.out.println("User Denied");
					}
					else if(rcv.split(",")[0].equals("ackls"))
						System.out.println(rcv.substring(6));
					else if(rcv.split(",")[0].equals("request"))
						System.out.println(rcv.split(",")[1] + " wants to play you");

					//					System.out.println("RX: " +  new String(receiveBuffer, 0, ServerMessagePacket.getLength()));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
