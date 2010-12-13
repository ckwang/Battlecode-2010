package team072.baseplayer;

import java.util.LinkedList;

import team072.message.MessageEncoder;
import team072.message.MessageStack;
import team072.message.MessageEncoder.MessageType;
import team072.navigation.Map;
import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotLevel;
import battlecode.common.RobotType;
import battlecode.common.TerrainTile.TerrainType;

public class ArchonOuterAI extends ArchonAI {
	private RobotController myRC;
	private MessageStack myMS;
	
	private MapLocation origin;
	private Direction centerDir;
	private Direction archonBuildDir;
	
	private int state;
	private Direction moveDir;
	
	private final int SCOUTING = 1;	
	private final int MINING = 2;	private final int RETURNING = 3;	
	private final int FLEEING = 4;	private final int FIGHTING = 5;
	private final int WOUT_NUM_THRESHOLD = 1;	// max num of wouts in sensor range
	private final int SPAWN_WOUT_ENERGON_THRESHOLD = 45;	// energon required to spawn wout
	private final int SPAWN_SOLDIER_ENERGON_THRESHOLD = 45;	// energon required to spawn soldier
	private final int ROUND_NUM_THRESHOLD = 1000;  
	private final int FIRST_FLUX_THRESHOLD = 2000;
	private final int SECOND_FLUX_THRESHOLD = 4000;	// the threshold for returning
	
	public ArchonOuterAI(RobotController rc, MessageStack ms, MapLocation o, Direction cdir, Direction adir) {
		myRC = rc;
		myMS = ms;
		origin = o;
		centerDir = cdir;
		archonBuildDir = adir;
		moveDir = archonBuildDir;
		
		state = SCOUTING;
	}
	
	public int proceed() throws GameActionException {
		myRC.setIndicatorString(0, centerDir.toString() );
		myRC.setIndicatorString(1, archonBuildDir.toString());

		switch ( state ) {
		case SCOUTING:
			
			myRC.setIndicatorString(2, "SCOUTING");
			scouting();
			break;
		case MINING:
			
			myRC.setIndicatorString(2, "MINING");
			mining();
			break;
		case RETURNING:
			
			myRC.setIndicatorString(2, "RETURNING");
			returning();
			break;
		case FLEEING:
			
			myRC.setIndicatorString(2, "FLEEING");
			fleeing();
			break;
		case FIGHTING:
			
			myRC.setIndicatorString(2, "FIGHTING");
			fighting();
			break;
		}
		
		myRC.yield();
		return FIGHTING;
	}
	
	private void scouting() throws GameActionException {
		Direction scoutDir;
		MapLocation otherScoutLastLoc;
		boolean isFirst = true;
		
		// if centerDir is not diagonal, start searching
		if ( !centerDir.isDiagonal() ) {
			scoutDir = archonBuildDir;
			/*
			if ( centerDir.rotateLeft() == archonBuildDir ) {
				scoutDir = archonBuildDir.rotateLeft();
			} else {
				scoutDir = archonBuildDir.rotateRight();
			}*/
		} else {
			state = MINING;
			return;
		}
		
		myRC.setDirection(scoutDir);
		myRC.yield();
		
		
		while ( myRC.canMove(scoutDir) ) {
			otherScoutLastLoc = senseOtherScoutLoc();
			
			if ( myRC.getRoundsUntilMovementIdle() == 0 ) {
				myRC.moveForward();
			}
			myRC.yield();
			
			if ( otherScoutLastLoc.directionTo( senseOtherScoutLoc() ).equals(scoutDir) ) {
				isFirst = false;
				break;
			} 
		}
		
		for ( int i = 0; i < 6; i++ ) {
			otherScoutLastLoc = senseOtherScoutLoc();
			
			if ( myRC.getRoundsUntilMovementIdle() == 0 ) {
				myRC.moveBackward();
			} 
			myRC.yield();
			
			if ( otherScoutLastLoc.directionTo( senseOtherScoutLoc() ).equals(scoutDir)  ) {
				isFirst = false;
				break;
			} 
		}
		
		if ( isFirst ) {
			if ( centerDir.rotateLeft().rotateLeft().equals(archonBuildDir)  ) {
				archonBuildDir = centerDir;
				centerDir = centerDir.rotateRight();
			} else {
				archonBuildDir = centerDir;
				centerDir = centerDir.rotateLeft();
			}
			origin = myRC.getLocation();
		} else {
			if ( centerDir.rotateLeft().rotateLeft().equals(archonBuildDir) ) {
				centerDir = centerDir.rotateLeft();
			} else {
				centerDir = centerDir.rotateRight();
			}
			origin = senseOtherScoutLoc();
		}
		
		/*
		if ( isFirst ) {
			if ( centerDir.rotateLeft() == archonBuildDir ) {
				centerDir = scoutDir.opposite().rotateLeft(); 
				archonBuildDir = centerDir.rotateLeft();
			} else {
				centerDir = scoutDir.opposite().rotateRight();
				archonBuildDir = centerDir.rotateRight();
			}
		} else {
			if ( centerDir.rotateLeft() == archonBuildDir ) {
				centerDir = scoutDir.rotateRight(); 
				archonBuildDir = centerDir.rotateLeft();
			} else {
				centerDir = scoutDir.rotateLeft();
				archonBuildDir = centerDir.rotateRight();
			}
		}*/
		
		moveDir = archonBuildDir;
		return;
		
	}
	
