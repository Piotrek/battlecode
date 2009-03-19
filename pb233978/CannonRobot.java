package pb233978;

import battlecode.common.*;
import static battlecode.common.GameConstants.*;
import java.util.ArrayList;

public class CannonRobot extends BaseRobot {
	public int repeat;
	public boolean air;
	public MapLocation enemyLoc;

	public CannonRobot(RobotController c) 
	{
		super(c);
		repeat = 0; // Type razy powtor strzal...
		air = false;
		enemyLoc = null;
	}

	public void run()
	{
		int lastGet = Clock.getRoundNum();

		waitForAttack();
		while (true) {

			Message[] msgs = rc.getAllMessages();
			lastGet = Clock.getRoundNum();
			Message msg = null;

			if(msgs != null) 
			   if (msgs.length > 0) { // Wybieramy najblizszego Archona
				int min1 = 100, min2 = 100, i1 = 0, i2 = 0;
				for (int i = 0; i < msgs.length; ++i) {
					if (msgs[i].strings == null) continue;
					if (msgs[i].strings[0] == "Stop") { enemyLoc = null; continue; }
					if ("Looking" != msgs[i].strings[0] && "Using" != msgs[i].strings[0]) continue;
					if (Clock.getRoundNum() > msgs[i].ints[1] + 1) continue;
					boolean attack = false;

					for (String s : msgs[i].strings) 
						if ("AEnemy" == s || "GEnemy" == s) { attack = true; break; }
					
					int len = rc.getLocation().distanceSquaredTo(msgs[i].locations[0]);
					if (len < min1) { min1 = len; i1 = i; }
					if (attack && len < min2) { min2 = len; i2 = i; }
				}

				// Sluchamy najblizszego archona zaangazowanego w atak lub
				// po prostu najblizszego
				if (min2 != 100) msg = msgs[i2];
				else if (min1 != 100) msg = msgs[i1];
			}

			if (null == msg || null == msg.locations ||
					null == msg.strings || null == msg.ints || 
					msg.strings.length != msg.locations.length ||
					msg.strings.length != msg.ints.length ||
					msg.strings.length == 0) {
				debug("Brak wiadomosci");
				dummyWork();
				continue;
			}

			archonID = msg.ints[0];
			archonLoc = msg.locations[0];	

			debug("Pobralem wiadomosci: " + msg.strings.length);

			ArrayList<MapLocation> as = new ArrayList<MapLocation>();
			ArrayList<MapLocation> gs = new ArrayList<MapLocation>();

			for (int i = 1; i < msg.strings.length; ++i) {
				if ("AEnemy" == msg.strings[i]) 
						if ((msg.locations[i].getX() + msg.locations[i].getY()) % 100 == msg.ints[i]) as.add(msg.locations[i]);
				if ("GEnemy" == msg.strings[i]) 
						if ((msg.locations[i].getX() + msg.locations[i].getY()) % 100 == msg.ints[i]) gs.add(msg.locations[i]);
			}
			// Teraz sprawdzimy czy w cos mozemy trafic...
			
			if (as.isEmpty() && gs.isEmpty()) { repeat = 0; continue; }
			
			boolean ok = false;

			for (MapLocation l : gs) {
				if (rc.canAttackSquare(l)) {
					try {
						rc.attackGround(l);
						rc.yield();
						waitForAttack();
						enemyLoc = l;	
						air = false;
						ok = true;
						break;
					} 
					catch (Exception e) { }
				}
			}

			if (ok) continue;

			for (MapLocation l : as) {
				if (rc.canAttackSquare(l)) {
					try {
						rc.attackAir(l);
						rc.yield();
						waitForAttack();
						enemyLoc = l;
						ok = true;
						air = true;
						break;
					} 
					catch (Exception e) { }
				}
			}

			if (ok) continue;

			// Nie moge bezposrednio atakowac, ale moze jakby sie obrocic?
			
			repeat = 0;
			enemyLoc = null;

			for (MapLocation l : as) {
				int len = rc.getLocation().distanceSquaredTo(l);
				// Napalowo sprawdze zasieg...
				if ((len >= 2 && len <= 4) || 
						(rc.getLocation().getX() == l.getX() && len == 5) ||
						(rc.getLocation().getY() == l.getY() && len == 5)) {
					repeat = 3;
					air = true;
					enemyLoc = l;

					try {
						waitForReady();
						rc.setDirection(rc.getLocation().directionTo(l));
						rc.yield();
						if (rc.canAttackSquare(l)) {
							try {
								rc.attackGround(l);
								rc.yield();
								waitForAttack();
								ok = true;
								break;
							} 
							catch (Exception e) { }
						}
					}
					catch (Exception e) { }
					break;
				}
			}

			if (ok) continue;

			for (MapLocation l : gs) {
				int len = rc.getLocation().distanceSquaredTo(l);
				// Napalowo sprawdze zasieg...
				if ((len >= 2 && len <= 4) || 
						(rc.getLocation().getX() == l.getX() && len == 5) ||
						(rc.getLocation().getY() == l.getY() && len == 5)) {
					repeat = 3;
					air = false;
					enemyLoc = l;

					try {
						waitForReady();
						rc.setDirection(rc.getLocation().directionTo(l));
						rc.yield();
						if (rc.canAttackSquare(l)) {
							try {
								rc.attackGround(l);
								rc.yield();
								waitForAttack();
								ok = true;
								break;
							} 
							catch (Exception e) { }
						}
					}
					catch (Exception e) { }	
					break;
				}
			}

			if (ok) continue;

			// Sprawdzimy teraz czy moze trzeba sie troche przesunac do przodu...
	
			int min = 100;

			for (MapLocation l : as) {
				int len = rc.getLocation().distanceSquaredTo(l);
				if (len > 1 && len < min) {
					min = len;
					enemyLoc = l;
					air = true;
					repeat = 3;
				}
			}

			for (MapLocation l : gs) {
				int len = rc.getLocation().distanceSquaredTo(l);
				if (len > 1 && len < min) {
					min = len;
					enemyLoc = l;
					air = false;
					repeat = 3;
				}
			}

			// Jesli atakuja mnie jednostki oddalone o 1 mam nadzieje, ze
			// jakis Channeler czy inny Soldier temu podola...
			if (null == enemyLoc) continue;

			// -
			stepTo(enemyLoc);
		}
	}

	public void dummyWork()
	{

		if (enemyLoc != null) {
			if (rc.canAttackSquare(enemyLoc)) {
			try { 
				if (air) rc.attackAir(enemyLoc);
				else rc.attackGround(enemyLoc);
				rc.yield();
				waitForAttack();
			}
			catch (Exception e) { }
			}
		} 

		if (null == archonLoc) 
			debug("Nie mam co robic, nie mam przydzielonego Archona...");
		else 
			debug("Nie mam co robic, odleglosc od Archona: " + archonLoc.distanceSquaredTo(rc.getLocation()));

		if (null != archonLoc) 
			if (archonLoc.distanceSquaredTo(rc.getLocation()) >= 2)
				stepTo(archonLoc);
		// +
		//if (rc.getLocation() != archonLoc) stepTo(archonLoc);
	}


}

