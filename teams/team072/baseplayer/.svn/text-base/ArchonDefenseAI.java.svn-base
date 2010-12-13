package team072.baseplayer;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Message;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotLevel;
import battlecode.common.RobotType;
import battlecode.common.Team;
import battlecode.common.TerrainTile;
import battlecode.common.TerrainTile.TerrainType;
import java.util.ArrayList;
import team072.message.MessageDecoder;
import team072.message.MessageEncoder;
import team072.message.MessageEncoder.MessageType;
import team072.message.MessageStack;
import team072.navigation.Map;

public class ArchonDefenseAI extends ArchonAI {

	private RobotController myRC;
	private Direction myBuildingDirection;
	private MapLocation myNextBuildingLocation;
	private MapLocation myLastBuildingLocation;
	private Map myMap;
	private MessageStack myMS;
	private MapLocation myOrigin;
	private boolean returned;
	private int numBuildings;
	private MapLocation[] repairLocations;
	private int repairNum;
	private int repairIndex;
	private Direction[] directions;
	private MapLocation[] locations;
	private Robot[] airRobots;
	private Robot[] groundRobots;
	private boolean justSpawned;
	private boolean justStarted;
	private Message enemyMessage;
	private Team myTeam;

	public ArchonDefenseAI(RobotController rc, Direction buildingDirection,
			MapLocation origin, Map map, MessageStack ms) {
		myRC = rc;
		myTeam = myRC.getTeam();
		myBuildingDirection = buildingDirection;
		myNextBuildingLocation = origin;
		myLastBuildingLocation = origin;
		myOrigin = origin;
		myMap = map;
		myMS = ms;
		returned = false;
		numBuildings = 0;
		repairLocations = new MapLocation[100];
		repairNum = 0;
		repairIndex = 0;
		directions = new Direction[8];
		directions[0] = myRC.getDirection();
		locations = new MapLocation[9];
		locations[0] = myRC.getLocation();
		setNextBuildingLocation();
		justSpawned = true;
		justStarted = true;
		enemyMessage = null;
		// TODO Auto-generated constructor stub
	}

