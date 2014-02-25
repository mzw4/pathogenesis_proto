import java.awt.Color;
import java.awt.Graphics2D;
import java.util.HashSet;
import java.util.Timer;
import java.util.TimerTask;


public class Player extends GameUnit {
	
	private final int INITIAL_PLASMIDS = 5;
	private final int MAX_ALLIES = 10;
	private final int PICKUP_RANGE = 20;
	private final int INFECT_RANGE = 150;
	private final int INFECT_OPACITY = 100;
	
	private boolean infect = false;
	private int infectx, infecty;
	private int infectFade = 100;
	private boolean infect_success;
	private boolean max_allies;
	
	private Timer timer;
	
	private boolean rally;
	private int plasmids;
	private int allies;
	
	public Player(Faction faction) {
		super(faction);
		timer = new Timer();
		plasmids = INITIAL_PLASMIDS;
	}
	
	public void update(HashSet<GameUnit> entities, HashSet<Pickup> pickups) {
		super.update(entities, this);
		for(Pickup p: pickups) {
			if(inRange(p, PICKUP_RANGE)) {
				pickup(p);
			}
		}
		infectFade -= 10;
		infectFade = Math.max(0, infectFade);
	}
	
	public void infect(HashSet<GameUnit> entities) {
		if(plasmids <= 0) {
			return;
		}
		plasmids--;
		infect = true;
		infectFade = INFECT_OPACITY;
		infectx = x;
		infecty = y;

		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				infect = false;
				infect_success = false;
			}
		}, 1000);
		
		GameUnit closestInRange = null;
		for(GameUnit e: entities) {
			if(e == this || e.faction == Faction.ALLY) {
				continue;
			}
			if(inRange(e, INFECT_RANGE)) {
				if(closestInRange == null || distance(e) < distance(closestInRange)) {
					closestInRange = e;
				}
			}
		}
		
		if(closestInRange != null && !max_allies) {
			closestInRange.faction = Faction.ALLY;
			infect_success = true;
			addAlly();
		}
	}
	
	public void addAlly() {
		allies++;
		if(allies >= MAX_ALLIES) {
			max_allies = true;
		}
	}
	
	public void pickup(Pickup p) {
		if(p.getType() == Pickup.Type.PLASMID) {
			plasmids++;
		}
		p.alive = false;
	}
	
	public int getPlasmids() {
		return plasmids;
	}
	
	public int getAllies() {
		return allies;
	}
	
	public boolean getRally() {
		return rally;
	}
	
	public void loseAlly() {
		allies--;
		if(allies < MAX_ALLIES) {
			max_allies = false;
		}
	}
	
	public void rally(boolean rally) {
		this.rally = rally;
	}
	
	@Override
	public void draw(Graphics2D g2d, float delta) {
		super.draw(g2d, delta);
		
		if(infect) {
			if(infect_success) {
				g2d.drawString("CONVERTED!", infectx - INFECT_RANGE, infecty - INFECT_RANGE);
			}
			if(max_allies) {
				g2d.drawString("MAX ALLIES!", infectx - INFECT_RANGE, infecty - INFECT_RANGE);
			}
			g2d.setColor(new Color(100, 50, 50, infectFade));
			g2d.fillOval(infectx - INFECT_RANGE, infecty - INFECT_RANGE, INFECT_RANGE*2, INFECT_RANGE*2);
		}

	}
}
