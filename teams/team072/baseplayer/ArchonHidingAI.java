package team072.baseplayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import team072.baseplayer.ArchonPlayer.STATUS;
import team072.message.MessageEncoder;
import team072.message.MessageStack;
import team072.message.MessageEncoder.MessageType;
import team072.navigation.Map;
import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotLevel;
import battlecode.common.RobotType;
import battlecode.common.Team;
import battlecode.common.TerrainTile;
import battlecode.common.TerrainTile.TerrainType;

public class ArchonHidingAI extends ArchonAI {

	RobotController myRC;
	public MessageStack msgStack;
	Map myMap;
	Direction boundaryOpp;
	MapLocation origin = null;
	boolean reachOrigin = false;
	ArrayList<MapLocation> outer = null;
	STATUS status;
	Direction transferFlux;
	Direction boundary = null;
	int round;
	boolean startBuilding = false;

	public ArchonHidingAI(RobotController rc, MessageStack msgStack,
			MapLocation base, Direction flux, Direction dir, STATUS s) {
		this.myRC = rc;
		this.msgStack = msgStack;
		this.boundaryOpp = dir;
		this.status = s;
		myMap = new Map(rc);
		transferFlux = flux;
		round = Clock.getRoundNum();
	}

	public Direction proceed1() throws GameActionException {
		MessageEncoder msgEncoder;
		if (myRC.getRoundsUntilMovementIdle() != 0 || myRC.hasActionSet()) {
			myRC.yield();
			return Direction.NONE;
		}

		myRC.setIndicatorString(0, "hiding");

		if (boundaryOpp.isDiagonal() && origin == null) {
			boundary = boundaryOpp;
			origin = myRC.getLocation();
			reachOrigin = true;
			return boundaryOpp;
		} else if (!boundaryOpp.isDiagonal() && !reachOrigin) {
			if (outer == null) {
				ArrayList<MapLocation> a = new ArrayList<MapLocation>();
				MapLocation myLoc = myRC.getLocation();
				while (a.size() < 2) {
					for (MapLocation e : myRC.senseAlliedArchons()) {
						if (e.distanceSquaredTo(myLoc) >= 4)
							a.add(e);
					}
					myRC.yield();
				}
				outer = a;

			}
			if (origin == null) {
				for (MapLocation e : myRC.senseAlliedArchons()) {
					if (!e.isAdjacentTo(myRC.getLocation())
							&& (boundaryOpp.equals(Direction.EAST) || boundaryOpp
									.equals(Direction.WEST))) {
						if (e.getX() != outer.get(0).getX()
								&& e.getX() != outer.get(1).getX()
								&& !e.equals(myRC.getLocation())) {
							this.origin = e;
						}
					} else if (!e.isAdjacentTo(myRC.getLocation())
							&& (boundaryOpp.equals(Direction.NORTH) || boundaryOpp
									.equals(Direction.SOUTH))) {
						if (e.getY() != outer.get(0).getY()
								&& e.getY() != outer.get(1).getY()
								&& !e.equals(myRC.getLocation())) {
							this.origin = e;
						}
					}
				}
			}
			if (!reachOrigin && origin != null) {
				Direction toOrigin = myRC.getLocation().directionTo(origin);
				myRC.setDirection(toOrigin);
				myRC.yield();
				loop: while (myRC.getLocation().distanceSquaredTo(origin) > 9) {
					while (myRC.getRoundsUntilMovementIdle() != 0) {
						myRC.yield();
					}
					for (int i = 0; i < 6 && !myRC.canMove(toOrigin); i++) {
						myRC.yield();
					}
					if (myRC.canMove(toOrigin)) {
						myRC.moveForward();
						myRC.yield();
						continue loop;
					} else
						break loop;
				}
				reachOrigin = true;
				boundaryOpp = findBoundary().opposite();
				return boundaryOpp;
			}
		}

//		if (this.status.equals(STATUS.HIDING))
//			transferFlux();
		
		if (this.startBuilding = false) {
			int buildNum = 0;
			Robot[] building = myRC.senseNearbyGroundRobots();
			Team myTeam = myRC.getTeam();
			for (Robot b : building) {
				RobotInfo info = myRC.senseRobotInfo(b);
				if (info.team.equals(myTeam) && info.type.isBuilding())
					buildNum++;
			}
			
			if (buildNum >= 1)
				this.startBuilding = true;
		}
		
//		myRC.setIndicatorString(1, ""+ );
		myRC.setIndicatorString(2, ""+startBuilding);


//		if (reachOrigin && status.equals(STATUS.HIDING)) {
//			moveDiagonal();
//		}

		supportTeam();

		// if (myRC.getEnergonLevel() > RobotType.ARCHON.maxEnergon() * 0.6) {
		// if (!canSpawn(myRC.getDirection())) {
		// Direction toSpawn = myRC.getDirection();
		// for (int i = 0; i < 7 && !canSpawn(toSpawn); i++) {
		// toSpawn = toSpawn.rotateRight();
		// }
		// myRC.setDirection(toSpawn);
		// myRC.yield();
		// if (!canSpawn(myRC.getDirection()))
		// return Direction.NONE;
		// }
		// myRC.spawn(RobotType.CHAINER);
		// round = Clock.getRoundNum();
		// myRC.yield();
		// msgEncoder = new MessageEncoder(myRC, msgStack, MessageType.ORIGIN,
		// myRC.getLocation().add(myRC.getDirection()),
		// this.boundaryOpp); // this.base shinnyih
		// myRC.broadcast(msgEncoder.encodeMessage());
		// MapLocation spawnloc = myRC.getLocation().add(myRC.getDirection());
		// myRC.yield();
		// Robot child = myRC.senseGroundRobotAtLocation(spawnloc);
		// RobotInfo info = myRC.senseRobotInfo(child);
		// double transferAmt = Math.min(10.0 - info.energonReserve, myRC
		// .getEnergonLevel() * 0.6);
		// myRC.transferUnitEnergon(transferAmt, spawnloc,
		// RobotLevel.ON_GROUND);
		// }

		if (myRC.getEnergonLevel() > 50 && Clock.getRoundNum() > round + 25) {
			while (myRC.getRoundsUntilMovementIdle() != 0)
				myRC.yield();
			if (!canSpawn(myRC.getDirection())) {
				Direction toSpawn = myRC.getDirection();
				for (int i = 0; i < 7 && !canSpawn(toSpawn); i++) {
					toSpawn = toSpawn.rotateRight();
				}
				myRC.setDirection(toSpawn);
				myRC.yield();
				if (!canSpawn(myRC.getDirection()))
					return Direction.NONE;
			}
			myRC.spawn(RobotType.WOUT);
			round = Clock.getRoundNum();
			myRC.yield();
			msgEncoder = new MessageEncoder(myRC, msgStack, MessageType.ORIGIN,
					myRC.getLocation().add(myRC.getDirection()),
					this.boundaryOpp);
			myRC.broadcast(msgEncoder.encodeMessage());
			MapLocation spawnloc = myRC.getLocation().add(myRC.getDirection());
			myRC.yield();
			Robot child = myRC.senseGroundRobotAtLocation(spawnloc);
			RobotInfo info = myRC.senseRobotInfo(child);
			double transferAmt = Math.min(info.type.maxEnergon()
					- info.eventualEnergon, GameConstants.ENERGON_RESERVE_SIZE
					- info.energonReserve);
			myRC.transferUnitEnergon(transferAmt, spawnloc,
					RobotLevel.ON_GROUND);

		}
		transferSelf();
		return Direction.NONE;
	}

