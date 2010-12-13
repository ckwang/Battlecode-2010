package team072.baseplayer;

import battlecode.common.Clock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

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
import battlecode.common.TerrainTile.TerrainType;

public class ArchonWallAI extends ArchonAI{
	private RobotController myRC;
	private MessageStack myMS;
	
	private MapLocation origin;
	private Direction centerDir;
	private Direction searchDir;
	private int priority;
	
	private int moveDistance;
	private MapLocation position;
	private int state;
	private boolean spawned = false;
	private int count = 0;
	
	private final int SEARCHING = 0;	private final int WAITING = 1;
	private final int MATURE = 2;	private final int FIGHTING = 3;
	private final int SUPPORT_THRESHOLD = 35;
	private final int SPAWN_WOUT_ENERGON_THRESHOLD = 70;	// energon required to spawn wout
	private final int SPAWN_CHAINER_ENERGON_THRESHOLD = 70;	// energon required to spawn soldier
	
	public ArchonWallAI(RobotController rc, MessageStack ms, MapLocation o, int p, Direction cdir, Direction sdir ){
		myRC = rc;
		myMS = ms;
		origin = o;
		priority = p;
		centerDir = cdir;
		searchDir = sdir;
		

		// if centerDir is not diagonal, start searching
		if ( centerDir.isDiagonal() ) {
			state = MATURE;
		} else if ( priority <= 1 ) {
			state = SEARCHING;
		} else if ( priority > 1 ) {
			state = WAITING;
		}
		
		renewPosition();
	}
	
	public MapLocation proceed1() throws GameActionException {

		switch (state) {
		case SEARCHING:
			
			myRC.setIndicatorString(2, "searching");
			runSearching();
			break;
		case WAITING:

			myRC.setIndicatorString(2, "waiting");
			runWaiting();
			
			
			if ( priority == 5 ){
				return origin;
			}
			break;
		case MATURE:
			
			myRC.setIndicatorString(2, "mature");
			runMature();
			
			if ( priority == 5 ){
				return origin;
			}
			break;
		case FIGHTING:
			
			myRC.setIndicatorString(2, "fighting");
			runFight();
			break;
		}
		
		return null;
	}
	
	/***
	 * Wait for the signal from the two scout archons
	 * @throws GameActionException
	 */
	private void runWaiting() throws GameActionException {
		
		
		ArrayList<MapLocation> archonLocs;

		MapLocation [] scoutLastLocs = new MapLocation [2];	// archon locations last round
		MapLocation [] scoutCurrentLocs = new MapLocation [2];	// archon locations this round
		
		
		// wait for 10 rounds
		for ( int i = 0; i < 10; i++ ) {
			myRC.yield();
		}
		
		// wait for signal
		while (true) {
			
			archonLocs = sortArchon();
			scoutLastLocs[0] = archonLocs.get(0);	scoutLastLocs[1] = archonLocs.get(1);
			myRC.yield();
			
			archonLocs = sortArchon();
			scoutCurrentLocs[0] = archonLocs.get(0);	scoutCurrentLocs[1] = archonLocs.get(1); 
			
			for ( MapLocation cLoc : scoutCurrentLocs ) {
				Direction scoutMovement = Direction.OMNI;
				Direction forwardDirection = origin.directionTo( cLoc );	// the direction to which the archon is supposed to move
				for ( MapLocation lLoc : scoutLastLocs ) {
					if ( cLoc.isAdjacentTo(lLoc) || cLoc.equals(lLoc) )
						scoutMovement = lLoc.directionTo(cLoc);	// the movement of the archon between the two rounds
				}
				
				if ( scoutMovement == forwardDirection.opposite() ) {	// if this archon is moving backward
					origin = cLoc;	// reset origin
					
					// reset centerDir
					if ( forwardDirection.rotateLeft().rotateLeft().equals(centerDir)  ) {
						centerDir = centerDir.rotateLeft();
					} else {
						centerDir = centerDir.rotateRight();
					}
					
					state = MATURE;
					renewPosition();
					return;
				}
			}
			
		}
		
		
	}
	
