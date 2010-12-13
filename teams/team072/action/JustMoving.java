package team072.action;

import team072.navigation.Map;
import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.TerrainTile.TerrainType;

public class JustMoving {
	public static boolean act(RobotController myRC, Map myMap, MapLocation loc)
			throws GameActionException {
		MapLocation myLoc = myRC.getLocation();
		if (myLoc.isAdjacentTo(loc) || myLoc.equals(loc)) {
			myRC.yield();
			return true;
		} else {
			int bytecode = Clock.getBytecodeNum();
			if (bytecode > 1000) {
				myRC.yield();
			}
			Direction dir = myMap.tangentBug(myLoc, loc);
			int bytecode1 = Clock.getBytecodeNum();
			if (dir != null) {
				if (myRC.senseTerrainTile(myRC.getLocation().add(dir))
						.getType().equals(TerrainType.OFF_MAP)) {
					return true;
				}
				moveCheckHelper(myRC, dir);
			} else {
			}
			myRC.yield();
			return false;
		}
	}

	private static void moveCheckHelper(RobotController myRC, Direction dir)
			throws GameActionException {
		while (true) {
			if (myRC.canMove(dir)) {
				goHelper(myRC, dir);
				break;
			} else if (myRC.canMove(dir.rotateLeft())) {
				goHelper(myRC, dir.rotateLeft());
				break;
			} else if (myRC.canMove(dir.rotateRight())) {
				goHelper(myRC, dir.rotateRight());
				break;
				// } else if (myRC.canMove(dir.rotateRight().rotateRight())){
				// goHelper(myRC,dir.rotateRight().rotateRight());
				// break;
				// } else if (myRC.canMove(dir.rotateLeft().rotateLeft())){
				// goHelper(myRC,dir.rotateLeft().rotateLeft());
				// break;
			} else {
				break;
			}
		}
	}

	private static void goHelper(RobotController myRC, Direction dir)
			throws GameActionException {
		if (myRC.getDirection().equals(dir)) {
			myRC.moveForward();
		} else if (myRC.getDirection().equals(dir.opposite())) {
			myRC.moveBackward();
		} else {
			myRC.setDirection(dir);
		}
	}
}
