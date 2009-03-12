package pb233978;

import java.util.Arrays;
import java.util.ArrayList;
import battlecode.common.*;
import static battlecode.common.GameConstants.*;

public class WorkerRobot extends BaseRobot {
	public WorkerRobot(RobotController c) 
	{
		super(c);
	}

	public void run()
	{
		while (true) {
			try {
				waitForReady();
				int lastGet = Clock.getRoundNum();
				
				while (true) {
					// Nie czytamy zbyt starych wiadomosci...
					if (lastGet + 3 < Clock.getRoundNum()) rc.clearBroadcast();

					Message msg = rc.getNextMessage();
					lastGet = Clock.getRoundNum();

					if (null == msg || null == msg.strings || 
							null == msg.ints || null == msg.locations || 
							msg.strings.length != msg.locations.length || 
							msg.strings.length != msg.ints.length || 
							msg.strings.length <= 1) 
						continue;

					// Tylko jesli Archon ma Fluxa !!
					if (msg.strings[0] != "Using") continue;

					debug("Mamy archona - zarzadce...");

					archonID = msg.ints[0];
					archonLoc = msg.locations[0];
				
					// Bedziemy brali blok pierwszy z brzegu...
					// TODO: Mozna by to walnac jakis inteligentniejszy alg...
					
					ArrayList<MapLocation> locs = new ArrayList<MapLocation>();
					MapLocation loc = null;


					for (int i = 1; i < msg.strings.length; ++i) {
						if ("Blocks" != msg.strings[i]) continue;
						locs.add(msg.locations[i]);
					}

					// Nic nie wiem o okolicznych blokach...
					if (locs.size() == 0) { 
						debug("Nie moge znalezc zadnych blokow w okolicy...");
						loc = archonLoc; 
					} else 
						loc = locs.get(rand.nextInt(locs.size()));

					// Wiemy po ktorego blocka idziemy...
					int ide = 1;
						
					//System.out.println("Worker o ID: " + rc.getRobot().getID() + " idzie po BLOCKA" + loc + " z " + rc.getLocation());
					Direction d = rc.getLocation().directionTo(loc);

					while (Direction.OMNI != d) {
						/* malo energii */
						if (rc.getEnergonLevel() < (rc.getMaxEnergonLevel() / 2))
							loc = archonLoc; // do Archona !
							
						d = rc.getLocation().directionTo(loc);
						if ((Math.abs(rc.getLocation().getX() - loc.getX()) <= 1) && (Math.abs(rc.getLocation().getY() - loc.getY()) <= 1)) { 
							// jestesmy sasiadem ladowania
							waitForReady();
							rc.setDirection(d);
							rc.yield();
							waitForReady();
							/* chcemy ladowac */
							if (ide == 1) {
								debug("Dotarlem do blocka: " + rc.getLocation() + " " + loc);
								if (rc.canLoadBlockFromLocation(loc) && (rc.senseFluxDepositAtLocation(loc) == null)) {
									rc.loadBlockFromLocation(loc);
									debug("Zaladowalem blocka: " + loc);
									rc.yield();
									waitForReady();
								} else 
									debug("Nie zaladowalem blocka: " + loc);
										
								ide = 0;
								loc = archonLoc;
								d = rc.getLocation().directionTo(loc);
								debug("Wracam do Archona na: " + loc);
							} else {
								debug("Wrocilem do Archona na: " + loc);
								while (!rc.canUnloadBlockToLocation(loc)) {
									debug("Nie moge wyladowac blocka: " + loc);
									loc = rc.getLocation();
									rc.moveBackward();
									rc.yield();
									waitForReady();
								}
 								debug("Klade blocka na: " + loc);
								waitForReady();
								rc.unloadBlockToLocation(loc);
								rc.yield();
								waitForReady(); 
							}
						} else if (Direction.NONE == d) {
							loc = archonLoc;
							// Nie ma dojscia? - Niech se pochodzi...							
							debug("Nie mam sie gdzie ruszyc :(");

							while (!rc.canMove(rc.getDirection())) {
								waitForReady();
								rc.setDirection(rc.getDirection().rotateLeft());
								rc.yield();
							}

							waitForReady();
							rc.moveForward();
							rc.yield();
						} else {
							if (rc.getDirection() != d) {
								waitForReady();
								rc.setDirection(d);
								rc.yield();
							}
					
							if (rc.canMove(rc.getDirection())) {
								waitForReady();
								rc.moveForward();
								rc.yield();
							} else { // wpp na slepca :(
								debug("Nie moge sie ruszyc we wsk kierunku :(");
								while (!rc.canMove(rc.getDirection())) {
									waitForReady();
									rc.setDirection(rc.getDirection().rotateLeft());
									rc.yield();
								}

								waitForReady();
								rc.moveForward();
								rc.yield();
							}
						}
					}
				}
			}
			catch (Exception e) { debug("Wyjatek: " + e.toString()); }
		}
	}

}