	/***
	 * Search for a boundary and give a moving-backward signal
	 * @throws GameActionException
	 */
	private void runSearching() throws GameActionException {

		MapLocation otherScoutLastLoc;
		boolean isFirst = true;
		
		myRC.setDirection(searchDir);
		myRC.yield();
		
		
		while ( myRC.senseTerrainTile( myRC.getLocation().add(searchDir) ).getType() != TerrainType.OFF_MAP ) {
			otherScoutLastLoc = sortArchon().get(0);
			
			if ( myRC.getRoundsUntilMovementIdle() == 0 ) {
				myRC.moveForward();
			}
			myRC.yield();
			
			Direction otherScoutMovement = otherScoutLastLoc.directionTo( sortArchon().get(0) );
			Direction forwardDirection = origin.directionTo(otherScoutLastLoc);
			if ( otherScoutMovement == forwardDirection.opposite() ) {
				isFirst = false;
				break;
			} 
		}
		
		for ( int i = 0; i < 6; i++ ) {
			if ( !isFirst) {
				break;
			}
			
			otherScoutLastLoc = sortArchon().get(0);
			
			if ( myRC.getRoundsUntilMovementIdle() == 0 ) {
				myRC.moveBackward();
			} 
			myRC.yield();
			
			Direction otherScoutMovement = otherScoutLastLoc.directionTo( sortArchon().get(0) );
			Direction forwardDirection = origin.directionTo(otherScoutLastLoc);
			if ( otherScoutMovement == forwardDirection.opposite() ) {
				isFirst = false;
				break;
			}  
			
		}
		
		if ( isFirst ) {
			if ( centerDir.rotateLeft().rotateLeft().equals(searchDir)  ) {
				centerDir = centerDir.rotateRight();
			} else {
				centerDir = centerDir.rotateLeft();
			}
			origin = myRC.getLocation();
		} else {
			if ( centerDir.rotateLeft().rotateLeft().equals(searchDir) ) {
				centerDir = centerDir.rotateLeft();
			} else {
				centerDir = centerDir.rotateRight();
			}
			origin = sortArchon().get(0);
		}
		
		myRC.yield();
		renewPosition();
		state = MATURE;
		return;
		
	}

	/***
	 * Move outward periodically and spawning units
	 * @throws GameActionException
	 */
	private void runMature() throws GameActionException {
		
		// check messages
		MessageDecoder msgDecoder;
		Message [] msgs = myRC.getAllMessages();

        for ( Message msg : msgs ) {
            if (Clock.getBytecodeNum() > 4000) {
                break;
            }
			msgDecoder = new MessageDecoder( myRC, myMS, msg );
			if ( msgDecoder.isValid() ) {
				switch ( msgDecoder.getType() ) {
				case ENEMY_LOCS:
					state = FIGHTING;
					myRC.broadcast( msg );
					myRC.yield();
					return;
					
				}
			}
		}
		
		// if enemy is near, fight
		MapLocation [] groundEnemyLocs = senseGroundEnemyLocations();
		MapLocation [] airEnemyLocs = senseAirEnemyLocations();
		
		if ( groundEnemyLocs.length + airEnemyLocs.length > 0 ) {
			state = FIGHTING;
			MessageEncoder msgEncoder = new MessageEncoder( myRC, myMS, MessageType.ENEMY_LOCS, airEnemyLocs, groundEnemyLocs );
			myRC.broadcast(msgEncoder.encodeMessage());
			myRC.yield();
			
			return;
		}
		
		
		// renew position
		renewPosition();
		
		// go to the new spot;
		goTo( position );
		
		if ( Clock.getBytecodeNum() > 4500 ) 
			myRC.yield();
		
		// support the team
		supportTeam();
		
		// transfer self
		transferSelf();
		
		// spawn a unit if possible
		if ( myRC.getEnergonLevel() > SPAWN_WOUT_ENERGON_THRESHOLD 
				&& count ==0 ) {
			spawned = true;
			spawn( RobotType.WOUT );
			supportTeam();
			myRC.yield();
			count++;
			return;
		} else if ( myRC.getEnergonLevel() > SPAWN_CHAINER_ENERGON_THRESHOLD ) {
			spawn( RobotType.CHAINER );
			if (count == 3){
				count =0;
			} else {
				count ++;
			}
			supportTeam();
			myRC.yield();
			return;
		}
		
		myRC.yield();
	}
	