	public void proceed1() throws GameActionException {

		// System.out.println(myBuildingDirection.toString());

		// Robot[] robots = myRC.senseNearbyGroundRobots();
		// int numEnemy = 0;
		// for (Robot robot : robots) {
		// if (myRC.canSenseObject(robot)) {
		// RobotInfo info = myRC.senseRobotInfo(robot);
		// if (info.team == myRC.getTeam().opponent()) {
		// numEnemy += 1;
		// }
		// }
		// }
		//
		// if (numEnemy > 2) {
		// MessageEncoder messageEncoder = new MessageEncoder(myRC, myMS,
		// MessageType.TELE_FLEE_REQUEST);
		// myRC.broadcast(messageEncoder.encodeMessage());
		// }

		if (Clock.getBytecodeNum() > 5000) {
			myRC.getAllMessages();
			myRC.yield();
			returned = false;
		}
		if (!returned) {
			preprocess();
		}

		returned = false;

		if ((myRC.getRoundsUntilMovementIdle() != 0)) {
			myRC.getAllMessages();
			myRC.yield();
			return;
		}

		if (justSpawned) {
			if (locations[0].isAdjacentTo(myNextBuildingLocation)
					|| (locations[0].equals(myNextBuildingLocation))) {
				justSpawned = false;
				if (justStarted) {
					myNextBuildingLocation = myLastBuildingLocation;
					justStarted = false;
				}
				returned = true;
				return;
			}
			goToLocation(myNextBuildingLocation);
			return;
		}

		if (repairNum > repairIndex) {
			myNextBuildingLocation = repairLocations[repairIndex];
		}

		if (myRC.getFlux() > 4000) {
			/*
			 * Build at myNextBuildingLocation
			 */
			if (locations[0].isAdjacentTo(myNextBuildingLocation)) {
				if (myNextBuildingLocation.equals(myOrigin)) {
					for (Robot robot : groundRobots) {
						if (myRC.canSenseObject(robot)) {
							RobotInfo info = myRC.senseRobotInfo(robot);
							if ((info.team.equals(myTeam))
									&& (info.type.isBuilding())) {
								myNextBuildingLocation = info.location;
								setNextBuildingLocation();
								returned = true;
								return;
							}
						}
					}
				}
				Robot robot = myRC
						.senseGroundRobotAtLocation(myNextBuildingLocation);
				if (robot != null) {
					/*
					 * If there is already a building at the building location,
					 * add 4 along the building direction
					 */
					RobotInfo info = myRC.senseRobotInfo(robot);
					if ((info.team.equals(myTeam)) && (info.type.isBuilding())) {
						setNextBuildingLocation();
						returned = true;
						return;
					}
				}
				if (directions[0].equals(locations[0]
						.directionTo(myNextBuildingLocation))) {
					if (canSpawn(myNextBuildingLocation)) {
						myRC.spawn(RobotType.AURA);
						myRC.yield();
						if (myRC
								.senseGroundRobotAtLocation(myNextBuildingLocation) != null) {
							preprocessDirections();
							preprocessLocations();
							airRobots = myRC.senseNearbyAirRobots();
							groundRobots = myRC.senseNearbyGroundRobots();
							preprocessMessage();
							preprocessSupport(locations);
							MessageEncoder messageEncoder = new MessageEncoder(
									myRC, myMS, MessageType.ORIGIN, myOrigin,
									myBuildingDirection);
							myRC.broadcast(messageEncoder.encodeMessage());
							numBuildings += 1;
							myRC.transferFlux(100, myNextBuildingLocation,
									RobotLevel.ON_GROUND);
							if (repairNum > repairIndex) {
								repairIndex += 1;
								myNextBuildingLocation = myLastBuildingLocation;
							} else {
								myLastBuildingLocation = myNextBuildingLocation;
							}
							setNextBuildingLocation();
							justSpawned = true;
							/*
							 * If reached boundary, go back and building in the
							 * other direction
							 */
							TerrainTile tile = myRC
									.senseTerrainTile(myNextBuildingLocation);
							if (tile.getType().equals(
									TerrainTile.TerrainType.OFF_MAP)) {
								if (numBuildings < 5) {
									myBuildingDirection = myBuildingDirection
											.opposite();
									myNextBuildingLocation = myOrigin;
									setNextBuildingLocation();
								} else {
									if (!myBuildingDirection.isDiagonal()) {
										myBuildingDirection = myBuildingDirection
												.rotateLeft().rotateLeft();
									} else {
										while (tile
												.getType()
												.equals(
														TerrainTile.TerrainType.OFF_MAP)) {
											myNextBuildingLocation = myNextBuildingLocation
													.subtract(myBuildingDirection);
											tile = myRC
													.senseTerrainTile(myNextBuildingLocation);
										}
										myNextBuildingLocation = myNextBuildingLocation
												.add(myBuildingDirection
														.rotateLeft());
										tile = myRC
												.senseTerrainTile(myNextBuildingLocation);
										if (tile
												.getType()
												.equals(
														TerrainTile.TerrainType.OFF_MAP)) {
											myBuildingDirection = myBuildingDirection
													.rotateRight();
										} else {
											myBuildingDirection = myBuildingDirection
													.rotateRight();
										}
									}
									myNextBuildingLocation = myLastBuildingLocation;
								}
								numBuildings = 0;
							}
							/*
							 * End
							 */
							myRC.yield();
							goToLocation(myNextBuildingLocation);
							return;
						} else {
                            myRC.yield();
							return;
						}

					} else {
						/*
						 * Change myNextBuildingLocation to a nearby location
						 */
						newBuildingLocation();
						return;
					}
				} else {
					if (canSpawn(myNextBuildingLocation)) {
						myRC.setDirection(locations[0]
								.directionTo(myNextBuildingLocation));
						myRC.yield();
						return;
					} else {
						/*
						 * Change myNextBuildingLocation to a nearby location
						 */
						newBuildingLocation();
						return;
					}
				}
			} else if (locations[0].equals(myNextBuildingLocation)) {
				/*
				 * If I am right above the building location, move backwards
				 */
				if (myRC.canMove(directions[0].opposite())) {
					myRC.moveBackward();
					myRC.yield();
					return;
				} else if (myRC.canMove(directions[0])) {
					myRC.moveForward();
					myRC.yield();
                    return;
				} else {
					directions = Map.directionHierarchy(directions[0]
							.opposite());
					for (Direction direction : directions) {
						if (myRC.canMove(direction)) {
							myRC.setDirection(direction.opposite());
							myRC.yield();
							if (myRC.canMove(direction)) {
								myRC.moveForward();
								preprocess();
								myRC.yield();
							} else {
								returned = true;
							}
							return;
						}
					}
					myRC.yield();
					return;
				}
			} else {
				/*
				 * If not adjacent, fly towards the destination
				 */
				goToLocation(myNextBuildingLocation);
				return;
			}
		} else {
			if (myRC.getEnergonLevel() > 60) {
				if (canSpawn(locations[0].add(directions[0]))) {
					myRC.spawn(RobotType.CHAINER);
					myRC.yield();
					return;
				} else {
					for (Direction dir : directions) {
						if (canSpawn(locations[0].add(dir))) {
							myRC.setDirection(dir);
							myRC.yield();
							return;
						}
					}
					myRC.yield();
					return;
				}
			} else {
				Direction directionToGoodLocation = directionToGoodLocation();
				if (directionToGoodLocation != Direction.NONE) {
					Direction[] directionsToGoodLocation = Map
							.directionHierarchy(directionToGoodLocation);
					for (Direction direction : directionsToGoodLocation) {
						if (myRC.canMove(direction)) {
							if (directions[0].equals(direction)) {
								myRC.moveForward();
								myRC.yield();
								return;
							} else if (directions[0].equals(direction
									.opposite())) {
								myRC.moveBackward();
								myRC.yield();
								return;
							} else {
								myRC.setDirection(direction);
								myRC.yield();
								if (myRC.canMove(direction)) {
									myRC.moveForward();
									preprocess();
									myRC.yield();
								} else {
									returned = true;
								}
								return;
							}
						}
					}
				}
				myRC.yield();
				return;
			}
		}
	}

