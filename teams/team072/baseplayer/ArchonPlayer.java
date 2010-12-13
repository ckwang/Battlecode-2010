package team072.baseplayer;

import team072.message.MessageDecoder;
import team072.message.MessageEncoder;
import team072.message.MessageStack;
import team072.message.MessageEncoder.MessageType;
import battlecode.common.*;
import battlecode.common.TerrainTile.TerrainType;

/**
 * Archon Player
 * 
 * @author USER
 * 
 */
public class ArchonPlayer extends BasePlayer {
	private Direction[] DIRECTIONS = { Direction.NORTH, Direction.NORTH_EAST,
			Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH,
			Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST };

	public enum STATUS {
		DEFENSE, WALL
	}

	private STATUS myStatus;
	private ArchonAI ai;
	private ArchonAI ai2 = null;
	private RobotController myRC;
	protected MapLocation origin; // origin for the L strategy -shinnyih
	protected MessageStack myStack; // message stack -shinnyih
	private int count = 1;
	private Direction changed = Direction.NONE;
	private Direction dir = Direction.NONE;
	private Direction goDir = Direction.NONE;
	private MapLocation largestP;
	private MapLocation oPlace;
	private MapLocation smallestP;
	private int priority;

	public ArchonPlayer(RobotController rc) {
		super(rc);
		myRC = rc;
		myType = RobotType.ARCHON;
		myStack = new MessageStack();
		oPlace = myRC.getLocation();
	}

	@Override
	public void proceed() throws Exception {
		if (count == 1) {
			myRC.setDirection(Direction.NORTH);
			MapLocation[] locs = myRC.senseAlliedArchons();
			int largestX = locs[0].getX();
			int largestY = locs[0].getY();
			int smallestX = locs[0].getX();
			int smallestY = locs[0].getY();
			for (int j = 1; j < 6; j++) {
				if (largestX < locs[j].getX()) {
					largestX = locs[j].getX();
				}
				if (largestY < locs[j].getY()) {
					largestY = locs[j].getY();
				}
				if (smallestX > locs[j].getX()) {
					smallestX = locs[j].getX();
				}
				if (smallestY > locs[j].getY()) {
					smallestY = locs[j].getY();
				}
			}
			largestP = new MapLocation(largestX, largestY);
			smallestP = new MapLocation(smallestX, smallestY);
			dir = setDirection();
			Message msg = new Message();
			if (dir == Direction.NONE) {

			} else {
				for (int i = 0; i < 8; i++) {
					if (DIRECTIONS[i].equals(dir)) {
						int[] ints = { i };
						msg.ints = ints;
						myRC.broadcast(msg);
					}
				}
			}
			count++;
			myRC.yield();
			return;
		} else if (count == 2) {
			Message[] msgs = myRC.getAllMessages();
			if (!dir.isDiagonal()) {
				for (Message msg : msgs) {
					if (msg.ints != null) {
						if (DIRECTIONS[msg.ints[0]].isDiagonal()) {
							dir = DIRECTIONS[msg.ints[0]];
							break;
						} else {
							dir = DIRECTIONS[msg.ints[0]];
						}
					}
				}
			}
			dependStatus();
			ai = new ArchonWallAI(myRC, myStack, smallestP, priority, dir, goDir);
			count++;
			return;
		} else if (myStatus.equals(STATUS.WALL)){
			MapLocation origin = ((ArchonWallAI) ai).proceed1();
			if (origin != null){
				myStatus = STATUS.DEFENSE;
				ai = new ArchonBuildAI(myRC,origin);
			}
		} else {
			ai.proceed();
		}
	}

