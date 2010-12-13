package team072.action;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

/**
 * Spawn new units
 * 
 * @author USER
 * 
 */
public class Spawning {

	/**
	 * In the future, it should take a parameter that specify which type to
	 * spawn possibly which direction to spawn to(Second parameter)
	 */
	public static void act(RobotController myRC, RobotType objectType)
			throws GameActionException {
		myRC.spawn(objectType);
		myRC.yield();
		//myRC.transferUnitEnergon(10, myRC.getLocation()
		//		.add(myRC.getDirection()), RobotLevel.ON_GROUND);
	}

}