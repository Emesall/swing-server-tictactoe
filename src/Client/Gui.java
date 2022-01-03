package Client;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

public class Gui extends JFrame {

	private JButton newGameButton;
	private JButton connectButton;
	private JButton gamesButton;
	private JPanel panel;
	private JButton buttons[][] = new JButton[3][3]; // 9 buttons as board
	private JLabel player_name; //display name of the player you play with
	private JLabel game_info; // show info about game
	private JList<String> games_list;
	private DefaultListModel<String> model;
	private JButton submit;
	private JTextField name_field;
	private JButton return_menu;
	

	public Gui() {
		super("TicTacToe");
		// menu screen
		panel = new JPanel();
		createComponents();
		displayWelcomeScreen();
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(500, 500);
		setLocationRelativeTo(null);
		setResizable(false);
		setVisible(true);
	}

	private void createComponents() {
		newGameButton = new JButton("Start new game");
		connectButton = new JButton("Connect to random game");
		gamesButton = new JButton("Find a game");
		return_menu = new JButton("Return to menu");
		submit = new JButton("Submit"); // submit player's name
		name_field = new JTextField(16); // field for player's name
		games_list = new JList<>(); // list of all available games
		model = new DefaultListModel<>();
		games_list.setModel(model);
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++)
				buttons[i][j] = new JButton();
		}
	}

	private void displayWelcomeScreen() {
		panel = new JPanel();
		Box box = new Box(BoxLayout.Y_AXIS);
		JLabel label = new JLabel("Please enter your name");
		box.add(label);
		box.add(Box.createRigidArea(new Dimension(10, 10)));
		box.add(name_field);
		box.add(Box.createRigidArea(new Dimension(20, 20)));
		box.add(submit);
		label.setAlignmentX(Box.CENTER_ALIGNMENT);
		name_field.setAlignmentX(Box.CENTER_ALIGNMENT);
		submit.setAlignmentX(Box.CENTER_ALIGNMENT);
		panel.add(box);
		getContentPane().add(panel);
		revalidate();
	}

	public void displayMenu() {
		getContentPane().remove(panel);
		panel = new JPanel();
		JLabel label = new JLabel("Welcome," + name_field.getText());
		Box box = new Box(BoxLayout.Y_AXIS);
		newGameButton.setMaximumSize(new Dimension(500, 500));
		gamesButton.setMaximumSize(new Dimension(500, 500));
		box.add(label);
		box.add(Box.createRigidArea(new Dimension(20, 20)));
		box.add(newGameButton);
		box.add(Box.createRigidArea(new Dimension(10, 10)));
		box.add(connectButton);
		box.add(Box.createRigidArea(new Dimension(10, 10)));
		box.add(gamesButton);
		label.setAlignmentX(Box.CENTER_ALIGNMENT);
		newGameButton.setAlignmentX(Box.CENTER_ALIGNMENT);
		connectButton.setAlignmentX(Box.CENTER_ALIGNMENT);
		gamesButton.setAlignmentX(Box.CENTER_ALIGNMENT);
		panel.add(box);
		getContentPane().add(panel);
		revalidate();

	}

	public void displayGamesScreen() {
		getContentPane().remove(panel);
		panel = new JPanel();
		Box box = new Box(BoxLayout.Y_AXIS);
		JScrollPane scroll = new JScrollPane(games_list);
		scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		JButton button = new JButton("Back");
		button.setAlignmentX(Box.CENTER_ALIGNMENT);
		scroll.setAlignmentX(Box.CENTER_ALIGNMENT);
		box.add(scroll);
		box.add(Box.createRigidArea(new Dimension(100, 100)));
		box.add(button);
		button.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				displayMenu();

			}
		});
		panel.add(box);
		getContentPane().add(panel);
		revalidate();
	}

	public void resetButtons() {
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++)
				buttons[i][j].setText(null);
		}
	}

	public void displayGame(String opponent_name) {
		getContentPane().remove(panel);
		panel = new JPanel();
		game_info = new JLabel(" ");
		player_name = new JLabel("You are playing with "+opponent_name);
		panel.setLayout(new GridLayout(3, 3));
		panel.setBackground(new Color(150, 150, 150));
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++)
				panel.add(buttons[i][j]);
		}

		this.add(panel, "Center");
		this.add(game_info, "South");
		this.add(player_name, "North");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setVisible(true);
		getContentPane().add(panel);
		revalidate();

	}

	public synchronized void displayWaitingScreen(String message) {
		Box box = new Box(BoxLayout.Y_AXIS);
		getContentPane().remove(panel);
		panel = new JPanel();
		JLabel label = new JLabel(message);
		label.setAlignmentX(Box.CENTER_ALIGNMENT);
		return_menu.setAlignmentX(Box.CENTER_ALIGNMENT);
		box.add(label);
		box.add(Box.createRigidArea(new Dimension(360, 360)));
		box.add(return_menu);
		panel.add(box);
		getContentPane().add(panel);
		revalidate();
	}

	public void updateList(String[] gamesID) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				model = new DefaultListModel<>();
				for (String game : gamesID) {
					model.addElement(game);
				}
				games_list.setModel(model);

			}
		});
	}

	public JButton[][] getButtons() {
		return buttons;
	}

	public void setButtons(JButton[][] buttons) {
		this.buttons = buttons;
	}

	public JPanel getPanel() {
		return panel;
	}

	public void setPanel(JPanel panel) {
		this.panel = panel;
	}

	public JButton getNewGameButton() {
		return newGameButton;
	}

	public void setNewGameButton(JButton newGameButton) {
		this.newGameButton = newGameButton;
	}

	public JButton getConnectButton() {
		return connectButton;
	}

	public void setConnectButton(JButton connectButton) {
		this.connectButton = connectButton;
	}

	public JLabel getGameInfo() {
		return game_info;
	}

	public void setGameInfo(JLabel label) {
		this.game_info = label;
	}

	public JButton getGamesButton() {
		return gamesButton;
	}

	public void setGamesButton(JButton gamesButton) {
		this.gamesButton = gamesButton;
	}

	public JList<String> getGames() {
		return games_list;
	}

	public void setGames(JList<String> games_list) {
		this.games_list = games_list;
	}

	public DefaultListModel<String> getModel() {
		return model;
	}

	public void setModel(DefaultListModel<String> model) {
		this.model = model;
	}

	public JButton getSubmit() {
		return submit;
	}

	public void setSubmit(JButton submit) {
		this.submit = submit;
	}

	public JTextField getName_field() {
		return name_field;
	}

	public void setName_field(JTextField name_field) {
		this.name_field = name_field;
	}

	public JButton getReturn_menu() {
		return return_menu;
	}

	public void setReturn_menu(JButton return_menu) {
		this.return_menu = return_menu;
	}

	public JLabel getPlayer_name() {
		return player_name;
	}

	public void setPlayer_name(JLabel player_name) {
		this.player_name = player_name;
	}

}
