import java.awt.Graphics2D;


public class Camera {
	private GameEntity following;
	
	private int center_x, center_y;
	
	public Camera() {}
	
	public void follow(GameEntity e) {
		following = e;
	}
	
	public void update() {
		center_x = following.x;
		center_y = following.y;
	}
	
	public void draw(Graphics2D g2d) {
		
	}
}
