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
import team072.message.MessageDecoder;
import team072.message.MessageEncoder;
import team072.message.MessageEncoder.MessageType;
import team072.message.MessageStack;
import team072.navigation.Map;

public class SoldierPlayerX extends BasePlayer {

    private final int DISTANCE_FROM_MOTHER = 17;
    private final int DISTANCE_SQUARED_FROM_ARCHON_MAX = 26;
    private final int ENERGON_MIN = 20;
    private final int ENERGON_MAX = 35;
    private final int PEACE_MIN = 5;
    private final int ENERGON_LEVEL_MIN = 8;
    private final int MOVING_RADIUS = 10;

    private MessageStack myMS;
    private Map myMap;
    // Identity information
    private Team myTeam;
//    private MapLocation myOrigin;
//    private double myMothersAngle;
//    private MapLocation myRestLocation;
//    private Direction corner;
//    private int radiusSquared;
    // Enemy information
    private int enemyID;
    private MapLocation enemyLoc;
    private RobotLevel enemyLevel;
    // State trackers
    private boolean returned = false;
    private int myAge;
   // private int warPeriod;
    private int peacePeriod;
    private int numMove;
//    private int blindCount;
    // Instance variables used as local variables
    private MapLocation[] airEnemyLocs;
    private MapLocation[] groundEnemyLocs;
    private MapLocation archonLocation;
    private MapLocation archonLocationX;
    private Message enemyMessage;
    private Message[] allMessages;
    private RobotInfo info;
    private int distanceSquared;
    private Direction[] directions;
    private MapLocation[] locations;
    private MapLocation location;
    private double energonDifference;
    private Message[] rebroadcastStack;
    private int rebroadcastIndex;
    private Robot[] groundRobots;
    private Robot[] airRobots;
//    private int radiusSquaredTemp;
//    private boolean radiusChanged;

    public SoldierPlayerX(RobotController rc) {
        super(rc);
        myMS = new MessageStack();
        myMap = new Map(myRC);

        // Identity information
        myTeam = myRC.getTeam();
        myType = RobotType.SOLDIER;

        // State trackers
        returned = false;
        myAge = 0;
        peacePeriod = PEACE_MIN;

        // Instance variables used as local variables
        airEnemyLocs = new MapLocation[0];
        groundEnemyLocs = new MapLocation[0];
        directions = new Direction[8];
        directions[0] = myRC.getDirection();
        locations = new MapLocation[9];
        locations[0] = myRC.getLocation();
    }

