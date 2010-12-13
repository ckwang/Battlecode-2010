package team072.baseplayer;

import java.util.LinkedList;

import team072.message.MessageEncoder;
import team072.message.MessageStack;
import team072.message.MessageEncoder.MessageType;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotType;


public class AuraPlayer extends BasePlayer {

	protected MessageStack myMS;
	private MessageEncoder msgEncoder;
	
	private int state;	// the state of the robot
	private final int WAKING = 0;	private final int MATURE = 1;	private final int DYING = 2;
	
	public AuraPlayer(RobotController rc) {
		super(rc);
		myType = RobotType.AURA;
		myMS = new MessageStack();
		
		state = WAKING;
	}

	@Override
	public void proceed() throws Exception {
		// TODO Auto-generated method stub
		
		switch ( state ){
		case MATURE:
			
//			// sense if there are enemies around. If so, broadcast their locations
//			MapLocation [] enemyLocs = senseEnemyLocations();
//			if ( enemyLocs.length > 0 ) {
//				msgEncoder = new MessageEncoder( myRC, myMS, MessageType.ENEMY_LOC, enemyLocs );
//				myRC.broadcast( msgEncoder.encodeMessage() );
//			}
			
			// if energon level is too low, state = DYING
			if ( myRC.getEventualEnergonLevel() < 10 ) {
				state = DYING;
			}
			
			break;
		case WAKING:
			
			// sending message telling its existence to neighbors
			msgEncoder = new MessageEncoder(myRC, myMS, MessageType.BUILDING_SPAWNED);
			myRC.broadcast( msgEncoder.encodeMessage() );
			state = MATURE;
			
			break;
		case DYING:
			
			// sending message telling that it is dying
			msgEncoder = new MessageEncoder(myRC, myMS, MessageType.BUILDING_DIED);
			myRC.broadcast( msgEncoder.encodeMessage() );
			
			break;
		}
		
		myRC.yield();
	}

	/***
	 * 
	 * @return all the enemy locations in the sensor range
	 * @throws GameActionException
	 */
	
	private MapLocation [] senseEnemyLocations() throws GameActionException {
		Robot [] nearbyAirRobots = myRC.senseNearbyAirRobots();
		Robot [] nearbyGroundRobots = myRC.senseNearbyGroundRobots();
		LinkedList<MapLocation> nearbyEnemyLocs = new LinkedList<MapLocation>();
		
		// select all the enemies out of the robots in range
		for ( Robot r : nearbyAirRobots ) {
			if ( myRC.senseRobotInfo(r).team != myRC.getTeam() ) {	// is enemy
				nearbyEnemyLocs.add( myRC.senseRobotInfo(r).location );
			}
		}
		
		for ( Robot r : nearbyGroundRobots ) {
			if ( myRC.senseRobotInfo(r).team != myRC.getTeam() ) {	// is enemy
				nearbyEnemyLocs.add( myRC.senseRobotInfo(r).location );
			}
		}
		
		return nearbyEnemyLocs.toArray( new MapLocation[nearbyEnemyLocs.size()] );
		
	}

}
