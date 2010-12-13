package team072.baseplayer;

import battlecode.common.*;
import team072.message.*;
import team072.message.MessageEncoder.MessageType;
import team072.navigation.Map;

public class SoldierPlayer extends BasePlayer {

	protected MessageStack myMS;
	private Map myMap;
//	private MessageEncoder msgEncoder;
	private final int CHASE_RADIUS = 5;
    private boolean returned = false;
    private MapLocation[] airEnemyLocs;
    private MapLocation[] groundEnemyLocs;
    private int enemyID;
    private MapLocation enemyLoc;
    private RobotLevel enemyLevel;
    private MapLocation archonLocation;
    private int numMove;
    private Message enemyMessage;
	
	public SoldierPlayer(RobotController rc) {
		super(rc);
		myType = RobotType.SOLDIER;
		myMS = new MessageStack();
		myMap = new Map(myRC);
        returned = false;
        airEnemyLocs = new MapLocation[0];
        groundEnemyLocs = new MapLocation[0];
        numMove = 0;
        enemyMessage = null;
	}

	@Override
	/***
	 * Attack enemy that is within a certain range
	 */
	public void proceed() throws Exception {

        if (Clock.getBytecodeNum() > 5000) {
            myRC.getAllMessages();
            myRC.yield();
            returned = false;
            return;
        }

//		MapLocation [] msgAirLocs = null;	// enemy in air from message
//		MapLocation [] msgGroundLocs = null;	// enemy on ground from message
//		MapLocation [] sensorAirLocs = null;	// enemy in air from sensor
//		MapLocation [] sensorGroundLocs = null;	// enemy on ground from sensor
//
//		// check the messages
//		MessageDecoder msgDecoder;
//		Message oldMsg = null;

//        if ((myRC.getRoundsUntilMovementIdle() != 0 ) || myRC.hasActionSet()) {
//            myRC.getAllMessages();
//            myRC.yield();
//            returned = false;
//            return;
//        }

        if (!returned) {
            if (preprocess()) {
                return;
            }
//            preprocessArchons();
//            preprocessMessage();
//            if (enemyID == 0) {
//                preprocessSense();
//            }
//            if (enemyID == 0) {
//                getClosestEnemy();
//            }
        }

        returned = false;
        
        if (myRC.getRoundsUntilMovementIdle() == 0 ) {
            if (archonLocation != null) {
                if (!myRC.getLocation().isAdjacentTo(archonLocation) &&
                        (myRC.getLocation() != archonLocation)) {
                    goToLocation(archonLocation);
                    return;
                } else {
                    myRC.yield();
                }
                return;

//                Direction[] bugDirections = Map.directionHierarchy(direction);
//                for (Direction dir : bugDirections) {
//                    if (myRC.canMove(dir)) {
//                        myRC.setDirection(dir);
//                        myRC.yield();
//                        if (myRC.canMove(dir)) {
//                            myRC.moveForward();
//                            preprocessMessage();
//                            preprocessArchons();
//                            myRC.yield();
//                        } else {
//                            returned = true;
//                        }
//                        return;
//                    }
//                }
//                myRC.yield();
//                return;
            }
        }
        

//        if (!returned) {
//
//        }

        if ((enemyLoc != null) && (myRC.getRoundsUntilAttackIdle() == 0)) {
            if (enemyLevel == RobotLevel.IN_AIR) {
                goAttackAir(enemyLoc);
            } else {
                goAttackGround(enemyLoc);
            }
            return;
        } else if ((myRC.getRoundsUntilMovementIdle() == 0) &&
                (enemyLoc == null)) {
            if (numMove < 3) {
                if (myRC.canMove(myRC.getDirection())) {
                    numMove += 1;
                    preprocess();
                    myRC.moveForward();
//                    preprocessMessage();
//                    preprocessArchons();
                    myRC.yield();
                } else {
                    numMove = 3;
                    returned = true;
                }
                return;
            } else {
                Direction[] bugDirections = Map.directionHierarchy
                        (Map.DIRECTIONS[(Clock.getRoundNum() * 3 +
                        myRC.getRobot().getID()) % 8]);
                for (Direction dir : bugDirections) {
                    if (myRC.canMove(dir)) {
                        numMove = 0;
                        myRC.setDirection(dir);
                        myRC.yield();
                        if (preprocess()) {
                            return;
                        }
                        if ((archonLocation != null) ||
                                (enemyLoc != null)) {
                            returned = true;
                            return;
                        }
                    }
                    if (myRC.canMove(dir)) {
                        numMove += 1;
                        myRC.moveForward();
                        myRC.yield();
                    } else {
                        numMove = 3;
                        returned = true;
                    }
                    return;
                    
                }
            }
            myRC.yield();
            return;
        } else if ((myRC.getRoundsUntilMovementIdle() == 0) &&
                (enemyLoc != null)) {
            if (myRC.canMove
                    (myRC.getDirection().opposite())) {
                myRC.moveBackward();
                myRC.yield();
                return;
            }
        } else {
            return;
        }

//
////            Direction enemyDirection = myRC.getLocation().directionTo(enemyLoc);
//            Direction enemyDirection = myMap.tangentBug(myRC.getLocation(), 
//                    enemyLoc);
////            if () {
////                myRC.setDirection(myRC.getDirection().opposite());
////                myRC.yield();
////                return;
////            }
//
//
//
//
//            if (myRC.getDirection() == enemyDirection) {
//                if (myRC.canMove(enemyDirection)) {
//                    myRC.moveForward();
//                    myRC.yield();
//                    return;
//                }
//            }
//
//            Direction[] bugDirections = Map.directionHierarchy(direction);
//            for (Direction dir : bugDirections) {
//                if (myRC.canMove(dir)) {
//                    myRC.setDirection(dir);
//                    myRC.yield();
//                    if (myRC.canMove(dir)) {
//                        myRC.moveForward();
//                        preprocessMessage();
//                        archonLocation = preprocessArchons();
//                        myRC.yield();
//                    } else {
//                        returned = true;
//                    }
//                    return;
//                }
//            }
//            myRC.yield();
//            return;

//            if (!myRC.canSenseSquare(enemyLoc)) {
//                myRC.setDirection(myRC.getLocation().directionTo(enemyLoc));
//            }
//        }
//
//
//
////        boolean hasRespondEnemy = false;
////        if (enemyMessage != null) {
////            myRC.broadcast(enemyMessage);
////            hasRespondEnemy = MessageStack.respondEnemyLoc(myRC, myMS,
////                    enemyMessage);
////        }
////
////        if (hasRespondEnemy) {
////            myRC.yield();
////            return;
////        }
//
//        // see if there are enemies in sensor range
//        sensorAirLocs = senseAirEnemyLocations();
//        sensorGroundLocs = senseGroundEnemyLocations();
//
////		// merge enemy list and rebroadcast
////		if ( sensorAirLocs.length + sensorGroundLocs.length > 0 ) {
////			msgEncoder = new MessageEncoder( myRC, myMS, MessageType.ENEMY_LOC, sensorAirLocs, sensorGroundLocs );
////			myRC.broadcast( msgEncoder.encodeMessage() );
////		} else {
////			if ( oldMsg != null )
////				myRC.broadcast( oldMsg );
////		}
////
////		// if enemy in the message is near, attack the first one on the list
////		if ( msgAirLocs != null && msgAirLocs.length > 0 &&
////				myRC.getLocation().distanceSquaredTo( msgAirLocs[0] ) < CHASE_RADIUS ) {
////			goAttackAir( msgAirLocs[0] );
////			myRC.yield();	return;
////		} else if ( msgGroundLocs != null && msgGroundLocs.length > 0 &&
////				myRC.getLocation().distanceSquaredTo( msgGroundLocs[0] ) < CHASE_RADIUS ) {
////			goAttackGround( msgGroundLocs[0] );
////			myRC.yield();	return;
////		}
////
//        // if enemy in the message is far, attack the first one in the sensor range
//        if ( sensorAirLocs.length > 0 ) {
//            MessageEncoder messageEncoder = new
//                        MessageEncoder(myRC, myMS, MessageType.ENEMY_LOC,
//                        sensorAirLocs[0]);
//            if (enemyMessage == null) {
//                myRC.broadcast(messageEncoder.encodeMessage());
//            } else {
//                myRC.broadcast(enemyMessage);
//                myMS.updateBroadcast(messageEncoder.encodeMessage());
//            }
//            goAttackAir( sensorAirLocs[0] );
//            myRC.yield();	return;
//        } else if ( sensorGroundLocs.length > 0 ) {
//            MessageEncoder messageEncoder = new
//                        MessageEncoder(myRC, myMS, MessageType.ENEMY_LOC,
//                        sensorGroundLocs[0]);
//            if (enemyMessage == null) {
//                myRC.broadcast(messageEncoder.encodeMessage());
//            } else {
//                myRC.broadcast(enemyMessage);
//                myMS.updateBroadcast(messageEncoder.encodeMessage());
//            }
//            goAttackGround( sensorGroundLocs[0] );
//            myRC.yield();	return;
//        }
//
//
//
//
//		// if no enemy, stick with archon
//		MapLocation [] archonLocs = myRC.senseAlliedArchons();
//		MapLocation nearestArchonLoc = archonLocs[0];
//		for ( MapLocation loc : archonLocs ) {	// find the nearest one
//			nearestArchonLoc = myRC.getLocation().distanceSquaredTo(loc) <
//				myRC.getLocation().distanceSquaredTo(nearestArchonLoc) ? loc : nearestArchonLoc;
//		}
//
//		if ( myRC.getRoundsUntilMovementIdle() == 0 ) {	// go for nearest archon
//			Direction dir = myMap.tangentBug( myRC.getLocation() , nearestArchonLoc);
//			if ( dir != Direction.OMNI ) {
//
//				if ( myRC.getDirection() == dir ){
//					if ( myRC.canMove(dir) )
//						myRC.moveForward();
//				} else {
//					myRC.setDirection(dir);
//				}
//			}
//		}
//
//		myRC.yield();
		
		
        
    }

