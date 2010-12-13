package team072.baseplayer;

import team072.message.MessageDecoder;
import team072.message.MessageEncoder;
import team072.message.MessageStack;
import team072.message.MessageEncoder.MessageType;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Message;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotLevel;
import battlecode.common.RobotType;


public class TeleporterPlayer extends BasePlayer {
	protected MessageStack myMS;
	private MessageEncoder msgEncoder;
	private MessageDecoder msgDecoder;
	
	private MapLocation origin;
	private Direction dir;
	private MapLocation vertexX;
	private MapLocation vertexY;
	
	private int state;	// the state of the robot
	private final int WAKING = 0;	private final int MATURE = 1;	private final int DYING = 2;
	private final Direction [] dlist = {Direction.EAST, Direction.NORTH, Direction.NORTH_EAST, Direction.NORTH_WEST,
			Direction.SOUTH, Direction.SOUTH_EAST, Direction.SOUTH_WEST, Direction.WEST};
	
	public TeleporterPlayer(RobotController rc) {
		super(rc);
		myType = RobotType.TELEPORTER;
		myMS = new MessageStack();
		
		state = WAKING;
	}

	@Override
	public void proceed() throws Exception {
		
		switch ( state ){
		case MATURE:
			myRC.setIndicatorString(2, "mature");
			mature();
			break;
			
		case WAKING:
			myRC.setIndicatorString(2, "waking");
			waking();		
			break;
			
		case DYING:
			myRC.setIndicatorString(2, "dying");
			dying();
			break;
		
			
		}
		
	}

	private void mature() throws GameActionException {
		
		// check messages
		Message [] msgs = myRC.getAllMessages();
		for ( Message m : msgs ) {
			msgDecoder = new MessageDecoder( myRC, myMS, m );
			if ( msgDecoder.isValid() ) {
				switch ( msgDecoder.getType() ) {
			
				case TELE_FLEE_REQUEST:
				{
					if ( !myRC.getLocation().isAdjacentTo(msgDecoder.getSourceLocation()) || myRC.isTeleporting() )
						return;
					
					Robot r = myRC.senseAirRobotAtLocation( msgDecoder.getSourceLocation() );
					MapLocation loc = getFleeLocation();
					
					for ( Direction d : dlist) { // find the direction to teleport
						
						if ( myRC.canTeleport( loc , loc.add(d), RobotLevel.IN_AIR) ) {
							myRC.teleport(r, loc, loc.add(d) );
							return;
						}
					}
					
					System.out.println("Sorry! I can't send you there.");
					myRC.yield();
					return;
					
				}
				case TELE_BUILD_REQUEST:
				{
					if ( !myRC.getLocation().isAdjacentTo(msgDecoder.getSourceLocation()) )
						return;
					
					Robot r = myRC.senseAirRobotAtLocation( msgDecoder.getSourceLocation() );
					MapLocation loc = getBuildLocation();
					
					myRC.setIndicatorString(1, "building request received");
					for ( Direction d : dlist) { // find the direction to teleport
						
						if ( myRC.canTeleport( loc , loc.add(d), RobotLevel.IN_AIR) ) {
							myRC.teleport(r, loc, loc.add(d) );
							return;
						}
					}
					
					System.out.println("Sorry! I can't send you there.");
					myRC.yield();
					return;
				}
				case TELE_ORIGIN_REQUEST:
				{
					Robot r = myRC.senseAirRobotAtLocation( msgDecoder.getSourceLocation() );
					MapLocation loc = getOriginLocation();
					
					for ( Direction d : dlist) { // find the direction to teleport
						
						if ( myRC.canTeleport( loc , loc.add(d), RobotLevel.IN_AIR) ) {
							myRC.teleport(r, loc, loc.add(d) );
							return;
						}
					}
					
					System.out.println("Sorry! I can't send you there.");
					myRC.yield();
					return;
				}
				case BUILDING_SPAWNED:
				{
					resetTowerPosition();
					
					break;
				}
				case BUILDING_DIED:
				{
					resetTowerPosition();
					break;
				}
				}
			}
		}
		
		/*
		// sense if there are enemies around. If so, broadcast their locations
		MapLocation [] airEnemyLocs = senseAirEnemyLocations();
		MapLocation [] groundEnemyLocs = senseGroundEnemyLocations();
		if ( airEnemyLocs.length > 0 ) {
			msgEncoder = new MessageEncoder( myRC, myMS, MessageType.ENEMY_LOC, airEnemyLocs, groundEnemyLocs );
			myRC.broadcast( msgEncoder.encodeMessage() );
		}
		
		if ( airEnemyLocs.length + groundEnemyLocs.length > 0 ) {
			msgEncoder = new MessageEncoder( myRC, myMS, MessageType.ENEMY_LOC, airEnemyLocs, groundEnemyLocs );
			myRC.broadcast( msgEncoder.encodeMessage() );
		}
		*/
		
		
		// if energon level is too low, state = DYING
		if ( myRC.getEventualEnergonLevel() < 1 ) {
			state = DYING;
		}
		
		myRC.yield();
	}
	