	public void dependStatus() {
		int shape = 0;
		if (largestP.getX() - smallestP.getX() == 1) {
			shape = 1;
		}
		switch (dir) {
		case NORTH:
			if (shape == 0) {
				if (oPlace.getY() == largestP.getY()
						&& oPlace.getX() != smallestP.getX() + 1) {
					if (oPlace.getX() == largestP.getX()) {
						priority = 0;
						goDir = Direction.EAST;
					} else {
						priority = 1;
						goDir = Direction.WEST;
					}
					myStatus = STATUS.WALL;
				} else if (oPlace.getX() == smallestP.getX() + 1
						&& oPlace.getY() == largestP.getY()) {
					priority = 5;
					myStatus = STATUS.WALL;
				} else {
					if (oPlace.getX() == largestP.getX()) {
						priority = 2;
						myStatus = STATUS.WALL;
					} else if (oPlace.getX() == largestP.getX() - 1) {
						priority = 3;
						myStatus = STATUS.WALL;
					} else {
						priority = 4;
						myStatus = STATUS.WALL;
					}
				}
			} else {
				if (oPlace.getY() == largestP.getY()) {
					if (oPlace.getX() == largestP.getX()) {
						priority = 0;
						goDir = Direction.EAST;
					} else {
						priority = 1;
						goDir = Direction.WEST;
					}
					myStatus = STATUS.WALL;
				} else if (oPlace.getY() == smallestP.getY() + 1) {
					if (oPlace.getX() == largestP.getX()) {
						priority = 5;
						myStatus = STATUS.WALL;
					} else {
						priority = 2;
						myStatus = STATUS.WALL;
					}
				} else {
					if (oPlace.getX() == largestP.getX()) {
						priority = 3;
						myStatus = STATUS.WALL;
					} else {
						priority = 4;
						myStatus = STATUS.WALL;
					}
				}
			}
			break;
		case EAST:
			if (shape == 0) {
				if (oPlace.getX() == largestP.getX()) {
					if (oPlace.getY() == largestP.getY()) {
						priority = 0;
						goDir = Direction.SOUTH;
					} else {
						priority = 1;
						goDir = Direction.NORTH;
					}
					myStatus = STATUS.WALL;
				} else if (oPlace.getX() == largestP.getX() - 1) {
					if (oPlace.getY() == largestP.getY()) {
						priority = 5;
						myStatus = STATUS.WALL;
					} else {
						priority = 2;
						myStatus = STATUS.WALL;
					}
				} else {
					if (oPlace.getY() == largestP.getY()) {
						priority = 3;
						myStatus = STATUS.WALL;
					} else {
						priority = 4;
						myStatus = STATUS.WALL;
					}
				}
			} else {
				if (oPlace.getX() == smallestP.getX()
						&& oPlace.getY() != largestP.getY() - 1) {
					if (oPlace.getY() == largestP.getY()) {
						priority = 0;
						goDir = Direction.SOUTH;
					} else {
						priority = 1;
						goDir = Direction.NORTH;
					}
					myStatus = STATUS.WALL;
				} else if (oPlace.getY() == largestP.getY() - 1
						&& oPlace.getX() == smallestP.getX()) {
					priority = 5;
					myStatus = STATUS.WALL;
				} else {
					if (oPlace.getY() == largestP.getY()) {
						priority = 2;
						myStatus = STATUS.WALL;
					} else if (oPlace.getY() == largestP.getY() - 1) {
						priority = 3;
						myStatus = STATUS.WALL;
					} else {
						priority = 4;
						myStatus = STATUS.WALL;
					}
				}
			}
			break;
		case SOUTH:
			if (shape == 0) {
				if (oPlace.getY() == smallestP.getY()
						&& oPlace.getX() != smallestP.getX() + 1) {
					if (oPlace.getX() == largestP.getX()) {
						priority = 0;
						goDir = Direction.EAST;
					} else {
						priority = 1;
						goDir = Direction.WEST;
					}
					myStatus = STATUS.WALL;
				} else if (oPlace.getX() == smallestP.getX() + 1
						&& oPlace.getY() == smallestP.getY()) {
					priority = 5;
					myStatus = STATUS.WALL;
				} else {
					if (oPlace.getX() == largestP.getX()) {
						priority = 2;
						myStatus = STATUS.WALL;
					} else if (oPlace.getX() == largestP.getX() - 1) {
						priority = 3;
						myStatus = STATUS.WALL;
					} else {
						priority = 4;
						myStatus = STATUS.WALL;
					}
				}
			} else {
				if (oPlace.getY() == largestP.getY()) {
					if (oPlace.getX() == largestP.getX()) {
						priority = 0;
						goDir = Direction.EAST;
					} else {
						priority = 1;
						goDir = Direction.WEST;
					}
					myStatus = STATUS.WALL;
				} else if (oPlace.getY() == largestP.getY() - 1) {
					if (oPlace.getX() == largestP.getX()) {
						priority = 5;
						myStatus = STATUS.WALL;
					} else {
						priority = 2;
						myStatus = STATUS.WALL;
					}
				} else {
					if (oPlace.getX() == largestP.getX()) {
						priority = 3;
						myStatus = STATUS.WALL;
					} else {
						priority = 4;
						myStatus = STATUS.WALL;
					}
				}
			}
			break;
		case WEST:
			if (shape == 0) {
				if (oPlace.getX() == smallestP.getX()) {
					if (oPlace.getY() == largestP.getY()) {
						priority = 0;
						goDir = Direction.SOUTH;
					} else {
						priority = 1;
						goDir = Direction.NORTH;
					}
					myStatus = STATUS.WALL;
				} else if (oPlace.getX() == largestP.getX() - 1) {
					if (oPlace.getY() == largestP.getY()) {
						priority = 5;
						myStatus = STATUS.WALL;
					} else {
						priority = 2;
						myStatus = STATUS.WALL;
					}
				} else {
					if (oPlace.getY() == largestP.getY()) {
						priority = 3;
						myStatus = STATUS.WALL;
					} else {
						priority = 4;
						myStatus = STATUS.WALL;
					}
				}
			} else {
				if (oPlace.getX() == largestP.getX()
						&& oPlace.getY() != largestP.getY() - 1) {
					if (oPlace.getY() == largestP.getY()) {
						priority = 0;
						goDir = Direction.SOUTH;
					} else {
						priority = 1;
						goDir = Direction.NORTH;
					}
					myStatus = STATUS.WALL;
				} else if (oPlace.getY() == largestP.getY() - 1
						&& oPlace.getX() == largestP.getX()) {
					priority = 5;
					myStatus = STATUS.WALL;
				} else {
					if (oPlace.getY() == largestP.getY()) {
						priority = 2;
						myStatus = STATUS.WALL;
					} else if (oPlace.getY() == largestP.getY() - 1) {
						priority = 3;
						myStatus = STATUS.WALL;
					} else {
						priority = 4;
						myStatus = STATUS.WALL;
					}
				}
			}
			break;
		case NORTH_WEST:
			if (shape == 0) {
				int level = (oPlace.getX() - smallestP.getX()) * 2
						+ oPlace.getY() - smallestP.getY();
				if (level < 5) {
					priority = level;
					myStatus = STATUS.WALL;
				} else {
					priority = 5;
					myStatus = STATUS.WALL;
				}
			} else {
				int level = (oPlace.getY() - smallestP.getY()) * 2
						+ oPlace.getX() - smallestP.getX();
				if (level < 5) {
					priority = level;
					myStatus = STATUS.WALL;
				} else {
					priority = 5;
					myStatus = STATUS.WALL;
				}
			}
			break;
		case SOUTH_WEST:
			if (shape == 0) {
				int level = (largestP.getX() - oPlace.getX()) * 2
						+ oPlace.getY() - smallestP.getY();
				if (level > 0) {
					priority = level - 1;
					myStatus = STATUS.WALL;
				} else {
					priority = 5;
					myStatus = STATUS.WALL;
				}
			} else {
				int level = (oPlace.getY() - smallestP.getY()) * 2
						+ largestP.getX() - oPlace.getX();
				if (level > 0) {
					priority = level - 1;
					myStatus = STATUS.WALL;
				} else {
					priority = 5;
					myStatus = STATUS.WALL;
				}
			}
			break;
		case NORTH_EAST:
			if (shape == 0) {
				int level = (oPlace.getX() - smallestP.getX()) * 2
						+ largestP.getY() - oPlace.getY();
				if (level > 0) {
					priority = level - 1;
					myStatus = STATUS.WALL;
				} else {
					priority = 5;
					myStatus = STATUS.WALL;
				}
			} else {
				int level = (largestP.getY() - oPlace.getY()) * 2
						+ oPlace.getX() - smallestP.getX();
				if (level > 0) {
					priority = level - 1;
					myStatus = STATUS.WALL;
				} else {
					priority = 5;
					myStatus = STATUS.WALL;
				}
			}
			break;
		case SOUTH_EAST:
			if (shape == 0) {
				int level = (oPlace.getX() - smallestP.getX()) * 2
						+ oPlace.getY() - smallestP.getY();
				if (level > 0) {
					priority = level - 1;
					myStatus = STATUS.WALL;
				} else {
					priority = 5;
					myStatus = STATUS.WALL;
				}
			} else {
				int level = (oPlace.getY() - smallestP.getY()) * 2
						+ oPlace.getX() - smallestP.getX();
				if (level > 0) {
					priority = level -1;
					myStatus = STATUS.WALL;
				} else {
					priority = 5;
					myStatus = STATUS.WALL;
				}
			}
			break;

		}
	}