    private void goToLocation(MapLocation localLocation) throws
            GameActionException {
//        System.out.print(myRC.getLocation());
//        System.out.println(localLocation);
        Direction direction = myMap.tangentBug(myRC.getLocation(), localLocation);
        if (direction != null && direction != Direction.OMNI) {
            if (myRC.getDirection() == direction) {
                if (myRC.canMove(direction)) {
                    myRC.moveForward();
                    myRC.yield();
                    return;
                }
            } else if (myRC.getDirection() == direction.opposite()) {
                if (myRC.canMove(direction)) {
                    myRC.moveBackward();
                    myRC.yield();
                    return;
                }
            }
            myRC.setDirection(direction);
            myRC.yield();
            if (preprocess()) {
                return;
            }
            if ((archonLocation != null) ||
                    (enemyLoc != null)) {
                returned = true;
                return;
            }
            if (myRC.canMove(direction)) {
                myRC.moveForward();
                myRC.yield();
            } else {
                returned = true;
            }
            return;
//            Direction[] bugDirections = Map.directionHierarchy(direction);
//            for (Direction dir : bugDirections) {
//                if (myRC.canMove(dir)) {
//                    myRC.setDirection(dir);
//                    myRC.yield();
//                    if (myRC.canMove(dir)) {
//                        myRC.moveForward();
//
//                        preprocessMessage();
//                        if (enemyID == 0) {
//                            preprocessSense();
//                        }
//                        if (enemyID == 0) {
//                            getClosestEnemy();
//                        }
//    //                    preprocessDirections();
//    //                    preprocessLocations();
//    //                    airRobots = myRC.senseNearbyAirRobots();
//    //                    groundRobots = myRC.senseNearbyGroundRobots();
//    //                    broadcastEnemies(airRobots, groundRobots);
//    //                    preprocessMessage();
//    //                    preprocessSupport(locations);
//                        myRC.yield();
//                    } else {
//                        returned = true;
//                    }
//                    return;
//                }
//            }
//            myRC.yield();
//            return;
        }
    }

