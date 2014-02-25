import java.awt.Color;
import java.awt.Graphics2D;


public class Pickup extends GameEntity {
	private final int SIZE = 10;
	
	public enum Type {
		PLASMID(Color.magenta), SPEED(Color.yellow), INFECT(Color.green), HEAL(Color.white);
		
		private Color color;
		
		Type(Color c) {
			this.color = c;
		}
	}
	private Type type;
	
	public Pickup(Type type) {
		this.type = type;
	}
	
	public Type getType() {
		return type;
	}
	
	public void draw(Graphics2D g2d) {
		g2d.setColor(type.color);
		g2d.fillRect(x, y, SIZE, SIZE);
	}
}
