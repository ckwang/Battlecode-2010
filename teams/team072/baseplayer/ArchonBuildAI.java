package team072.baseplayer;

import java.util.ArrayList;
import java.util.List;
import team072.message.MessageDecoder;
import team072.message.MessageEncoder;
import team072.message.MessageStack;
import team072.message.MessageEncoder.MessageType;
import team072.navigation.Map;
import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
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

public class ArchonBuildAI extends ArchonAI {

    private final int TELEPORT_THRESHOLD = 10;
    private final int BUILD_THRESHOLD = 4000;
    private final int FREEZE_TIME = 5;
    private final int SPAWN_THRESHOLD = 50;
    private final int SUPPORT_ENERGON_MIN = 40;
    private final int BUILDING_FLUX_TARGET = 500;
    private final int SPAWN_INTERVAL = 25;
    private int WOUT_RATIO = 3;

    private RobotController myRC;
    private MessageStack myMS;
    // Identity information
    private Team myTeam;
    private MapLocation myOrigin;
    private Direction myBuildingDirection;
    private MapLocation myNextBuildingLocation;
    private MapLocation[] myLastBuildingLocation;
    private Direction corner;
    // Repair
    private MapLocation[] repairLocations;
    private int repairNum;
    private int repairIndex;
    // State trackers
    private boolean returned;
    private int count;
    private int round = 0;
    private int numBuildings;
    private boolean justSpawned;
    private boolean dontMove;
    private int woutCount;
    private boolean justStarted;
    private int countFlee;
    private boolean turned;
    // Instance variables used as local variables
    private Message[] allMessages;
    private MessageEncoder messageEncoder;
    private Message enemyMessage;
    private Direction[] directions;
    private MapLocation[] locations;
    private Robot[] airRobots;
    private Robot[] groundRobots;
    private Direction directionToGoodLocation;
    private Direction[] bugDirections;
    private RobotInfo info;
    private Robot robot;
    private MapLocation tempBuildingLocation;

//	private boolean justStarted;
    public ArchonBuildAI(RobotController rc, MapLocation origin) {
        myRC = rc;
        myMS = new MessageStack();

        // Identity information
        myTeam = myRC.getTeam();
        myOrigin = origin;
        myNextBuildingLocation = origin;
        myLastBuildingLocation = new MapLocation[2];
        myLastBuildingLocation[0] = origin;
        myLastBuildingLocation[1] = origin;

        // Repair
        repairLocations = new MapLocation[100];
        repairNum = 0;
        repairIndex = 0;

        // State trackers
        returned = false;
        numBuildings = 0;
        justSpawned = true;
        count = 0;
        round = Clock.getRoundNum();
        dontMove = false;
        woutCount = 0;
        justStarted = true;
        turned = false;

        // Local variables used as instance variables
        directions = new Direction[8];
        directions[0] = myRC.getDirection();
        locations = new MapLocation[9];
        locations[0] = myRC.getLocation();
        bugDirections = new Direction[8];

//		justStarted = true;
    }

