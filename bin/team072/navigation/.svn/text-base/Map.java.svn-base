
package team072.navigation;

import battlecode.common.*;
import battlecode.common.TerrainTile.TerrainType;

import java.util.PriorityQueue;
import java.util.Comparator;

public class Map {
	private RobotController myRC;
	private int moveDelayDiagonal;
	private int moveDelayOrthogonal;
	private EnhancedMapLocation source;
	private EnhancedMapLocation target;
	
	private final static int MAX_DISTANCE = 9;
	private final static int CCW = 1;	private final static int CW = -1;	private final static int NONE = 0;
	
	public final static Direction[] DIRECTIONS = new Direction[] {
		Direction.NORTH,
		Direction.NORTH_EAST,
		Direction.EAST,
		Direction.SOUTH_EAST,
		Direction.SOUTH,
		Direction.SOUTH_WEST,
		Direction.WEST,
		Direction.NORTH_WEST};
	
	public static Direction[] directionHierarchy(Direction direction) {
		int index = 0;
		
		switch(direction) {
		case NORTH: break;
		case NORTH_EAST: index = 1; break;
		case EAST: index = 2; break;
		case SOUTH_EAST: index = 3; break;
		case SOUTH: index = 4; break;
		case SOUTH_WEST: index = 5; break;
		case WEST: index = 6; break;
		case NORTH_WEST: index = 7; break;
		}
		
		Direction[] directionHierarchy = new Direction[8];
		directionHierarchy[0] = direction;
		for (int i = 1; i < 7; i = i+2) {
			directionHierarchy[i] = DIRECTIONS[(index+i) % 8];
			directionHierarchy[i+1] = DIRECTIONS[(index-i+8) % 8];
		}
		directionHierarchy[7] = DIRECTIONS[(index+4) % 8];
		
		return directionHierarchy;
	}
	
	public Map(RobotController rc) {
		myRC = rc;
		moveDelayDiagonal = myRC.getRobotType().moveDelayDiagonal();
		moveDelayOrthogonal = myRC.getRobotType().moveDelayOrthogonal();
	}

	public Direction tangentBug(MapLocation s, MapLocation t) throws GameActionException{
		
		if ( s.equals(t) ) {
			System.out.println("TANGENT BUG: SOURCE == TARGET");
			myRC.setIndicatorString(2, "OMNI, SOURCE == TARGET");
			return Direction.OMNI;
		}
		
		PriorityQueue<EnhancedMapLocation> openLocs = 
			new PriorityQueue<EnhancedMapLocation>(MAX_DISTANCE*2, new Comparator<EnhancedMapLocation>(){
				public int compare(EnhancedMapLocation a, EnhancedMapLocation b){
					if (a.cost > b.cost){
						return 1;
					} else if (a.cost < b.cost) {
						return -1;
					} else{
						return 0;
					}
				}
			});
		
		EnhancedMapLocation currentLoc;
		source = new EnhancedMapLocation(s, 0, Direction.OMNI, NONE);
		target = new EnhancedMapLocation(t);
				
		openLocs.offer(source);	// add start to openLocs
		
		while (openLocs.size() > 0){	// while openLocs not empty
			currentLoc = openLocs.poll();	// current = lowest cost location in openLocs
			
			if ( currentLoc.location.equals(t) || currentLoc.distance > MAX_DISTANCE ){	// if current == goal or exceed certain amount
				
				EnhancedMapLocation adjacentLoc = source.add(source.directionTo(currentLoc));
				//Direction dir = currentLoc.nextMove;
				Direction dir = !canMove( source, adjacentLoc ) ? currentLoc.nextMove : source.directionTo(currentLoc);
				
				if ( dir == Direction.OMNI ){
					return dir;
				}
				
				for ( Direction d : directionHierarchy( dir ) ) {
					if ( myRC.canMove(d) ){
						myRC.setIndicatorString(2, d.toString());
						return d;
					}
				}
				return null;
				
				/*
				if ( currentLoc.tracing == NONE || canMove( source.add( currentLoc.nextMove ) ) ){
					for ( Direction d : directionHierarchy( currentLoc.nextMove ) ) {
						if ( myRC.canMove(d) )
							return d;
					}
					return null;
				} else {
					return tangentBug(s, currentLoc.location );
				}*/
				
			} else {
				Direction dirToTarget = currentLoc.directionTo(target);
				
				if ( currentLoc.tracing != NONE ){	// if tracing
					if ( needTrace(currentLoc, target) ) {
						EnhancedMapLocation nextLoc = ( currentLoc.tracing == CW ) ?
								nextCW( currentLoc, dirToTarget ): nextCCW( currentLoc, dirToTarget );
						openLocs.offer( nextLoc );
					} else {
						
						EnhancedMapLocation adjacentLoc = source.add(source.directionTo(currentLoc));
						//Direction dir = currentLoc.nextMove;
						Direction dir = !canMove( source, adjacentLoc) ? currentLoc.nextMove : source.directionTo(currentLoc);
						
						//Direction dir = currentLoc.nextMove;
						if ( dir == Direction.OMNI ){
							return dir;
						}
						
						for ( Direction d : directionHierarchy( dir ) ) {
							if ( myRC.canMove(d) ){
								myRC.setIndicatorString(2, d.toString());
								return d;
							}
						}
						return null;
					}
					
				} else {	// if not tracing
					EnhancedMapLocation nextDeadReckonLoc = currentLoc.add(dirToTarget);
					
					if ( canMove( currentLoc, nextDeadReckonLoc) ){	// if can move from currentLoc to nextDeadReckonLoc
						nextDeadReckonLoc.cost = calCost(currentLoc,nextDeadReckonLoc);
						nextDeadReckonLoc.tracing = NONE;
						nextDeadReckonLoc.setNextMove(currentLoc);
						nextDeadReckonLoc.previousLoc = currentLoc;
						openLocs.offer(nextDeadReckonLoc);
					} else {	// if we need to trace
						openLocs.offer( nextCCW(currentLoc, dirToTarget) );
						openLocs.offer( nextCW(currentLoc, dirToTarget) );
						
					}
				}
			}
		}
		
		System.out.println("TANGENT BUG UNEXPECTED OMNI");
		myRC.setIndicatorString(2, "OMNI, UNEXPECTED");
		return Direction.OMNI;
		
	}
	