	/***
	 * Fight for enemies
	 */
	private void runFight() throws GameActionException {
		MapLocation [] groundEnemyLocs = senseGroundEnemyLocations();
		MapLocation [] airEnemyLocs = senseAirEnemyLocations();
		MapLocation [] msgGroundEnemyLocs = null;
		MapLocation [] msgAirEnemyLocs = null;
		
		
		
		// check messages
		MessageDecoder msgDecoder;
		Message [] msgs = myRC.getAllMessages();

		for ( Message msg : msgs ) {
            if (Clock.getBytecodeNum() > 4000) {
                break;
            }

			msgDecoder = new MessageDecoder( myRC, myMS, msg );
			if ( msgDecoder.isValid() ) {
				switch ( msgDecoder.getType() ) {
				case ENEMY_LOCS:
					msgGroundEnemyLocs = msgDecoder.getGroundEnemyLocs();
					msgAirEnemyLocs = msgDecoder.getAirEnemyLocs();
					goTo( msgDecoder.getSourceLocation() );
					break;
				}
			}
		}
	
		// if no enemy is near, return mature
		
		if ( msgGroundEnemyLocs == null && msgAirEnemyLocs == null && groundEnemyLocs.length + airEnemyLocs.length == 0 ) {
			state = MATURE;
			myRC.yield();
			return;
		}
		
		// send enemy location message
		if ( groundEnemyLocs.length + airEnemyLocs.length > 0 ) {
			MessageEncoder msgEncoder = new MessageEncoder( myRC, myMS, MessageType.ENEMY_LOCS, airEnemyLocs, groundEnemyLocs );
			myRC.broadcast( msgEncoder.encodeMessage() );
		}
		
		if ( Clock.getBytecodeNum() > 4500 ) 
			myRC.yield();
		
		// support the team
		supportTeam();
		
		// transfer self
		transferSelf();
		
		// spawn a chainer if possible
//		if ( myRC.getEnergonLevel() > SPAWN_CHAINER_ENERGON_THRESHOLD ) {
//			spawn( RobotType.CHAINER );
//			myRC.yield();
//			return;
//		}

        if ( myRC.getEnergonLevel() > SPAWN_WOUT_ENERGON_THRESHOLD
				&& count ==0 ) {
			spawned = true;
			spawn( RobotType.SOLDIER );
			supportTeam();
			myRC.yield();
			count++;
			return;
		} else if ( myRC.getEnergonLevel() > SPAWN_CHAINER_ENERGON_THRESHOLD ) {
			spawn( RobotType.CHAINER );
			if (count == 1){
				count =0;
			} else {
				count ++;
			}
			supportTeam();
			myRC.yield();
			return;
		}
		
		// go near enemy
		if ( groundEnemyLocs.length < airEnemyLocs.length ) {
			MapLocation nearest = calNearestUnit(airEnemyLocs);
			int distance = (int) Math.sqrt( myRC.getLocation().distanceSquaredTo( nearest ) );
			if ( distance > 4 ) {
				goTo( nearest );
			} 
		} else if ( groundEnemyLocs.length > 0 ){
			MapLocation nearest = calNearestUnit(groundEnemyLocs);
			int distance = (int) Math.sqrt( myRC.getLocation().distanceSquaredTo( nearest ) );
			if ( distance > 5.1 ) {
				goTo( nearest );
			} else if ( distance <= 5.1 /*|| origin.distanceSquaredTo( myRC.getLocation() ) > Math.sqrt( origin.distanceSquaredTo( calBuildArchon() ) ) + 16 */) {
				goTo( myRC.getLocation().add( myRC.getLocation().directionTo(nearest).opposite() ) );
			}
		} else if ( msgGroundEnemyLocs != null && msgGroundEnemyLocs.length > 0) {
		
			MapLocation nearest = calNearestUnit(msgGroundEnemyLocs);
			int distance = (int) Math.sqrt( myRC.getLocation().distanceSquaredTo( nearest ) );
			if ( distance > 5.1 ) {
				goTo( nearest );
			} else if ( distance <= 5.1 ) {
				goTo( position );
			} 
		}
		
		myRC.yield();
	}
	
