import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.awt.image.TileObserver;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

public class Game extends JPanel implements Runnable {
	
	private static int sWidth, sHeight;	
	private boolean running = false;
	
	private int TICKS_PER_SECOND = 25;
	private int SKIP_TICKS = 1000 / TICKS_PER_SECOND;
	private int MAX_FRAMESKIP = 5;
	private float fps;
	
	private final double PLASMID_DROP_CHANCE = 0.1;
	private final double ENEMY_SPAWN_CHANCE = 0.015;
	private final double PLASMID_SPAWN_CHANCE = 0.005;

	private float interpolation;
	private KeyInputHandler keyboard;
	private BufferedImage background;
	private final String background_path = "src/red_blood_cells.jpg";
	
	private GameUnit following;	// the entity that the camera is following
	private Map map;
	
	private int kills;
	
	public enum Direction {
		NORTH(KeyEvent.VK_W), SOUTH(KeyEvent.VK_S), EAST(KeyEvent.VK_D), WEST(KeyEvent.VK_A);
		
		private final int key;
		Direction(int key) {
			this.key = key;
		}
		public int getKey() {
			return key;
		}
	}
 	
	private HashSet<GameUnit> units = new HashSet<>();
	private HashSet<Pickup> pickups = new HashSet<>();
	private Player player;

	public Game(int w, int h) {
		setPreferredSize(new Dimension(w, h));
		setLayout(new BorderLayout());
		sWidth = w;
		sHeight = h;
		
		init();
		
		Thread gameThread = new Thread(this);
		gameThread.start();
	}
	
