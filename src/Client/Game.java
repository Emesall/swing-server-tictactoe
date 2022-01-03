package Client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import javax.swing.JOptionPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class Game {

	private ObjectInputStream reader;
	private ObjectOutputStream writer;
	private Socket socket;
	private Gui gui;
	private String gameId;
	private String name;
	private boolean connected; // true if player is connected to server

	public Game() {
		gui = new Gui();
		// add listeners to buttons
		addListeners();

	}

	// listener for "Submit" Button
	public class SubmitListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {

			String login = gui.getName_field().getText();
			if (isValid(login)) {// check if login is valid
				name = login;
				if (!connected) {
					start_conn(); // connect to server
					start_thread();
				}
				sendName(name); // send name to server

			} else if (login.isBlank()) {
				JOptionPane.showMessageDialog(gui.getPanel(), "You have to enter a name first");
			} else {
				JOptionPane.showMessageDialog(gui.getPanel(), "Login invalid. Min. 5 characters, no whitespaces");
			}

		}

		// checks if player's name is valid. No whitespaces,more than 5 characters
		private boolean isValid(String login) {
			if (!login.isBlank() && !login.contains(" ") && login.length() >= 5) {
				return true;
			}

			return false;
		}

	}

	// listener for "NewGame" Button
	public class NewGameListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			String message = "NewGame";
			sendToServer(message);
			gui.displayWaitingScreen("Waiting for the server response...");

		}

	}

	// listener for return_menu Button
	public class ReturnListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				writer.writeObject("GameQuit");
				writer.writeObject(gameId);
				writer.flush();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			gui.displayMenu();

		}

	}

	// listener for "Connect" Button
	public class ConnectListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			String message = "Connect";
			sendToServer(message);

		}

	}

	// listener for "Games" Button
	public class GamesListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			gui.displayGamesScreen();

		}

	}

	// listener for games selecting
	public class ListGamesListener implements ListSelectionListener {

		public void valueChanged(ListSelectionEvent ev) {

			String s = gui.getGames().getSelectedValue();
			System.out.println(s);
			if (!ev.getValueIsAdjusting() && s != null) {

				try {
					writer.writeObject("JoinGame");
					writer.writeObject(s);
					writer.flush();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

		}

	}

	// listener for board buttons
	public class ButtonListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			String message = "Play";
			for (int i = 0; i < 3; i++) {
				for (int j = 0; j < 3; j++)
					if (e.getSource() == gui.getButtons()[i][j]) {
						String place = String.valueOf(i) + "," + String.valueOf(j); // format of place on the board:
																					// row,column
						try {
							writer.writeObject(message);
							writer.writeObject(gameId);
							writer.writeObject(place);
							writer.flush();
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
			}

		}

	}

	// assign all listeners to buttons
	private void addListeners() {
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++)
				gui.getButtons()[i][j].addActionListener(new ButtonListener());
		}
		gui.getNewGameButton().addActionListener(new NewGameListener());
		gui.getConnectButton().addActionListener(new ConnectListener());
		gui.getGamesButton().addActionListener(new GamesListener());
		gui.getGames().addListSelectionListener(new ListGamesListener());
		gui.getSubmit().addActionListener(new SubmitListener());
		gui.getReturn_menu().addActionListener(new ReturnListener());
	}

	// send message to the server
	private void sendToServer(String message) {
		try {
			writer.writeObject(message);
			writer.flush();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	// send client name to Server
	private void sendName(String name) {
		try {
			writer.writeObject("Name");
			writer.writeObject(name);
			writer.flush();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	// start connection with the server
	private void start_conn() {
		try {
			socket = new Socket("127.0.0.1", 5000);
			writer = new ObjectOutputStream(socket.getOutputStream());
			reader = new ObjectInputStream(socket.getInputStream());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		connected = true;
	}

	// start new thread to read new messages from server
	private void start_thread() {
		Thread th = new Thread(new readFromServer());
		th.start();
	}

	// read messages from the server
	public class readFromServer implements Runnable {

		@Override
		public void run() {
			Object ob1 = null;
			String message1 = "";
			String message2 = "";
			try {
				while ((ob1 = reader.readObject()) != null) {
					message1 = (String) ob1;
					switch (message1) {
					case "NameOk": // name was properly sent and saved in to the server
						gui.displayMenu();
						break;
					case "NameNok": // name taken
						JOptionPane.showMessageDialog(gui.getPanel(), "This name is taken by someone else. Try again");
						break;
					case "GameCreated":
						gameId = (String) reader.readObject();
						gui.displayWaitingScreen("Game created....Waiting for another player to join");
						break;
					case "ConnectOk":
						gameId = (String) reader.readObject();
						String opponent_name = (String) reader.readObject();
						gui.displayGame(opponent_name);
						break;
					case "ConnectNok":
						JOptionPane.showMessageDialog(gui.getPanel(),
								"No game found. Create game by yourself or try later");
						gui.displayMenu();
						break;
					case "Play":
						gameId = (String) reader.readObject();
						String typ = (String) reader.readObject();
						message2 = (String) reader.readObject();
						int x = Integer.valueOf(message2.split(",")[0]); // get coordinate x to put X or O
						int y = Integer.valueOf(message2.split(",")[1]); // get coordinate y to put X or O
						gui.getButtons()[x][y].setText(String.valueOf(typ));
						break;
					case "Winner":
						message2 = (String) reader.readObject();
						JOptionPane.showMessageDialog(gui.getPanel(), message2);
						gui.getGameInfo().setText("");
						gui.getPlayer_name().setText("");
						gui.displayMenu();
						gui.resetButtons();
						break;
					case "Message":
						message2 = (String) reader.readObject();
						gui.getGameInfo().setText(message2);
						break;
					case "UpdateGames":
						String[] gamesID = (String[]) reader.readObject();
						gui.updateList(gamesID);
						break;
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}

		}

	}


	public static void main(String[] args) {
		new Game();

	}
}
