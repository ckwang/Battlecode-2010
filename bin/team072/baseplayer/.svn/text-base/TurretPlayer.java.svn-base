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
import team072.message.MessageDecoder;
import team072.message.MessageEncoder;
import team072.message.MessageEncoder.MessageType;
import team072.message.MessageStack;
import team072.navigation.Map;


public class TurretPlayer extends BasePlayer {

    protected MessageStack myMS;
	private Map myMap;
    private boolean returned = false;
    private MapLocation[] airEnemyLocs;
    private MapLocation[] groundEnemyLocs;
    private int enemyID;
    private MapLocation enemyLoc;
    private RobotLevel enemyLevel;
    private MapLocation archonLocation;
    private int numMove;
    private Message enemyMessage;

	public TurretPlayer(RobotController rc) {
		super(rc);
		myType = RobotType.TURRET;
		myMS = new MessageStack();
		myMap = new Map(myRC);
        returned = false;
        airEnemyLocs = new MapLocation[0];
        groundEnemyLocs = new MapLocation[0];
        numMove = 0;
        enemyMessage = null;
	}

	@Override
	public void proceed() throws Exception {

         if (Clock.getBytecodeNum() > 5000) {
            myRC.getAllMessages();
            myRC.yield();
            returned = false;
            return;
        }

        if (!returned) {
            if (preprocess()) {
                return;
            }
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
            }
        }

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
    }

    private void goToLocation(MapLocation localLocation) throws
            GameActionException {
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
            } else {
                myRC.attackAir(enemyLoc);
                myRC.yield();
                return;
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
	}

	private void goAttackGround(MapLocation targetLoc) throws
            GameActionException {
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
            } else {
                myRC.attackGround(enemyLoc);
                myRC.yield();
                return;
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
	}

    private boolean preprocess() throws GameActionException {
        preprocessArchons();
        preprocessMessage();
        if (enemyID == 0) {
            if (preprocessSense()) {
                return true;
            }
        }
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
                        enemyID = msgDecoder.getEnemyID();
                        enemyLoc = msgDecoder.getEnemyLoc();
                        enemyLevel = msgDecoder.getLevel();
                    }
                }
            }
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