	private void mining() throws GameActionException {
		
		// if flux is enough, change to returning state
		if ( Clock.getRoundNum() > ROUND_NUM_THRESHOLD ){
			if ( myRC.getFlux() > FIRST_FLUX_THRESHOLD ) {
				state = RETURNING;
			}
		} else {
			if ( myRC.getFlux() > SECOND_FLUX_THRESHOLD ) {
				state = RETURNING;
			}
		}
		
		// if enemy is near, flee
		if ( senseGroundEnemyLocations().length + senseAirEnemyLocations().length > 3 ) {
			moveDir = myRC.getLocation().directionTo( senseLeaderArchonLoc() );
			state = FLEEING;
			return;
		}
		
		// spawn wouts
		if ( myRC.getEnergonLevel() > SPAWN_WOUT_ENERGON_THRESHOLD 
				&& senseNumOfWouts() < WOUT_NUM_THRESHOLD ) {
			spawn(RobotType.WOUT);
			myRC.yield();
			return;
		} else if ( myRC.getEnergonLevel() > SPAWN_SOLDIER_ENERGON_THRESHOLD ) {
			spawn(RobotType.SOLDIER);
			myRC.yield();
			return;
		}
		
		// support wouts
		supportTeam();
		
		// move
		move();
		
		myRC.yield();
		
	}
	
	private void returning() throws GameActionException {
		
		
		// if enemy is near, fight

		if ( senseGroundEnemyLocations().length + senseAirEnemyLocations().length > 2 ) {
			moveDir = myRC.getLocation().directionTo( senseLeaderArchonLoc() );
			state = FLEEING;
			return;
		}
		
		MapLocation archonLoc = senseLeaderArchonLoc();
		
		// go for archon. If archon is adjacent, give flux.
		if ( myRC.getLocation().isAdjacentTo( archonLoc ) ) {
			if ( myRC.senseAirRobotAtLocation(archonLoc) != null )
				myRC.transferFlux( myRC.getFlux(), archonLoc, RobotLevel.IN_AIR );
			
			moveDir = archonBuildDir;
			state = MINING;
		} else {
			supportTeam();
			goTo( senseLeaderArchonLoc() );
		}
		
		myRC.yield();
	}
	
