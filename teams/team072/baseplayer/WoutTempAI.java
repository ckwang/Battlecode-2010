package team072.baseplayer;

import team072.action.JustMoving;
import team072.baseplayer.WoutPlayer.Status;
import team072.message.MessageStack;
import team072.navigation.Map;
import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotLevel;
import battlecode.common.TerrainTile.TerrainType;

public class WoutTempAI implements WoutAI {

	private RobotController myRC;
	private MapLocation origin;
	private MapLocation newOrigin;
	private MapLocation newPass;
	private MapLocation newDes;
	private Direction dir;
	private MessageStack msgStack;
	private Map myMap;
	private Status myStatus;
	private int myRound;
	private int beginning = 0;

	public WoutTempAI(RobotController myRC, MapLocation origin, Direction dir,
			MessageStack msgStack, Status myStatus, int myRound) {
		this.myRC = myRC;
		this.origin = origin;
		this.dir = dir;
		this.msgStack = msgStack;
		this.myMap = new Map(myRC);
		this.myStatus = myStatus;
		this.myRound = myRound;
	}

	@Override
	public void proceed() throws GameActionException {
		rush();
	}

	private void rush() throws GameActionException {
		if (beginning == 0) {
			if (!decideDes()) {
				myRC.suicide();
			}
			beginning++;
			// myRC.setIndicatorString(0, newOrigin.toString());
		}
		setUpDes();
		if (myRC.getEventualEnergonLevel() < energonThreshold()) {
			senseNextArchons();
			myRC.yield();
		}
		// setUpDes();
		if (myRC.getFlux() >= 2000) {
			myRC.setIndicatorString(0, "toDes:DeliverFlux");
			deliverFlux();
			myRC.setIndicatorString(0, "toOrigin");
			goToOrigin();
		} else {
			myRC.setIndicatorString(0, "toPass");
			goToPass();
			myRC.setIndicatorString(0, "toDes");
			goToDes();
//			if (myRC.getEventualEnergonLevel() > 20) {
//				myRC.setIndicatorString(0, "toPass");
//				goToPass();
//				myRC.setIndicatorString(0, "toDes");
//				goToDes();
//			}
			myRC.setIndicatorString(0, "toOrigin");
			goToOrigin();
			myRC.setIndicatorString(0, "done");
		}
		myRC.yield();
	}

	private void deliverFlux() throws GameActionException {
		decideDes();
		while (!(newDes.isAdjacentTo(myRC.getLocation()) || newDes.equals(myRC
				.getLocation()))) {
			while (myRC.getRoundsUntilMovementIdle() != 0
					|| Clock.getBytecodeNum() > 5000) {
				// myRC.setIndicatorString(1,
				// Clock.getBytecodeNum()+","+myRC.getRoundsUntilMovementIdle());
				myRC.yield();
				senseNextArchons();
				myRC.setIndicatorString(2, Clock.getBytecodeNum() + ","
						+ myRC.getRoundsUntilMovementIdle());
			}
			JustMoving.act(myRC, myMap, newDes);
			myRC.setIndicatorString(0, "willMove(goToDes):");
			myRC.setIndicatorString(1, Clock.getBytecodeNum() + ","
					+ myRC.getRoundsUntilMovementIdle());
			// senseNextArchons();
		}
		myRC.transferFlux(myRC.getFlux(), newDes, RobotLevel.IN_AIR);

	}

	private double energonThreshold() {
		return Math.min((newOrigin.distanceSquaredTo(newPass)
				+ newPass.distanceSquaredTo(newDes) + newDes
				.distanceSquaredTo(newOrigin)) * 3 * 0.15, 28);
	}

	private void goToOrigin() throws GameActionException {
		while (!(newOrigin.isAdjacentTo(myRC.getLocation())
				|| newOrigin.equals(myRC.getLocation()))) {
			while (myRC.getRoundsUntilMovementIdle() != 0
					|| Clock.getBytecodeNum() > 5000) {
				myRC.yield();
				senseNextArchons();
				myRC.setIndicatorString(2, Clock.getBytecodeNum() + ","
						+ myRC.getRoundsUntilMovementIdle());
			}
			JustMoving.act(myRC, myMap, newOrigin);
			myRC.setIndicatorString(0, "willMove(goToOr):");
			myRC.setIndicatorString(1, Clock.getBytecodeNum() + ","
					+ myRC.getRoundsUntilMovementIdle());
			// senseNextArchons();
		}

	}

