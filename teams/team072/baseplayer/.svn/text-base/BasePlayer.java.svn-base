package team072.baseplayer;

import battlecode.common.*;

import java.util.ArrayList;
import java.util.LinkedList;

public abstract class BasePlayer {
	protected final RobotController myRC;
	protected RobotType myType;

	public BasePlayer(RobotController rc) {
		myRC = rc;
	}

	/**
	 * Should deal with choosing the right state to action depending on the
	 * strategy and other conditions
	 * 
	 * @throws Exception
	 */
	public abstract void proceed() throws Exception;

	// All sensor functions should later be put in a separate folder
	// This only works for full angle sensing robots
	

	protected MapLocation [] senseAirEnemyLocations() throws GameActionException {
			
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
	
	protected MapLocation [] senseGroundEnemyLocations() throws GameActionException {
		
		Robot [] nearbyGroundRobots = myRC.senseNearbyGroundRobots();
		LinkedList<MapLocation> nearbyEnemyLocs = new LinkedList<MapLocation>();
		
		// select all the enemies out of the robots in range
		for ( Robot r : nearbyGroundRobots ) {
			RobotInfo robotInfo = myRC.senseRobotInfo(r);
			if ( robotInfo == null )	continue;
			if ( robotInfo.team != myRC.getTeam() ) {	// is enemy
				nearbyEnemyLocs.add( myRC.senseRobotInfo(r).location );
			}
		}
		
		return nearbyEnemyLocs.toArray( new MapLocation[nearbyEnemyLocs.size()] );
		
	}

	
}