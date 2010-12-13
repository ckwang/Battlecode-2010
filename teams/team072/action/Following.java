package team072.action;

import team072.navigation.Map;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;

public class Following {
	private RobotController myRC;
	private Map myMap;

	public Following(RobotController rc) {
		myRC = rc;
		myMap = new Map(myRC);
	}

	public static void act(RobotController myRC, Object... objects)
			throws GameActionException {
		Robot[] robots = myRC.senseNearbyAirRobots();
		for (int i = 0; i < robots.length; i++) {
			if (myRC.canSenseObject(robots[i])
					&& robots[i].getID() == (Integer) objects[0]) {
				MapLocation leaderLoc = myRC.senseRobotInfo(robots[i]).location;
				MapLocation myLoc = myRC.getLocation();
				if (myLoc.distanceSquaredTo(leaderLoc) < 4) {
					Direction dir = myLoc.directionTo(leaderLoc).opposite();
					if (!myRC.canMove(dir)) {
						if (myRC.canMove(dir.rotateLeft())) {
							dir = dir.rotateLeft();
						} else if (myRC.canMove(dir.rotateRight())) {
							dir = dir.rotateRight();
						} else if (myRC.canMove(dir.rotateLeft().rotateLeft())) {
							dir = dir.rotateLeft().rotateLeft();
						} else if (myRC.canMove(dir.rotateLeft().rotateLeft())) {
							dir = dir.rotateRight().rotateRight();
						} else {
							myRC.yield();
							return;
						}
					}
					if (!myRC.getDirection().equals(dir)) {
						myRC.setDirection(dir);
					}
					myRC.yield();
					if (myRC.canMove(myRC.getDirection())) {
						myRC.moveForward();
					}
				} else if (myLoc.distanceSquaredTo(leaderLoc) > 5) {
					Direction dir = myLoc.directionTo(leaderLoc);
					if (!myRC.canMove(dir)) {
						if (myRC.canMove(dir.rotateLeft().rotateLeft())) {
							dir = dir.rotateLeft().rotateLeft();
						} else if (myRC
								.canMove(dir.rotateRight().rotateRight())) {
							dir = dir.rotateRight().rotateRight();
						} else if (myRC
								.canMove(dir.rotateRight().rotateRight())) {
							dir = dir.rotateRight().rotateRight().rotateRight();
						} else {
							myRC.yield();
							return;
						}
					}
					if (!myRC.getDirection().equals(dir)) {
						myRC.setDirection(dir);
					}
					myRC.yield();
					if (myRC.canMove(myRC.getDirection())) {
						myRC.moveForward();
					}
				} else {
					Direction dir = myLoc.directionTo(leaderLoc);
					if (!myRC.getDirection().equals(dir) && myRC.canMove(dir)) {

					} else if (myRC.canMove(dir.rotateLeft().rotateLeft())) {
						dir = dir.rotateLeft().rotateLeft();
					} else if (myRC.canMove(dir.rotateRight().rotateRight())) {
						dir = dir.rotateRight().rotateRight();
					} else if (myRC.canMove(dir.rotateRight().rotateRight()
							.rotateRight())) {
						dir = dir.rotateRight().rotateRight().rotateRight();
					} else {
						myRC.yield();
						return;
					}
					if (!myRC.getDirection().equals(dir)) {
						myRC.setDirection(dir);
					}
					myRC.yield();
					if (myRC.canMove(myRC.getDirection())) {
						myRC.moveForward();
					}
				}
			}
		}
	}

}