	private void moveDiagonal() throws GameActionException {
		ArrayList<MapLocation> defenseAI = defenseArchon();
		MapLocation two = defenseAI.get(2);
		MapLocation three = defenseAI.get(3);
		MapLocation min = two.distanceSquaredTo(origin) <= three
				.distanceSquaredTo(origin) ? two : three;
		if (myRC.getLocation().distanceSquaredTo(min) > 9) {
			int x = origin.getX() + (min.getY() - origin.getY()) * 5 / 7;
			int y = origin.getY() + (min.getX() - origin.getX()) * 5 / 7;
			int x1 = origin.getX() + (min.getX() - origin.getX()) * 5 / 7;
			int y1 = origin.getY() + (min.getY() - origin.getY()) * 5 / 7;
			int aveX = (x + x1) / 2;
			int aveY = (y + y1) / 2;
			MapLocation locToGo = new MapLocation(aveX, aveY);
			if (locToGo.equals(myRC.getLocation())) {
				return;
			}
			myRC.setDirection(myRC.getLocation().directionTo(locToGo));
			myRC.yield();
			while (myRC.getLocation().distanceSquaredTo(locToGo) > 1) {
				while (!myRC.canMove(myRC.getDirection())
						|| myRC.getRoundsUntilMovementIdle() != 0)
					myRC.yield();
				myRC.moveForward();
				myRC.yield();
			}
		}
	}