	private boolean canSpawn(MapLocation loc) throws GameActionException {
		return myRC.senseTerrainTile(loc).getType().equals(TerrainType.LAND)
				&& myRC.senseGroundRobotAtLocation(loc) == null;
	}

	private void preprocess() throws GameActionException {
		preprocessDirections();
		preprocessLocations();
		airRobots = myRC.senseNearbyAirRobots();
		groundRobots = myRC.senseNearbyGroundRobots();
		preprocessMessage();
		if (!broadcastEnemies(airRobots, groundRobots)
				&& (enemyMessage != null)) {
			MessageStack.broadcastAttack(myRC, enemyMessage);
		}
		preprocessSupport(locations);
	}

	private void preprocessDirections() throws GameActionException {
		directions = Map.directionHierarchy(myRC.getDirection());
	}

	private void preprocessLocations() throws GameActionException {
		locations[0] = myRC.getLocation();
		for (int i = 1; i < 9; i++) {
			locations[i] = locations[0].add(directions[i - 1]);
		}
	}

	private boolean broadcastEnemies(Robot[] airRobots, Robot[] groundRobots)
			throws GameActionException {

		ArrayList<MapLocation> airEnemyLocations = new ArrayList<MapLocation>();
		ArrayList<MapLocation> groundEnemyLocations = new ArrayList<MapLocation>();

		for (Robot airRobot : airRobots) {
			if (myRC.canSenseObject(airRobot)) {
				RobotInfo info = myRC.senseRobotInfo(airRobot);
				if (info.team.equals(myTeam.opponent())) {
					airEnemyLocations.add(info.location);
				}
			}
		}
		for (Robot groundRobot : groundRobots) {
			if (myRC.canSenseObject(groundRobot)) {
				RobotInfo info = myRC.senseRobotInfo(groundRobot);
				if (info.team.equals(myTeam.opponent())) {
					groundEnemyLocations.add(info.location);
				}
			}
		}
		if ((airEnemyLocations.size() != 0)
				|| (groundEnemyLocations.size() != 0)) {
			MessageEncoder messageEncoder = new MessageEncoder(myRC, myMS,
					MessageType.ENEMY_LOCS,
					airEnemyLocations.toArray(new MapLocation[airEnemyLocations
							.size()]), groundEnemyLocations
							.toArray(new MapLocation[groundEnemyLocations
									.size()]));
			myRC.broadcast(messageEncoder.encodeMessage());
			return true;
		} else {
			return false;
		}
	}

	private void preprocessMessage() throws GameActionException {
		Message[] messages = myRC.getAllMessages();
		enemyMessage = null;
		for (Message message : messages) {
			MessageDecoder messageDecoder = new MessageDecoder(myRC, myMS,
					message);
			if (messageDecoder.isValid()
					&& (messageDecoder.getType() == MessageType.BUILDING_DIED)) {
				repairLocations[repairNum] = messageDecoder.getSourceLocation();
				repairNum += 1;
			} else if ((enemyMessage == null) && messageDecoder.isEnemy()) {
				enemyMessage = message;
			}
		}
	}

