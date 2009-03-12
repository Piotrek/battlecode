package pb233978;

import battlecode.common.*;
import static battlecode.common.GameConstants.*;

public class RobotPlayer implements Runnable {

	private final RobotController myRC;

	//static private int ArchonID = 1;
	//static private int WorkerID = 2;
	//...

	public RobotPlayer(RobotController rc) 
	{
		myRC = rc;
	}

	public void run() 
	{
		BaseRobot robot;

		if (RobotType.valueOf("ARCHON") == myRC.getRobotType())
			robot = new ArchonRobot(myRC);
		else if (RobotType.valueOf("WORKER") == myRC.getRobotType())
			robot = new WorkerRobot(myRC);
		else if (RobotType.valueOf("SOLDIER") == myRC.getRobotType())
			robot = new SoldierRobot(myRC);
		else if (RobotType.valueOf("CANNON") == myRC.getRobotType())
			robot = new CannonRobot(myRC);
		else if (RobotType.valueOf("CHANNELER") == myRC.getRobotType())
			robot = new ChannelerRobot(myRC);
		else if (RobotType.valueOf("SCOUT") == myRC.getRobotType())
			robot = new ScoutRobot(myRC);
		else {
			System.out.println("Niesklasyfikowany robot :(");
			return;
		}

		robot.run();
	}

}

