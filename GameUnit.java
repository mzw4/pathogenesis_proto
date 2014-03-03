import java.awt.Color;
import java.awt.Graphics2D;
import java.util.HashSet;
import java.util.Timer;
import java.util.TimerTask;

public class GameUnit extends GameEntity {	
	public static int SIZE = 20;
	protected int ATTACK_RANGE = 50;
	protected int CHASE_RANGE = 200;
	protected int ATTACK_COOLDOWN = 20;
	protected final int ALLY_SAFE_RANGE = 200;

	protected int MAX_HEALTH = 100;
	protected int MAX_SPEED = 15;
	protected int speed = 3;
	protected int velx, vely;
	protected int targetX = -1, targetY = -1;
	
	private Timer timer;
	
	public enum Faction {
		PLAYER, ALLY, ENEMY;
		
		public Faction opposite() {
			if(this == ENEMY) {
				return ALLY;
			} else {
				return ENEMY;
			}
		}
	}
	protected Faction faction;
	
	private boolean safe;
	protected int health;
	
	protected int attack_cooldown = 0;
	
	public GameUnit(Faction faction) {
		this.faction = faction;
		this.health = MAX_HEALTH;
		this.alive = true;
		this.velx = 0;
		this.vely = 0;
		
		timer = new Timer();
		timer.schedule(new AllyAttrition(), 1000, 1000);
	}
	
	public void move(Game.Direction d) {
		switch(d) {
		case NORTH:
			vely -= speed;
			break;
		case SOUTH:
			vely += speed;
			break;
		case EAST:
			velx += speed;
			break;
		case WEST:
			velx -= speed;
			break;
		}
		// Clamp speeds to MAX_SPEED
		velx = Math.min(Math.max(velx, -MAX_SPEED), MAX_SPEED);
		vely = Math.min(Math.max(vely, -MAX_SPEED), MAX_SPEED);
	}
	
	public void setTarget(int targetX, int targetY) {
		this.targetX = targetX;
		this.targetY = targetY;
	}
	
	public boolean hasTarget() {
		return targetX > -1 && targetY > -1;
	}
	
	public void makeMove() {
		if(!hasTarget()) {
			return;
		}
		if(Math.abs(targetY-y) < 20) {
			if(targetY-y > 0) vely++;
			else if(targetY-y < 0) vely--;
		}
		if(Math.abs(targetX-x) < 20) {
			if(targetX-x > 0) velx++;
			else if(targetX-x < 0) velx--;
		}
		
		if(targetY > 0 && Math.abs(targetY-y) > 20) {
			if(targetY < y) {
				move(Game.Direction.NORTH);
			} else if (targetY > y) {
				move(Game.Direction.SOUTH);
			}
		}
		if(targetX > 0 && Math.abs(targetX-x) > 20) {
			if(targetX > x) {
				move(Game.Direction.EAST);
			} else if (targetX < x) {
				move(Game.Direction.WEST);
			}
		}
	}
	
	public void attack(GameUnit e) {
		if(attack_cooldown <= 0) {
			e.health -= 5;
			attack_cooldown = ATTACK_COOLDOWN;
		}
	}
	
	/*
	 * Update function
	 */
	public void update(HashSet<GameUnit> entities, Player player, Map map) {
		// default actions
		if(faction == GameUnit.Faction.ALLY) {
			MAX_SPEED = 25;
			speed = 7;
			safe = distance(player) < ALLY_SAFE_RANGE;
			setTarget(player.x + player.velx * 3, player.y + player.vely * 3);
		}
		if(faction == GameUnit.Faction.ENEMY) {
			MAX_SPEED = 10;
			speed = 2;
			if(inRange(player, CHASE_RANGE)) {
				setTarget(player.x, player.y);
				if(inRange(player, ATTACK_RANGE)) {
					attack(player);
				}
			} else if(Math.random() < 0.05) {
				setTarget((int)(Math.random() * Game.getW()), (int)(Math.random() * Game.getH()));
			}
		}
		
		// handle targetting and attacking
		if(this != player && !player.getRally()) {
			for(GameUnit e: entities) {
				if(e.faction == faction.opposite() && inRange(e, ATTACK_RANGE)) {
					setTarget(e.x, e.y);
					attack(e);
				}
			}
		}
		attack_cooldown--;
		attack_cooldown = Math.max(0, attack_cooldown);

		// check health
		if(health <= 0) {
			this.alive = false;
		}
		
		// update velocity
		if(map.canMoveTo(x + velx, y + vely)) {
			x += velx;
			y += vely;
		} else {
			velx = 0;
			vely = 0;
		}
		
		if(velx < 0) velx++;
		else if(velx > 0) velx--;
		if(vely < 0) vely++;
		else if(vely > 0) vely--;
		
		// make next move
		makeMove();
	}
	
	public void draw(Graphics2D g2d, float delta) {
		if(faction == Faction.PLAYER) {
			g2d.setColor(Color.green);
		} else if(faction == Faction.ALLY){
			g2d.setColor(Color.blue);
		} else {
			g2d.setColor(Color.red);
		}
		g2d.fillOval(screen_x, screen_y, 20, 20);

		//g2d.setColor(Color.black);
		//g2d.drawString(health + "/" + MAX_HEALTH, x-(velx * (1-delta))-20, y -(int)(vely * (1-delta))- 20);
		
		g2d.setColor(Color.green);
		g2d.fillRect(screen_x-10, screen_y -10, 45 * health/MAX_HEALTH, 5);

		g2d.setColor(Color.white);
		if(faction != Faction.PLAYER) {
			g2d.drawString("Target: " + targetX + ", " + targetY, 500, 20);
			g2d.drawString("Vel: " + velx + ", " + vely, 500, 50);
		}
	}
	
	public void calculateScreenPos(GameUnit following, float delta) {
		screen_x = x - (following.x - GUI.getW()/2) + 
				(int)(((following == null? 0: following.velx) -velx) * (1-delta)) -10;
		screen_y = y - (following.y - GUI.getH()/2) +
				(int)(((following == null? 0: following.vely) -vely) * (1-delta)) -10;
	}
	
	public int getX() {
		return x;
	}
	public int getY() {
		return y;
	}
	public int getVelX() {
		return velx;
	}
	public int getVelY() {
		return vely;
	}
	
	public class AllyAttrition extends TimerTask {
		@Override
		public void run() {
			if(faction == Faction.ALLY && !safe) {
				health -= 5;
			}
		}
	}
}
