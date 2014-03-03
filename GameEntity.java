
public class GameEntity {
	protected int x, y;
	protected int screen_x, screen_y;
	protected boolean alive;

	public GameEntity() {
		alive = true;
	}
	
	public void placeAt(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public boolean inRange(GameEntity other, int range) {
		return distance(other) <= range;
	}
	
	public double distance(GameEntity other) {
		return Math.sqrt(Math.pow(Math.abs(other.x - x), 2) + Math.pow(Math.abs(other.y - y), 2));
	}
}