	private void fleeing() throws GameActionException {
		
		supportTeam();
		
		
		/*
		if ( senseGroundEnemyLocations().length > 3 && myRC.getFlux() > 4000 ) {
			spawn( RobotType.AURA );
			myRC.yield();
		}*/
		
		if ( myRC.getRoundsUntilMovementIdle() == 0 ) {
			
			while ( !myRC.canMove( moveDir.opposite() ) ) {
				moveDir = moveDir.rotateRight();
			}
			
			if ( myRC.getDirection().equals( moveDir ) ) {
				myRC.moveBackward();
			} else {
				myRC.setDirection( moveDir );
			}
			
			myRC.yield();
		}
		
		if ( senseGroundEnemyLocations().length == 0 ) {
			state = RETURNING;
		} 
		
		/*
		if ( myRC.getEnergonLevel() > SPAWN_WOUT_ENERGON_THRESHOLD ) {
			MapLocation loc = myRC.getLocation().add( myRC.getDirection() );
			
			if ( myRC.senseTerrainTile(loc).getType() == TerrainType.LAND 
					&& myRC.senseGroundRobotAtLocation(loc) == null ) {
				myRC.spawn(RobotType.WOUT);
			}
			myRC.yield();
		}*/
		
		/*
		// go for archon. If archon is adjacent, give flux.
		if ( myRC.getLocation().isAdjacentTo( archonLoc ) ) {
			if ( myRC.senseAirRobotAtLocation(archonLoc) != null )
				myRC.transferFlux( myRC.getFlux(), archonLoc, RobotLevel.IN_AIR );
			
			moveDir = archonBuildDir;
			state = MINING;
		} else {
			supportTeam();
			goTo( senseLeaderArchonLoc() );
		}
		
		myRC.yield();
		*/
	}
	
	private void fighting() throws GameActionException {
		
		// if flux enough, build a tower
		if ( myRC.getFlux() > 4000 ) {
			spawn( RobotType.AURA );
			myRC.yield();
		}
		
		// if energon level enough, spawn soldier
		if ( myRC.getEnergonLevel() > SPAWN_SOLDIER_ENERGON_THRESHOLD ) {
			spawn( RobotType.SOLDIER );
			myRC.yield();
			return;
		}
		
		// support the team
		supportTeam();
	}
	
	/***
	 * Find the right direction to spawn a wout if possible.
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
                    for ( int i = 0; i < 5; i++ ) {
                    	myRC.yield();
                    }
                    
                    if ( type == RobotType.WOUT ) {
                    	MessageEncoder msgEncoder = new MessageEncoder( myRC, myMS, MessageType.ORIGIN, null, archonBuildDir );
                    	myRC.broadcast( msgEncoder.encodeMessage() );
                    }
                }
				return;
			}
		}
	
	}
	
	private void move() throws GameActionException {
	
		if ( myRC.getRoundsUntilMovementIdle() == 0 ){
			
			// if out of bound, go toward archonBuildDir
			if ( isOut( myRC.getLocation() ) ) {
				
				if ( myRC.getDirection().equals( archonBuildDir ) ) {
					if ( myRC.canMove(archonBuildDir) )
						myRC.moveForward();
				} else {
					myRC.setDirection( archonBuildDir );
				}
				
			} else {	// otherwise, go with regular pattern
			
				while ( !myRC.canMove(moveDir) || isOut( myRC.getLocation().add(moveDir) ) ) {
					moveDir = moveDir.opposite().rotateRight();
				}
				
				
				if ( myRC.getDirection().equals(moveDir) ) {
					myRC.moveForward();
				} else {
					myRC.setDirection( moveDir );
					
				}
			}
			
		}
	}
	
	/***
	 * Check whether the given location is out of bound
	 * @param loc
	 * @return 
	 * @throws GameActionException
	 */
	private boolean isOut( MapLocation loc ) throws GameActionException {
		boolean result = true;
		
		switch ( centerDir ) {
		case NORTH:
			result = loc.getX() - origin.getX() < 0 ;
			break;
		case NORTH_EAST:
			result = ( loc.getX() - origin.getX() ) + ( loc.getY() - origin.getY() ) < 0 ;
			break;
		case EAST:
			result = loc.getY() - origin.getY() < 0 ;
			break;
		case SOUTH_EAST:
			result = ( loc.getX() - origin.getX() ) - ( loc.getY() - origin.getY() ) > 0 ;
			break;
		case SOUTH:
			result = loc.getX() - origin.getX() > 0 ;
			break;
		case SOUTH_WEST:
			result = ( loc.getX() - origin.getX() ) + ( loc.getY() - origin.getY() ) > 0 ;
			break;
		case WEST:
			result = loc.getY() - origin.getY() > 0 ;
			break;
		case NORTH_WEST:
			result = ( loc.getX() - origin.getX() ) - ( loc.getY() - origin.getY() ) < 0 ;
			break;
		}
		
		result = ( centerDir.rotateRight().equals(archonBuildDir) ) ? result : !result;
		
		return result;
	}
	