	private EnhancedMapLocation nextCW(EnhancedMapLocation s, Direction faceDir) throws GameActionException {
		Direction currentFaceDir = faceDir;
		EnhancedMapLocation newLoc;
		
		if ( canMove( s, s.add(currentFaceDir) ) ){
			do {
				currentFaceDir = currentFaceDir.rotateRight();
			} while ( canMove( s, s.add(currentFaceDir) ) ); 
			currentFaceDir = currentFaceDir.rotateLeft();
		} else {
			do {
				currentFaceDir = currentFaceDir.rotateLeft();
			} while ( !canMove( s, s.add(currentFaceDir)) );
		}
		
		newLoc = s.add(currentFaceDir);
		newLoc.cost = calCost(s,newLoc);
		newLoc.tracing = CW;
		newLoc.setNextMove(s);
		newLoc.previousLoc = s;
		
		return newLoc;
	}
	
	private EnhancedMapLocation nextCCW(EnhancedMapLocation s, Direction faceDir) throws GameActionException {
		Direction currentFaceDir = faceDir;
		EnhancedMapLocation newLoc;
		
		if ( canMove( s, s.add(currentFaceDir) ) ){
			do {
				currentFaceDir = currentFaceDir.rotateLeft();
			} while ( canMove( s, s.add(currentFaceDir) ) ); 
			currentFaceDir = currentFaceDir.rotateRight();
		} else {
			do {
				currentFaceDir = currentFaceDir.rotateRight();
			} while ( !canMove( s, s.add(currentFaceDir) ) );
		}
		
		newLoc = s.add(currentFaceDir);
		newLoc.cost = calCost(s,newLoc);
		newLoc.tracing = CCW;
		newLoc.setNextMove(s);
		newLoc.previousLoc = s;
		
		return newLoc;
	}
	
	private int calCost(EnhancedMapLocation s, EnhancedMapLocation t){
		int newCost = s.cost;
		
		newCost += s.directionTo(t).isDiagonal() ? moveDelayDiagonal : moveDelayOrthogonal;
		
		return newCost;
	}
	
	private boolean canMove( EnhancedMapLocation s, EnhancedMapLocation t ) throws GameActionException{
		TerrainTile tile = myRC.senseTerrainTile( t.location );
		return (tile == null || tile.getType() == TerrainType.LAND) && (s.previousLoc.equals(source)  || !s.previousLoc.location.equals(t.location) );
	}
	
	private boolean needTrace(EnhancedMapLocation s, EnhancedMapLocation t) throws GameActionException{
		Direction dirToTarget = s.directionTo( t );
		EnhancedMapLocation nextLoc = s.add(dirToTarget);
		
		return !canMove( s, nextLoc );
	}
	
}