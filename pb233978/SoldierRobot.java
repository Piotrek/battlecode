package pb233978;

import battlecode.common.*;
import static battlecode.common.GameConstants.*;
import java.util.ArrayList;

public class SoldierRobot extends BaseRobot {
	public MapLocation enemyLoc;
	public int enemyID;
	public boolean air;

	public SoldierRobot(RobotController c) 
	{
		super(c);
		enemyLoc = null;
		enemyID = 0;
		air = false;
	}

	public void run()
	{
		int lastGet = Clock.getRoundNum();

		while (true) {
			dummyWork();

			// Nie czytamy zbyt starych wiadomosci...
			//if (lastGet + 3 < Clock.getRoundNum()) rc.clearBroadcast();
			
			// Odbieramy wszystkie message jakie sa.
			Message[] msgs = rc.getAllMessages();
			lastGet = Clock.getRoundNum();
			Message msg = null;

			if (msgs.length > 0) { // Wybieramy najblizszego Archona
				int max1 = -1, max2 = -1, i1 = 0, i2 = 0;
				for (int i = 0; i < msgs.length; ++i) {
					if (msgs[i].strings == null) continue;
					if (msgs[i].strings[0] == "Stop") { enemyLoc = null; continue; }
					if ("Looking" != msgs[i].strings[0] && "Using" != msgs[i].strings[0]) continue;
					if (Clock.getRoundNum() > msgs[i].ints[1] + 1) continue;
					boolean attack = false;

					for (String s : msgs[i].strings) 
						if ("AEnemy" == s || "GEnemy" == s) { attack = true; break; }
					
					if (msgs[i].ints[0] > max1) { max1 = msgs[i].ints[0]; i1 = i; }
					if (attack && msgs[i].ints[0] > max2) { max2 = msgs[i].ints[0]; i2 = i; }
				}

				if (max2 > -1) msg = msgs[i2];
				else if (max1 > -1) msg = msgs[i1];
			}

			if (null == msg || null == msg.locations ||
					null == msg.strings || null == msg.ints || 
					msg.strings.length != msg.locations.length ||
					msg.strings.length != msg.ints.length ||
					msg.strings.length == 0) 
				continue;

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

			int min = 100;
			for (MapLocation l : as) {
				int len = rc.getLocation().distanceSquaredTo(l);
				if (len < min) {
					min = len;
					enemyLoc = l;
				}
			}

			for (MapLocation l : gs) {
				int len = rc.getLocation().distanceSquaredTo(l);
				if (len < min) {
					min = len;
					enemyLoc = l;
				}
			}

			doAttack();
		}
	}

	public void doAttack()
	{
		// Sprawdzam sensorami czy cos widze...
		
		Robot[] rair = null, rground = null;
		try {
			rair = rc.senseNearbyAirRobots();
			rground = rc.senseNearbyGroundRobots();
		} 
		catch (Exception e) { }
		
		boolean nAir = false;
		MapLocation nLoc = null;
		int nID = 0, min = 100;

		for (Robot r : rair) { // Powietrze...
			try {
				RobotInfo inf = rc.senseRobotInfo(r);
				if (rc.getTeam() == inf.team) continue;
				int len = rc.getLocation().distanceSquaredTo(inf.location);
				if (len < min) {
					min = len; 
					nLoc = inf.location;
					nID = r.getID();
					nAir = true;
				}
			}
			catch (Exception e) { }
		}

		for (Robot r : rground) { // Ziemia...
			try {
				RobotInfo inf = rc.senseRobotInfo(r);
				if (rc.getTeam() == inf.team) continue;
				int len = rc.getLocation().distanceSquaredTo(inf.location);
				if (len < min) {
					min = len; 
					nLoc = inf.location;
					nID = r.getID();
					nAir = false;
				}
			}
			catch (Exception e) { }
		}

		if (nLoc == null) {
			if (enemyLoc != null) stepTo(enemyLoc);
		}
		else {
			if (rc.canAttackSquare(nLoc)) {
				try {
					waitForAttack();
					debug("Strzelam !!");
					if (nAir) rc.attackAir(nLoc);
					else rc.attackGround(nLoc);
					rc.yield();
				}
				catch (Exception e) { }
			} else { 
				if ((rc.getLocation().distanceSquaredTo(nLoc)) < 2)
					try { 
						rc.setDirection(rc.getLocation().directionTo(nLoc)); 
						rc.yield();
					}
					catch (Exception e) { }
				else {
				// Trzeba jeszcze dojsc...
				debug("Ide do celu, odleglosc: " + rc.getLocation().distanceSquaredTo(nLoc));
				stepTo(nLoc);
				}
			}
		}		
	}

	public void dummyWork()
	{
			if (null == archonLoc) 
				debug("Nie mam co robic, nie mam przydzielonego Archona...");
			else 
				debug("Nie mam co robic, odleglosc od Archona: " + archonLoc.distanceSquaredTo(rc.getLocation()));

			if (rc.getEnergonLevel() / rc.getMaxEnergonLevel() < 0.3 && null != archonLoc)
				while (rc.getEnergonLevel() / rc.getMaxEnergonLevel() < 0.8) stepTo(archonLoc);
	}

}

