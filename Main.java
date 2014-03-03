import javax.swing.JFrame;

import org.jbox2d.dynamics.Body;

public class Main extends JFrame {
	public Main() {
		super("Pathogenesis");
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		add(new GUI());
		pack();
		setVisible(true);
	}
	
	public static void main(String[] args) {
		new Main();
		
		Joint j
	}
}
