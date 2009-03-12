package pb233978;

import battlecode.common.*;
import static battlecode.common.GameConstants.*;

public class ChannelerRobot extends BaseRobot {
	public int repeat;
	public ChannelerRobot(RobotController c) 
	{
		super(c);
		repeat = 0;
	}

	public void run()
	{
		while (true) {
			if (repeat > 0) {
				try { rc.drain(); rc.yield(); } 
				catch (Exception e) { }
				--repeat;
			}

			Message msg = rc.getNextMessage();

			if (null == msg || null == msg.locations || 
					null == msg.ints || null == msg.strings ||
					msg.ints.length != msg.locations.length || 
					msg.ints.length != msg.strings.length || 
					msg.strings.length == 0) 
				continue;

			// Channelera interesuje tylko:
			// - gdzie jest archon-dowodca,
			// - gdzie jest najblizszy przeciwnik.
			//
			// Jesli bedzie jakas obca jednostka, Channeler
			// sprobuje pojsc w jej kierunku i wy-'Drain'-owac.

			// Pobieramy lokalizacje Archona z Fluxem
			if ("Using" == msg.strings[0]) {
				archonID = msg.ints[0];
				archonLoc = msg.locations[0];
			} 

			// Jak nie znamy Archona - odpuszczamy...
			if (null == archonLoc) continue;

			MapLocation nearest = null;
			int min = 100;

			for (int i = 1; i < msg.strings.length; ++i) {
				if ("AEnemy" != msg.strings[i] && "GEnemy" != msg.strings[i]) continue;
				int len = archonLoc.distanceSquaredTo(msg.locations[i]);
				if (len < min) { min = len; nearest = msg.locations[i]; }
			}

			// Brak wrogow...
			if (null == nearest ) continue;

			// Sprobujemy sie przyblizyc do wroga...
			Direction d = rc.getLocation().directionTo(nearest);

			// Ruszamy sie tak, aby nie odejsc za daleko od Archona
			if (Direction.OMNI != d && Direction.NONE != d && // syfny kierunek(?)
					(rc.getLocation().add(d).isAdjacentTo(archonLoc) || rc.getLocation() == archonLoc)) {
				try { 
					if (rc.getDirection() != d) {
						waitForReady();
						rc.setDirection(d);
						rc.yield();
					}

					waitForReady();
					rc.moveForward(); 
					debug("Podchodze do przeciwnika, odleglosc: " + rc.getLocation().distanceSquaredTo(nearest));
					rc.yield(); 
				} 
				catch (Exception e) { }
			}

			repeat = 3;

			// Drain !!
			if (rc.getLocation().distanceSquaredTo(nearest) <= 2) {
				try {
					debug("Wysysam okoliczna energie !!");
					rc.drain(); 
					rc.yield();
				}
				catch (Exception e) { }
			} 	
		}
	}
}

