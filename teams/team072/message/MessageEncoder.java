package team072.message;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import java.util.ArrayList;
import battlecode.common.MapLocation;
import battlecode.common.Message;
import battlecode.common.Robot;
import battlecode.common.RobotController;

/**
 * MessageEncoder takes information as constructor variables and translate the 
 * information into a message.
 *
 * @author Bunkie and Casablanca
 */
public class MessageEncoder {

    public enum MessageType {

        ORIGIN, ENEMY_LOC, ENERGON_REQUEST, BUILDING_REQUEST, BUILDING_SPAWNED,
        BUILDING_DIED, BUILDING_LOCATION, ENERGON_CONFIRMATION,
        TELE_FLEE_REQUEST, TELE_BUILD_REQUEST, TELE_ORIGIN_REQUEST, ENEMY_LOCS,
        ANGLE, ORIGINX, ORIGINY, ORIGINZ
    }

    public static int getDirectionNum(Direction direction) {
        int num = 0;

        switch (direction) {
            case NORTH:
                num = 0;
                break;
            case NORTH_EAST:
                num = 1;
                break;
            case EAST:
                num = 2;
                break;
            case SOUTH_EAST:
                num = 3;
                break;
            case SOUTH:
                num = 4;
                break;
            case SOUTH_WEST:
                num = 5;
                break;
            case WEST:
                num = 6;
                break;
            case NORTH_WEST:
                num = 7;
                break;
        }
        return num;
    }
    private ArrayList<Integer> ints;
    private ArrayList<String> strings;
    private ArrayList<MapLocation> locations;
    private RobotController myRC;

    private MessageEncoder(RobotController rc, MessageStack ms) {
        myRC = rc;
        ints = new ArrayList<Integer>();
        locations = new ArrayList<MapLocation>();
        strings = new ArrayList<String>();
        ints.add(104729 * Clock.getRoundNum());
        ints.add(myRC.getRobot().getID());
        locations.add(rc.getLocation());
    }

    public MessageEncoder(RobotController rc, MessageStack ms, MessageType type, MapLocation loc, Direction direction) {
        this(rc, ms);
        if (type == MessageType.ORIGIN) {
            ints.add(0);
        } else if (type == MessageType.ORIGINX) {
            ints.add(13);
        } else if (type == MessageType.ORIGINY) {
            ints.add(14);
        } else if (type == MessageType.ORIGINZ) {
            ints.add(15);
        }

        ints.add(getDirectionNum(direction));
        locations.add(loc);
    }

    public MessageEncoder(RobotController rc, MessageStack ms, MessageType type, MapLocation loc) throws GameActionException {
        this(rc, ms);

        if (type == MessageType.ENEMY_LOC) {
            ints.add(1);
            locations.add(loc);

            Robot groundRobot = myRC.senseGroundRobotAtLocation(loc);

            if ((groundRobot != null) && (myRC.senseRobotInfo(groundRobot).type.isBuilding())) {
                ints.add(groundRobot.getID());
                ints.add(0);
            } else if (groundRobot == null) {
                Robot airRobot = myRC.senseAirRobotAtLocation(loc);
                if (airRobot != null) {
                    ints.add(airRobot.getID());
                    ints.add(1);
                }
            } else if (groundRobot != null) {
                ints.add(groundRobot.getID());
                ints.add(0);
            }
        } else if (type == MessageType.BUILDING_LOCATION) {
            ints.add(6);
            locations.add(loc);
        }
    }

    public MessageEncoder(RobotController rc, MessageStack ms, MessageType type, int id) {
        this(rc, ms);

        if (type == MessageType.ENERGON_REQUEST) {
            ints.add(2);
        } else if (type == MessageType.BUILDING_REQUEST) {
            ints.add(3);
        } else if (type == MessageType.ENERGON_CONFIRMATION) {
            ints.add(7);
        } else if (type == MessageType.ANGLE) {
            ints.add(12);
        }

        ints.add(id);
    }

    public MessageEncoder(RobotController rc, MessageStack ms, MessageType type) {
        this(rc, ms);

        if (type == MessageType.BUILDING_SPAWNED) {
            ints.add(4);
        } else if (type == MessageType.BUILDING_DIED) {
            ints.add(5);
        } else if (type == MessageType.TELE_FLEE_REQUEST) {
            ints.add(8);
        } else if (type == MessageType.TELE_BUILD_REQUEST) {
            ints.add(9);
        } else if (type == MessageType.TELE_ORIGIN_REQUEST) {
            ints.add(10);
        }
    }

    public MessageEncoder(RobotController rc, MessageStack ms,
            MessageType type, MapLocation[] airEnemyLocs, MapLocation[] groundEnemyLocs) {
        this(rc, ms);

        ints.add(11);

        ints.add(airEnemyLocs.length);

        for (MapLocation loc : airEnemyLocs) {
            locations.add(loc);
        }
        for (MapLocation loc : groundEnemyLocs) {
            locations.add(loc);
        }
    }

    public Message encodeMessage() {
        Message message = new Message();

        message.ints = new int[ints.size()];
        for (int i = 0; i < ints.size(); i++) {
            message.ints[i] = ints.get(i).intValue();
        }

        message.locations = locations.toArray(new MapLocation[locations.size()]);

        if (strings.size() != 0) {
            message.strings = strings.toArray(new String[strings.size()]);
        }

        return message;
    }
}
