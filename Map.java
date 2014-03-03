import java.awt.Color;
import java.awt.Graphics2D;

import org.newdawn.slick.util.pathfinding.PathFindingContext;
import org.newdawn.slick.util.pathfinding.TileBasedMap;


public class Map implements TileBasedMap {
	public static final int TILE_SIZE = 20;
	
	private int width, height;
	private int[][] map;
	
	public Map(int w, int h) {
		width = w;
		height = h;
		map = new int[h][w];
	}
	
	public void generate() {
		for(int i = 0; i < 20; i++) {
			map[10][i] = 1;
		}
	}
	
	public boolean canMoveTo(int x, int y) {
		return x-GameUnit.SIZE/2 > 0 && y-GameUnit.SIZE/2 > 0 &&
				x+GameUnit.SIZE/2 < width*TILE_SIZE && y+GameUnit.SIZE/2 < height*TILE_SIZE && 
				map[(y-GameUnit.SIZE/2)/TILE_SIZE][(x-GameUnit.SIZE/2)/TILE_SIZE] != 1 &&
				map[(y+GameUnit.SIZE/2)/TILE_SIZE][(x+GameUnit.SIZE/2)/TILE_SIZE] != 1;
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

	public int getTileAt(int x, int y) {
		return map[y][x];
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
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
					g2d.fillRect(screen_x, screen_y, TILE_SIZE, TILE_SIZE);
				}
			}
		}
	}
}