    @Override
    public int proceed() throws GameActionException {
        // Terminate round if bytecode exceeded
        if (Clock.getBytecodeNum() > 5000) {
            myRC.getAllMessages();

            myRC.yield();

            returned = false;
        }

        // If new round, preprocess
        if (!returned) {
            preprocess();
        }

        returned = false;

        // End round if can't move
        if ((myRC.getRoundsUntilMovementIdle() != 0) || dontMove) {
            myRC.getAllMessages();

            myRC.yield();
            return 0;
        }

        // Go to origin and detect boundary
        if (corner == null) {
            if (!locations[0].equals(myOrigin) && !locations[0].isAdjacentTo(myOrigin)) {
                goToLocation(myOrigin);
                return 0;
            } else {
                corner = findBoundary().opposite();
                myBuildingDirection = corner;
                setNextBuildingLocation();
                myBuildingDirection = corner.rotateLeft();
            }
        }

        // Go to next location right after spawning
        if (/*(countFlee > 20) ||*/ (justSpawned && ((numBuildings < BUILD_THRESHOLD) || (count > 20)))) {
            if (!locations[0].equals(myNextBuildingLocation) && !locations[0].isAdjacentTo(myNextBuildingLocation)) {
                if (myRC.getRoundsUntilMovementIdle() == 0) {
                    goToLocation(myNextBuildingLocation);
                    return 0;
                } else {
                    if ((myRC.getEnergonLevel() > SPAWN_THRESHOLD) && (Clock.getRoundNum() - round > SPAWN_INTERVAL)) {
                        if (canSpawn(locations[0].add(directions[0]))) {
                            myRC.spawn(RobotType.WOUT);

                            round = Clock.getRoundNum();

                            myRC.yield();

                            //yoyoyo
                            // Preprocess everything except broadcasting
                            preprocessDirections();
                            preprocessLocations();
                            airRobots = myRC.senseNearbyAirRobots();
                            groundRobots = myRC.senseNearbyGroundRobots();
                            preprocessMessage();
                            preprocessSupport(locations);

                            // Education
                            if (woutCount < WOUT_RATIO) {
                                messageEncoder = new MessageEncoder(myRC, myMS, MessageType.ORIGINX, myOrigin, corner);
                                woutCount += 1;
                            } else {
                                messageEncoder = new MessageEncoder(myRC, myMS, MessageType.ORIGINY, myOrigin, corner);
                                woutCount = 0;
                            }
                            myRC.broadcast(messageEncoder.encodeMessage());

                            returned = true;
                            return 0;
                        }
                    }

                    myRC.yield();
                    return 0;
                }
            } else {
                if (justStarted) {
					myNextBuildingLocation = myLastBuildingLocation[0];
					justStarted = false;
				}
                justSpawned = false;
            }
        }

        // Go to next location right after spawning
//        if (justSpawned && (numBuildings < 4)) {
//            if (!locations[0].equals(myNextBuildingLocation)
//                    && !locations[0].isAdjacentTo(myNextBuildingLocation)) {
//                goToLocation(myNextBuildingLocation);
//                return 0;
//            } else {
//                justSpawned = false;
//            }
//        }

        // Set next building location to repair location
        if (repairNum > repairIndex) {
            myNextBuildingLocation = repairLocations[repairIndex];
        }

        // Build at next building location if flux is enough
        if (myRC.getFlux() > BUILD_THRESHOLD) {
            if (locations[0].isAdjacentTo(myNextBuildingLocation)) {
                if (directions[0].equals(locations[0].directionTo(myNextBuildingLocation))) {
                    if (canSpawn(myNextBuildingLocation)) {
                        myRC.spawn(RobotType.TELEPORTER);

                        myRC.yield();

                        myRC.setDirection(directions[0].rotateLeft());

                        if (myRC.senseGroundRobotAtLocation(myNextBuildingLocation) != null) {
                            count = 0;
                            numBuildings += 1;
                            justSpawned = true;


                            // Preprocess everything except broadcasting
                            preprocessDirections();
                            preprocessLocations();
                            airRobots = myRC.senseNearbyAirRobots();
                            groundRobots = myRC.senseNearbyGroundRobots();
                            preprocessMessage();
                            preprocessSupport(locations);

                            // Education
                            messageEncoder = new MessageEncoder(myRC, myMS, MessageType.ORIGIN, myOrigin, corner);
                            myRC.broadcast(messageEncoder.encodeMessage());

                            myRC.transferFlux(100, myNextBuildingLocation,
                                    RobotLevel.ON_GROUND);

                            if (repairNum > repairIndex) {
                                repairIndex += 1;
                                myNextBuildingLocation = myLastBuildingLocation[0];
                            } else {

                                tempBuildingLocation = myNextBuildingLocation;
                                setNextBuildingLocation();

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
                                            turned = true;

                                            toggleBuildingDirection();
                                        }
                                    }
                                    numBuildings = 0;
                                }

                                myNextBuildingLocation = tempBuildingLocation;

                                if (!turned) {
                                    toggleBuildingDirection();
                                
                                    myLastBuildingLocation[0] = myNextBuildingLocation;
                                    myNextBuildingLocation = myLastBuildingLocation[1];
                                    myLastBuildingLocation[1] = myLastBuildingLocation[0];
//                                } else {
//                                    setNextBuildingLocation();
                                }
                            }

                            setNextBuildingLocation();






//                            myRC.setIndicatorString(1, myNextBuildingLocation.toString());

                            if (numBuildings > (TELEPORT_THRESHOLD - 1)) {
                                myRC.yield();

                                // Preprocess everything except broadcasting
                                preprocessDirections();
                                preprocessLocations();
                                airRobots = myRC.senseNearbyAirRobots();
                                groundRobots = myRC.senseNearbyGroundRobots();
                                preprocessMessage();
                                preprocessSupport(locations);

                                // Request to be teleported to next building
                                // location
                                messageEncoder = new MessageEncoder(myRC, myMS,
                                        MessageType.TELE_BUILD_REQUEST);
                                myRC.broadcast(messageEncoder.encodeMessage());
                            }
                            returned = true;
                            return 0;
                        }
                        return 0;
                    } else {
                        // Change next building location to a nearby location
                        newBuildingLocation();
                        return 0;
                    }
                } else {
                    if (canSpawn(myNextBuildingLocation)) {
                        myRC.setDirection(locations[0].directionTo(myNextBuildingLocation));

                        myRC.yield();
                        return 0;
                    } else {
                        // Change next building location to a nearby location
                        newBuildingLocation();
                        return 0;
                    }
                }
            } else if (locations[0].equals(myNextBuildingLocation)) {
                // Move backwards if right above building location
                if (myRC.canMove(directions[0].opposite())) {
                    myRC.moveBackward();

                    myRC.yield();
                    return 0;
                } else if (myRC.canMove(directions[0])) {
                    myRC.moveForward();

                    myRC.yield();
                    return 0;
                } else {
                    directions = Map.directionHierarchy(directions[0].opposite());
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
                            return 0;
                        }
                    }
                    myRC.yield();
                    return 0;
                }
            } else {
                // Fly towards location if not at or adjacent
                if ((numBuildings < TELEPORT_THRESHOLD) || (count > FREEZE_TIME) || (countFlee > FREEZE_TIME)) {
                    if (myRC.getRoundsUntilMovementIdle() == 0) {
                        goToLocation(myNextBuildingLocation);
                        return 0;
                    } else {
                        if ((myRC.getEnergonLevel() > SPAWN_THRESHOLD) && (Clock.getRoundNum() - round > SPAWN_INTERVAL)) {
                            if (canSpawn(locations[0].add(directions[0]))) {
                                myRC.spawn(RobotType.WOUT);

                                round = Clock.getRoundNum();

                                myRC.yield();

                                //yoyoyo
                                // Preprocess everything except broadcasting
                                preprocessDirections();
                                preprocessLocations();
                                airRobots = myRC.senseNearbyAirRobots();
                                groundRobots = myRC.senseNearbyGroundRobots();
                                preprocessMessage();
                                preprocessSupport(locations);

                                // Education
                                if (woutCount < WOUT_RATIO) {
                                    messageEncoder = new MessageEncoder(myRC, myMS, MessageType.ORIGINX, myOrigin, corner);
                                    woutCount += 1;
                                } else {
                                    messageEncoder = new MessageEncoder(myRC, myMS, MessageType.ORIGINY, myOrigin, corner);
                                    woutCount = 0;
                                }
                                myRC.broadcast(messageEncoder.encodeMessage());

                                returned = true;
                            }
                        }
                    }
                }
                myRC.yield();
                return 0;
            }
        } else {
            // Spawn wouts if flux enough
            if ((myRC.getEnergonLevel() > SPAWN_THRESHOLD) && (Clock.getRoundNum() - round > SPAWN_INTERVAL)) {
                if (canSpawn(locations[0].add(directions[0]))) {
                    myRC.spawn(RobotType.WOUT);

                    round = Clock.getRoundNum();

                    myRC.yield();
                    
                    //yoyoyo
                    // Preprocess everything except broadcasting
                    preprocessDirections();
                    preprocessLocations();
                    airRobots = myRC.senseNearbyAirRobots();
                    groundRobots = myRC.senseNearbyGroundRobots();
                    preprocessMessage();
                    preprocessSupport(locations);

                    // Education
                    if (woutCount < WOUT_RATIO) {
                        messageEncoder = new MessageEncoder(myRC, myMS, MessageType.ORIGINX, myOrigin, corner);
                        woutCount += 1;
                    } else {
                        messageEncoder = new MessageEncoder(myRC, myMS, MessageType.ORIGINY, myOrigin, corner);
                        woutCount = 0;
                    }
                    myRC.broadcast(messageEncoder.encodeMessage());

                    returned = true;
                    return 0;
                } else {
                    for (Direction direction : directions) {
                        if (canSpawn(locations[0].add(direction))) {
                            myRC.setDirection(direction);

                            myRC.yield();
                            return 0;
                        }
                    }
                    myRC.yield();
                    return 0;
                }
            } else {
                // Move to nearby location if not surrounded all by land
                directionToGoodLocation = directionToGoodLocation();
                if (directionToGoodLocation != Direction.NONE) {
                    Direction[] directionsToGoodLocation = Map.directionHierarchy(directionToGoodLocation);
                    for (Direction direction : directionsToGoodLocation) {
                        if (myRC.canMove(direction)) {
                            if (directions[0].equals(direction)) {
                                myRC.moveForward();

                                myRC.yield();
                                return 0;
                            } else if (directions[0].equals(direction.opposite())) {
                                myRC.moveBackward();

                                myRC.yield();
                                return 0;
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
                                return 0;
                            }
                        }
                    }
                }
                myRC.yield();
                return 0;
            }
        }
    }

    // Perform a set of actions at beginning of each round
    private void preprocess() throws GameActionException {
        preprocessDirections();
        preprocessLocations();
        airRobots = myRC.senseNearbyAirRobots();
        groundRobots = myRC.senseNearbyGroundRobots();
        preprocessMessage();
        if (!broadcastEnemies(airRobots, groundRobots) && (enemyMessage != null)) {
            MessageStack.broadcastAttack(myRC, enemyMessage);
        }
        preprocessSupport(locations);
        count += 1;
        countFlee += 1;
    }

    // Determine if the location is spawnable
    private boolean canSpawn(MapLocation loc) throws GameActionException {
        return myRC.senseTerrainTile(loc).getType().equals(TerrainType.LAND) &&
                myRC.senseGroundRobotAtLocation(loc) == null;
    }

    // Broadcast enemy locations sensed
    private boolean broadcastEnemies(Robot[] airRobots, Robot[] groundRobots)
            throws GameActionException {
        ArrayList<MapLocation> airEnemyLocations = new ArrayList<MapLocation>();
        ArrayList<MapLocation> groundEnemyLocations = new ArrayList<MapLocation>();

        for (Robot airRobot : airRobots) {
            if (myRC.canSenseObject(airRobot)) {
                info = myRC.senseRobotInfo(airRobot);
                if (info.team.equals(myTeam.opponent())) {
                    airEnemyLocations.add(info.location);
                }
            }
        }

        for (Robot groundRobot : groundRobots) {
            if (myRC.canSenseObject(groundRobot)) {
                info = myRC.senseRobotInfo(groundRobot);
                if (info.team.equals(myTeam.opponent())) {
                    groundEnemyLocations.add(info.location);
                }
            }
        }

        if (groundEnemyLocations.size() > 3) {
            messageEncoder = new MessageEncoder(myRC, myMS,
                                        MessageType.TELE_BUILD_REQUEST);
            countFlee = 0;
            myRC.broadcast(messageEncoder.encodeMessage());
            myRC.yield();
//            dontMove = true;
        }

        if ((airEnemyLocations.size() != 0) || (groundEnemyLocations.size() != 0)) {
            messageEncoder = new MessageEncoder(myRC, myMS, MessageType.ENEMY_LOCS,
                    airEnemyLocations.toArray(new MapLocation[airEnemyLocations.size()]),
                    groundEnemyLocations.toArray(new MapLocation[groundEnemyLocations.size()]));
            myRC.broadcast(messageEncoder.encodeMessage());
            return true;
        } else {
            return false;
        }
    }

    // Check for repair requests
    private void preprocessMessage() throws GameActionException {
        allMessages = myRC.getAllMessages();

        enemyMessage = null;

        for (Message message : allMessages) {
            MessageDecoder messageDecoder = new MessageDecoder(myRC, myMS, message);

            if (messageDecoder.isValid()) {
                if (messageDecoder.getType() == MessageType.BUILDING_DIED) {
                    repairLocations[repairNum] = messageDecoder.getSourceLocation();
                    repairNum += 1;
                }

                myMS.updateDontProcess(message);
            } else if (messageDecoder.isEnemy()) {
                enemyMessage = message;
            }
        }
    }

    // Construct an array of directions following direction hierarchy
    private void preprocessDirections() throws GameActionException {
        directions = Map.directionHierarchy(myRC.getDirection());
    }

    // Construct an array of locations representing the archons location plus nearby eight locations
    private void preprocessLocations() throws GameActionException {
        locations[0] = myRC.getLocation();
        for (int i = 1; i < 9; i++) {
            locations[i] = locations[0].add(directions[i - 1]);
        }
    }

    // Transfer energon and flux to nearby robots
    private void preprocessSupport(MapLocation[] locations)
            throws GameActionException {
        dontMove = false;

        for (MapLocation location : locations) {
            robot = myRC.senseGroundRobotAtLocation(location);
            if (robot != null) {
                info = myRC.senseRobotInfo(robot);
                if ((info.team.equals(myTeam)) && (!info.type.isBuilding())) {
                    if (myRC.getEnergonLevel() > SUPPORT_ENERGON_MIN) {
                        if (info.energonReserve < 9) {
                            myRC.transferUnitEnergon(10 - info.energonReserve,
                                    info.location, RobotLevel.ON_GROUND);
                        }
                    }
                } else if ((info.team.equals(myTeam)) && (info.type.isBuilding())) {
                    if (myRC.getFlux() > 100) {
                        if ((info.energonReserve < 9)
                                && (info.energonLevel < BUILDING_FLUX_TARGET)) {
                            myRC.transferFlux(100 - (info.energonReserve * 10),
                                    info.location, RobotLevel.ON_GROUND);
                        }

                        if ((info.energonLevel + 10) * 10 < BUILDING_FLUX_TARGET) {
                            dontMove = true;
                        }
                    }
                }
            }
        }
    }

    // Find a good direction to avoid non-land type tiles
    private Direction directionToGoodLocation() {
        TerrainTile tile = null;
        Direction goodDirection = null;
        int countTile = 0;
        for (Direction direction : directions) {
            tile = myRC.senseTerrainTile(locations[0].add(direction));
            if (tile.getType() != TerrainTile.TerrainType.LAND) {
                goodDirection = direction.opposite();
                countTile += 1;
            }
        }
        if (goodDirection == null) {
            return Direction.NONE;
        } else if (countTile == 8) {
            return directions[0];
        } else {
            return goodDirection;
        }
    }

    // Switch building direction
    private void toggleBuildingDirection() {
        if (myBuildingDirection == corner.rotateLeft()) {
            myBuildingDirection = corner.rotateRight();
        } else if (myBuildingDirection == corner.rotateRight()) {
            myBuildingDirection = corner.rotateLeft();
        }
    }

    // Add four to last building location
    private void setNextBuildingLocation() {
        for (int cnt = 0; cnt < 4; cnt++) {
            myNextBuildingLocation = myNextBuildingLocation.add(myBuildingDirection);
        }
    }

    // Get nearby building location if original is occupied
    private void newBuildingLocation() throws GameActionException {
        Direction[] buildingDirections = Map.directionHierarchy(myBuildingDirection);
        MapLocation[] buildingLocations = new MapLocation[8];
        for (int i = 0; i < 8; i++) {
            buildingLocations[i] = locations[0].add(buildingDirections[i]);
        }

        returned = true;

        for (MapLocation location : buildingLocations) {
            if (canSpawn(location) && (myLastBuildingLocation[1].distanceSquaredTo(location) < 26)) {
                myNextBuildingLocation = location;
                return;
            }
        }

        myNextBuildingLocation = myNextBuildingLocation.add(myBuildingDirection.opposite());
        return;
    }

    // Move towards destination
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

        bugDirections = Map.directionHierarchy(direction);

        for (Direction dir : bugDirections) {
            if (myRC.canMove(dir)) {
                myRC.setDirection(dir);
                myRC.yield();
                if (myRC.canMove(dir)) {
                    myRC.moveForward();

                    // Preprocess
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

    private boolean spawnWout() throws GameActionException {
        while (myRC.getRoundsUntilMovementIdle() != 0) {
            myRC.yield();
        }
        if (!canSpawn(myRC.getDirection())) {
            Direction toSpawn = myRC.getDirection();
            for (int i = 0; i < 7 && !canSpawn(toSpawn); i++) {
                toSpawn = toSpawn.rotateRight();
            }
            myRC.setDirection(toSpawn);
            myRC.yield();
            if (!canSpawn(myRC.getDirection())) {
                return false;
            }
        }
        myRC.spawn(RobotType.WOUT);
        myRC.yield();
        messageEncoder = new MessageEncoder(myRC, myMS,
                MessageType.ORIGIN,
                myRC.getLocation().add(myRC.getDirection()),
                myBuildingDirection);
        myRC.broadcast(messageEncoder.encodeMessage());
        myRC.yield();
        MapLocation spawnloc = myRC.getLocation().add(myRC.getDirection());
        Robot child = myRC.senseGroundRobotAtLocation(spawnloc);
        info = myRC.senseRobotInfo(child);
        double transferAmt = Math.min(info.type.maxEnergon()
                - info.eventualEnergon, GameConstants.ENERGON_RESERVE_SIZE
                - info.energonReserve);
        myRC.transferUnitEnergon(transferAmt, spawnloc, RobotLevel.ON_GROUND);
        return true;
    }

//	private void supportTeam() throws GameActionException {
//		Robot[] robots = myRC.senseNearbyGroundRobots();
//		Team myTeam = myRC.getTeam();
//		for (int i = 0; i < robots.length && myRC.getEnergonLevel() > 20; i++) {
//			RobotInfo info = myRC.senseRobotInfo(robots[i]);
//			if (info.team.equals(myTeam) && !info.type.isBuilding()
//					&& !info.type.equals(RobotType.ARCHON)) {
//				if ((info.location.isAdjacentTo(myRC.getLocation()) || info.location
//						.equals(myRC.getLocation()))) {
//					if (info.eventualEnergon < info.type.maxEnergon() * 0.8) {
//						double transfer = Math.min(info.type.maxEnergon()
//								- info.eventualEnergon,
//								GameConstants.ENERGON_RESERVE_SIZE
//										- info.energonReserve);
//						myRC.transferUnitEnergon(transfer, info.location,
//								RobotLevel.ON_GROUND);
//					}
//				}
//			}
//		}
//	}
    private void transferSelf() throws GameActionException {
        if (myRC.getEnergonLevel() == myRC.getMaxEnergonLevel()) {
            double transferAmt = GameConstants.ENERGON_RESERVE_SIZE
                    - myRC.getEnergonReserve();
            myRC.transferUnitEnergon(transferAmt, myRC.getLocation(),
                    RobotLevel.IN_AIR);
        }
    }

    // Find boundary
    private Direction findBoundary() {
        Direction[] dir = {Direction.EAST, Direction.SOUTH, Direction.WEST,
            Direction.NORTH};
        List<Direction> wall = new ArrayList<Direction>(4);
        Direction bound = Direction.OMNI;
        for (Direction d : dir) {
            if (offMap(d)) {
                wall.add(d);
            }
        }
        switch (wall.size()) {
            case 2:
                if (wall.contains(dir[0]) && wall.contains(dir[3])) {
                    bound = Direction.NORTH_EAST;
                } else if (wall.contains(dir[2]) && wall.contains(dir[3])) {
                    bound = Direction.NORTH_WEST;
                } else if (wall.contains(dir[2]) && wall.contains(dir[1])) {
                    bound = Direction.SOUTH_WEST;
                } else if (wall.contains(dir[0]) && wall.contains(dir[1])) {
                    bound = Direction.SOUTH_EAST;
                }
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

    private boolean offMap(Direction d) {
        MapLocation loc = myRC.getLocation();
        for (int i = 0; i < 6; i++) {
            loc = loc.add(d);
        }
        TerrainTile tt = myRC.senseTerrainTile(loc);
        return tt.getType().equals(TerrainType.OFF_MAP);
    }

    // Determine whether direction is spawnable
    private boolean canSpawn(Direction dir) throws GameActionException {
        MapLocation loc = myRC.getLocation().add(dir);
        return myRC.senseTerrainTile(loc).getType() == TerrainType.LAND
                && myRC.senseGroundRobotAtLocation(loc) == null;
    }
}