	private void renewPosition() {
		int x = origin.getX();	int y = origin.getY();
//		double theta = 22.5 * priority * Math.PI / 180;
        double theta = (65 - 10 * priority) * Math.PI / 180;
		MapLocation newPosition;
		
		int newMoveDistance = (int) Math.sqrt( origin.distanceSquaredTo( calBuildArchon() ) ) + 12;
		if ( moveDistance >= newMoveDistance )
			return;
		
		moveDistance = newMoveDistance;
		
		switch (centerDir) {
		case NORTH_EAST:
			x += (int) (moveDistance * Math.cos(theta));
			y -= (int) (moveDistance * Math.sin(theta));
			break;
		case SOUTH_EAST:
			x += (int) (moveDistance * Math.cos(theta));
			y += (int) (moveDistance * Math.sin(theta));
			break;
		case SOUTH_WEST:
			x -= (int) (moveDistance * Math.cos(theta));
			y += (int) (moveDistance * Math.sin(theta));
			break;
		case NORTH_WEST:
			x -= (int) (moveDistance * Math.cos(theta));
			y -= (int) (moveDistance * Math.sin(theta));
			break;
		}
		
		newPosition = new MapLocation(x, y);
		
		if ( myRC.canSenseSquare(newPosition) && myRC.senseTerrainTile(newPosition).getType() == TerrainType.OFF_MAP ) {
			if ( theta <= Math.PI / 4 ) {
				x = position.getX();	y = position.getY();
				int r = moveDistance - (int) Math.sqrt( origin.distanceSquaredTo( position ) );
				switch (centerDir) {
				case NORTH_EAST:
					y -= r;
					break;
				case SOUTH_EAST:
					y += r;
					break;
				case SOUTH_WEST:
					y += r;
					break;
				case NORTH_WEST: 
					y -= r;
					break;
				}
			} else {
				x = position.getX();	y = position.getY();
				int r = moveDistance - (int) Math.sqrt( origin.distanceSquaredTo( position ) );
				switch (centerDir) {
				case NORTH_EAST:
					x += r;
					break;
				case SOUTH_EAST:
					x += r;
					break;
				case SOUTH_WEST:
					x -= r;
					break;
				case NORTH_WEST: 
					x -= r;
					break;
				}
			}
		}
		
		position = new MapLocation(x, y);
	}
	
	private void goTo(MapLocation loc) throws GameActionException {
		
		if ( /*myRC.getLocation().isAdjacentTo(loc) ||*/ myRC.getLocation().equals(loc) ){
			return;
		}
		
		
		Direction dir = myRC.getLocation().directionTo(loc);
		for ( Direction d : Map.directionHierarchy(dir) ) {

			if ( myRC.canMove(d) ) {
				dir = d;
				break;
			}
		}
		
		if ( myRC.getRoundsUntilMovementIdle() == 0 ){
			
			if ( myRC.getDirection() == dir ){
				myRC.moveForward();
			} else if ( myRC.getDirection() == dir.opposite() ) {
				myRC.moveBackward();
			} else {
				myRC.setDirection(dir);
				myRC.yield();
				while ( myRC.getRoundsUntilMovementIdle() != 0) {
					myRC.yield();
				}
				if ( myRC.canMove(dir) ) 
					myRC.moveForward();
			}
			
			myRC.yield();
			
		} 
		
	}
	
	/***
	 * Give energon to adjacent wouts when conditions are satisfied.
	 * @throws GameActionException
	 */
	private void supportTeam() throws GameActionException {
		Robot[] robots = myRC.senseNearbyGroundRobots();
		
		for ( Robot r : robots ) {
			
			if ( myRC.getEnergonLevel() < SUPPORT_THRESHOLD )
				break;
			
			if ( myRC.canSenseObject(r) ) {
				RobotInfo info = myRC.senseRobotInfo(r);
				
				if ( myRC.getTeam().equals(info.team) 
						&& !info.type.isBuilding() && !info.type.isAirborne()
						&& myRC.getLocation().isAdjacentTo(info.location)
						&& info.eventualEnergon < 40 ) {
						double transfer = Math.min(
								info.type.maxEnergon() - info.eventualEnergon,
								GameConstants.ENERGON_RESERVE_SIZE - info.energonReserve);
						myRC.transferUnitEnergon(transfer, info.location,
								RobotLevel.ON_GROUND);
				}
			}
			
		}
		
	}
	
