import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;


public class Server {

	private static HashMap<String, Player> players;
	private static int receiverPort = 4119;
	private static int bufferSize = 1024;
	private static DatagramSocket serverSocket;
	private static ArrayList<TicTacToeGame> games;


	public static void main(String[] args) throws IOException {
		serverSocket = new DatagramSocket(receiverPort);
		byte[] receiveBuffer = new byte[bufferSize];
		byte[] ackPacketBuffer = new byte[bufferSize];
		byte[] ackCommandBuffer = new byte[bufferSize];
		players = new HashMap<String, Player>();
		games = new ArrayList<TicTacToeGame>();

		while (true) {


			/*
			 * client commands
			 * 
			 * 
			 */
			DatagramPacket requestPacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
			serverSocket.receive(requestPacket);
			String request = new String(receiveBuffer, 0, requestPacket.getLength());
			InetAddress clientIP = requestPacket.getAddress();
			int clientPort = requestPacket.getPort();
			System.out.println("RX: " + clientIP + "||" + String.valueOf(clientPort) + "||" +  request);
			boolean validLogin = true;
			boolean sendCommand = true;

			/*
			 * server response
			 * 
			 * 
			 */
			String[] command = request.split(",");
			String ackPacket = null;
			String ackCommand = null;
			switch(command[0]){
			case "login": 
				if(getPlayer(command[2]) != null){
					ackPacket = "ack," + command[1];
					ackCommand = "ackLogin,F";
					ackCommandBuffer = ackCommand.getBytes();
					DatagramPacket commandAck = new DatagramPacket(ackCommandBuffer, ackCommandBuffer.length, 
							clientIP, Integer.parseInt(command[3]));
					serverSocket.send(commandAck);
					System.out.println("TX: " + clientIP + "||" + clientPort + "||" + ackCommand);
					validLogin = false;
					break;
				}
				players.put(command[2], new Player(command[2], clientIP, Integer.parseInt(command[3])));
				ackPacket = "ack," + command[1];
				ackCommand = "ackLogin,S";
				break;
			case "list": 
				ackPacket = "ack," + command[1];
				ackCommand = "ackls";
				for(Player p: players.values()){
					ackCommand = ackCommand + "," + p.getName();
				}
				break;
			case "choose": 
				ackPacket = "ack," + command[1];
				ackCommand = "ackchoose," + command[3];
				if(request(getPlayer(command[3]), command[2])){
					ackCommand = ackCommand + ",A";
					games.add(new TicTacToeGame(getPlayer(command[3]), getPlayer(command[2])));
				}
				else
					ackCommand = ackCommand + ",D";
				break;

			case "play": 
				ackPacket = "ack," + command[1];
				TicTacToeGame game = getGame(command[2]);
				game.move(command[2], Integer.parseInt(command[3])-1);
				ackCommand = "play," + game.getBoardString();
				break;
			case "logout": 
				ackPacket = "ack," + command[1];
				sendCommand = false;
				players.remove(command[2]);
				break;
			default: 
				//deal with invalid usage; also account for incorrect second args
				System.out.println("Usage invalid");
				break;
			}			

			ackPacketBuffer = ackPacket.getBytes();
			DatagramPacket packetAck = new DatagramPacket(ackPacketBuffer, ackPacketBuffer.length, 
					clientIP, clientPort);
			serverSocket.send(packetAck);
			System.out.println("TX: " + clientIP + "||" + String.valueOf(clientPort) + "||" + ackPacket);

			if(validLogin && sendCommand){
				ackCommandBuffer = ackCommand.getBytes();
				DatagramPacket commandAck = new DatagramPacket(ackCommandBuffer, ackCommandBuffer.length, 
						clientIP, getPlayer(command[2]).getPort());
				serverSocket.send(commandAck);
				System.out.println("TX: " + clientIP + "||" + getPlayer(command[2]).getPort() + "||" + ackCommand);
			}
		}
	}

	private static Player getPlayer(String name){
		for(Player p: players.values()){
			if(p.getName().equals(name))
				return p;
		}
		return null;
	}

	private static boolean request(Player p, String name) throws IOException{
		byte[] requestBuffer = new byte[bufferSize];
		byte[] ackBuffer = new byte[bufferSize];
		byte[] responseBuffer = new byte[bufferSize];

		//formulate and send request to second player
		String request = "request," + name;
		requestBuffer = request.getBytes();
		DatagramPacket requestPacket = new DatagramPacket(requestBuffer, requestBuffer.length, 
				p.getIP(), p.getPort());
		serverSocket.send(requestPacket);
		System.out.println("TX: " + p.getIP() + "||" + p.getPort() + "||" + request);
		//receive request accept/deny
		DatagramPacket responsePacket = new DatagramPacket(responseBuffer, responseBuffer.length);
		serverSocket.receive(responsePacket);
		String response = new String(responseBuffer, 0, responsePacket.getLength());
		System.out.println("RX: " + responsePacket.getAddress().getHostAddress() + "||" + responsePacket.getPort() + "||" + response);
		//ack accept/deny
		String ack = "ack," + response.split(",")[1];
		ackBuffer = ack.getBytes();
		DatagramPacket ackPacket = new DatagramPacket(ackBuffer, ackBuffer.length, 
				responsePacket.getAddress(), responsePacket.getPort());
		serverSocket.send(ackPacket);
		System.out.println("TX: " + ackPacket.getAddress().getHostAddress() + "||" + ackPacket.getPort() + "||" + ack);


		if(response.split(",")[4].equals("A")){
			return true;
		}
		return false;
	}

	private static TicTacToeGame getGame(String name){
		for(TicTacToeGame g: games){
			if(g.getPlayers().contains(name))
				return g;
		}
		return null;
	}

}
