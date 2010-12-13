package team072.baseplayer;

import team072.action.GoingTo;
import team072.action.JustMoving;
import team072.action.Wandering;
import team072.baseplayer.WoutPlayer.Status;
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

public class WoutOuterAI implements WoutAI {

	private RobotController myRC;
	private MapLocation origin;
	private MapLocation home;
	private MapLocation newOrigin;
	private MapLocation pass1;
	private MapLocation pass2;
	private Direction dir;
	private MessageStack msgStack;
	private Map myMap;
	private int myRound;

	public WoutOuterAI(RobotController myRC, Direction dir, MapLocation origin,
			MessageStack msgStack) {
		this.myRC = myRC;
		this.dir = dir;
		this.origin = origin;
		this.msgStack = msgStack;
		this.myMap = new Map(myRC);
		home = myRC.getLocation();
		myRound = 0;
		decideOrigin();
		setUpDes();
	}

	@Override
	public void proceed() throws GameActionException {
		decideOrigin();
		MapLocation temp = myRC.getLocation();
		MapLocation firstPass = new MapLocation(temp.getX() + pass1.getX(),
				temp.getY() + pass1.getY());
		MapLocation secondPass = new MapLocation(temp.getX() + pass2.getX(),
				temp.getY() + pass2.getY());
		myRC.setIndicatorString(0, "willMove(goToPass1)");
		goToPass(firstPass);
		// if (myRC.getEventualEnergonLevel() > (Math.sqrt(myRC.getLocation()
		// .distanceSquaredTo(firstPass))
		// + Math.sqrt(firstPass.distanceSquaredTo(secondPass))
		// + Math.sqrt(secondPass.distanceSquaredTo(home))) * 3 * 0.15) {
		// goToPass(firstPass);
		// }
		if (myRC.getEventualEnergonLevel() > Math.sqrt(myRC.getLocation()
				.distanceSquaredTo(secondPass)) * 3 * 0.15 * 1.5) {
			myRC.setIndicatorString(0, "willMove(goToPass1)");
			goToPass(secondPass);
		}
		goToHome();
		goToOrigin();
		goToHome();
		myRC.yield();
	}

	private void goToHome() throws GameActionException {
		while (!(home.isAdjacentTo(myRC.getLocation()) || home.equals(myRC
				.getLocation()))) {
			while (myRC.getRoundsUntilMovementIdle() != 0
					|| Clock.getBytecodeNum() > 5000) {
				myRC.yield();
				decideOrigin();
				myRC.setIndicatorString(2, Clock.getBytecodeNum() + ","
						+ myRC.getRoundsUntilMovementIdle());
			}
			if (senseEnemy()){
				return;
			}
			JustMoving.act(myRC, myMap, home);
			// myRC.setIndicatorString(0, "willMove(goToOr):");
			myRC.setIndicatorString(1, Clock.getBytecodeNum() + ","
					+ myRC.getRoundsUntilMovementIdle());
		}
	}

	private void setUpDes() {
		int x = 0;
		int y = 0;
		int x1 = 0;
		int y1 = 0;
		switch (dir) {
		case NORTH_EAST:
			x = 20;
			y1 = -20;
			break;
		case NORTH_WEST:
			x = -20;
			y1 = -20;
			break;
		case SOUTH_WEST:
			x = -20;
			y1 = 20;
			break;
		case SOUTH_EAST:
			x = 20;
			y1 = 20;
			break;
		}
		pass1 = new MapLocation(x, y);
		pass2 = new MapLocation(x1, y1);
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

	private void goToPass(MapLocation newPass) throws GameActionException {
		while (!(newPass.isAdjacentTo(myRC.getLocation()) || newPass
				.equals(myRC.getLocation()))) {
			if (myRC.getEventualEnergonLevel() < Math.sqrt(myRC.getLocation()
					.distanceSquaredTo(home)) * 3 * 0.15 * 1.5) {
				return;
			}
			if ((myRC.canSenseSquare(newPass) && (myRC
					.senseTerrainTile(newPass).getType().equals(
							TerrainType.OFF_MAP) || myRC.senseTerrainTile(
					newPass).getType().equals(TerrainType.VOID)))
					|| senseEnemy()) {
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
//			myRC.setIndicatorString(0, "willMove(goToPass):"
//					+ newOrigin.toString() + "," + newPass.toString());
			// senseNextArchons();
		}

	}

	private void decideOrigin() {
		MapLocation[] locs = myRC.senseAlliedArchons();
		MapLocation finalDes = locs[0];
		for (int i = 1; i < locs.length; i++) {
			if (Math.abs(finalDes.getX() - origin.getX())
					+ Math.abs(finalDes.getY() - origin.getY()) > Math
					.abs(locs[i].getX() - origin.getX())
					+ Math.abs(locs[i].getY() - origin.getY())) {
				finalDes = locs[i];
			}
			if (locs[i].isAdjacentTo(home) || locs[i].equals(home)) {
				home = locs[i];
			}
		}
		newOrigin = finalDes;
		// MapLocation[] locs = myRC.senseAlliedArchons();
		// MapLocation finalDes = locs[0];
		// MapLocation finalDes1 = locs[1];
		// for (int i = 1; i < locs.length; i++) {
		// if (Math.abs(finalDes.getX() - origin.getX()) > Math.abs(locs[i]
		// .getX()
		// - origin.getX())) {
		// finalDes = locs[i];
		// }
		// if (Math.abs(finalDes1.getY() - origin.getY()) > Math.abs(locs[i]
		// .getY()
		// - origin.getY())) {
		// finalDes1 = locs[i];
		// }
		// if (locs[i].isAdjacentTo(home) || locs[i].equals(home)) {
		// home = locs[i];
		// }
		// }
		// if (finalDes.distanceSquaredTo(origin) >
		// finalDes1.distanceSquaredTo(origin)){
		// newOrigin = finalDes1;
		// } else {
		// newOrigin = finalDes;
		// }
	}

}
