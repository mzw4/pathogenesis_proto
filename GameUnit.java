import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Timer;
import java.util.TimerTask;

import org.newdawn.slick.util.pathfinding.AStarPathFinder;
import org.newdawn.slick.util.pathfinding.Path;
import org.newdawn.slick.util.pathfinding.heuristics.ClosestHeuristic;

public class GameUnit extends GameEntity {	
	public static int SIZE = 20;
	protected int ATTACK_RANGE = 40;
	protected int CHASE_RANGE = 300;
	protected int ATTACK_COOLDOWN = 20;
	protected int ATTACK_OPACITY = 150;
	protected final int ALLY_SAFE_RANGE = 200;
	protected final int ALLY_MOVE_DIST = 7;
	
	protected final int MAX_SEARCH_DIST = 1000;
	protected final float BOUNCE_DAMPENING_FACTOR = 0.5f;
	
	protected int MAX_HEALTH = 100;
	protected int MAX_SPEED = 15;
	protected int speed = 3;
	protected int velx, vely;
	protected int targetX = -1, targetY = -1;
	protected int nextX = -1, nextY = -1;
	
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
	
	private int attack_fade;
	protected int attack_cooldown = 0;
	
	public GameUnit(BufferedImage image, Faction faction) {
		this.image = image;
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
	
	public boolean hasNext() {
		return nextX > -1 && nextY > -1;
	}
	
	public void setNextMove(Map map) {
		if(!hasTarget()) {
			return;
		}
		//if there is no obstacle in the way to the target, no need to use A*
		if(!map.rayCastHasObstacle(x, y, targetX, targetY)) {
			nextX = targetX;
			nextY = targetY;
		} else {
			// A* around obstacles
			Path path = findPath(map);
			if(path != null) {
				//set the next step to the latest step in the path not blocked by an obstacle
				int step = 1;
				while(step < path.getLength() && !map.rayCastHasObstacle(x, y,
						path.getX(step) * Map.TILE_SIZE, path.getY(step) * Map.TILE_SIZE)) {
					nextX = path.getX(step)*Map.TILE_SIZE + Map.TILE_SIZE/2;
					nextY = path.getY(step)*Map.TILE_SIZE + Map.TILE_SIZE/2;
					step++;
				}
				//debug line
				//map.setTarget(nextX/Map.TILE_SIZE, nextY/Map.TILE_SIZE);
			}
		}
	}
	
	public Path findPath(Map map) {
		AStarPathFinder pathFinder = new AStarPathFinder(map, MAX_SEARCH_DIST, false, new ClosestHeuristic());
        return pathFinder.findPath(null, x/Map.TILE_SIZE, y/Map.TILE_SIZE,
        		targetX/Map.TILE_SIZE, targetY/Map.TILE_SIZE);
	}
	
	public void attack(GameUnit e) {
		if(attack_cooldown <= 0) {
			attack_fade = ATTACK_OPACITY;
			e.health -= 5;
			attack_cooldown = ATTACK_COOLDOWN;
		}
	}
	
	/*
	 * Update function
	 */
	public void update(HashSet<GameUnit> entities, Player player, Map map) {
		int prev_target_x = targetX;
		int prev_target_y = targetY;
		
		// default actions
		if(faction == GameUnit.Faction.ALLY) {
			MAX_SPEED = 25;
			speed = 7;
			safe = distance(player) < ALLY_SAFE_RANGE;
			setTarget(player.x + player.velx * ALLY_MOVE_DIST, player.y + player.vely * ALLY_MOVE_DIST);
		}
		if(faction == GameUnit.Faction.ENEMY) {
			MAX_SPEED = 10;
			speed = 2;
			if(player.alive && inRange(player, CHASE_RANGE)) {
				setTarget(player.x, player.y);
			} else if(Math.random() < 0.05) {
				setTarget((int)(Math.random() * map.getWidth()*Map.TILE_SIZE),
						(int)(Math.random() * map.getHeight() *Map.TILE_SIZE));
			}
		}
		
		// handle targetting and attacking
		if(this != player && !player.getRally()) {
			for(GameUnit e: entities) {
				if((e.faction == faction.opposite() ||
						(faction == Faction.ENEMY && e.faction == Faction.PLAYER))
						&& inRange(e, ATTACK_RANGE)) {
					setTarget(e.x, e.y);
					attack(e);
					break; //only attack one unit
				}
			}
		}
		attack_cooldown--;
		attack_cooldown = Math.max(0, attack_cooldown);

		// check health
		if(health <= 0) {
			this.alive = false;
			velx = 0;
			vely = 0;
		}
		
		// update velocity
		if(map.canMoveTo(x + velx, y + vely)) {
			x += velx;
			y += vely;
		} else {
			if(map.canMoveTo(x+velx, y)) {
				x += velx;
			} else {
				velx *= -1 * BOUNCE_DAMPENING_FACTOR;
			}
			if(map.canMoveTo(x, y+vely)) {
				y += vely;
			} else {
				vely *= -1 * BOUNCE_DAMPENING_FACTOR;
			}
		}
		
		if(velx < 0) velx++;
		else if(velx > 0) velx--;
		if(vely < 0) vely++;
		else if(vely > 0) vely--;
		
		if(faction != Faction.PLAYER) {
			if(prev_target_x != targetX || prev_target_y != targetY ||
					Math.abs(x-nextX) < 20 && Math.abs(y-nextY) < 20) {
				setNextMove(map);
			}

			if(hasNext()){
				if(nextY/Map.TILE_SIZE < y/Map.TILE_SIZE) {
					move(Game.Direction.NORTH);
				} else if (nextY/Map.TILE_SIZE > y/Map.TILE_SIZE) {
					move(Game.Direction.SOUTH);
				}
				if(nextX/Map.TILE_SIZE > x/Map.TILE_SIZE) {
					move(Game.Direction.EAST);
				} else if (nextX/Map.TILE_SIZE < x/Map.TILE_SIZE) {
					move(Game.Direction.WEST);
				}
			}
		}
		
		attack_fade -= 10;
		attack_fade = Math.max(0, attack_fade);
	}
	
	public void draw(Graphics2D g2d, float delta) {
		// draw attack indicator
		if(attack_fade > 0) {
			g2d.setColor(new Color(255, 100, 0, attack_fade));
			g2d.fillOval(screen_x - ATTACK_RANGE, screen_y - ATTACK_RANGE, ATTACK_RANGE*2, ATTACK_RANGE*2);
		}
		
		// draw unit
		if(faction == Faction.ALLY){
			g2d.setColor(Color.blue);
		} else {
			g2d.setColor(Color.red);
		}
		if(faction != Faction.PLAYER) {
			g2d.drawOval(screen_x-20, screen_y-20, 40, 40);
		}
		if(image != null) {
			g2d.drawImage(image, screen_x-SIZE, screen_y-SIZE, SIZE*2, SIZE*2, null);
		} else {
			g2d.fillOval(screen_x, screen_y, 20, 20);
		}

		// health bar
		g2d.setColor(Color.green);
		g2d.fillRect(screen_x-30, screen_y -30, 45 * health/MAX_HEALTH, 5);

		// some randomass debug info meant for only one enemy
		g2d.setColor(Color.white);
		if(faction != Faction.PLAYER) {
			g2d.drawString("Target: " + targetX + ", " + targetY, 500, 20);
			g2d.drawString("Vel: " + velx + ", " + vely, 500, 50);
		}
	}
	
	public void calculateScreenPos(GameUnit following, float delta) {
		screen_x = x - (following.x - GUI.getW()/2) + 
				(int)(((following == null? 0: following.velx) -velx) * (1-delta));
		screen_y = y - (following.y - GUI.getH()/2) +
				(int)(((following == null? 0: following.vely) -vely) * (1-delta));
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
