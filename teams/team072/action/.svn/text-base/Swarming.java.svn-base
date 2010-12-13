package team072.action;

import java.util.ArrayList;

import team072.navigation.Map;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;

public class Swarming {
	private RobotController myRC;
	private boolean lost = false;	//true if see no alliedGroundRobots
	private int leaderID = 0;
	
	/*
	 * given a leader ID, follow the leader by swarming
	 * might have problem if leader died
	 */
	public Swarming(RobotController rc, int id) {
		this.myRC = rc;
		this.leaderID = id;
	}
	
	public void swarm() throws GameActionException {
		while (!lost) {
			move();
			myRC.yield();
		}
		findLeader();
		swarm();
	}
	
	public MapLocation move()throws GameActionException {
		Robot[] nearbyAll = myRC.senseNearbyGroundRobots(); //nearby Robots
		ArrayList<MapLocation> nearby = new ArrayList<MapLocation>(); //nearby alliedRobots
		MapLocation locToGo = myRC.getLocation();
		if (nearbyAll.length > 0) {
			for (Robot r: nearbyAll) {
				try {
					RobotInfo info = myRC.senseRobotInfo(r);
					if (info.id == leaderID) {
						nearby.add(info.location);	//leader location add twice
						if (myRC.getLocation().distanceSquaredTo(info.location) < 3)
							myRC.yield(); //if too close to leader, wait 1 round
					}
					if (info.team.equals(myRC.getTeam()))
						nearby.add(info.location);	//all team member location add once
				} catch (GameActionException e) {
					System.out.println("Cannot sense nearby robots");
					e.printStackTrace();
				}
			}
			locToGo = averageLoc(nearby);
		} 
		if (locToGo.equals(myRC.getLocation()))
			lost = true;
		else if (locToGo.distanceSquaredTo(myRC.getLocation()) < 3)
			myRC.yield();
		new Map(myRC).tangentBug(myRC.getLocation(), locToGo);
		return locToGo;
	}
	
	
	/*
	 * @param list of nearby alliedRobots
	 * @return MapLocation with average coordinate or currentLocation if no alliedRobots nearby
	 */
	public MapLocation averageLoc (ArrayList<MapLocation> locList) {
		int len = locList.size();
		int sumX = 0;
		int sumY = 0;
		MapLocation averageLoc = myRC.getLocation();
		if (len > 0) {
			for (int i = 0; i < len; i++) {
				MapLocation ml = locList.get(i);
				sumX += ml.getX();
				sumY += ml.getY();
			}
			averageLoc = new MapLocation(sumX/len, sumY/len);
		}
		return averageLoc;
	}
	
	
	public int distToLeader() {return leaderLoc().distanceSquaredTo(myRC.getLocation());}
	
	//go to current location of leader and lost = false
	public void findLeader() throws GameActionException {
		MapLocation loc = leaderLoc();
		if (!loc.equals(myRC.getLocation()) ) {
			lost = false;
			new Map(myRC).tangentBug(myRC.getLocation(), 
					new MapLocation(loc.getX()-1,loc.getY()));
			myRC.yield();
		} else {
			//leader died; broadcast??
		}
	}
	
	//@return MapLocation of leader
	public MapLocation leaderLoc() {
		MapLocation[] archonList = myRC.senseAlliedArchons();
		MapLocation leaderLoc = myRC.getLocation(); //might have problem if leader died
		for (MapLocation ml: archonList) {
			try {
				Robot r = myRC.senseGroundRobotAtLocation(ml);
				RobotInfo info = myRC.senseRobotInfo(r);
				if (info.id == leaderID) {
					leaderLoc = info.location;
				}
			} catch (GameActionException e) {
				System.out.println("Cannot sense nearby archons");
				e.printStackTrace();
			}
			
		}
		return leaderLoc;
	}
	
}
