package team072.action;

import team072.navigation.Map;
import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.TerrainTile.TerrainType;

public class Wandering {

	public static void act(RobotController myRC, MapLocation origin,
			Direction dir, Map myMap, int index) throws GameActionException {
		// System.out.println("run");
		MapLocation lastDes = myRC.getLocation();
		int lastX = lastDes.getX();
		int lastY = lastDes.getY();
		MapLocation myLoc = myRC.getLocation();
		Direction myDir = myRC.getDirection();
		int toOrigin = myLoc.distanceSquaredTo(origin);
		Robot[] robots = myRC.senseNearbyGroundRobots();
		for (Robot robot : robots) {
			if (myRC.canSenseObject(robot)) {
				try {
					RobotInfo info = myRC.senseRobotInfo(robot);
					MapLocation loc = info.location;
					if (loc.distanceSquaredTo(origin) < toOrigin) {
						int distance = myLoc.distanceSquaredTo(loc);
						lastX = lastX + repulseX(myLoc, loc, distance, index);
						lastY = lastY + repulseY(myLoc, loc, distance, index);
					}
				} catch (GameActionException e) {

				}
			}
		}
		// if (!myRC.hasBroadcastMessage()){
		// MessageEncoder encoder = new MessageEncoder(RobotController rc,
		// MessageStack ms, MessageType type,
		// MapLocation[] enemyLocs);
		// }
		lastX = lastX
				+ toOriginX(myLoc, origin, myRC.getEventualEnergonLevel(), myRC
						.getFlux(), toOrigin, index);
		lastY = lastY
				+ toOriginY(myLoc, origin, myRC.getEventualEnergonLevel(), myRC
						.getFlux(), toOrigin, index);
		lastDes = new MapLocation(lastX, lastY);
		lastDes = lastDes.add(dir);
		//myRC.setIndicatorString(0, dir.toString());
		if (myLoc.isAdjacentTo(lastDes) || myLoc.equals(lastDes)) {
			return;
		}
		Direction finalDir = myMap.tangentBug(myLoc, lastDes);
		if (finalDir == null){
			myRC.getAllMessages();
			myRC.yield();
			return;
		}
		if (Clock.getBytecodeNum() > 5000){
			myRC.yield();
		}
		//myRC.setIndicatorString(0, lastDes.equals(myRC.getLocation())+"");
		if (myRC.senseTerrainTile(myRC.getLocation().add(finalDir)).equals(TerrainType.OFF_MAP)){
			finalDir = dir;
		}
		// System.out.println(finalDir.toString());
		if (myRC.canMove(finalDir)) {
			if (myDir.equals(finalDir)) {
				myRC.moveForward();
			} else {
				myRC.setDirection(finalDir);
				myRC.getAllMessages();
				myRC.yield();
				if (myRC.canMove(myRC.getDirection())) {
					myRC.moveForward();
				}
			}
		}
		myRC.getAllMessages();
		myRC.yield();
	}

	private static int repulseX(MapLocation myLoc, MapLocation loc,
			int distanceS, int index) {
		if (index == 0) {
			return ((myLoc.getX() - loc.getX()) * 30 / distanceS);
		} else {
			return ((myLoc.getX() - loc.getX()) * 10 / distanceS);
		}
	}

	private static int repulseY(MapLocation myLoc, MapLocation loc,
			int distanceS, int index) {
		if (index == 0) {
			return ((myLoc.getY() - loc.getY()) * 30 / distanceS);
		} else {
			return ((myLoc.getY() - loc.getY()) * 10 / distanceS);
		}
	}

	private static int toOriginX(MapLocation myLoc, MapLocation origin,
			double blood, double flux, int distanceS, int index) {
		if (index == 0) {
			if (flux > 1000) {
				return (int) ((origin.getX() - myLoc.getX())
						* (30 - (blood) + 2000) * distanceS * distanceS / 50000);
			}
			return (int) ((origin.getX() - myLoc.getX()) * (30 - (blood))
					* distanceS * distanceS / 50000);
		} else {
			if (flux > 500) {
				return (int) ((origin.getX() - myLoc.getX())
						* (30 - (blood) + 2000) * distanceS * distanceS / 3000);
			}
			return (int) ((origin.getX() - myLoc.getX()) * (30 - (blood))
					* distanceS * distanceS / 3000);
		}
	}

	private static int toOriginY(MapLocation myLoc, MapLocation origin,
			double blood, double flux, int distanceS, int index) {
		if (index == 0) {
			if (flux > 1000) {
				return (int) ((origin.getX() - myLoc.getX())
						* (30 - (blood) + 2000) * distanceS * distanceS / 50000);
			}
			return (int) ((origin.getY() - myLoc.getY()) * (30 - (blood))
					* distanceS * distanceS / 50000);
		} else {
			if (flux > 500) {
				return (int) ((origin.getX() - myLoc.getX())
						* (30 - (blood) + 2000) * distanceS * distanceS / 3000);
			}
			return (int) ((origin.getY() - myLoc.getY()) * (30 - (blood))
					* distanceS * distanceS / 3000);
		}
	}
}