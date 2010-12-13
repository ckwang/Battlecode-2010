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

public class RunForFlux {

	public static int act(RobotController myRC, Direction dir, Map myMap)
			throws GameActionException {
		MapLocation newLoc = myRC.getLocation();
		MapLocation temp = myRC.getLocation();
		RobotInfo info = null;
		Robot[] robots = myRC.senseNearbyGroundRobots();
		for (Robot robot : robots) {
			if (myRC.canSenseObject(robot)) {
				try {
					info = myRC.senseRobotInfo(robot);
					MapLocation loc = info.location;
					if ((!info.team.equals(myRC.getTeam()))
							&& loc.distanceSquaredTo(newLoc) < 16) {
						if (temp.equals(myRC.getLocation())) {
							temp = loc;
						} else if (loc.distanceSquaredTo(newLoc) < temp
								.distanceSquaredTo(newLoc)) {
							temp = loc;
						}
					}
				} catch (GameActionException e) {

				}
			}
		}
		if (!temp.equals(newLoc)) {
			int distance = newLoc.distanceSquaredTo(temp);
			newLoc = repulse(newLoc, temp, distance, info.directionFacing);
		} else {
			for (int i = 0; i < 3; i++) {
				newLoc = newLoc.add(dir);
			}
			 myRC.setIndicatorString(1, "did change:" + dir.toString());
		}
		// myRC.setIndicatorString(1, newLoc.equals(myRC.getLocation())+"");
		// myRC.setIndicatorString(1, newLoc
		// .equals(myRC.getLocation()) + ":" + newLoc.toString() +
		// myRC.getLocation().toString());
		return RunForFlux.goToPass(myRC, newLoc, myMap);

	}

	private static int goToPass(RobotController myRC, MapLocation newPass,
			Map myMap) throws GameActionException {
		while (!(newPass.isAdjacentTo(myRC.getLocation()) || newPass
				.equals(myRC.getLocation()))) {
			if ((myRC.canSenseSquare(newPass) && (myRC
					.senseTerrainTile(newPass).getType().equals(
							TerrainType.OFF_MAP) || myRC.senseTerrainTile(
					newPass).getType().equals(TerrainType.VOID)))) {
				return 0;
			}
			while (myRC.getRoundsUntilMovementIdle() != 0
					|| Clock.getBytecodeNum() > 5000 || myRC.hasActionSet()) {
				// myRC.setIndicatorString(1,
				// Clock.getBytecodeNum()+","+myRC.getRoundsUntilMovementIdle());
				myRC.yield();
				myRC.setIndicatorString(2, Clock.getBytecodeNum() + ","
						+ myRC.getRoundsUntilMovementIdle());
			}
			boolean back = JustMoving.act(myRC, myMap, newPass);
			myRC.setIndicatorString(1, back + "");
			if (back) {
				return 0;
			}
		}
		return 1;
		// myRC.setIndicatorString(1, "gethere");

	}

	public static MapLocation repulse(MapLocation myLoc, MapLocation loc,
			int distanceS, Direction dir) {
		MapLocation newLoc = loc.add(dir.opposite()).add(dir.opposite()).add(
				dir.opposite()).add(dir.opposite()).add(dir.opposite());
		newLoc = new MapLocation((myLoc.getX()*3 + newLoc.getX())/4,(myLoc.getY()*3 + newLoc.getY())/4);
		return newLoc;
	}
}
