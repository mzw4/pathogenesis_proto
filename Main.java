import javax.swing.JFrame;

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
	}
}
