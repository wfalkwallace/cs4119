import java.net.InetAddress;
import java.util.Scanner;

/**
 * 
 */

/**
 * @author wgf2104
 *
 */
public class Player {

	String name;
	String ipString;
	InetAddress ip;
	int port;
	
	int[] board;
	Scanner input;
	
	int playerNumber;
	
	
	public Player(String name, InetAddress ip, int port){
		this.name = name;
		this.ip = ip;
		this.ipString = ip.getHostAddress();
		this.port = port;
		
		board = new int[9];
		input = new Scanner(System.in);
		playerNumber = -1;
	}
	
	public int move(){
		int mv = -1;
		boolean valid = false;
		while(!valid){
		System.out.println("Where would you like to move (1-9)?");
		mv = input.nextInt();
		if(board[mv] == 0){
			board[mv] = playerNumber;
			valid = true;
		}
		else
			System.out.println("invalid move");
		}
		return mv;
	}
	
	public void inform(int mv){
		if(playerNumber == 1)
			board[mv] = 2;
		else if(playerNumber == 2)
			board[mv] = 1;
	}
	
	public void setPlayerNumber(int num){
		playerNumber = num;
	}
	
	public int getPlayerNumber(){
		return playerNumber;
	}
	
	public String getName(){
		return name;
	}
	
	public int getPort(){
		return port;
	}
	
	public String getIPString(){
		return ipString;
	}
	
	public InetAddress getIP(){
		return ip;
	}
}
