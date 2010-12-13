package team072.action;

import team072.navigation.Map;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class GoingTo{
	public static void act(RobotController myRC, Map myMap, MapLocation loc)
			throws GameActionException {
		if (myRC.getLocation().isAdjacentTo(loc) || myRC.getLocation().equals(loc)){
			return;
		}
		Direction dir = myMap.tangentBug(myRC.getLocation(), loc);
		if (dir != null && myRC.canMove(dir)){
			if (myRC.getDirection().equals(dir)){
			} else {
				myRC.setDirection(dir);
				myRC.getAllMessages();
				myRC.yield();
			}
			while (myRC.senseGroundRobotAtLocation(myRC.getLocation().add(dir)) != null){
				myRC.getAllMessages();
				myRC.yield();
			}
			myRC.moveForward();
		} 
		myRC.getAllMessages();
		myRC.yield();
	}
}
