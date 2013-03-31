/**
 * 
 */

/**
 * @author wgf2104
 *
 */
public class TicTacToeGame {

	Player p1;
	Player p2;
	int[] board;


	public TicTacToeGame(Player p1, Player p2){
		this.p1 = p1;
		p1.setPlayerNumber(1);
		this.p2 = p2;
		p2.setPlayerNumber(2);
		board = new int[9];
	}

	public void move(String player, int position){
		if(player.equals(p1.getName()))
		{
			board[position] = 1;
		}
		else
		{
			board[position] = 2;
		}
	}

	public boolean checkEnd(){
		boolean isOver = true;
		for(int i: board)
			if(i==0)
				isOver = false;
		return isOver;
	}

	public void printBoard(){
		System.out.println(" board[0] || board[1] || board[2]\n" +
				" =============\n" +
				" board[3] || board[4] || board[5]\n" +
				" =============\n" +
				" board[6] || board[7] || board[8]\n");
	}

	public String getBoardString(){
		String boardState = "";
		for(int i: board)
			boardState += i;
		return boardState;
	}
	
	public String getPlayers(){
		return p1.getName() + p2.getName();
	}
}