	private void goAttackAir(MapLocation targetLoc) throws GameActionException {

        Direction[] closeDirections = Map.directionHierarchy
                (myRC.getLocation().directionTo(targetLoc));
        if (myRC.canAttackSquare(targetLoc) &&
                (myRC.getRoundsUntilAttackIdle() == 0)) {
            if (myRC.canSenseSquare(targetLoc)) {
                Robot robot = myRC.senseAirRobotAtLocation(targetLoc);
                if ((robot != null) &&
                        (myRC.senseRobotInfo(robot).team == myRC.getTeam().
                        opponent())) {
                myRC.attackAir(enemyLoc);
                MessageEncoder messageEncoder = new MessageEncoder(myRC, myMS,
                        MessageType.ENEMY_LOC, enemyLoc);
                myRC.broadcast(messageEncoder.encodeMessage());
                myRC.yield();
                return;
                } else {
                    enemyID = 0;
                    enemyLoc = null;
                    enemyLevel = null;
                    returned = true;
                    return;
                }
            }
        }
        if (myRC.getRoundsUntilMovementIdle() == 0) {
            boolean isGoodDirection = false;
            for (int i = 0; i < 3; i++) {
                if (myRC.getDirection() == closeDirections[i]) {
                    isGoodDirection = true;
                }
            }
            if (!isGoodDirection && myRC.getLocation().
                    isAdjacentTo(targetLoc)) {
                myRC.setDirection(myRC.getLocation().directionTo(targetLoc));
                myRC.yield();
                return;
            } else {
                if (!myRC.getLocation().isAdjacentTo(targetLoc) &&
                        (myRC.getLocation() != targetLoc)) {
                    goToLocation(targetLoc);
                    return;
                } else {
                    enemyID = 0;
                    enemyLoc = null;
                    enemyLevel = null;
                    returned = true;
                }
                return;
            }
        }

		// if in sensor range, see if the target is enemy. If so, do nothing.
//		Robot r = myRC.senseAirRobotAtLocation(targetLoc);
//		if ( r != null && myRC.senseRobotInfo(r).team == myRC.getTeam() ) {
//			return;
//		}
//
//		if ( myRC.getLocation().isAdjacentTo(targetLoc) ) {	// if adjacent to enemy
//			if ( myRC.canAttackSquare(targetLoc) ) {	// if facing enemy
//				if ( myRC.getRoundsUntilAttackIdle() == 0 )
//					myRC.attackAir(targetLoc);
//			} else {
//				myRC.setDirection( myRC.getLocation().directionTo(targetLoc) );
//			}
//
//		} else {
//			if ( myRC.getRoundsUntilMovementIdle() == 0 ) { // chase
//				Direction dir = myMap.tangentBug( myRC.getLocation() , targetLoc);
//				if ( myRC.getDirection() == dir ){
//					if ( myRC.canMove(dir) )
//						myRC.moveForward();
//				} else {
//					myRC.setDirection(dir);
//				}
//			}
//		}
	}
	