	private ArrayList<MapLocation> defenseArchon() {
		MapLocation[] a = myRC.senseAlliedArchons();
		final MapLocation myLoc = myRC.getLocation();
		ArrayList<MapLocation> locList = new ArrayList<MapLocation>(Arrays
				.asList(a));
		Collections.sort(locList, new Comparator<MapLocation>() {
			private final MapLocation my = myLoc;

			public int compare(MapLocation a, MapLocation b) {
				if (a.distanceSquaredTo(my) > b.distanceSquaredTo(my)) {
					return 1;
				} else if (a.distanceSquaredTo(my) < b.distanceSquaredTo(my)) {
					return -1;
				} else {
					return 0;
				}
			}
		});
		return locList;
	}

	private int woutNum() throws GameActionException {
		Robot[] nearbyWouts = myRC.senseNearbyGroundRobots();
		int woutNum = 0;
		for (Robot r : nearbyWouts) {
			RobotInfo info = myRC.senseRobotInfo(r);
			if (info.team.equals(myRC.getTeam())
					&& info.type.equals(RobotType.WOUT))
				woutNum++;
		}
		return woutNum;
	}

	public void supportTeam() throws GameActionException {
		Robot[] robots = myRC.senseNearbyGroundRobots();
		Team myTeam = myRC.getTeam();
		for (int i = 0; i < robots.length && myRC.getEnergonLevel() > 20; i++) {
			RobotInfo info = myRC.senseRobotInfo(robots[i]);
			if (info.team.equals(myTeam) && !info.type.isBuilding()
					&& !info.type.equals(RobotType.ARCHON)) {
				if ((info.location.isAdjacentTo(myRC.getLocation()) || info.location
						.equals(myRC.getLocation()))) {
					if (info.eventualEnergon < info.type.maxEnergon() * 0.8) {
						double transfer = Math.min(info.type.maxEnergon()
								- info.eventualEnergon,
								GameConstants.ENERGON_RESERVE_SIZE
										- info.energonReserve);
						myRC.transferUnitEnergon(transfer, info.location,
								RobotLevel.ON_GROUND);
					}
				}
			}
		}
		for (int i = 0; i < robots.length; i++) {
			RobotInfo info = myRC.senseRobotInfo(robots[i]);
			if (info.team.equals(myTeam) && !info.type.isBuilding()
					&& !info.type.equals(RobotType.ARCHON)) {
				if ((info.location.isAdjacentTo(myRC.getLocation()) || info.location
						.equals(myRC.getLocation()))) {
					if (info.type.equals(RobotType.WOUT)
							&& myRC.getFlux() > 3000) {
						myRC.transferFlux(Math.min(myRC.getFlux(), 5000 - info.flux),
								info.location, RobotLevel.ON_GROUND);
						break;
					}
				}
			}
		}
	}