    @Override
    public void proceed() throws Exception {
//        System.out.println("beg");

//        if (archonLocation != null) {
//            myRC.setIndicatorString(0, archonLocation.toString());
//        } else {
//            myRC.setIndicatorString(0, "no");
//        }
//        myRC.setIndicatorString(1, new Integer(enemyID).toString());

        // Return if bytecode exceeded
        if (Clock.getBytecodeNum() > 4500) {
            myRC.getAllMessages();

            attackMessage();

            myRC.yield();

            returned = false;
//            System.out.println("a");
            return;
        }

        // Preprocess
        if (!returned) {
            if (preprocess()) {
//                System.out.println("d");
                return;
            }
        }

        returned = false;

        // Go find nearest archon if energon is low
        if (myRC.getRoundsUntilMovementIdle() == 0) {
            if (archonLocation != null) {
                if (!locations[0].isAdjacentTo(archonLocation) && !(locations[0].equals(archonLocation))) {
                    goToLocation(archonLocation);
                } else {
                    attackMessage();

                    myRC.yield();
                }
//                System.out.println("e");
                return;
            }
        }

//        System.out.println(Clock.getBytecodeNum());
//        System.out.println("hh");

        // Attack enemies
        if ((enemyLoc != null) && (myRC.getRoundsUntilAttackIdle() == 0)) {
            if (enemyLevel == RobotLevel.IN_AIR) {
                goAttackAir(enemyLoc);
            } else {
                goAttackGround(enemyLoc);
            }
            return;

            // Stick with nearest archon
//        } else if ((myRC.getRoundsUntilMovementIdle() == 0) && (enemyLoc == null)) {
//            if (!locations[0].isAdjacentTo(archonLocationX) && !(locations[0].equals(archonLocationX))) {
//                goToLocation(archonLocationX);
////                System.out.println("g");
//                return;
//            }
//
////            if (!directions[0].equals(corner)) {
////                myRC.setDirection(corner);
////            }
//
//            attackMessage();
//
//            myRC.yield();
////            System.out.println("h");
//            return;

            // Random wandering
        } else if ((myRC.getRoundsUntilMovementIdle() == 0) && (enemyLoc == null)) {
            if (numMove < MOVING_RADIUS) {
                if (myRC.canMove(directions[0])) {
                    numMove += 1;
                    preprocess();
                    myRC.moveForward();
                    myRC.yield();
                } else {
                    numMove = MOVING_RADIUS;
                    returned = true;
                }
                return;
            } else {
                Direction[] bugDirections = Map.directionHierarchy(Map.DIRECTIONS[(Clock.getRoundNum() * 3
                        + myRC.getRobot().getID()) % 8]);
                for (Direction dir : bugDirections) {
                    if (myRC.canMove(dir)) {
                        numMove = 0;
                        myRC.setDirection(dir);
                        myRC.yield();
                        if (preprocess()) {
                            return;
                        }
                        if ((archonLocation != null) || (enemyLoc != null)) {
                            returned = true;
                            return;
                        }
                    }
                    if (myRC.canMove(dir)) {
                        numMove += 1;
                        myRC.moveForward();
                        myRC.yield();
                    } else {
                        numMove = MOVING_RADIUS;
                        returned = true;
                    }
                    return;
                }
            }
            myRC.yield();
            return;

            // Move to rest location
//        } else if ((myRC.getRoundsUntilMovementIdle() == 0) && (enemyLoc == null) && radiusChanged) {
//            if (!locations[0].equals(myRestLocation) && !locations[0].isAdjacentTo(myRestLocation)) {
//                goToLocation(myRestLocation);
//                return;
//            } else if (locations[0].isAdjacentTo(myRestLocation)) {
//                if (myRC.canMove(locations[0].directionTo(myRestLocation))) {
//                    goToLocation(myRestLocation);
//                    return;
//                } else {
//                    if ((Clock.getRoundNum()) % 2 == 0) {
//                        myRestLocation = myRestLocation.add(corner.rotateLeft().rotateLeft());
//                    } else {
//                        myRestLocation = myRestLocation.add(corner.rotateRight().rotateRight());
//                    }
//                }
//            } else {
//                radiusChanged = false;
//                if (!directions[0].equals(corner)) {
//                    myRC.setDirection(corner);
//
//                    attackMessage();
//
//                    myRC.yield();
//                    return;
//                }
//            }

            // Move back after attacking
//        } else if ((myRC.getRoundsUntilMovementIdle() == 0) && (enemyLoc != null)) {
//            if (myRC.canMove(directions[0].opposite())) {
//                myRC.moveBackward();
//
//                attackMessage();
//
//                myRC.yield();
////                System.out.println("i");
//                return;
//            }
        }
        attackMessage();

        myRC.yield();
//        System.out.println("j");
        return;
    }

    private void goToLocation(MapLocation localLocation) throws
            GameActionException {
        Direction direction = myMap.tangentBug(locations[0], localLocation);
        if (direction != null && direction != Direction.OMNI && direction != Direction.NONE) {
            if (directions[0].equals(direction)) {
                if (myRC.canMove(direction)) {
                    myRC.moveForward();

                    attackMessage();

                    myRC.yield();
                    return;
                }
            } else if (directions[0].equals(direction.opposite())) {
                if (myRC.canMove(direction)) {
                    myRC.moveBackward();

                    attackMessage();

                    myRC.yield();
                    return;
                }
            }
            myRC.setDirection(direction);

            attackMessage();

            myRC.yield();

            if (preprocess()) {
                returned = false;
                return;
            }

            if ((archonLocation != null) || (enemyLoc != null)) {
                returned = true;
                return;
            }

            if (myRC.canMove(direction)) {
                myRC.moveForward();

                attackMessage();

                myRC.yield();
            } else {
                returned = true;
            }
            return;
        } else {
            myRC.yield();
            return;
        }
    }