	private void goAttackGround(MapLocation targetLoc) throws GameActionException {

        Direction[] closeDirections = Map.directionHierarchy
                (myRC.getLocation().directionTo(targetLoc));
        if (myRC.canAttackSquare(targetLoc) &&
                (myRC.getRoundsUntilAttackIdle() == 0)) {
            if (myRC.canSenseSquare(targetLoc)) {
                Robot robot = myRC.senseGroundRobotAtLocation(targetLoc);
                if ((robot != null) &&
                        (myRC.senseRobotInfo(robot).team == myRC.getTeam().
                        opponent())) {
                myRC.attackGround(enemyLoc);
                MessageEncoder messageEncoder = new MessageEncoder(myRC, myMS,
                        MessageType.ENEMY_LOC, enemyLoc);
                myRC.broadcast(messageEncoder.encodeMessage());
                myRC.yield();
                return;
                } else {
                    enemyID = 0;
                    enemyLoc = null;
                    enemyLevel = null;
                    returned = true;
                    return;
                }
            }
        }
        if (myRC.getRoundsUntilMovementIdle() == 0) {
            boolean isGoodDirection = false;
            for (int i = 0; i < 3; i++) {
                if (myRC.getDirection() == closeDirections[i]) {
                    isGoodDirection = true;
                }
            }
            if (!isGoodDirection && myRC.getLocation().
                    isAdjacentTo(targetLoc)) {
                myRC.setDirection(myRC.getLocation().directionTo(targetLoc));
                myRC.yield();
                return;
            } else {
                if (!myRC.getLocation().isAdjacentTo(targetLoc) &&
                        (myRC.getLocation() != targetLoc)) {
                    goToLocation(targetLoc);
                    return;
                } else {
                    enemyID = 0;
                    enemyLoc = null;
                    enemyLevel = null;
                    returned = true;
                }
                return;
            }
        }
		// if in sensor range, see if the target is enemy. If so, do nothing.
//		Robot r = myRC.senseGroundRobotAtLocation(targetLoc);
//		if ( r != null && myRC.senseRobotInfo(r).team == myRC.getTeam() ) {
//			return;
//		}
//
//		if ( myRC.getLocation().isAdjacentTo(targetLoc) ) {	// if adjacent to enemy
//			if ( myRC.canAttackSquare(targetLoc) ) {	// if facing enemy
//				if ( myRC.getRoundsUntilAttackIdle() == 0 )
//					myRC.attackGround(targetLoc);
//			} else {
//				myRC.setDirection( myRC.getLocation().directionTo(targetLoc) );
//			}
//
//		} else {
//			if ( myRC.getRoundsUntilMovementIdle() == 0 ) { // chase
//				Direction dir = myMap.tangentBug( myRC.getLocation() , targetLoc);
//				if ( myRC.getDirection() == dir ){
//					if ( myRC.canMove(dir) )
//						myRC.moveForward();
//				} else {
//					myRC.setDirection(dir);
//				}
//			}
//		}
	}

