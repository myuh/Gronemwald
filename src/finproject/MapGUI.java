package finproject;

// >>>>>>> origin/master

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

public class MapGUI implements ActionListener {
		int state = 0; // holds state of application
		JFrame mapFrame;
		JPanel welcomePanel, titlePanel, mapPanel, optionsPanel;
		JButton addVillage, delVillage, placeGnome, moveGnome, addRoad, welcomeButton;
	
	public MapGUI() { // builds the window
		if (state == 0) {
			mapFrame = new JFrame("Gnomenwald");
			mapFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			mapFrame.setSize(600, 400);
			mapFrame.setMinimumSize(new Dimension(800, 500));
			
			welcomePanel = new JPanel();
			welcomePanel.setPreferredSize(new Dimension (600,400));
			welcomePanel.setBackground(Color.GRAY);
			
			addWelcome();
			
			mapFrame.getContentPane().add(welcomePanel, BorderLayout.CENTER);
			mapFrame.pack();
			mapFrame.setVisible(true); }
		else if (state == 1) {
			//mapFrame = new JFrame("Gnomenwald");
			//mapFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			//mapFrame.setSize(600, 400);
			//mapFrame.setMinimumSize(new Dimension(800, 500));
			titlePanel = new JPanel();
			titlePanel.setPreferredSize(new Dimension(800, 50));
			titlePanel.setBackground(Color.DARK_GRAY);
			mapPanel = new JPanel();
			mapPanel.setPreferredSize(new Dimension(650, 450));
			mapPanel.setBackground(Color.RED);
			optionsPanel = new JPanel();
			optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
			optionsPanel.setPreferredSize(new Dimension(150, 450));
			optionsPanel.setBackground(Color.BLUE);
			addTitle();
			addGraph();
			addOptions();
			mapFrame.getContentPane().add(titlePanel, BorderLayout.NORTH);
			mapFrame.getContentPane().add(mapPanel, BorderLayout.CENTER);
			mapFrame.getContentPane().add(optionsPanel, BorderLayout.EAST);
			mapFrame.pack();
			mapFrame.setVisible(true);
		}
	} // end of constructor
	
	public void addWelcome() {
		JLabel welcomeLabel = new JLabel("Welcome to Gnomenwald!", SwingConstants.CENTER);
		welcomeButton = new JButton("Click to start");
		welcomeButton.addActionListener(this);
		
		welcomePanel.add(welcomeLabel);
		welcomePanel.add(welcomeButton);
	} // end of addWelcome()
	
	public void addTitle() {
		JLabel title = new JLabel("Gnomenwald", SwingConstants.CENTER);
		title.setForeground(Color.WHITE);
		titlePanel.add(title);
	} // end of addTitle()
	
	public void addGraph() {

	} // end of addGraph()
	
	public void addOptions() {
		addVillage = new JButton("Add village");
		delVillage = new JButton("Delete village");
		placeGnome = new JButton("Place gnome");
		moveGnome = new JButton("Move gnome");
		addRoad = new JButton("Add road");
		
		optionsPanel.add(Box.createRigidArea(new Dimension(0,5)));
		addOptionsButton(addVillage); optionsPanel.add(Box.createRigidArea(new Dimension(0,5)));
		addOptionsButton(delVillage); optionsPanel.add(Box.createRigidArea(new Dimension(0,5)));
		addOptionsButton(placeGnome); optionsPanel.add(Box.createRigidArea(new Dimension(0,5)));
		addOptionsButton(moveGnome); optionsPanel.add(Box.createRigidArea(new Dimension(0,5)));
		addOptionsButton(addRoad); optionsPanel.add(Box.createRigidArea(new Dimension(0,5)));
	} // end of addOptions()
	
	public void addOptionsButton(JButton button) {
		button.addActionListener(this);
		button.setAlignmentX(Component.CENTER_ALIGNMENT);
		optionsPanel.add(button);
	} // end of addOptionsButton()
	
	public void actionPerformed(ActionEvent e) {  
        if (e.getSource() == addVillage) {
        	addVillage();
        } else if (e.getSource() == delVillage) {
        	delVillage();
        } else if (e.getSource() == placeGnome) {
        	placeGnome();
        } else if (e.getSource() == moveGnome) {
        	moveGnome();
        } else if (e.getSource() == addRoad) {
        	addRoad();
        } else if (e.getSource() == welcomeButton) {
        	welcomeButton();
        }
    } // end of actionPerformed() 
	
	public void addVillage() {
		// TODO
	}
	
	public void delVillage() {
		
	} // TODO
	
	public void placeGnome() {
		
	} // TODO
	
	public void moveGnome() {
		
	} // TODO
	
	public void addRoad() {
		
	} // TODO
	
	public void welcomeButton() {
		welcomePanel.setVisible(false);
		state = GUIConstants.STATE_ACTIVE;
	}
	
	public static void createAndShowGUI() {
		JFrame.setDefaultLookAndFeelDecorated(true);
		MapGUI map = new MapGUI();
	} // end of createAndShowGUI()
	
	public static void main (String [] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() { createAndShowGUI(); }
		});
	} // end of main()
	
} // end of MapGUI()
// >>>>>>> origin/master
