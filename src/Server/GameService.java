package Server;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.UUID;

import Exception.NotFoundGameException;

public class GameService {
	private ArrayList<Player> players; // list of all connected clients to the server

	public GameService() {
		createServer();
	}

	// inner class to handle clients (new thread for every client)
	public class HandleClients implements Runnable {

		private Player player;

		public HandleClients(Player player) {
			try {
				this.player = player;

			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		@Override
		public void run() {
			Object o = null;
			String message = "";
			try {
				while ((o = player.getReader().readObject()) != null) {
					message = (String) o;
					communicateWithClient(player, message);

				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}

		}

	}

	// prepare server connection, accept clients connection
	public void createServer() {
		players = new ArrayList<>();
		try {
			ServerSocket serv = new ServerSocket(5000);
			while (true) {
				Socket socket = serv.accept();
				ObjectOutputStream writer = new ObjectOutputStream(socket.getOutputStream());
				ObjectInputStream reader = new ObjectInputStream(socket.getInputStream());
				Player player = new Player(writer, reader);
				players.add(player);
				Thread th1 = new Thread(new HandleClients(player));
				th1.start();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	// method to communicate with clients
	private synchronized void communicateWithClient(Player player, String message) {

		switch (message) {
		case "Name":
			try {
				String name = (String) player.getReader().readObject();
				boolean nok = false;
				for (Player pl : players) {
					if (pl.getName() != null) {
						if (pl.getName().equals(name)) {
							player.getWriter().writeObject("NameNok");
							player.getWriter().flush();
							nok = true;
						}
					}
				}
				if (!nok) {
					player.setName(name);
					player.getWriter().writeObject("NameOk");
					player.getWriter().flush();
				}
				updateGames();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			break;
		case "NewGame":

			try {
				String gameID = createNewGame(player);
				if (gameID != null) {
					player.getWriter().writeObject("GameCreated");
					player.getWriter().writeObject(gameID);
					player.getWriter().flush();
				}
				updateGames();
			} catch (Exception ex) {
				ex.printStackTrace();
			}

			break;

		case "GameQuit":
			try {
				String gameID = (String) player.getReader().readObject();
				if (GameStorage.getInstance().getGames().get(gameID) != null) {
					GameStorage.getInstance().getGames().remove(gameID);
				}

				updateGames();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			break;
		case "Connect":
			try {
				String gameID;
				if ((gameID = connectToRandomGame(player)) != "") { // checks if connected properly
					sendConnectionOk(gameID);
				} else {
					player.getWriter().writeObject("ConnectNok");
					player.getWriter().flush();
				}
				updateGames();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			break;

		case "Play":

			try {
				String gameID = (String) player.getReader().readObject();
				String message2 = (String) player.getReader().readObject();
				int x = Integer.valueOf(message2.split(",")[0]); // get coordinate x to put X or O
				int y = Integer.valueOf(message2.split(",")[1]); // get coordinate y to put X or O
				if (processMove(player, gameID, x, y)) {
					TicTacToe game = GameStorage.getInstance().getGames().get(gameID);
					// check who is the second player in game
					Player player2;
					if (player == game.getPlayer1()) {
						player2 = game.getPlayer2();
					} else {
						player2 = game.getPlayer1();
					}

					// send message to player that made a move
					sendMove(player, gameID, player.getTyp(), message2);
					// send message to second player about move to update gui
					sendMove(player2, gameID, player.getTyp(), message2);

					checkGameStatus(player, game); // checks if there is a winner or tie

				}

			} catch (Exception ex) {
				ex.printStackTrace();
			}
			break;
		case "JoinGame":
			try {
				String mess = (String) player.getReader().readObject();
				String gameID = mess.split(" ")[1];
				if (connectToGame(player, gameID)) { // checks if connected properly
					sendConnectionOk(gameID);
				} else {
					player.getWriter().writeObject("ConnectNok");
					player.getWriter().flush();
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			updateGames();
			break;

		}

	}

	// method to check a winner, change to one dimensional array (0-8 fields as
	// presented below) and compare with all the win possibilities
	// 0 1 2
	// 3 4 5
	// 6 7 8
	public boolean checkWinner(Player player, String[][] board) {

		int[][] winComb = { { 0, 1, 2 }, { 3, 4, 5 }, { 6, 7, 8 }, { 0, 3, 6 }, { 1, 4, 7 }, { 2, 5, 8 }, { 0, 4, 8 },
				{ 2, 4, 6 } };
		String type = player.getTyp().toString();
		String[] one_board = new String[9];
		int counter = 0;
		for (int i = 0; i < board.length; i++) {
			for (int j = 0; j < board[0].length; j++) {
				one_board[counter] = board[i][j];
				counter++;
			}
		}
		
		for (int i = 0; i < winComb.length; i++) {
			counter = 0;
			for (int j = 0; j < winComb[0].length; j++) {
				int field = winComb[i][j];
				if (one_board[field] != null) {
					if (one_board[field].equals(type)) {
						counter++;
					}
				}
			}
			if (counter == 3) {
				return true;
			}
		}

		return false;
	}

	

	// if board is full and there is no winner, it's tie
	private boolean checkTie(String[][] board) {
		for (int i = 0; i < board.length; i++) {
			for (int j = 0; j < board[0].length; j++) {
				if (board[i][j] == null) {
					return false;
				}
			}
		}

		return true;
	}

	// send accepted move:player,gameId,typ,place
	private  synchronized void sendMove(Player player, String gameId, Typ type, String message) {
		try {
			player.getWriter().writeObject("Play");
			player.getWriter().writeObject(gameId);
			player.getWriter().writeObject(type.toString());
			player.getWriter().writeObject(message);
			player.getWriter().flush();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	// create new game.Return gameID
	private String createNewGame(Player player1) {
		TicTacToe game = new TicTacToe();
		String id = UUID.randomUUID().toString();
		String[][] board = new String[3][3];
		player1.setTyp(Typ.X);
		game.setId(id);
		game.setBoard(board);
		game.setPlayer1(player1);
		game.setStatus(GameStatus.NEW);
		game.setTurn(Typ.X);
		GameStorage.getInstance().addGame(game);
		System.out.println("Game created by" + player1.getName() + " ID: " + game.getId());
		return game.getId();
	}

	// handle connection to random game, return gameId if connected
	private String connectToRandomGame(Player player2) {
		String gameID = "";
		for (TicTacToe game : GameStorage.getInstance().getGames().values()) {
			if (game.getStatus() == GameStatus.NEW) {
				game.setStatus(GameStatus.ON);
				game.setPlayer2(player2);
				player2.setTyp(Typ.O);
				gameID = game.getId();
				System.out.println(player2.getName() + " connected to " + game.getId());

				return gameID;
			}
		}
		return gameID;
	}

	private boolean connectToGame(Player player2, String gameID) throws NotFoundGameException {
		if (!GameStorage.getInstance().getGames().containsKey(gameID)) {
			throw new NotFoundGameException("Game with this ID doesn't exist");
		}
		TicTacToe game = GameStorage.getInstance().getGames().get(gameID);
		if (game.getStatus() == GameStatus.NEW) {
			game.setStatus(GameStatus.ON);
			game.setPlayer2(player2);
			player2.setTyp(Typ.O);
			gameID = game.getId();
			System.out.println(player2.getName() + " connected to " + game.getId());
			return true;
		}

		return false;
	}
	//send message to the players that connection was ok and game is about to start
	private synchronized void sendConnectionOk(String gameID) {
		TicTacToe game=GameStorage.getInstance().getGames().get(gameID);
		
		try {
			//send message to player1
			game.getPlayer1().getWriter().writeObject("ConnectOk");
			game.getPlayer1().getWriter().writeObject(gameID);
			game.getPlayer1().getWriter().writeObject(game.getPlayer2().getName());
			game.getPlayer1().getWriter().flush();
			//send message to player2
			game.getPlayer2().getWriter().writeObject("ConnectOk");
			game.getPlayer2().getWriter().writeObject(gameID);
			game.getPlayer2().getWriter().writeObject(game.getPlayer1().getName());
			game.getPlayer2().getWriter().flush();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		sendMessage(game.getPlayer1(), "Message", "It's your turn to move");
		sendMessage(game.getPlayer2(), "Message", "Wait for opponent to move");
	}

	// return true if move accepted
	private boolean processMove(Player player, String gameID, int x, int y) throws NotFoundGameException {

		if (!GameStorage.getInstance().getGames().containsKey(gameID)) {
			throw new NotFoundGameException("Game with this ID doesn't exist");
		}
		TicTacToe game = GameStorage.getInstance().getGames().get(gameID);
		if (player.getTyp() == game.getTurn()) { // checks if it's player's turn
			if (isMoveLegal(game.getBoard(), x, y)) { // checks if specific field is unoccupied
				game.getBoard()[x][y] = player.getTyp().toString();
				game.flipTurn();
				return true;
			}
		}

		return false;

	}

	private void checkGameStatus(Player player, TicTacToe game) {
		Player player2;
		if (player == game.getPlayer1()) {
			player2 = game.getPlayer2();
		} else {
			player2 = game.getPlayer1();
		}
		// check winner
		if (checkWinner(player, game.getBoard())) {
			game.setTurn(null);
			game.setWinner(player);
			game.setStatus(GameStatus.FINISHED);
			sendMessage(player, "Winner", "Congratulations!!! You won.");
			sendMessage(player2, "Winner", "You lost! Try harder next time");

			// check tie
		} else if (checkTie(game.getBoard())) {
			game.setTurn(null);
			game.setStatus(GameStatus.FINISHED);
			sendMessage(player, "Winner", "Tie!");
			sendMessage(player2, "Winner", "Tie!");
		} else {
			sendMessage(player, "Message", "Wait for opponent to move");
			sendMessage(player2, "Message", "It's your turn to move");
		}
	}

	// send info about game status to the player
	private synchronized void sendMessage(Player player, String message1, String message2) {
		try {
			player.getWriter().writeObject(message1);
			player.getWriter().writeObject(message2);
			player.getWriter().flush();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	// checks if the field is free
	private boolean isMoveLegal(String[][] board, int x, int y) {

		if (board[x][y] == null)
			return true;

		return false;
	}
	// game added, deleted or started , update view of all players

	private synchronized void updateGames() {
		ArrayList<String> gamesID = new ArrayList<>();
		for (TicTacToe game : GameStorage.getInstance().getGames().values()) {
			if (game.getStatus() == GameStatus.NEW) {
				gamesID.add(game.getPlayer1().getName() + ": " + game.getId());
			}
		}
		String[] games = gamesID.toArray(new String[0]);
		for (Player player : players) {
			try {
				player.getWriter().writeObject("UpdateGames");
				player.getWriter().writeObject(games);
				player.getWriter().flush();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

	}

	public static void main(String[] args) {
		new GameService();

	}
}