	private void waking() throws GameActionException {
		
		// reset origin
		Message [] msgs = myRC.getAllMessages();
		for ( Message m : msgs ) {
			msgDecoder = new MessageDecoder( myRC, myMS, m );
			if ( msgDecoder.isValid() ) {
				switch ( msgDecoder.getType() ) {
				case ORIGIN:
					origin = msgDecoder.getOrigin();
					dir = msgDecoder.getDirection();
					
					state = MATURE;
					resetTowerPosition();
					
					// sending message telling its existence to neighbors
					msgEncoder = new MessageEncoder(myRC, myMS, MessageType.BUILDING_SPAWNED);
					myRC.broadcast( msgEncoder.encodeMessage() );
					break;
				}
			}
		}
			
		myRC.yield();
	}
	
	private void dying() throws GameActionException {
		// sending message telling that it is dying
		msgEncoder = new MessageEncoder(myRC, myMS, MessageType.BUILDING_DIED);
		myRC.broadcast( msgEncoder.encodeMessage() );
		myRC.yield();
	}
	
	private void resetTowerPosition() throws GameActionException {
		
		MapLocation locs [] = myRC.senseAlliedTeleporters();
		
		vertexX = locs[0];
		vertexY = locs[0];
		for ( MapLocation loc : locs ) {
			switch ( dir ) {
			case NORTH_EAST:
				vertexX = loc.getX() > vertexX.getX() ? loc : vertexX;
				vertexY = loc.getY() < vertexY.getY() ? loc : vertexY; 
				break;
			case NORTH_WEST:
				vertexX = loc.getX() < vertexX.getX() ? loc : vertexX;
				vertexY = loc.getY() < vertexY.getY() ? loc : vertexY; 
				break;
			case SOUTH_EAST:
				vertexX = loc.getX() > vertexX.getX() ? loc : vertexX;
				vertexY = loc.getY() > vertexY.getY() ? loc : vertexY; 
				break;
			case SOUTH_WEST:
				vertexX = loc.getX() < vertexX.getX() ? loc : vertexX;
				vertexY = loc.getY() > vertexY.getY() ? loc : vertexY; 
				break;
			}
		}
		
		
		
		/*
		MapLocation locs [] = myRC.senseAlliedTeleporters();
		
		if (locs.length == 1) {
			vertexA = locs[0];
			vertexB = locs[0];
			return;
		} 
		
		vertexA = locs[0];
		vertexB = locs[1];
		if ( origin.distanceSquaredTo( locs[0] ) > origin.distanceSquaredTo( locs[1] ) ) {
			vertexA = locs[0];	vertexB = locs[1];
		} else {
			vertexA = locs[1];	vertexB = locs[0];
		}
		
		for ( MapLocation loc : locs ) {
			if ( origin.distanceSquaredTo( loc ) > origin.distanceSquaredTo( vertexA ) ) {
				vertexB = vertexA;
				vertexA = loc;
			} else if ( origin.distanceSquaredTo( loc ) > origin.distanceSquaredTo( vertexB ) ) {
				vertexB = loc;
			}
		}
		*/
		
	}

	private MapLocation getOriginLocation() {
		return origin;
	}
	
	private MapLocation getFleeLocation() {
		
		return myRC.getLocation().distanceSquaredTo( vertexX ) < myRC.getLocation().distanceSquaredTo( vertexY ) ?
				vertexY : vertexX;
	}
	
	private MapLocation getBuildLocation() {
		return getFleeLocation();
		//return origin.distanceSquaredTo(vertexX) < origin.distanceSquaredTo(vertexY) ?
			//	vertexX : vertexY;
	}

}