    private boolean preprocess() throws GameActionException {
        preprocessArchons();
        preprocessMessage();
//        if (enemyID == 0) {
        if (preprocessSense()) {
            return true;
        }
//        }
        if (enemyID == 0) {
            getClosestEnemy();
        }
        return false;
    }
    
    private void preprocessMessage() {
        int timeStamp = 10001;
        for ( Message m : myRC.getAllMessages() ){
            MessageDecoder msgDecoder = new MessageDecoder(myRC, myMS, m);
            if ((enemyMessage == null) && msgDecoder.isEnemy()) {
                enemyMessage = m;
            }
            if (enemyID == 0) {
                if (msgDecoder.isValid() && msgDecoder.getType() ==
                        MessageType.ENEMY_LOC && myRC.getLocation().
                        distanceSquaredTo(msgDecoder.getEnemyLoc()) < 9){
                    if ((msgDecoder.getTimeStamp() < timeStamp) &&
                            (Clock.getRoundNum() - msgDecoder.getTimeStamp()) < 10) {
//                        enemyID = msgDecoder.getEnemyID();
//                        enemyLoc = msgDecoder.getEnemyLoc();
//                        enemyLevel = msgDecoder.getLevel();
                    }
                }
            }
//            airEnemyLocs = new MapLocation[0];
//            groundEnemyLocs = new MapLocation[0];
            if (msgDecoder.isValid() && msgDecoder.getType() ==
                    MessageType.ENEMY_LOCS){
                airEnemyLocs = msgDecoder.getAirEnemyLocs();
                groundEnemyLocs = msgDecoder.getGroundEnemyLocs();
            }
        }
    }
    
