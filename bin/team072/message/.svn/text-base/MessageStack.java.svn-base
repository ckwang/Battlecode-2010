package team072.message;

import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.Message;
import battlecode.common.RobotController;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotInfo;
import battlecode.common.RobotLevel;

/**
 *
 * @author Ying
 */
public class MessageStack {

    private Message[] toBroadcast;
    private int toBroadcastIndex;
    private int[][] dontProcess;
    private int dontProcessIndex;

    public MessageStack() {
        toBroadcast = new Message[5];
        toBroadcastIndex = 0;
        dontProcess = new int[5][2];
        dontProcessIndex = 0;
//        dontProcess = new boolean[]
    }

    public Message getNextBroadcast() {
        Message message = toBroadcast[(toBroadcastIndex + 4) % 5];
        if ((message != null) &&
                (Clock.getRoundNum() - (message.ints[0] / 104729) < 10)) {
            toBroadcast[(toBroadcastIndex + 4) % 5] = null;
            return message;
        } else {
            return null;
        }
    }

    public void updateBroadcast(Message message) {

        toBroadcast[toBroadcastIndex] = message;
        toBroadcastIndex = (toBroadcastIndex + 1) % 5;
    }

    public boolean dontProcess(Message message) {

        int timeStamp = message.ints[0] / 104729;
        int id = message.ints[1];

        for (int i = 0; i < 5; i++) {
            if (timeStamp == dontProcess[i][0]) {
                if (id == dontProcess[i][1]) {
                    return true;
                }
            }
        }
        return false;
    }

    public void updateDontProcess(Message message) {

        int timeStamp = message.ints[0] / 104729;
        dontProcess[dontProcessIndex][0] = timeStamp;
        dontProcess[dontProcessIndex][1] = message.ints[1];
        dontProcessIndex = (dontProcessIndex + 1) % 5;
    }

    public static boolean rebroadcast(RobotController rc, MessageStack ms, Message[] messages) throws GameActionException {
        int num = messages.length;

        Message[] rebroadcastPriority = new Message[5];

        int num0 = 0;

        for (int i = 0; i < num; i++) {
            if (messages[i] != null) {
                int type = messages[i].ints[2];
                if ((type == 11) && (num0 < 5)) {
                    rebroadcastPriority[num0] = messages[i];
                    num0 += 1;
                }
            }
        }

        boolean hasBroadcasted = false;

        if (!rc.hasBroadcastMessage()) {
            if (rebroadcastPriority[0] != null) {
                rc.broadcast(rebroadcastPriority[0]);
                hasBroadcasted = true;
            } else {
                Message toBroadcast = ms.getNextBroadcast();
                if (toBroadcast != null) {
                    rc.broadcast(toBroadcast);
                }
            }

            for (int i = 1; i < 5; i++) {
                if (rebroadcastPriority[i] != null) {
                    ms.updateBroadcast(rebroadcastPriority[i]);
                } else {
                    break;
                }
            }
        } else {
            for (int i = 0; i < 5; i++) {
                if (rebroadcastPriority[i] != null) {
                    ms.updateBroadcast(rebroadcastPriority[i]);
                } else {
                    break;
                }
            }
        }

        return hasBroadcasted;
    }

//    public static boolean rebroadcast(RobotController rc, MessageStack ms, Message[] messages) throws GameActionException {
//
//        int num = messages.length;
//        Message[][] rebroadcastPriority = new Message[6][3];
//        int num0 = 0;
//        int num1 = 0;
//        int num2 = 0;
//
//        for (int i = 0; i < num; i++) {
//            if (messages[i] != null) {
//                int type = messages[i].ints[2];
//                if ((type == 5) && (num0 < 6)) {
//                    rebroadcastPriority[num0][0] = messages[i];
//                    num0 += 1;
//                } else if ((type == 4) && (num1 < 6)) {
//                    rebroadcastPriority[num1][1] = messages[i];
//                } else if ((type == 1) && (num1 < 6)) {
//                    rebroadcastPriority[num2][2] = messages[i];
//                }
//            }
//        }
//
//        num = 0;
//        Message[] broadcastStack = new Message[6];
//        for (int i = 0; (i < 3) && (num < 6); i++) {
//            for (int j = 0; (j < 6) && (num < 6); j++) {
//                Message message = rebroadcastPriority[j][i];
//                if (message != null) {
//                    broadcastStack[num] = message;
//                    num += 1;
//                } else {
//                    break;
//                }
//            }
//        }
//
//        boolean hasBroadcasted = false;
//
//        if (broadcastStack[0] != null) {
//            rc.broadcast(broadcastStack[0]);
//            hasBroadcasted = true;
//        } else {
//            Message toBroadcast = ms.getNextBroadcast();
//            if (toBroadcast != null) {
//                rc.broadcast(toBroadcast);
//            }
//        }
//
//        for (int i = 1; i < 6; i++) {
//            if (broadcastStack[i] != null) {
//                ms.updateBroadcast(broadcastStack[i]);
//            } else {
//                break;
//            }
//        }
//
//        return hasBroadcasted;
//    }