	public Direction setDirection() {
		MapLocation myLoc = myRC.getLocation();
		Direction dir = Direction.NORTH;
		if (myRC.senseTerrainTile(
				new MapLocation(myLoc.getX(), myLoc.getY() + 5)).getType()
				.equals(TerrainType.OFF_MAP)) {
			dir = Direction.NORTH;
			if (myRC.senseTerrainTile(
					new MapLocation(myLoc.getX() + 5, myLoc.getY())).getType()
					.equals(TerrainType.OFF_MAP)) {
				dir = dir.rotateLeft();

			} else if (myRC.senseTerrainTile(
					new MapLocation(myLoc.getX() - 5, myLoc.getY())).getType()
					.equals(TerrainType.OFF_MAP)) {
				dir = dir.rotateRight();
			}
		} else if (myRC.senseTerrainTile(
				new MapLocation(myLoc.getX(), myLoc.getY() - 5)).getType()
				.equals(TerrainType.OFF_MAP)) {
			dir = Direction.SOUTH;
			if (myRC.senseTerrainTile(
					new MapLocation(myLoc.getX() + 5, myLoc.getY())).getType()
					.equals(TerrainType.OFF_MAP)) {
				dir = dir.rotateRight();

			} else if (myRC.senseTerrainTile(
					new MapLocation(myLoc.getX() - 5, myLoc.getY())).getType()
					.equals(TerrainType.OFF_MAP)) {
				dir = dir.rotateLeft();
			}
		} else if (myRC.senseTerrainTile(
				new MapLocation(myLoc.getX() + 5, myLoc.getY())).getType()
				.equals(TerrainType.OFF_MAP)) {
			dir = Direction.WEST;
			if (myRC.senseTerrainTile(
					new MapLocation(myLoc.getX(), myLoc.getY() - 5)).getType()
					.equals(TerrainType.OFF_MAP)) {
				dir = dir.rotateLeft();

			} else if (myRC.senseTerrainTile(
					new MapLocation(myLoc.getX(), myLoc.getY() + 5)).getType()
					.equals(TerrainType.OFF_MAP)) {
				dir = dir.rotateRight();
			}
		} else {
			dir = Direction.EAST;
			if (myRC.senseTerrainTile(
					new MapLocation(myLoc.getX(), myLoc.getY() - 5)).getType()
					.equals(TerrainType.OFF_MAP)) {
				dir = dir.rotateRight();

			} else if (myRC.senseTerrainTile(
					new MapLocation(myLoc.getX(), myLoc.getY() + 5)).getType()
					.equals(TerrainType.OFF_MAP)) {
				dir = dir.rotateLeft();
			}
		}
		return dir;
	}

	private Direction getBuildDirection1(Direction dir) {
		if (dir.equals(Direction.EAST) || dir.equals(Direction.NORTH)
				|| dir.equals(Direction.SOUTH) || dir.equals(Direction.WEST)) {
			return dir.rotateRight().rotateRight();
		} else if (dir.equals(Direction.NORTH_EAST)
				|| dir.equals(Direction.NORTH_WEST)) {
			return Direction.NORTH;
		} else {
			return Direction.SOUTH;
		}
	}

	private Direction getBuildDirection2(Direction dir) {
		if (dir.equals(Direction.EAST) || dir.equals(Direction.NORTH)
				|| dir.equals(Direction.SOUTH) || dir.equals(Direction.WEST)) {
			return dir;
		} else if (dir.equals(Direction.NORTH_EAST)
				|| dir.equals(Direction.SOUTH_EAST)) {
			return Direction.EAST;
		} else {
			return Direction.WEST;
		}
	}
}