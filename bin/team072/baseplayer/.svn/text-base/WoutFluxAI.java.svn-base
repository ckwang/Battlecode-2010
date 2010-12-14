package team072.baseplayer;

import team072.action.JustMoving;
import team072.message.MessageStack;
import team072.navigation.Map;
import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotLevel;
import battlecode.common.TerrainTile.TerrainType;

public class WoutFluxAI implements WoutAI {

	private RobotController myRC;
	private MapLocation origin;
	private MapLocation newOrigin;
	private MapLocation newPass;
	private Direction dir;
	private Direction realDir;
	private MessageStack msgStack;
	private Map myMap;
	private int myRound;

	public WoutFluxAI(RobotController myRC, MapLocation origin, Direction dir,
			MessageStack msgStack, int myRound) {
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
		goToOrigin();
		myRC.yield();
	}

	private void goToOrigin() throws GameActionException {
		while (!(newOrigin.isAdjacentTo(myRC.getLocation()) || newOrigin
				.equals(myRC.getLocation()))) {
			while (myRC.getRoundsUntilMovementIdle() != 0
					|| Clock.getBytecodeNum() > 5000 || myRC.hasActionSet()) {
				myRC.yield();
				decideOrigin();
				myRC.setIndicatorString(2, Clock.getBytecodeNum() + ","
						+ myRC.getRoundsUntilMovementIdle());
			}
			boolean back = JustMoving.act(myRC, myMap, newOrigin);
			if (back) {
				return;
			}
			// myRC.setIndicatorString(0, "willMove(goToOr):");
			myRC.setIndicatorString(1, Clock.getBytecodeNum() + ","
					+ myRC.getRoundsUntilMovementIdle());
		}
		if (myRC.senseAirRobotAtLocation(newOrigin) != null) {
			myRC.transferFlux(myRC.getFlux(), newOrigin, RobotLevel.IN_AIR);
		}

	}

	private double energonThreshold() {
		// TODO Auto-generated method stub
		return 0;
	}

