package team072.baseplayer;

import team072.action.JustMoving;
import team072.action.RunForFlux;
import team072.message.MessageStack;
import team072.navigation.Map;
import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.TerrainTile;
import battlecode.common.TerrainTile.TerrainType;

public class WoutEatingAI implements WoutAI {

	private RobotController myRC;
	private MapLocation origin;
	private MapLocation newOrigin;
	private MapLocation newPass;
	private Direction realDir;
	private Direction dir;
	private MessageStack msgStack;
	private Map myMap;
	private int myRound;
	private MapLocation jackSpot;
	private MapLocation jackSpot1;

	public WoutEatingAI(RobotController myRC, MapLocation origin,
			Direction dir, MessageStack msgStack, int myRound) {
		this.myRC = myRC;
		this.origin = origin;
		this.dir = dir;
		this.msgStack = msgStack;
		this.myMap = new Map(myRC);
		this.myRound = myRound;
	}

	@Override
	public void proceed() throws GameActionException {
		decideOrigin();
		setUpDes();
		goToPass();
		myRC.setIndicatorString(1, "goToEat");
		eatFluxForever();
		myRC.yield();
	}

	private boolean senseEnemy() throws GameActionException {
		Robot[] robots = myRC.senseNearbyGroundRobots();
		int enemyNum = 0;
		for (Robot robot : robots) {
			if (myRC.canSenseObject(robot)) {
				try {
					RobotInfo info = myRC.senseRobotInfo(robot);
					if (!info.team.equals(myRC.getTeam())
							&& info.location.distanceSquaredTo(myRC
									.getLocation()) < 49) {
						enemyNum++;
					}
				} catch (GameActionException e) {

				}
			}
		}
		if (enemyNum > 0) {
			return true;
		} else {
			return false;
		}

	}

	private void eatFluxForever() throws GameActionException {
		Direction faceDir = dir;
		while (true) {
			int x = RunForFlux.act(myRC, faceDir, myMap);
			if (myRC.getFlux() > 4500) {
				myRC.setIndicatorString(1, "goToSpawn");
				myRC.yield();
				boolean jackPot = spawn();
				if (jackPot) {
					myRC.setIndicatorString(1, "goToSecondSpawn");
					boolean jackPotAgain = spawnMore(jackSpot);
					if (jackPotAgain) {
						spawnMore(jackSpot1);
					}
				}
			}
			if (x == 0) {
				faceDir = myRC.getDirection().rotateLeft();
			}
			myRC.yield();
		}

	}

	private void finalConquer() {
		// TODO Auto-generated method stub

	}

	private boolean spawnMore(MapLocation jackPot) throws GameActionException {
		Direction dir = findFlux();
		for (Direction d : Map.directionHierarchy(dir)) {
			myRC.setIndicatorString(1, d.toString());
			moveAlong(d, jackPot);
			if (myRC.getFlux() > 3000) {
				jackSpot1 = spawnSomeWhere();
				if (jackSpot1 != null){
					return true;
				} else {
					return false;
				}
			}
			myRC.yield();
		}
		return false;
	}

	private Direction findFlux() throws GameActionException {
		Direction dir = myRC.getDirection();
		MapLocation myLoc = myRC.getLocation();
		double flux = myRC.senseFluxAtLocation(myLoc.add(dir));
		for (Direction d : Map.directionHierarchy(myRC.getDirection())){
			double flux1 = myRC.senseFluxAtLocation(myLoc.add(d));
			if (flux1 > flux){
				flux = flux1;
				dir = d;
			}
		}
		return dir;
	}

	private MapLocation spawnSomeWhere() throws GameActionException {
		MapLocation loc = myRC.getLocation();
		MapLocation nextLoc = myRC.getLocation();
		boolean canSpawn = false;
		for (int i = 0; i < 3; i++) {
			loc = loc.add(myRC.getDirection().opposite());
			nextLoc = loc;
		}
		canSpawn = dependSpawn(loc);
		if (canSpawn) {
			goToSpot(loc);
			while (myRC.getRoundsUntilMovementIdle() != 0
					|| myRC.hasActionSet()) {
				myRC.yield();
			}
			myRC.setDirection(myRC.getLocation().directionTo(loc));
			while (myRC.getRoundsUntilMovementIdle() != 0
					|| myRC.hasActionSet()) {
				myRC.yield();
			}
			myRC.spawn(RobotType.COMM);
			return myRC.getLocation().add(myRC.getDirection());
		} else {
			nextLoc = nextLoc(nextLoc);
			while (!loc.equals(nextLoc)){
				canSpawn = dependSpawn(nextLoc);
				if (canSpawn) {
					goToSpot(nextLoc);
					while (myRC.getRoundsUntilMovementIdle() != 0
							|| myRC.hasActionSet()) {
						myRC.yield();
					}
					myRC.setDirection(myRC.getLocation().directionTo(nextLoc));
					while (myRC.getRoundsUntilMovementIdle() != 0
							|| myRC.hasActionSet()) {
						myRC.yield();
					}
					myRC.spawn(RobotType.COMM);
					return myRC.getLocation().add(myRC.getDirection());
				} else {
					nextLoc = nextLoc(nextLoc);
				}
			}
		}
		return null;
	}

	private MapLocation nextLoc(MapLocation loc) {
		// TODO Auto-generated method stub
		return new MapLocation(loc.getX()-1,loc.getY()+1);
	}