	private void transferFlux() throws GameActionException {
		switch (this.transferFlux) {
		case SOUTH_EAST:
			Direction[] d1 = { Direction.NORTH_EAST, Direction.EAST,
					Direction.WEST, Direction.NORTH_WEST, Direction.SOUTH_EAST,
					Direction.SOUTH_WEST };
			for (int i = 0; i < d1.length; i++) {
				MapLocation right = myRC.getLocation().add(d1[i]);
				Robot rightRobot = myRC.senseAirRobotAtLocation(right);
				if (rightRobot != null) {
					RobotInfo info = myRC.senseRobotInfo(rightRobot);
					if (info.type.equals(RobotType.ARCHON)
							&& info.team.equals(myRC.getTeam())) {
						double amt1 = Math.min(myRC.getFlux(),
								10000 - info.flux);
						myRC.transferFlux(amt1, right, RobotLevel.IN_AIR);
					}
				}
			}
			break;
		case NORTH_WEST:
			Direction[] d2 = { Direction.NORTH_EAST, Direction.NORTH,
					Direction.SOUTH, Direction.SOUTH_EAST,
					Direction.NORTH_WEST, Direction.SOUTH_WEST };
			for (int i = 0; i < d2.length; i++) {
				MapLocation right = myRC.getLocation().add(d2[i]);
				Robot rightRobot = myRC.senseAirRobotAtLocation(right);
				if (rightRobot != null) {
					RobotInfo info = myRC.senseRobotInfo(rightRobot);
					if (info.type.equals(RobotType.ARCHON)
							&& info.team.equals(myRC.getTeam())) {
						double amt2 = Math.min(myRC.getFlux(),
								10000 - info.flux);
						myRC.transferFlux(amt2, right, RobotLevel.IN_AIR);
					}
				}
			}
			break;
		default:
			MapLocation right = myRC.getLocation().add(transferFlux);
			Robot rightRobot = myRC.senseAirRobotAtLocation(right);
			if (rightRobot != null) {
				RobotInfo info = myRC.senseRobotInfo(rightRobot);
				if (info.type.equals(RobotType.ARCHON)
						&& info.team.equals(myRC.getTeam())) {
					double amt = Math.min(myRC.getFlux(), 10000 - info.flux);
					myRC.transferFlux(amt, right, RobotLevel.IN_AIR);
				}
			}
		}
	}

	private void transferSelf() throws GameActionException {
		if (myRC.getEnergonLevel() == myRC.getMaxEnergonLevel()) {
			double transferAmt = GameConstants.ENERGON_RESERVE_SIZE
					- myRC.getEnergonReserve();
			myRC.transferUnitEnergon(transferAmt, myRC.getLocation(),
					RobotLevel.IN_AIR);
		}
	}

	public boolean checkSurrounding(int len) {
		int x = myRC.getLocation().getX();
		int y = myRC.getLocation().getY();
		for (int i = x - len; i < x + len; i++) {
			for (int j = y - len; j < y + len; j++) {
				TerrainTile tt = myRC.senseTerrainTile(new MapLocation(i, j));
				if (!(tt.getType() == TerrainTile.TerrainType.LAND))
					return false;
			}
		}
		return true;
	}

	private Direction findBoundary() {
		Direction[] dir = { Direction.EAST, Direction.SOUTH, Direction.WEST,
				Direction.NORTH };
		List<Direction> wall = new ArrayList<Direction>(4);
		Direction bound = Direction.OMNI;
		for (Direction d : dir) {
			if (senseDir(d))
				wall.add(d);
		}
		System.out.println(wall.size());
		switch (wall.size()) {
		case 2:
			if (wall.contains(dir[0]) && wall.contains(dir[3]))
				bound = Direction.NORTH_EAST;
			else if (wall.contains(dir[2]) && wall.contains(dir[3]))
				bound = Direction.NORTH_WEST;
			else if (wall.contains(dir[2]) && wall.contains(dir[1]))
				bound = Direction.SOUTH_WEST;
			else if (wall.contains(dir[0]) && wall.contains(dir[1]))
				bound = Direction.SOUTH_EAST;
			break;
		case 1:
			bound = wall.get(0);
			break;
		default:
			System.out.print("cant find boundary");
			break;
		}
		return bound;
	}

	private boolean senseDir(Direction d) {
		MapLocation loc = myRC.getLocation();
		for (int i = 0; i < 6; i++) {
			loc = loc.add(d);
		}
		TerrainTile tt = myRC.senseTerrainTile(loc);
		return tt.equals(TerrainTile.OFF_MAP);
	}

	private boolean canSpawn(Direction dir) throws GameActionException {
		MapLocation loc = myRC.getLocation().add(dir);
		return myRC.senseTerrainTile(loc).getType() == TerrainType.LAND
				&& myRC.senseGroundRobotAtLocation(loc) == null;
	}

}