	// int length;
	// if (messages.length > 18) {
	// length = 18;
	// } else {
	// length = messages.length;
	// }
	// Message[] toBroadcast = new Message[18];
	// int numToBroadcast = 0;
	// Message enemyMessage = null;
	// boolean hasEnemyMessage = false;
	// for (Message message : messages) {
	// MessageDecoder messageDecoder
	// = new MessageDecoder(myRC, myMS, message);
	// if (numToBroadcast < length) {
	// if (messageDecoder.isValid()) {
	// if ((messageDecoder.getType() == MessageType.ENEMY_LOC) ||
	// (messageDecoder.getType() ==
	// MessageType.BUILDING_SPAWNED) ||
	// (messageDecoder.getType() ==
	// MessageType.BUILDING_DIED)
	// ) {
	// toBroadcast[numToBroadcast] = message;
	// numToBroadcast += 1;
	// }
	// }
	// }
	// if (!hasEnemyMessage) {
	// if (messageDecoder.isEnemy()) {
	// enemyMessage = message;
	// hasEnemyMessage = true;
	// }
	// }
	// }
	// if (numToBroadcast != 0) {
	// MessageStack.rebroadcast(myRC, myMS, toBroadcast);
	// } else if (hasEnemyMessage) {
	// MessageStack.broadcastAttack(myRC, enemyMessage);
	// }

	private void preprocessSupport(MapLocation[] locations)
			throws GameActionException {
		/*
		 * Transfer energon and flux to nearby robots
		 */
		for (MapLocation location : locations) {
			Robot robot = myRC.senseGroundRobotAtLocation(location);
			if (robot != null) {
				RobotInfo info = myRC.senseRobotInfo(robot);
				if ((info.team.equals(myTeam)) && (info.type != RobotType.AURA)) {
					if (myRC.getEnergonLevel() > 40) {
						if (info.energonReserve < 9) {
							myRC.transferUnitEnergon(10 - info.energonReserve,
									info.location, RobotLevel.ON_GROUND);
						}
					}
				} else if ((info.team.equals(myTeam))
						&& (info.type.equals(RobotType.AURA))) {
					if (myRC.getFlux() > 100) {
						if ((info.energonReserve < 9)
								&& (info.energonLevel < 100)) {
							myRC.transferFlux(100 - (info.energonReserve * 10),
									info.location, RobotLevel.ON_GROUND);
						}
					}
				}
			}
		}
	}

	private Direction directionToGoodLocation() {
		TerrainTile tile = null;
		for (Direction direction : directions) {
			tile = myRC.senseTerrainTile(locations[0].add(direction));
			if (tile.getType() != TerrainTile.TerrainType.LAND) {
				return direction.opposite();
			}
		}

		return Direction.NONE;
	}

	private void setNextBuildingLocation() {
		for (int cnt = 0; cnt < 4; cnt++) {
			myNextBuildingLocation = myNextBuildingLocation
					.add(myBuildingDirection);
		}
	}

	private void newBuildingLocation() throws GameActionException {
		Direction[] buildingDirections = Map
				.directionHierarchy(myBuildingDirection);
		MapLocation[] buildingLocations = new MapLocation[8];
		for (int i = 0; i < 8; i++) {
			buildingLocations[i] = locations[0].add(buildingDirections[i]);
		}
		returned = true;
		for (MapLocation location : buildingLocations) {
			if (canSpawn(location)
					&& (myLastBuildingLocation.distanceSquaredTo(location) < 26)) {
				myNextBuildingLocation = location;
				return;
			}
		}
		myNextBuildingLocation = myNextBuildingLocation.add(myBuildingDirection
				.opposite());
		return;
	}

	private void goToLocation(MapLocation localLocation)
			throws GameActionException {
		Direction direction = locations[0].directionTo(localLocation);
		if (directions[0].equals(direction)) {
			if (myRC.canMove(direction)) {
				myRC.moveForward();
				myRC.yield();
				return;
			}
		} else if (directions[0].equals(direction.opposite())) {
			if (myRC.canMove(direction)) {
				myRC.moveBackward();
				myRC.yield();
				return;
			}
		}
		Direction[] bugDirections = Map.directionHierarchy(direction);
		for (Direction dir : bugDirections) {
			if (myRC.canMove(dir)) {
				myRC.setDirection(dir);
				myRC.yield();
				if (myRC.canMove(dir)) {
					myRC.moveForward();
					preprocessDirections();
					preprocessLocations();
					airRobots = myRC.senseNearbyAirRobots();
					groundRobots = myRC.senseNearbyGroundRobots();
					broadcastEnemies(airRobots, groundRobots);
					preprocessMessage();
					preprocessSupport(locations);
					myRC.yield();
				} else {
					returned = true;
				}
				return;
			}
		}
		myRC.yield();
		return;
	}
}
