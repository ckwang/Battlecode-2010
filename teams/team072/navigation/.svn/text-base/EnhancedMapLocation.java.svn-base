package team072.navigation;

import battlecode.common.Direction;
import battlecode.common.MapLocation;

public class EnhancedMapLocation {
	public MapLocation location;
	public EnhancedMapLocation previousLoc;
	public int cost;
	public Direction nextMove;
	public int tracing;
	public int distance = 0;
	
	public EnhancedMapLocation( MapLocation loc ) {
		this.location = loc;
	}
	
	public EnhancedMapLocation( MapLocation loc, int cost, Direction nextMove, int tracing ) {
		this.location = loc;
		this.cost = cost;
		this.nextMove = nextMove;
		this.tracing = tracing;
		this.previousLoc = this;
	} 
	
	public void setNextMove( EnhancedMapLocation s ) {
		nextMove = s.nextMove;
		if ( nextMove == Direction.OMNI ) {
			nextMove = s.directionTo(this);
		} 
	}
	
	public EnhancedMapLocation add( Direction dir ) {
		EnhancedMapLocation newLoc = new EnhancedMapLocation( location.add(dir) );
		newLoc.distance =  distance + 1;
		return newLoc;
	}
	
	public Direction directionTo( EnhancedMapLocation loc ) {
		return location.directionTo( loc.location );
	}

}