	/***
	 * Spawn a unit in a direction available
	 * @throws GameActionException
	 */
	private void spawn( RobotType type ) throws GameActionException {

        while (( myRC.getRoundsUntilMovementIdle() != 0 ) ||
                (myRC.hasActionSet())) {
            myRC.yield();
        }

		final Direction [] dlist = Map.directionHierarchy( myRC.getDirection() );
		
		for ( Direction d : dlist ) {
			MapLocation loc = myRC.getLocation().add(d);
			if (myRC.senseTerrainTile(loc).getType() == TerrainType.LAND 
					&& myRC.senseGroundRobotAtLocation(loc) == null) {
				myRC.setDirection(d);
				myRC.yield();
                if (myRC.senseGroundRobotAtLocation(loc) == null) {
                    myRC.spawn( type );
                    for ( int i = 0; i < 1; i++ ) {
                    	myRC.yield();
                    }
                    
                    if ( type == RobotType.CHAINER || type == RobotType.SOLDIER ) {
                    	MessageEncoder msgEncoder = new MessageEncoder( myRC, myMS, MessageType.ORIGIN, origin, centerDir );
                    	myRC.broadcast( msgEncoder.encodeMessage() );
                    } else if (type == RobotType.WOUT) {
                    	MessageEncoder msgEncoder = new MessageEncoder( myRC, myMS, MessageType.ORIGIN, origin, centerDir );
                    	myRC.broadcast( msgEncoder.encodeMessage() );
                    }
                }
				return;
			}
		}
	
	}

	/***
	 * 
	 * @return A list of archon locations sorted by the proximity to this robot ( the first one is the farthest )
	 */
	private ArrayList<MapLocation> sortArchon() {
		MapLocation[] archonLocs = myRC.senseAlliedArchons();
		final MapLocation myLoc = myRC.getLocation();
		ArrayList<MapLocation> locList = new ArrayList<MapLocation>(Arrays
				.asList(archonLocs));
		Collections.sort(locList, new Comparator<MapLocation>() {
			public int compare(MapLocation a, MapLocation b) {
				if (a.distanceSquaredTo(myLoc) > b.distanceSquaredTo(myLoc)) {
					return -1;
				} else if (a.distanceSquaredTo(myLoc) < b.distanceSquaredTo(myLoc)) {
					return 1;
				} else {
					return 0;
				}
			}
		});
		
		
		return locList;
	}
	
	private MapLocation calBuildArchon() {
		MapLocation [] locs = myRC.senseAlliedArchons();
		MapLocation result = locs[0];
		
		for ( MapLocation loc : locs ) {
			if ( origin.distanceSquaredTo(loc) < origin.distanceSquaredTo(result) ) {
				result = loc;
			}
		}
		
		return result;
		
	}
	
	private MapLocation calNearestUnit(MapLocation [] locs) {
		final MapLocation myLoc = myRC.getLocation();
		MapLocation result = locs[0];
		
		for ( MapLocation loc : locs ) {
			if ( myLoc.distanceSquaredTo(loc) < myLoc.distanceSquaredTo(result) ) {
				result = loc;
			}
		}
		
		return result;
		
	}
	
	private MapLocation [] senseAirEnemyLocations() throws GameActionException {
		
		Robot [] nearbyAirRobots = myRC.senseNearbyAirRobots();
		LinkedList<MapLocation> nearbyEnemyLocs = new LinkedList<MapLocation>();
		
		// select all the enemies out of the robots in range
		for ( Robot r : nearbyAirRobots ) {
			RobotInfo robotInfo = myRC.senseRobotInfo(r);
			if ( robotInfo == null )	continue;
			if ( robotInfo.team != myRC.getTeam() ) {	// is enemy
				nearbyEnemyLocs.add( myRC.senseRobotInfo(r).location );
			}
		
		}
		
		return nearbyEnemyLocs.toArray( new MapLocation[nearbyEnemyLocs.size()] );
		
	}
	
	private MapLocation [] senseGroundEnemyLocations() throws GameActionException {
		
		Robot [] nearbyGroundRobots = myRC.senseNearbyGroundRobots();
		LinkedList<MapLocation> nearbyEnemyLocs = new LinkedList<MapLocation>();
		
		// select all the enemies out of the robots in range
		for ( Robot r : nearbyGroundRobots ) {
			RobotInfo robotInfo = myRC.senseRobotInfo(r);
			if ( robotInfo.team != myRC.getTeam() && robotInfo.type.attackPower() > 2.5 ) {	// is enemy
				nearbyEnemyLocs.add( myRC.senseRobotInfo(r).location );
			}
		}
		
		return nearbyEnemyLocs.toArray( new MapLocation[nearbyEnemyLocs.size()] );
		
	}
	
	private void transferSelf() throws GameActionException {
        if (myRC.getEnergonLevel() == myRC.getMaxEnergonLevel()) {
            double transferAmt = GameConstants.ENERGON_RESERVE_SIZE
                    - myRC.getEnergonReserve();
            myRC.transferUnitEnergon(transferAmt, myRC.getLocation(),
                    RobotLevel.IN_AIR);
        }
    }
}