	private void goToPass() throws GameActionException {
		while (!(newPass.isAdjacentTo(myRC.getLocation())
				|| newPass.equals(myRC.getLocation()))) {
			while (myRC.getRoundsUntilMovementIdle() != 0
					|| Clock.getBytecodeNum() > 5000) {
				// myRC.setIndicatorString(1,
				// Clock.getBytecodeNum()+","+myRC.getRoundsUntilMovementIdle());
				myRC.yield();
				senseNextArchons();
				myRC.setIndicatorString(2, Clock.getBytecodeNum() + ","
						+ myRC.getRoundsUntilMovementIdle());
			}
			if (myRC.canSenseSquare(newPass)
					&& (myRC.senseTerrainTile(newPass).getType().equals(
							TerrainType.OFF_MAP) || myRC.senseTerrainTile(
							newPass).getType().equals(TerrainType.VOID))) {
				return;
			}
			JustMoving.act(myRC, myMap, newPass);
			myRC.setIndicatorString(0, "willMove(goToPass):"
					+ newOrigin.toString() + "," + newPass.toString());
			myRC.setIndicatorString(1, Clock.getBytecodeNum() + ","
					+ myRC.getRoundsUntilMovementIdle());
			// senseNextArchons();
		}

	}

	private void setUpDes() {
		int x = 0;
		int y = 0;
		double gradian = ((myRound / 3) % 45) * Math.PI / 180;
		switch (dir) {
		case NORTH_EAST:
			x = (int) (newOrigin.getX() + 15 * Math.cos(gradian));
			y = (int) (newOrigin.getY() - 15 * Math.sin(gradian));
			break;
		case NORTH_WEST:
			x = (int) (newOrigin.getX() - 15 * Math.cos(gradian));
			y = (int) (newOrigin.getY() - 15 * Math.sin(gradian));
			break;
		case SOUTH_WEST:
			x = (int) (newOrigin.getX() - 15 * Math.cos(gradian));
			y = (int) (newOrigin.getY() + 15 * Math.sin(gradian));
			break;
		case SOUTH_EAST:
			x = (int) (newOrigin.getX() + 15 * Math.cos(gradian));
			y = (int) (newOrigin.getY() + 15 * Math.sin(gradian));
			break;
		}
		newPass = new MapLocation(x, y).add(Map.DIRECTIONS[(myRC.getRobot()
				.getID() % 17) % 8]);
	}

	private void goToDes() throws GameActionException {
		if (newDes == null) {
			return;
		}
		decideDes();
		while (!(newDes.isAdjacentTo(myRC.getLocation()) || newDes.equals(myRC
				.getLocation()))) {
			while (myRC.getRoundsUntilMovementIdle() != 0
					|| Clock.getBytecodeNum() > 5000) {
				// myRC.setIndicatorString(1,
				// Clock.getBytecodeNum()+","+myRC.getRoundsUntilMovementIdle());
				myRC.yield();
				senseNextArchons();
				myRC.setIndicatorString(2, Clock.getBytecodeNum() + ","
						+ myRC.getRoundsUntilMovementIdle());
			}
			JustMoving.act(myRC, myMap, newDes);
			myRC.setIndicatorString(0, "willMove(goToDes):");
			myRC.setIndicatorString(1, Clock.getBytecodeNum() + ","
					+ myRC.getRoundsUntilMovementIdle());
			// senseNextArchons();
		}
		if (myRC.senseAirRobotAtLocation(newDes) != null) {
			myRC.transferFlux(myRC.getFlux(), newDes, RobotLevel.IN_AIR);
		}

	}

	private void senseNextArchons() {
		MapLocation[] locs = myRC.senseAlliedArchons();
		if (senseNextArchon(newDes, locs) == null) {
			if (!decideDes()) {
				myRC.suicide();
			}
			return;
		}
		// senseOrigin(locs);
		// decideDes();

	}

	private MapLocation senseNextArchon(MapLocation des, MapLocation[] locs) {
		for (MapLocation loc : locs) {
			if (loc.equals(des)) {
				return des;
			}
			if (loc.isAdjacentTo(des)) {
				return loc;
			}
		}
		return null;
	}

	private void senseOrigin(MapLocation[] locs) {
		for (int i = 0; i < locs.length; i++) {
			if (locs[0].equals(newOrigin)) {
				return;
			}
			if (locs[i].distanceSquaredTo(newOrigin) < locs[0]
					.distanceSquaredTo(newOrigin)) {
				locs[0] = locs[i];
			}
		}
		newOrigin = locs[0];
	}

	private boolean decideDes() {
		MapLocation[] locs = myRC.senseAlliedArchons();
		if (locs.length < 6) {
			return false;
		}
		for (int i = 1; i < locs.length; i++) {
			for (int j = 0; j < i; j++) {
				if (locs[i].distanceSquaredTo(origin) < locs[j]
						.distanceSquaredTo(origin)) {
					MapLocation temp = locs[j];
					locs[j] = locs[i];
					locs[i] = temp;
				}
			}
		}
		MapLocation pointer;
		if (newOrigin == null) {
			pointer = myRC.getLocation();
		} else {
			pointer = newOrigin;
		}
		if (locs[2].distanceSquaredTo(pointer) < locs[3]
				.distanceSquaredTo(pointer)) {
			newOrigin = locs[0];
			newDes = locs[2];
		} else {
			newOrigin = locs[0];
			newDes = locs[3];
		}
		return true;

	}

}
