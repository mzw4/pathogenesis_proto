import java.awt.BorderLayout;
import java.awt.Graphics;

import javax.swing.JPanel;


public class GUI extends JPanel {
	private final static int pHeight = 720;	// panel pixel h
	private final static int pWidth = 1024; // panel pixel w
	
	private Game game;
	
	public GUI() {
		super(new BorderLayout()); // should double buffer?
		game = new Game(pWidth, pHeight);
		add(game);
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		game.repaint();
	}
	
	public static int getW() {
		return pWidth;
	}
	
	public static int getH() {
		return pHeight;
	}
}
