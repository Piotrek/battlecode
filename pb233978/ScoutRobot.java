package pb233978;

import battlecode.common.*;
import static battlecode.common.GameConstants.*;

public class ScoutRobot extends BaseRobot {
	public ScoutRobot(RobotController c) 
	{
		super(c);
	}

	public void run()
	{		
		while (true) {
			doAttack();
		}
	}

	public void doAttack()
	{
		boolean air = false;
		MapLocation loc = null;
		
		try {
			//System.out.println("Odbieram polecenie");
			Message msg = rc.getNextMessage();
			if (msg != null &&
					msg.strings != null && msg.strings.length == 2 &&
					msg.locations != null && msg.locations.length == 1 &&
					msg.strings[0] == "Attack") {
				//System.out.println("Dostalem polecenie ataku");
				if (msg.strings[1] == "Ground" && rc.canAttackGround()) {
					loc = msg.locations[0];
					air = false;
				} else if (msg.strings[1] == "Air" && rc.canAttackAir()) {
					loc = msg.locations[0];
					air = true;
				}
			}
		} 
		catch (Exception e) { }

		if (null == loc) {rc.yield(); return; }

		//System.out.println("Bede atakowal " + loc + air + rc.getLocation());

		try {
			if (rc.canAttackSquare(loc)) { // Bam, bam...
				// Moge strzelac!
				//System.out.println("Atakuje " + loc + air);
				waitForAttack();
				if (air) { // nie atakujemy swoich
					if (rc.senseRobotInfo(rc.senseAirRobotAtLocation(loc)).team == rc.getTeam())
						rc.clearBroadcast();
					else rc.attackAir(loc);
				}
				else {
					if (rc.senseRobotInfo(rc.senseGroundRobotAtLocation(loc)).team == rc.getTeam())
						rc.clearBroadcast();
					else rc.attackGround(loc);
				}
				rc.yield();

			} else { // Moze nie ten kierunek?
				//System.out.println("Nie moge atakowac " + loc + air);
				Direction d = rc.getLocation().directionTo(loc);

				if (rc.getDirection() != d) {
					waitForReady();
					rc.setDirection(d);
					rc.yield();

					if (rc.canAttackSquare(loc)) { // Bam, bam...
						//System.out.println("Atakuje2 " + loc + air);
						waitForAttack();
						if (air) rc.attackAir(loc);
						else rc.attackGround(loc);
						rc.yield();
					} else {
						// Podchodze do przeciwnika...
						stepTo(loc);
					}
				}
			}
		}
		catch (Exception e) { stepTo(loc); }
	}

}