	/***
	 * 
	 * @return the number of wouts in the sensor range
	 * @throws GameActionException 
	 */
	private int senseNumOfWouts() throws GameActionException {
		int num = 0;
		Robot [] robots = myRC.senseNearbyGroundRobots();
		
		for ( Robot r : robots ) {
			RobotInfo info = myRC.senseRobotInfo(r);
			
			if ( myRC.getTeam().equals(info.team) 
					&& info.type.equals(RobotType.WOUT) ) {
				num++;
			}
		}
		
		return num;
	}
	
	private MapLocation senseOtherScoutLoc() throws GameActionException {
		MapLocation [] archonLocs = myRC.senseAlliedArchons();
		MapLocation result = archonLocs[0];
		
		for ( MapLocation loc : archonLocs ) {
			if ( myRC.getLocation().distanceSquaredTo(loc) > myRC.getLocation().distanceSquaredTo(result) ) {
				result = loc;
			} 
		}
		
		return result;
		
	}
	
	/***
	 * 
	 * @return the location of the leader archon
	 * @throws GameActionException 
	 */
	private MapLocation senseLeaderArchonLoc() throws GameActionException {
		MapLocation [] archonLocs = myRC.senseAlliedArchons();
		MapLocation result = null;
		
		for ( MapLocation loc : archonLocs ) {
			if ( !isOut(loc) && !loc.equals(myRC.getLocation()) ) {
				if ( result == null || origin.distanceSquaredTo(loc) > origin.distanceSquaredTo(result) ) {
					result = loc;
				} 
			}
		}
		
		if ( result == null ) {
			result = origin;
		}
		
		return result;
		
		/*
		MapLocation [] archonLocs = myRC.senseAlliedArchons();
		MapLocation result = archonLocs[0];
		
		// find the one that is farthest from origin along the archonBuildDir
		for ( MapLocation loc : archonLocs ) {
			switch ( archonBuildDir ) {
			case NORTH:
				result = loc.getY() < result.getY() ? loc : result;
				break;
			case NORTH_EAST:
				
				break;
			case EAST:
				result = loc.getX() > result.getX() ? loc : result;
				break;
			case SOUTH:
				result = loc.getY() > result.getY() ? loc : result;
				break;
			case WEST:
				result = loc.getX() < result.getX() ? loc : result;
				break;
			}
		}
		
		return result; */
	}
	
	/***
	 * Go to a specific location. Rotating and moving are all handled.
	 * @param loc
	 * @throws GameActionException
	 */
	private void goTo(MapLocation loc) throws GameActionException {
		
		if (myRC.getLocation().isAdjacentTo(loc)){
			return;
		}
		
		Direction dir = myRC.getLocation().directionTo(loc);
		
		if ( myRC.canMove(dir) && myRC.getRoundsUntilMovementIdle() == 0 ){
			
			if ( myRC.getDirection().equals(dir) ){
				myRC.moveForward();
			} else {
				myRC.setDirection(dir);
			}
			
		} 
		
	}
	
	/***
	 * Give energon to adjacent wouts when conditions are satisfied.
	 * @throws GameActionException
	 */
	private void supportTeam() throws GameActionException {
		Robot[] robots = myRC.senseNearbyGroundRobots();
		
		for ( Robot r : robots ) {
			
			if ( myRC.getEnergonLevel() < 15 )
				break;
			
			if ( myRC.canSenseObject(r) ) {
				RobotInfo info = myRC.senseRobotInfo(r);
				
				if ( myRC.getTeam().equals(info.team) 
						&& !info.type.isBuilding() && !info.type.isAirborne()
						&& myRC.getLocation().distanceSquaredTo(info.location) < 2 
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
			if ( robotInfo == null )	continue;
			if ( robotInfo.team != myRC.getTeam() && robotInfo.type.attackPower() > 2.5 ) {	// is enemy
				nearbyEnemyLocs.add( myRC.senseRobotInfo(r).location );
			}
		}
		
		return nearbyEnemyLocs.toArray( new MapLocation[nearbyEnemyLocs.size()] );
		
	}
	
}
