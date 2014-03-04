import java.awt.Color;
import java.awt.Graphics2D;

import org.newdawn.slick.util.pathfinding.PathFindingContext;
import org.newdawn.slick.util.pathfinding.TileBasedMap;


public class Map implements TileBasedMap {
	public static final int TILE_SIZE = 20;
	
	private int width, height;
	private int[][] map;
	private int targX, targY;
	
	public Map(int w, int h) {
		width = w;
		height = h;
		map = new int[h][w];
	}
	
	public void generate() {
		for(int i = 5; i < 35; i++) {
			if(i != 20 && i != 21 && i != 22) {
				map[10][i] = 1;
			}
		}
		for(int i = 20; i < 50; i++) {
			map[20][i] = 1;
		}
		for(int i = 40; i < 80; i++) {
			if(i != 55 && i != 56 && i!= 57) {
				map[40][i] = 1;
			}
		}
		for(int i = 0; i < 30; i++) {
			if(i != 20 && i != 21 && i != 22) {
				map[i][30] = 1;
			}
		}
	}
	
	public void setAsWall(int x, int y) {
		if(x > 0 && y > 0 && x < width && y < height) {
			map[y][x] = 1;
		}
	}
	
	public void clearWalls () {
		map = new int[height][width];
	}
	
	public boolean canMoveTo(int x, int y) {
		return x-GameUnit.SIZE/2 > 0 && y-GameUnit.SIZE/2 > 0 &&
				x+GameUnit.SIZE/2 < width*TILE_SIZE && y+GameUnit.SIZE/2 < height*TILE_SIZE && 
				map[(y-GameUnit.SIZE/2)/TILE_SIZE][(x-GameUnit.SIZE/2)/TILE_SIZE] != 1 &&
				map[(y+GameUnit.SIZE/2)/TILE_SIZE][(x+GameUnit.SIZE/2)/TILE_SIZE] != 1 &&
				map[(y+GameUnit.SIZE/2)/TILE_SIZE][(x-GameUnit.SIZE/2)/TILE_SIZE] != 1 &&
				map[(y-GameUnit.SIZE/2)/TILE_SIZE][(x+GameUnit.SIZE/2)/TILE_SIZE] != 1;
	}
	
	/*
	 * Returns true if obstacle in path
	 */
	public boolean rayCastHasObstacle(int start_x, int start_y, int end_x, int end_y) {
		if(start_x == end_x && start_y == end_y ||
				!canMoveTo(end_x, end_y) || !canMoveTo(start_x, start_y)) {
			return false;
		}
		
		boolean steep = Math.abs(end_x-start_x) < Math.abs(end_y-start_y);
		int x0,x1,y0,y1;
		
		if(steep) {
			x0 = start_y;
			x1 = end_y;
			y0 = start_x;
			y1 = end_x;
		} else {
			x0 = start_x;
			x1 = end_x;
			y0 = start_y;
			y1 = end_y;
		}
		if(x0 > x1) {
			int t = x1;
			x1 = x0;
			x0 = t;
			t = y1;
			y1 = y0;
			y0 = t;
		}
		
		float slope = (float)(y1-y0)/(x1-x0);
		float b = y0 - slope * x0;

	    for (int i = x0; i <= x1; i++) {
	    	float x = steep? slope*i + b : i;
	    	float y = steep? i : slope*i + b;
	    	if(map[(int)(y/TILE_SIZE)][(int)(x/TILE_SIZE)] == 1) {
	    		return true;
	    	}
	    }
	    return false;
	}

	// --------------------------------- Pathfindiing -----------------------------------

	@Override
	public boolean blocked(PathFindingContext context, int x, int y) {
		return map[y][x] == 1;
	}

	@Override
	public float getCost(PathFindingContext context, int x, int y) {
		return 1f;
	}

	@Override
	public int getHeightInTiles() {
		return height;
	}

	@Override
	public int getWidthInTiles() {
		return width;
	}

	@Override
	public void pathFinderVisited(int x, int y) {}
	
	// --------------------------------- Getters and Setters -----------------------------------

	public int getTileFromPix(int x, int y) {
		return map[y/TILE_SIZE][x/TILE_SIZE];
	}
	
	public int getTileAt(int x, int y) {
		return map[y][x];
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public void setTarget(int x, int y) {
		targX = x;
		targY = y;
	}
	// --------------------------------- Draw -----------------------------------

	public void draw(Graphics2D g2d, GameUnit following, float delta) {
		g2d.setColor(Color.black);
		
		for(int i = 0; i < map.length; i++) {
			for(int j = 0; j < map[0].length; j++) {
				int screen_x = j*TILE_SIZE - (following.x - GUI.getW()/2) + 
						(int)(((following == null? 0: following.velx)) * (1-delta));
				int	screen_y = i*TILE_SIZE - (following.y - GUI.getH()/2) +
						(int)(((following == null? 0: following.vely)) * (1-delta));
				
				if(map[i][j] == 1) {
					g2d.setColor(Color.black);
					g2d.fillRect(screen_x, screen_y, TILE_SIZE, TILE_SIZE);
				}
			}
		}
		
//		int screen_x = targX*TILE_SIZE - (following.x - GUI.getW()/2) + 
//				(int)(((following == null? 0: following.velx)) * (1-delta));
//		int	screen_y = targY*TILE_SIZE - (following.y - GUI.getH()/2) +
//				(int)(((following == null? 0: following.vely)) * (1-delta));
//		g2d.setColor(Color.cyan);
//		g2d.fillRect(screen_x, screen_y, TILE_SIZE, TILE_SIZE);
	}
}
