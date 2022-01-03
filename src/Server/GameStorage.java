package Server;

import java.util.HashMap;


public class GameStorage {
	private HashMap<String,TicTacToe> games;
	private static GameStorage instance;
	
	private GameStorage() {
		games=new HashMap<>();
	}
	public HashMap<String, TicTacToe> getGames() {
		return games;
	}

	public void addGame(TicTacToe game) {
		games.put(game.getId(), game);
	}
	
	public static synchronized GameStorage getInstance()
	{
		if (instance==null) {
			instance=new GameStorage();
		}
		return instance;
	}
	
}
