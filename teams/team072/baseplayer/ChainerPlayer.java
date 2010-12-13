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

public class ChainerPlayer extends BasePlayer {

    private final int DISTANCE_FROM_MOTHER = 17;
    private final int DISTANCE_SQUARED_FROM_ARCHON_MAX = 26;
    private final int ENERGON_MIN = 15;
    private final int ENERGON_MAX = 45;
    private final int PEACE_MIN = 5;
    private final int ENERGON_LEVEL_MIN = 8;
    private final int BLIND_COUNT = 1;
    private final int WAR_MIN = 100;
    private final int TRANSFORM_MIN = 40;

    private MessageStack myMS;
    private Map myMap;
    private BasePlayer soldierPlayer;
    // Identity information
    private Team myTeam;
    private MapLocation myOrigin;
    private double myMothersAngle;
    private MapLocation myRestLocation;
    private Direction corner;
    private int radiusSquared;
    // Enemy information
    private int enemyID;
    private MapLocation enemyLoc;
    private RobotLevel enemyLevel;
    // State trackers
    private boolean returned = false;
    private int myAge;
    private int warPeriod;
    private int peacePeriod;
    private int numMove;
    private int blindCount;
    // Instance variables used as local variables
//    private MapLocation[] airEnemyLocs;
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
    private int radiusSquaredTemp;
    private boolean radiusChanged;

    public ChainerPlayer(RobotController rc) {
        super(rc);
        myMS = new MessageStack();
        myMap = new Map(myRC);

        // Identity information
        myTeam = myRC.getTeam();
        myType = RobotType.CHAINER;

        // State trackers
        returned = false;
        numMove = 0;
        myAge = 0;
        warPeriod = 0;
        peacePeriod = PEACE_MIN;
        blindCount = BLIND_COUNT;

        // Instance variables used as local variables
        groundEnemyLocs = new MapLocation[0];
        directions = new Direction[8];
        directions[0] = myRC.getDirection();
        locations = new MapLocation[9];
        locations[0] = myRC.getLocation();
        radiusChanged = true;
    }