    private void preprocessArchons() {
        MapLocation[] archonLocations = myRC.senseAlliedArchons();
        int distance = 10001;
        for (MapLocation location : archonLocations) {
            if (myRC.getLocation().distanceSquaredTo(location) < distance) {
                distance = myRC.getLocation().distanceSquaredTo(location);
                archonLocation = location;
            }
        }
        if ((distance < 100) && ((myRC.getEnergonLevel() > 20 &&
                !myRC.getLocation().isAdjacentTo(archonLocation) &&
                !myRC.getLocation().equals(archonLocation)) ||
                myRC.getEnergonLevel() > 30)) {
            archonLocation = null;
        }
    }

    private boolean preprocessSense() throws GameActionException {
        Robot[] airRobots = myRC.senseNearbyAirRobots();
        Robot[] groundRobots = myRC.senseNearbyGroundRobots();
        if ((enemyID != 0) && (myRC.getRoundsUntilAttackIdle() == 0)) {
            for (Robot robot : airRobots) {
                if (myRC.canSenseObject(robot)) {
                    RobotInfo info = myRC.senseRobotInfo(robot);
                    if (info.id == enemyID) {
                        if (myRC.canAttackSquare(info.location)) {
                            enemyLoc = info.location;
                            myRC.attackAir(info.location);
                            MessageEncoder messageEncoder = new MessageEncoder
                                    (myRC, myMS, MessageType.ENEMY_LOC,
                                    enemyLoc);
                            myRC.broadcast(messageEncoder.encodeMessage());
                            myRC.yield();
                            return true;
                        }
                    }
                }
            }
            for (Robot robot : groundRobots) {
                 if (myRC.canSenseObject(robot)) {
                    RobotInfo info = myRC.senseRobotInfo(robot);
                    if (info.id == enemyID) {
                        if (myRC.canAttackSquare(info.location)) {
                            enemyLoc = info.location;
                            myRC.attackGround(info.location);
                            MessageEncoder messageEncoder = new MessageEncoder
                                    (myRC, myMS, MessageType.ENEMY_LOC,
                                    enemyLoc);
                            myRC.broadcast(messageEncoder.encodeMessage());
                            myRC.yield();
                            return true;
                        }
                    }
                }
            }
            enemyID = 0;
            enemyLoc = null;
            enemyLevel = null;
        } else if (enemyID == 0) {
            for (Robot robot : airRobots) {
                if (myRC.canSenseObject(robot)) {
                    RobotInfo info = myRC.senseRobotInfo(robot);
                    if (info.team == myRC.getTeam().opponent()) {
                        enemyID = info.id;
                        enemyLoc = info.location;
                        enemyLevel = RobotLevel.IN_AIR;
                        return false;
                    }
                }
            }
            for (Robot robot : groundRobots) {
                if (myRC.canSenseObject(robot)) {
                    RobotInfo info = myRC.senseRobotInfo(robot);
                    if (info.team == myRC.getTeam().opponent()) {
                        enemyID = info.id;
                        enemyLoc = info.location;
                        enemyLevel = RobotLevel.ON_GROUND;
                        return false;
                    }
                }
            }
        }
        return false;
    }

    private void getClosestEnemy() {
        int distance = 10000;
        boolean hasEnemy = false;
        for (MapLocation location : airEnemyLocs) {
            if (myRC.getLocation().distanceSquaredTo(location) < distance) {
                distance = myRC.getLocation().distanceSquaredTo(location);
                enemyLoc = location;
                enemyLevel = RobotLevel.IN_AIR;
                hasEnemy = true;
            }
        }
        if (hasEnemy) {
            return;
        }
        distance = 10000;
        for (MapLocation location : groundEnemyLocs) {
            if (myRC.getLocation().distanceSquaredTo(location) < distance) {
                distance = myRC.getLocation().distanceSquaredTo(location);
                enemyLoc = location;
                enemyLevel = RobotLevel.ON_GROUND;
            }
        }
    }
}