	private boolean dependSpawn(MapLocation loc) throws GameActionException {
		return myRC.senseTerrainTile(loc).getType().equals(TerrainType.LAND)
				&& myRC.senseGroundRobotAtLocation(loc) == null;
	}

	private void moveAlong(Direction d, MapLocation jackPot)
			throws GameActionException {
		while (true) {
			if (myRC.getLocation().distanceSquaredTo(jackPot) > 100
					|| senseEnemy()
					|| myRC.senseTerrainTile(myRC.getLocation().add(d)).equals(
							TerrainTile.OFF_MAP)
					|| myRC.senseTerrainTile(myRC.getLocation().add(d))
							.getType().equals(TerrainType.VOID)
					|| myRC.getFlux() > 3000) {
				break;
			}
			while (myRC.getRoundsUntilMovementIdle() != 0
					|| myRC.hasActionSet()) {
				myRC.yield();
			}
			if (myRC.canMove(d)) {
				if (myRC.getDirection().equals(d)) {
					myRC.moveForward();
				} else if (myRC.getDirection().equals(d.opposite())) {
					myRC.moveBackward();
				} else {
					myRC.setDirection(d);
				}
			} else {
				break;
			}
		}
		goToSpot(jackPot);

	}

	private void goToSpot(MapLocation Pass) throws GameActionException {
		while (!(Pass.isAdjacentTo(myRC.getLocation()))) {
			while (myRC.getRoundsUntilMovementIdle() != 0
					|| Clock.getBytecodeNum() > 5000 || myRC.hasActionSet()) {
				// myRC.setIndicatorString(1,
				// Clock.getBytecodeNum()+","+myRC.getRoundsUntilMovementIdle());
				myRC.yield();
				myRC.setIndicatorString(2, Clock.getBytecodeNum() + ","
						+ myRC.getRoundsUntilMovementIdle());
			}
			boolean back = JustMoving.act(myRC, myMap, Pass);
			if (back) {
				return;
			}
			// senseNextArchons();
		}

	}

	private boolean spawn() throws GameActionException {
		Direction originalDir = myRC.getDirection();
		Direction tempDir = myRC.getDirection();
		for (int i = 0; i < 8; i++) {
			MapLocation newLoc = myRC.getLocation().add(tempDir);
			if (myRC.senseTerrainTile(newLoc).getType()
					.equals(TerrainType.LAND)
					&& myRC.senseGroundRobotAtLocation(newLoc) == null) {
				if (tempDir.equals(myRC.getDirection())) {
					while (myRC.hasActionSet()
							|| myRC.getRoundsUntilMovementIdle() != 0) {
						myRC.yield();
					}
					myRC.spawn(RobotType.COMM);
					jackSpot = myRC.getLocation().add(myRC.getDirection());
					return true;
				} else {
					while (myRC.hasActionSet()
							|| myRC.getRoundsUntilMovementIdle() != 0) {
						myRC.yield();
					}
					myRC.setDirection(tempDir);
					while (myRC.hasActionSet()
							|| myRC.getRoundsUntilMovementIdle() != 0) {
						myRC.yield();
					}
					// myRC.setIndicatorString(1, "ready to spawn");
					myRC.spawn(RobotType.COMM);
					jackSpot = myRC.getLocation().add(myRC.getDirection());
					while (myRC.hasActionSet()
							|| myRC.getRoundsUntilMovementIdle() != 0) {
						myRC.yield();
					}
					// myRC.yield();
					myRC.setDirection(originalDir);
					return true;
				}
			} else {
				tempDir = tempDir.rotateRight();
			}
		}
		return false;
	}

	private void goToPass() throws GameActionException {
		while (!(newPass.isAdjacentTo(myRC.getLocation()) || newPass
				.equals(myRC.getLocation()))) {
			if ((myRC.canSenseSquare(newPass) && (myRC
					.senseTerrainTile(newPass).getType().equals(
							TerrainType.OFF_MAP) || myRC.senseTerrainTile(
					newPass).getType().equals(TerrainType.VOID)))) {
				return;
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
			if (back) {
				return;
			}
			// senseNextArchons();
		}

	}

	private void decideOrigin() throws GameActionException {
		MapLocation[] locs = myRC.senseAlliedArchons();
		MapLocation finalDes = locs[0];
		for (int i = 1; i < locs.length; i++) {
			if (Math.abs(finalDes.getX() - origin.getX())
					+ Math.abs(finalDes.getY() - origin.getY()) > Math
					.abs(locs[i].getX() - origin.getX())
					+ Math.abs(locs[i].getY() - origin.getY())) {
				finalDes = locs[i];
			}
		}
		newOrigin = finalDes;
		decideDir();
	}

	private void decideDir() {
		realDir = origin.directionTo(newOrigin);
	}

	private void setUpDes() {
		int x = 0;
		int y = 0;
		if (realDir.equals(Direction.OMNI)) {
			realDir = dir;
		}
		switch (realDir) {
		case NORTH:
			y = -7;
			break;
		case SOUTH:
			y = 7;
			break;
		case EAST:
			x = 7;
			break;
		case WEST:
			x = -7;
			break;
		case NORTH_WEST:
			y = -7;
			break;
		case NORTH_EAST:
			y = -7;
			break;
		case SOUTH_WEST:
			y = 7;
			break;
		case SOUTH_EAST:
			y = 7;
			break;
		}
		newPass = new MapLocation(myRC.getLocation().getX() + x, myRC
				.getLocation().getY()
				+ y);
	}

}