	private void init() {
		units.clear();
		pickups.clear();
		kills = 0;
		
		// create player
		player = new Player(GameUnit.Faction.PLAYER);
		player.placeAt(sWidth/2, sHeight/2);
		units.add(player);
		following = player;
		
		// create map
		map = new Map(96, 54);
		// populate map
		map.generate();
		
		// load images
		try {
		    background = ImageIO.read(new File(background_path));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	    keyboard = new KeyInputHandler();
	    addKeyListener(keyboard);
	    
	    setKeyBindings();
	}
	
	private void restart() {
		init();
	}
	
	/*
	 * Game's core execution loop.
	 * Updates the game and all units.
	 * Repaints the GUI
	 * 
	 */
	public void run() {	
		long startTime = System.currentTimeMillis();
		long next_game_tick = 0;
		int loops = 0;

		running = true;
		long beforeFrame = System.nanoTime();
		while(running) {
	        loops = 0;
	        while(System.currentTimeMillis() - startTime > next_game_tick && loops < MAX_FRAMESKIP) {
	            update();

	            next_game_tick += SKIP_TICKS;
	            loops++;
	        }
	        
	        interpolation = (float)(System.currentTimeMillis()
	        		- startTime + SKIP_TICKS - next_game_tick) / SKIP_TICKS;
	        display_game(interpolation);
	        
	        while(System.nanoTime() - beforeFrame < 10);
	        long diff = System.nanoTime() - beforeFrame;
	        fps = 1000000000f/diff;
	        beforeFrame = System.nanoTime();
		}
	}
	
	private void display_game(float interpolation) {
		repaint();
	}
	
	private void update() {
		keyboard.update();

		// Handle input
		if(player.alive) {
			if(keyboard.keyPressed(KeyEvent.VK_W)) {
				player.move(Direction.NORTH);
			}
			if(keyboard.keyPressed(KeyEvent.VK_A)) {
				player.move(Direction.WEST);
			}
			if(keyboard.keyPressed(KeyEvent.VK_S)) {
				player.move(Direction.SOUTH);
			}
			if(keyboard.keyPressed(KeyEvent.VK_D)) {
				player.move(Direction.EAST);
			}
			if(keyboard.keyPressedOnce(KeyEvent.VK_SPACE)) {
				player.infect(units);
			}
			if(keyboard.keyPressed(KeyEvent.VK_E)) {
				player.rally(true);
			} else if(keyboard.keyReleased(KeyEvent.VK_E)) {
				player.rally(false);
			}
		}
		if(keyboard.keyPressedOnce(KeyEvent.VK_1)) {
			GameUnit u = new GameUnit(GameUnit.Faction.ALLY);
			u.placeAt((int)(player.getX() + Math.random() * 20 - 10),
					(int) (player.getY() + Math.random() * 20 - 10));
			units.add(u);
			player.addAlly();
		}	
		if(keyboard.keyPressedOnce(KeyEvent.VK_2) || Math.random() < ENEMY_SPAWN_CHANCE) {
			GameUnit u = new GameUnit(GameUnit.Faction.ENEMY);
			u.placeAt((int)(Math.random() * map.getWidth()*Map.TILE_SIZE),
					(int)(Math.random() * map.getHeight()*Map.TILE_SIZE));
			units.add(u);
		}		
		if(keyboard.keyPressedOnce(KeyEvent.VK_3) || Math.random() < PLASMID_SPAWN_CHANCE) {
			placePickup(Pickup.Type.PLASMID,
					(int)(Math.random() * map.getWidth()*Map.TILE_SIZE),
					(int)(Math.random() * map.getHeight()*Map.TILE_SIZE));
		}
		if(keyboard.keyPressedOnce(KeyEvent.VK_R)) {
			restart();
		}
		
		HashSet<GameEntity> dead = new HashSet<>();
		for(GameUnit e: units) {
			if(e == player) {
				((Player) e).update(units, pickups, map);
			} else {
				e.update(units, player, map);
			}
			if(!e.alive) {
				dead.add(e);
			}
		}
		
		for(Pickup p: pickups) {
			if(!p.alive) dead.add(p);
		}
		
		for(GameEntity e: dead) {
			if(e instanceof GameUnit) {
				units.remove(e);
				if(((GameUnit) e).faction == GameUnit.Faction.ENEMY) {
					kills++;
					if(Math.random() < PLASMID_DROP_CHANCE) {
						placePickup(Pickup.Type.PLASMID, e.x, e.y);
					}
				} else if(((GameUnit) e).faction == GameUnit.Faction.ALLY){
					player.loseAlly();
				}
			} else if(e instanceof Pickup) {
				pickups.remove(e);
			}
		}
	}
	
	private void placePickup(Pickup.Type type, int x, int y) {
		Pickup p = new Pickup(type);
		p.placeAt(x, y);
		pickups.add(p);
	}
	
	private void setKeyBindings() {
		getInputMap().put(KeyStroke.getKeyStroke("A"), "press");
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		g2d.setColor(Color.black);
		g2d.fillRect(0, 0, sWidth, sHeight);
		g2d.drawImage(background, 0, 0, sWidth, sHeight,
				following.x-sWidth/2 - (int)(((following == null? 0: following.velx)) * (1-interpolation)),
				following.y-sHeight/2 - (int)(((following == null? 0: following.vely)) * (1-interpolation)),
				following.x+sWidth/2 - (int)(((following == null? 0: following.velx)) * (1-interpolation)),
				following.y+sHeight/2 - (int)(((following == null? 0: following.vely)) * (1-interpolation)), null);
		
		map.draw(g2d, following, interpolation);
		for(GameUnit e: units) {
			e.calculateScreenPos(following, interpolation);
			e.draw(g2d, interpolation);
		}
		for(Pickup p: pickups) {
			p.calculateScreenPos(following, interpolation);
			p.draw(g2d);
		}
		g2d.setColor(Color.white);
		g2d.drawString("FPS: " + fps, 20, 20);
		g2d.drawString("Player: " + player.x + ", " + player.y, 20, 50);
		g2d.drawString("Player vel: " + player.velx + ", " + player.vely, 20, 80);
		g2d.drawString("Plasmids: " + player.getPlasmids(), 20, 110);
		g2d.drawString("Allies: " + player.getAllies(), 20, 140);
		g2d.drawString("Kills: " + kills, 20, 170);

		if(player.getRally()) {
			g2d.drawString("RALLY!", player.x, player.y + 30);
		}
		if(!player.alive) {
			g2d.drawString("YOU LOSE! Press R to restart.", sWidth/2-50, sHeight/2);
		}
	}
	
	public static int getW() {
		return sWidth;
	}
	public static int getH() {
		return sHeight;
	}
}
