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
	
	public void calculateScreenPos(GameUnit following, float delta) {
		screen_x = x - (following.x - GUI.getW()/2) + 
				(int)(((following == null? 0: following.velx)) * (1-delta)) -10;
		screen_y = y - (following.y - GUI.getH()/2) +
				(int)(((following == null? 0: following.vely)) * (1-delta)) -10;
	}
	
	public void draw(Graphics2D g2d) {
		g2d.setColor(type.color);
		g2d.fillRect(screen_x, screen_y, SIZE, SIZE);
	}
}