    public static boolean broadcastAttack(RobotController rc, Message enemyMessage) throws GameActionException {
        if (enemyMessage.locations != null) {
//            int num = enemyMessage.locations.length;
            enemyMessage.strings = null;
//            if (num != 0) {
//                enemyMessage.locations = new MapLocation[0];
//            } else {
//                enemyMessage.locations = null;
//            }
            enemyMessage.locations = new MapLocation[1];
            enemyMessage.locations[0] = MessageDecoder.locationKey;

            rc.broadcast(enemyMessage);
            return true;
        }

        int[] ints = enemyMessage.ints;

        if (ints != null) {
            enemyMessage.locations = new MapLocation[1];
            enemyMessage.locations[0] = MessageDecoder.locationKey;

            if (ints.length == 2) {
                enemyMessage.ints[1] = 0;

                rc.broadcast(enemyMessage);
                return true;
            } else if (ints.length > 2) {
                enemyMessage.ints[ints.length - 3] = ints[ints.length - 3] + ints[ints.length - 2];
                enemyMessage.ints[ints.length - 2] = 0;

                rc.broadcast(enemyMessage);
                return true;
            }
        }
        
        if (enemyMessage.strings != null) {
            enemyMessage.locations = new MapLocation[1];
            enemyMessage.locations[0] = MessageDecoder.locationKey;
            
            int num = enemyMessage.strings.length;

            if (num != 0) {
                enemyMessage.strings = new String[0];
            } else {
                enemyMessage.strings = null;
            }

            rc.broadcast(enemyMessage);
            return true;
        }

        return false;
    }

    public static boolean respondEnemyLoc(RobotController rc, MessageStack ms,
            Message message) throws GameActionException {

        MessageDecoder messageDecoder = new MessageDecoder(rc, ms, message);
        RobotLevel level = messageDecoder.getLevel();
        MapLocation location = messageDecoder.getEnemyLoc();
        int id = messageDecoder.getEnemyID();
        if (!rc.canSenseSquare(messageDecoder.getEnemyLoc())) {
            if (rc.canAttackSquare(messageDecoder.getEnemyLoc())) {
                attack(rc, level, location);
                return true;
            } else {
//                Direction direction = rc.getLocation().directionTo(location);
//                if (inSensorDirection(rc, location)) {
//                    myRC
//                }
            }
        } else {
            Robot[] robots = null;
            if (level == RobotLevel.IN_AIR) {
                robots = rc.senseNearbyAirRobots();
            } else {
                robots = rc.senseNearbyGroundRobots();
            }
            for (Robot robot : robots) {
                RobotInfo info = rc.senseRobotInfo(robot);
                if (id == info.id) {
                    attack(rc, level, info.location);
                    return true;
                }
            }
        }
        return false;
    }

    private static void attack(RobotController rc, RobotLevel level,
            MapLocation location) throws GameActionException {
        if (level == RobotLevel.IN_AIR) {
            rc.attackAir(location);
        } else {
            rc.attackGround(location);
        }
    }

//    public boolean hasBroadcasted(Message message) {
//        assert message.ints[0] % 104729 == 0;
//
//        int timeStamp = message.ints[0] / 104729;
//        for (int i = 0; i < 10; i++) {
//            if (broadcasted[i][0] == timeStamp) {
//                int id = message.ints[1];
//                if (broadcasted[i][1] == id) {
//                    return true;
//                }
//            }
//        }
//
//        return false;
//    }

//    public boolean dontProcess(Message message) {
//        assert message.ints[0] % 104729 == 0;
//
//        int timeStamp = message.ints[0] / 104729;
//        int id = message.ints[1];
//
//
//
//    }

//    public void updateReceived(int timeStamp, int id) {
//
//    }
}