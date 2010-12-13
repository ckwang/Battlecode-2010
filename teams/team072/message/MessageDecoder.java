package team072.message;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.Message;
import battlecode.common.RobotController;
import battlecode.common.RobotLevel;

/**
 * MessageDecoder takes an encoded message and gives robot access to information
 * via methods.  It also checks if the message is valid or out of date.
 *
 * @author Bunkie and Casablanca
 */
public class MessageDecoder {
    public static final MapLocation locationKey = new MapLocation(104729, 104729);

    public static Direction[] DIRECTIONS = {Direction.NORTH,
        Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST,
        Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST,
        Direction.NORTH_WEST};
    private int[] ints;
    private MapLocation[] locations;
    private MessageStack myMS;
    private Message myMessage;

    public MessageDecoder(RobotController rc, MessageStack ms, Message message) {
        myMS = ms;
        myMessage = message;
        ints = myMessage.ints;
        locations = myMessage.locations;
    }

    public boolean isValid() {
        if ((ints == null) || (ints.length == 0)) {
//            System.out.println("ab");
            return false;
        }

        if (ints.length < 3) {
//            System.out.println("bc");
            return false;
        }

        int res = (ints[0] % 104729);

        if (res != 0) {
//            System.out.println(ints[0]);
//            System.out.println("cd");
            return false;
        } else {
            if ((Clock.getRoundNum() - (myMessage.ints[0] / 104729)) > 10) {
//                System.out.println("de");
                return false;
            } else {
//                System.out.println("ef");
                return !(myMS.dontProcess(myMessage));
            }
        }
    }

    public boolean isEnemy() {
        if ((locations.length == 1) && (locations[0].equals(locationKey))) {
            return false;
        }

        if ((ints == null) || (ints.length < 3)) {
            return true;
        }

        int res = ints[0] % 104729;

        if (res != 0) {
            return true;
        }

        return false;
    }

    public boolean isValidX() {
        if ((Clock.getRoundNum() - (myMessage.ints[0] / 104729)) > 10) {
//            System.out.println("fefew");
            return false;
        } else {
//            System.out.println("fwef");
            return !(myMS.dontProcess(myMessage));
        }
    }


    public int getTimeStamp() {
        return (ints[0] / 104729);
    }

    public MessageEncoder.MessageType getType() {
        MessageEncoder.MessageType type = null;

        switch (myMessage.ints[2]) {
            case 0:
                type = MessageEncoder.MessageType.ORIGIN;
                break;
            case 1:
                type = MessageEncoder.MessageType.ENEMY_LOC;
                break;
            case 2:
                type = MessageEncoder.MessageType.ENERGON_REQUEST;
                break;
            case 3:
                type = MessageEncoder.MessageType.BUILDING_REQUEST;
                break;
            case 4:
                type = MessageEncoder.MessageType.BUILDING_SPAWNED;
                break;
            case 5:
                type = MessageEncoder.MessageType.BUILDING_DIED;
                break;
            case 6:
                type = MessageEncoder.MessageType.BUILDING_LOCATION;
                break;
            case 7:
                type = MessageEncoder.MessageType.ENERGON_CONFIRMATION;
                break;
            case 8:
                type = MessageEncoder.MessageType.TELE_FLEE_REQUEST;
                break;
            case 9:
                type = MessageEncoder.MessageType.TELE_BUILD_REQUEST;
                break;
            case 10:
                type = MessageEncoder.MessageType.TELE_ORIGIN_REQUEST;
                break;
            case 11:
                type = MessageEncoder.MessageType.ENEMY_LOCS;
                break;
            case 12:
                type = MessageEncoder.MessageType.ANGLE;
                break;
            case 13:
                type = MessageEncoder.MessageType.ORIGINX;
                break;
            case 14:
                type = MessageEncoder.MessageType.ORIGINY;
                break;
            case 15:
                type = MessageEncoder.MessageType.ORIGINZ;
        }
        return type;
    }

    public int getSourceID() {
        return myMessage.ints[1];
    }

    public MapLocation getOrigin() {
        return myMessage.locations[1];
    }

    public Direction getDirection() {
        return DIRECTIONS[ints[3]];
    }

    public MapLocation getEnemyLoc() {
        return myMessage.locations[1];
    }

    public int getEnemyID() {
        return myMessage.ints[3];
    }

    public RobotLevel getLevel() {
        if (ints[4] == 0) {
            return RobotLevel.ON_GROUND;
        } else {
            return RobotLevel.IN_AIR;
        }
    }

    public MapLocation[] getAirEnemyLocs() {
        int size = ints[3];

        MapLocation[] airEnemyLocs = new MapLocation[size];
        for (int i = 0; i < size; i++) {
            airEnemyLocs[i] = locations[i + 1];
        }
        return airEnemyLocs;
    }

    public MapLocation[] getGroundEnemyLocs() {
        int airSize = ints[3];
        int size = locations.length - airSize - 1;

        MapLocation[] groundEnemyLocs = new MapLocation[size];
        for (int i = 0; i < size; i++) {
            groundEnemyLocs[i] = locations[i + 1 + airSize];
        }

        return groundEnemyLocs;
    }

    public int getID() {
        return ints[3];
    }

    public int getAngle() {
        return ints[3];
    }

    public MapLocation getBuildingLocation() {
        return myMessage.locations[1];
    }

    public MapLocation getSourceLocation() {
        return myMessage.locations[0];
    }
}