	private boolean senseBorder() {
		return (myRC.senseTerrainTile(myRC.getLocation().add(Direction.NORTH))
				.getType().equals(TerrainType.OFF_MAP)
				|| myRC.senseTerrainTile(
						myRC.getLocation().add(Direction.SOUTH)).getType()
						.equals(TerrainType.OFF_MAP)
				|| myRC
						.senseTerrainTile(
								myRC.getLocation().add(Direction.WEST))
						.getType().equals(TerrainType.OFF_MAP) || myRC
				.senseTerrainTile(myRC.getLocation().add(Direction.EAST))
				.getType().equals(TerrainType.OFF_MAP));
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
									.getLocation()) < 28) {
						enemyNum++;
					}
				} catch (GameActionException e) {

				}
			}
		}
		if (enemyNum > 2) {
			return true;
		} else {
			return false;
		}

	}
	
	private void goToPass() throws GameActionException {
		while (!(newPass.isAdjacentTo(myRC.getLocation()) || newPass
				.equals(myRC.getLocation()))) {
			if (myRC.getEventualEnergonLevel() < Math.sqrt(myRC.getLocation()
					.distanceSquaredTo(newOrigin)) * 3 * 0.15 * 1.5
					|| senseEnemy()) {
				return;
			}
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
				decideOrigin();
				myRC.setIndicatorString(2, Clock.getBytecodeNum() + ","
						+ myRC.getRoundsUntilMovementIdle());
			}
			boolean back = JustMoving.act(myRC, myMap, newPass);
			if (back) {
				return;
			}
			myRC.setIndicatorString(0, "willMove(goToPass):"
					+ newOrigin.toString() + "," + newPass.toString());
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
		if (newOrigin.distanceSquaredTo(origin) < 225) {
			realDir = dir;
		} else {
			Direction temp = origin.directionTo(newOrigin);
			switch (dir) {
			case NORTH_WEST:
				if (temp.equals(Direction.NORTH)) {
					realDir = Direction.WEST;
				} else {
					realDir = Direction.NORTH;
				}
				break;
			case NORTH_EAST:
				if (temp.equals(Direction.NORTH)) {
					realDir = Direction.EAST;
				} else {
					realDir = Direction.NORTH;
				}
				break;
			case SOUTH_WEST:
				if (temp.equals(Direction.SOUTH)) {
					realDir = Direction.WEST;
				} else {
					realDir = Direction.SOUTH;
				}
				break;
			case SOUTH_EAST:
				if (temp.equals(Direction.SOUTH)) {
					realDir = Direction.EAST;
				} else {
					realDir = Direction.SOUTH;
				}
				break;
			}
		}
	}

	private void setUpDes() {
		int x = 0;
		int y = 0;
		myRC.setIndicatorString(0, realDir.toString());
		if (realDir.isDiagonal()) {
			double gradian = ((myRound / 3.5) % 90) * Math.PI / 180;
			switch (realDir) {
			case NORTH_EAST:
				x = (int) (newOrigin.getX() + 25 * Math.cos(gradian));
				y = (int) (newOrigin.getY() - 25 * Math.sin(gradian));
				break;
			case NORTH_WEST:
				x = (int) (newOrigin.getX() - 25 * Math.cos(gradian));
				y = (int) (newOrigin.getY() - 25 * Math.sin(gradian));
				break;
			case SOUTH_WEST:
				x = (int) (newOrigin.getX() - 25 * Math.cos(gradian));
				y = (int) (newOrigin.getY() + 25 * Math.sin(gradian));
				break;
			case SOUTH_EAST:
				x = (int) (newOrigin.getX() + 25 * Math.cos(gradian));
				y = (int) (newOrigin.getY() + 25 * Math.sin(gradian));
				break;
			}
		} else {
			double gradian1 = (myRound) % 180;
			int reverse = 0;
			if (gradian1 > 45) {
				reverse = 1;
			}
			double gradian = ((myRound / 3.5) % 90) * Math.PI / 180;
			switch (realDir) {
			case NORTH:
				if (reverse == 0) {
					x = (int) (newOrigin.getX() + 25 * Math.cos(gradian));
					y = (int) (newOrigin.getY() - 25 * Math.sin(gradian));
				} else {
					x = (int) (newOrigin.getX() - 25 * Math.cos(gradian));
					y = (int) (newOrigin.getY() - 25 * Math.sin(gradian));
				}
				break;
			case WEST:
				if (reverse == 0) {
					x = (int) (newOrigin.getX() - 25 * Math.cos(gradian));
					y = (int) (newOrigin.getY() - 25 * Math.sin(gradian));
				} else {
					x = (int) (newOrigin.getX() - 25 * Math.cos(gradian));
					y = (int) (newOrigin.getY() + 25 * Math.sin(gradian));
				}
				break;
			case SOUTH:
				if (reverse == 0) {
					x = (int) (newOrigin.getX() - 25 * Math.cos(gradian));
					y = (int) (newOrigin.getY() + 25 * Math.sin(gradian));
				} else {
					x = (int) (newOrigin.getX() + 25 * Math.cos(gradian));
					y = (int) (newOrigin.getY() + 25 * Math.sin(gradian));
				}
				break;
			case EAST:
				if (reverse == 0) {
					x = (int) (newOrigin.getX() + 25 * Math.cos(gradian));
					y = (int) (newOrigin.getY() + 25 * Math.sin(gradian));
				} else {
					x = (int) (newOrigin.getX() + 25 * Math.cos(gradian));
					y = (int) (newOrigin.getY() - 25 * Math.sin(gradian));
				}
				break;
			}
		}
		newPass = new MapLocation(x, y).add(Map.DIRECTIONS[(myRC.getRobot()
				.getID() % 17) % 8]);
	}

}