    private void goAttackAir(MapLocation targetLoc) throws GameActionException {
        Direction[] closeDirections = Map.directionHierarchy(locations[0].directionTo(targetLoc));
        if (myRC.canAttackSquare(targetLoc) && (myRC.getRoundsUntilAttackIdle() == 0)) {
            if (myRC.canSenseSquare(targetLoc)) {
                Robot robot = myRC.senseAirRobotAtLocation(targetLoc);
                if ((robot != null) && (myRC.senseRobotInfo(robot).team.equals(myRC.getTeam().opponent()))) {
                    if (myRC.canAttackSquare(targetLoc)) {
                        myRC.attackAir(targetLoc);
                    }

                    if (!myRC.hasBroadcastMessage()) {
                        MessageEncoder messageEncoder = new MessageEncoder(myRC, myMS, MessageType.ENEMY_LOC, targetLoc);

//                        myRC.setIndicatorString(0,"ground");
                        myRC.broadcast(messageEncoder.encodeMessage());
                    }
                    
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
                if (directions[0].equals(closeDirections[i])) {
                    isGoodDirection = true;
                }
            }
            if (!isGoodDirection && locations[0].isAdjacentTo(targetLoc)) {
                myRC.setDirection(locations[0].directionTo(targetLoc));

                attackMessage();

                myRC.yield();
                return;
            } else {
                if (!locations[0].isAdjacentTo(targetLoc) && (locations[0] != targetLoc)) {
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

    private void goAttackGround(MapLocation targetLoc) throws GameActionException {
        Direction[] closeDirections = Map.directionHierarchy(locations[0].directionTo(targetLoc));
        if (myRC.canAttackSquare(targetLoc) && (myRC.getRoundsUntilAttackIdle() == 0)) {
            if (myRC.canSenseSquare(targetLoc)) {
                Robot robot = myRC.senseGroundRobotAtLocation(targetLoc);
                if ((robot != null) && (myRC.senseRobotInfo(robot).team.equals(myTeam.opponent()))) {
                    if (myRC.canAttackSquare(targetLoc)) {
                        myRC.attackGround(targetLoc);
                    }
                    if (!myRC.hasBroadcastMessage()) {
                        MessageEncoder messageEncoder = new MessageEncoder(myRC, myMS, MessageType.ENEMY_LOC, targetLoc);

//                        myRC.setIndicatorString(0,"ground");
                        myRC.broadcast(messageEncoder.encodeMessage());
                    }

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
                if (directions[0].equals(closeDirections[i])) {
                    isGoodDirection = true;
                }
            }
            if (!isGoodDirection && locations[0].isAdjacentTo(targetLoc)) {
                myRC.setDirection(locations[0].directionTo(targetLoc));

                attackMessage();

                myRC.yield();
                return;
            } else {
                if (!locations[0].isAdjacentTo(targetLoc) && (locations[0] != targetLoc)) {
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

        myRC.yield();
        return;
    }

    private boolean preprocess() throws GameActionException {
//        if (enemyLoc != null) {
//            myRC.setIndicatorString(2, enemyLoc.toString());
//        }

        if (enemyLoc != null) {
            peacePeriod = 0;
        } else if (peacePeriod < PEACE_MIN) {
            peacePeriod += 1;
        }

        preprocessDirections();
        preprocessLocations();
        preprocessSupport();
//        System.out.print("a");
//        System.out.println(Clock.getBytecodeNum());
        preprocessArchons();
//        System.out.print("b");
//        System.out.println(Clock.getBytecodeNum());
        if (preprocessSense()) {
            return true;
        }
//        System.out.print("c");
//        System.out.println(Clock.getBytecodeNum());
        preprocessMessage();
//        System.out.print("d");
//        System.out.println(Clock.getBytecodeNum());
        if (enemyID == 0) {
            getClosestEnemy();
        }
//        System.out.print("e");
//        System.out.println(Clock.getBytecodeNum());
        if (!myRC.hasBroadcastMessage()) {
            myRC.setIndicatorString(0,"rebroadcast");
            MessageStack.rebroadcast(myRC, myMS, rebroadcastStack);
        }

        return false;
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

    private void preprocessMessage() {
//        if (archonLocation != null) {
//            enemyMessage = null;
//
//            myRC.getAllMessages();
//
//            return;
//        }

        airEnemyLocs = new MapLocation[0];
        groundEnemyLocs = new MapLocation[0];

        int timeStamp = 10001;

        allMessages = myRC.getAllMessages();

        rebroadcastStack = new Message[5];
        rebroadcastIndex = 0;

        enemyMessage = null;

//        System.out.print("x");
//        System.out.println(Clock.getBytecodeNum());

        for (Message m : allMessages) {

            if (Clock.getBytecodeNum() > 4000) {
                break;
            }

            MessageDecoder msgDecoder = new MessageDecoder(myRC, myMS, m);
//            System.out.print("y");
//            System.out.println(Clock.getBytecodeNum());

            if (msgDecoder.isEnemy()) {
//                System.out.print("first");
//                System.out.println(m.ints[1]);
//                System.out.print("first");
//                System.out.println(m.ints[2]);
//                System.out.print("first");
//                System.out.println(m.ints[3]);
//                System.out.print("hash");
//                System.out.println(m.hashCode());
                enemyMessage = m;
            } else if (msgDecoder.isValidX()) {
                if (enemyID == 0) {
                    if (msgDecoder.getType().equals(MessageType.ENEMY_LOC) && locations[0].distanceSquaredTo(msgDecoder.getEnemyLoc()) < 9) {
                        if ((msgDecoder.getTimeStamp() < timeStamp) /*&& (Clock.getRoundNum() - msgDecoder.getTimeStamp()) < 10*/) {
                            timeStamp = msgDecoder.getTimeStamp();

                            enemyID = msgDecoder.getEnemyID();
                            enemyLoc = msgDecoder.getEnemyLoc();
                            enemyLevel = msgDecoder.getLevel();
    //                        System.out.print("ourhash");
    //                        System.out.println(m.hashCode());
                        }
                    }
                }

//                System.out.print("z");
//                System.out.println(Clock.getBytecodeNum());

                if (msgDecoder.getType() == MessageType.ENEMY_LOCS) {
                    airEnemyLocs = msgDecoder.getAirEnemyLocs();
                    groundEnemyLocs = msgDecoder.getGroundEnemyLocs();
//                    System.out.print("ourhash");
//                    System.out.println(m.hashCode());
                    if (rebroadcastIndex < 5) {
                        rebroadcastStack[rebroadcastIndex] = m;
                        rebroadcastIndex += 1;
                    }
                }

//                System.out.print("u");
//                System.out.println(Clock.getBytecodeNum());

                myMS.updateDontProcess(m);
            }
        }
    }

    private void preprocessArchons() {
        MapLocation[] archonLocations = myRC.senseAlliedArchons();
        distanceSquared = 10001;
        for (MapLocation loc : archonLocations) {
            if (locations[0].distanceSquaredTo(loc) < distanceSquared) {
                distanceSquared = locations[0].distanceSquaredTo(loc);
                archonLocation = loc;
            }
        }

        archonLocationX = archonLocation;

        if ((peacePeriod < PEACE_MIN) || ((distanceSquared < DISTANCE_SQUARED_FROM_ARCHON_MAX) && ((myRC.getEnergonLevel() > ENERGON_MIN
                && !locations[0].isAdjacentTo(archonLocation) && !locations[0].equals(archonLocation))
                || myRC.getEnergonLevel() > ENERGON_MAX))) {
            archonLocation = null;
        }

//        radiusSquaredTemp = Math.max(radiusSquared, distanceSquared);
//        if (radiusSquared != radiusSquaredTemp) {
//            radiusChanged = true;
//        }
//
//        myRestLocation = new MapLocation(myOrigin.getX() + (int) ((DISTANCE_FROM_MOTHER + Math.sqrt(radiusSquared))
//                * Math.cos(myMothersAngle)),
//                myOrigin.getY() + (int) ((DISTANCE_FROM_MOTHER + Math.sqrt(radiusSquared)) * Math.sin(myMothersAngle)));
    }

    private boolean preprocessSense() throws GameActionException {
        airRobots = myRC.senseNearbyAirRobots();
        if (archonLocation != null) {
            return false;
        }

        groundRobots = myRC.senseNearbyGroundRobots();
        if ((enemyID != 0) && (myRC.getRoundsUntilAttackIdle() == 0)) {
            for (Robot robot : airRobots) {
                if (myRC.canSenseObject(robot)) {
                    info = myRC.senseRobotInfo(robot);
                    if (info.id == enemyID) {
                        if (myRC.canAttackSquare(info.location)) {
                            enemyLoc = info.location;
                            myRC.attackAir(enemyLoc);
                            
                            MessageEncoder messageEncoder = new MessageEncoder(myRC, myMS, MessageType.ENEMY_LOC, enemyLoc);
                            myRC.broadcast(messageEncoder.encodeMessage());
                            myRC.yield();
                            return true;
                        }
                    }
                }
            }
            for (Robot robot : groundRobots) {
                if (myRC.canSenseObject(robot)) {
                    info = myRC.senseRobotInfo(robot);
                    if (info.id == enemyID) {
                        if (myRC.canAttackSquare(info.location)) {
                            enemyLoc = info.location;
                            myRC.attackGround(enemyLoc);
                            

                            enemyID = info.id;
                            enemyLoc = info.location;
                            enemyLevel = RobotLevel.ON_GROUND;

                            MessageEncoder messageEncoder = new MessageEncoder(myRC, myMS, MessageType.ENEMY_LOC,
                                    enemyLoc);

                            myRC.setIndicatorString(0,"sense");

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
                    info = myRC.senseRobotInfo(robot);
                    if (info.team.equals(myRC.getTeam().opponent())) {
                        enemyID = info.id;
                        enemyLoc = info.location;
                        enemyLevel = RobotLevel.IN_AIR;
                        return false;
                    }
                }
            }
            for (Robot robot : groundRobots) {
                if (myRC.canSenseObject(robot)) {
                    info = myRC.senseRobotInfo(robot);
                    if (info.team.equals(myRC.getTeam().opponent())) {
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

    // Transfer energon and flux to nearby robots
    private void preprocessSupport() throws GameActionException {
        for (int i = 1; i < 4; i++) {
            location = locations[i];
            Robot robot = myRC.senseGroundRobotAtLocation(location);
            if (robot != null) {
                info = myRC.senseRobotInfo(robot);
                if ((info.team.equals(myTeam)) && ((info.type == RobotType.CHAINER) || (info.type == RobotType.SOLDIER))) {
                    energonDifference = myRC.getEventualEnergonLevel() - info.eventualEnergon;
                    if ((energonDifference > 0) && (myRC.getEnergonLevel() > ENERGON_LEVEL_MIN)) {
                        myRC.transferUnitEnergon(Math.min(myRC.getEnergonLevel() - ENERGON_LEVEL_MIN, Math.min(10 - info.energonReserve, energonDifference)), info.location,
                                RobotLevel.ON_GROUND);
                    }
                }
            }
        }
    }

    private void getClosestEnemy() {
//        if (archonLocation != null) {
//            return;
//        }


        distanceSquared = 10001;
        boolean hasEnemy = false;
        for (MapLocation loc : airEnemyLocs) {
            if (locations[0].distanceSquaredTo(loc) < distanceSquared) {
                distanceSquared = locations[0].distanceSquaredTo(loc);
                enemyLoc = loc;
                enemyLevel = RobotLevel.IN_AIR;
                hasEnemy = true;
            }
        }
        if (hasEnemy) {
            return;
        }
        distanceSquared = 10001;
        for (MapLocation loc : groundEnemyLocs) {
            if (locations[0].distanceSquaredTo(loc) < distanceSquared) {
                distanceSquared = locations[0].distanceSquaredTo(loc);
                enemyLoc = loc;
                enemyLevel = RobotLevel.ON_GROUND;
            }
        }
    }

    private void attackMessage() throws GameActionException {
        if (!myRC.hasBroadcastMessage() && (enemyMessage != null)) {
            myRC.setIndicatorString(0,"attack");
            MessageStack.broadcastAttack(myRC, enemyMessage);
        }
    }

}