    @Override
    public void proceed() throws Exception {
        myRC.setIndicatorString(1, new Integer(blindCount).toString());
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

        if (myType == RobotType.SOLDIER) {
            while (true) {
                soldierPlayer.proceed();
            }
        }

        // Education
        if (myAge == 0) {
            myRC.getAllMessages();

            myAge += 1;

            myRC.yield();
//            System.out.println("b");
            return;
        } else if (myAge == 1) {
            preprocessEducation();

            myAge += 1;

//            if (!preprocess()) {
//                returned = true;
//            }
            myRC.yield();
//            System.out.println("c");
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
            goAttackGround(enemyLoc);
//            System.out.println("f");
            return;

            // Stick with nearest archon
        } else if ((myRC.getRoundsUntilMovementIdle() == 0) && (enemyLoc == null)) {
            if (!locations[0].isAdjacentTo(archonLocationX) && !(locations[0].equals(archonLocationX))) {
                goToLocation(archonLocationX);
//                System.out.println("g");
                return;
            }

            if (!directions[0].equals(corner)) {
                myRC.setDirection(corner);
            }

            attackMessage();

            myRC.yield();
//            System.out.println("h");
            return;

            // Random wandering
//        } else if ((myRC.getRoundsUntilMovementIdle() == 0) && (enemyLoc == null)) {
//            if (numMove < 3) {
//                if (myRC.canMove(directions[0])) {
//                    numMove += 1;
//                    preprocess();
//                    myRC.moveForward();
//                    myRC.yield();
//                } else {
//                    numMove = 3;
//                    returned = true;
//                }
//                return;
//            } else {
//                Direction[] bugDirections = Map.directionHierarchy(Map.DIRECTIONS[(Clock.getRoundNum() * 3
//                        + myRC.getRobot().getID()) % 8]);
//                for (Direction dir : bugDirections) {
//                    if (myRC.canMove(dir)) {
//                        numMove = 0;
//                        myRC.setDirection(dir);
//                        myRC.yield();
//                        if (preprocess()) {
//                            return;
//                        }
//                        if ((archonLocation != null) || (enemyLoc != null)) {
//                            returned = true;
//                            return;
//                        }
//                    }
//                    if (myRC.canMove(dir)) {
//                        numMove += 1;
//                        myRC.moveForward();
//                        myRC.yield();
//                    } else {
//                        numMove = 3;
//                        returned = true;
//                    }
//                    return;
//                }
//            }
//            myRC.yield();
//            return;

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
        } else if ((myRC.getRoundsUntilMovementIdle() == 0) && (enemyLoc != null)) {
            if ((myRC.canMove(directions[0].opposite())) &&
                    /*(!locations[0].equals(archonLocationX) && !locations[6].equals(archonLocationX) &&
                    !locations[7].equals(archonLocationX) && !locations[8].equals(archonLocationX)) &&*/
                    (locations[0].distanceSquaredTo(archonLocationX) > 2)) {

//                if (locations[0].distanceSquaredTo(enemyLoc) < 10) {
                    myRC.moveBackward();

//                } else {
//                    myRC.setDirection(directions[0].rotateLeft());
//                    attackMessage();
//                }

                myRC.yield();
//                System.out.println("i");
                return;
            } else {
                attackMessage();

                myRC.yield();
        //        System.out.println("j");
                return;
            }
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

//    private void goAttackAir(MapLocation targetLoc) throws GameActionException {
//        Direction[] closeDirections = Map.directionHierarchy(locations[0].directionTo(targetLoc));
//        if (myRC.canAttackSquare(targetLoc) && (myRC.getRoundsUntilAttackIdle() == 0)) {
//            if (myRC.canSenseSquare(targetLoc)) {
//                Robot robot = myRC.senseAirRobotAtLocation(targetLoc);
//                if ((robot != null) && (myRC.senseRobotInfo(robot).team.equals(myRC.getTeam().opponent()))) {
//                    if (locations[0].distanceSquaredTo(targetLoc) > 2) {
//                        myRC.attackAir(targetLoc);
//                    } else {
//                        myRC.attackAir(targetLoc.add(locations[0].directionTo(targetLoc)));
//                    }
//                    MessageEncoder messageEncoder = new MessageEncoder(myRC, myMS, MessageType.ENEMY_LOC, targetLoc);
//                    myRC.broadcast(messageEncoder.encodeMessage());
//
//                    myRC.yield();
//                    return;
//                } else {
//                    enemyID = 0;
//                    enemyLoc = null;
//                    enemyLevel = null;
//                    returned = true;
//                    return;
//                }
//            }
//        }
//        if (myRC.getRoundsUntilMovementIdle() == 0) {
//            boolean isGoodDirection = false;
//            for (int i = 0; i < 3; i++) {
//                if (directions[0].equals(closeDirections[i])) {
//                    isGoodDirection = true;
//                }
//            }
//            if (!isGoodDirection && locations[0].isAdjacentTo(targetLoc)) {
//                myRC.setDirection(locations[0].directionTo(targetLoc));
//
//                attackMessage();
//
//                myRC.yield();
//                return;
//            } else {
//                if (!locations[0].isAdjacentTo(targetLoc) && (locations[0] != targetLoc)) {
//                    goToLocation(targetLoc);
//                    return;
//                } else {
//                    enemyID = 0;
//                    enemyLoc = null;
//                    enemyLevel = null;
//                    returned = true;
//                }
//                return;
//            }
//        }
//    }

    private void goAttackGround(MapLocation targetLoc) throws GameActionException {
        Direction[] closeDirections = Map.directionHierarchy(locations[0].directionTo(targetLoc));
        if (myRC.canAttackSquare(targetLoc) && (myRC.getRoundsUntilAttackIdle() == 0)) {
            if (myRC.canSenseSquare(targetLoc)) {
                Robot robot = myRC.senseGroundRobotAtLocation(targetLoc);
                if ((robot != null) && (myRC.senseRobotInfo(robot).team.equals(myTeam.opponent()))) {
                    if (locations[0].distanceSquaredTo(targetLoc) < 5) {
                        myRC.attackGround(targetLoc.add(locations[0].directionTo(targetLoc)));
                        myRC.setIndicatorString(2, "a");
                    } else if (myRC.canAttackSquare(targetLoc)) {
                        myRC.attackGround(targetLoc);
                        myRC.setIndicatorString(2, "b");
                    } else {
                        myRC.attackGround(targetLoc.add(directions[0]));
                        myRC.setIndicatorString(2, "c");
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

        if (myRC.canAttackSquare(targetLoc.subtract(locations[0].directionTo(targetLoc))) &&
                (myRC.getRoundsUntilAttackIdle() == 0) && ((Clock.getRoundNum() - blindCount) < BLIND_COUNT)) {
            myRC.attackGround(targetLoc.subtract(locations[0].directionTo(targetLoc)));
            myRC.setIndicatorString(2, "d");

//            blindCount += 1;

            myRC.yield();
            return;
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

    private void preprocessEducation() throws GameActionException {
        allMessages = myRC.getAllMessages();

        for (Message message : allMessages) {
            MessageDecoder messageDecoder = new MessageDecoder(myRC, myMS, message);

            if (messageDecoder.isValid()) {
                if (messageDecoder.getType() == MessageType.ORIGIN && messageDecoder.getSourceLocation().distanceSquaredTo(myRC.getLocation()) < 3) {
                    myOrigin = messageDecoder.getOrigin();
                    corner = messageDecoder.getDirection();
    //                myMothersAngle = Math.atan((messageDecoder.getSourceLocation().getY() - myOrigin.getY())
    //                        / ((double) (messageDecoder.getSourceLocation().getX() - myOrigin.getX())));
    //                myRestLocation = myMothersLocation;
    //                for (int i = 0; i < DISTANCE_FROM_MOTHER; i++) {
    //                    myRestLocation = myRestLocation.add(corner);
    //                }
                }

                myMS.updateDontProcess(message);
            }
        }
    }

    private boolean preprocess() throws GameActionException {
//        if (enemyLoc != null) {
//            myRC.setIndicatorString(2, enemyLoc.toString());
//        }

        if (enemyLoc != null) {
            peacePeriod = 0;
        } else if (peacePeriod < PEACE_MIN) {
            peacePeriod += 1;
            warPeriod = 1;
        }

        if (warPeriod > 0) {
            warPeriod += 1;
        }

        if ((warPeriod > WAR_MIN) && (myRC.getEnergonLevel() > TRANSFORM_MIN)) {
            myRC.transform(RobotType.SOLDIER);
            myType = RobotType.SOLDIER;

            soldierPlayer = new SoldierPlayerX(myRC);
            myRC.yield();
            return true;
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
//            myRC.setIndicatorString(0,"rebroadcast");
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
//                            enemyLevel = msgDecoder.getLevel();
    //                        System.out.print("ourhash");
    //                        System.out.println(m.hashCode());
                        }
                    }
                }

//                System.out.print("z");
//                System.out.println(Clock.getBytecodeNum());

                if (msgDecoder.getType() == MessageType.ENEMY_LOCS) {
    //                airEnemyLocs = msgDecoder.getAirEnemyLocs();
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

        archonLocation = archonLocations[0];
        distanceSquared = 10001;
        for (MapLocation loc : archonLocations) {
            if ((locations[0].distanceSquaredTo(loc) < distanceSquared) &&
                    (Math.min(Math.abs(loc.getX() - myOrigin.getX()), Math.abs(loc.getY() - myOrigin.getY())) >  5)) {
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
//        Robot[] airRobots = myRC.senseNearbyAirRobots();
        if (archonLocation != null) {
            return false;
        }

        groundRobots = myRC.senseNearbyGroundRobots();
        if ((enemyID != 0) && (myRC.getRoundsUntilAttackIdle() == 0)) {
//            for (Robot robot : airRobots) {
//                if (myRC.canSenseObject(robot)) {
//                    info = myRC.senseRobotInfo(robot);
//                    if (info.id == enemyID) {
//                        if (myRC.canAttackSquare(info.location)) {
//                            enemyLoc = info.location;
//                            if (locations[0].distanceSquaredTo(enemyLoc) > 2) {
//                                myRC.attackAir(enemyLoc);
//                            } else {
//                                myRC.attackAir(enemyLoc.add(locations[0].directionTo(enemyLoc)));
//                            }
//                            MessageEncoder messageEncoder = new MessageEncoder(myRC, myMS, MessageType.ENEMY_LOC, enemyLoc);
//                            myRC.broadcast(messageEncoder.encodeMessage());
//                            myRC.yield();
//                            return true;
//                        }
//                    }
//                }
//            }
            for (Robot robot : groundRobots) {
                if (myRC.canSenseObject(robot)) {
                    info = myRC.senseRobotInfo(robot);
                    if (info.id == enemyID) {
                        if (myRC.canAttackSquare(info.location)) {
                            enemyLoc = info.location;
                            if (locations[0].distanceSquaredTo(enemyLoc) < 5) {
                                myRC.attackGround(enemyLoc.add(locations[0].directionTo(enemyLoc)));
                                myRC.setIndicatorString(2, "e");
                            } else if (myRC.canAttackSquare(enemyLoc)) {
                                myRC.attackGround(enemyLoc);
                                myRC.setIndicatorString(2, "f");
                            } else {
                                myRC.attackGround(enemyLoc.add(directions[0]));
                                myRC.setIndicatorString(2, "g");
                            }

                            enemyID = info.id;
                            enemyLoc = info.location;
                            enemyLevel = RobotLevel.ON_GROUND;

                            MessageEncoder messageEncoder = new MessageEncoder(myRC, myMS, MessageType.ENEMY_LOC,
                                    enemyLoc);

//                            myRC.setIndicatorString(0,"sense");

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
//            for (Robot robot : airRobots) {
//                if (myRC.canSenseObject(robot)) {
//                    info = myRC.senseRobotInfo(robot);
//                    if (info.team.equals(myRC.getTeam().opponent())) {
//                        enemyID = info.id;
//                        enemyLoc = info.location;
//                        enemyLevel = RobotLevel.IN_AIR;
//                        return false;
//                    }
//                }
//            }
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


//        distanceSquared = 10001;
//        boolean hasEnemy = false;
//        for (MapLocation loc : airEnemyLocs) {
//            if (locations[0].distanceSquaredTo(loc) < distanceSquared) {
//                distanceSquared = locations[0].distanceSquaredTo(loc);
//                enemyLoc = loc;
//                enemyLevel = RobotLevel.IN_AIR;
//                hasEnemy = true;
//            }
//        }
//        if (hasEnemy) {
//            return;
//        }
        distanceSquared = 10001;
        for (MapLocation loc : groundEnemyLocs) {
            if (locations[0].distanceSquaredTo(loc) < distanceSquared) {
                distanceSquared = locations[0].distanceSquaredTo(loc);
                enemyLoc = loc;
                enemyLevel = RobotLevel.ON_GROUND;
                
                blindCount = Clock.getRoundNum();
            }
        }
    }

    private void attackMessage() throws GameActionException {
        if (!myRC.hasBroadcastMessage() && (enemyMessage != null)) {
//            myRC.setIndicatorString(0,"attack");
            MessageStack.broadcastAttack(myRC, enemyMessage);
        }
    }
}
