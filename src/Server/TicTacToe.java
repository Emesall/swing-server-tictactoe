package Server;

public class TicTacToe {
	private String Id;
	private Player player1;
	private Player player2;
	private String[][] board;
	private GameStatus status;
	private Player winner;
	private Typ turn; //which player (X or O) is expected to move next


	public Player getPlayer1() {
		return player1;
	}

	public void setPlayer1(Player player1) {
		this.player1 = player1;
	}

	public Player getPlayer2() {
		return player2;
	}

	public void setPlayer2(Player player2) {
		this.player2 = player2;
	}

	public String[][] getBoard() {
		return board;
	}

	public void setBoard(String[][] board) {
		this.board = board;
	}

	public GameStatus getStatus() {
		return status;
	}

	public void setStatus(GameStatus status) {
		this.status = status;
	}

	public String getId() {
		return Id;
	}

	public void setId(String id) {
		Id = id;
	}

	public Player getWinner() {
		return winner;
	}

	public void setWinner(Player winner) {
		this.winner = winner;
	}

	public Typ getTurn() {
		return turn;
	}

	public void setTurn(Typ turn) {
		this.turn = turn;
	}
	//change turn to the opposite 
	public void flipTurn() {
		if(turn==Typ.O) {
			turn=Typ.X;
		}
		else {
			turn=Typ.O;
		}
	}
	@Override
	
	public String toString() {

		StringBuilder s = new StringBuilder();
		s.append("ID: "+Id+"\n");
		s.append("BOARD:\n");
		for (int i = 0; i < board.length; i++) {
			for (int j = 0; j < board[0].length; j++) {
				s.append(board[i][j] + " ");
			}
			s.append("\n");
		}
		return s.toString();
	}
	
	

}
