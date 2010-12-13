package team072.baseplayer;

import team072.action.GoingTo;
import team072.action.Wandering;
import team072.baseplayer.WoutPlayer.Status;
import team072.message.MessageDecoder;
import team072.message.MessageEncoder;
import team072.message.MessageStack;
import team072.message.MessageEncoder.MessageType;
import team072.navigation.Map;
import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Message;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotLevel;
import battlecode.common.RobotType;

public class WoutWanderingAI implements WoutAI {
	private RobotController myRC;
	private MapLocation origin;
	private Direction dir;
	private MessageStack msgStack;
	private Map myMap;
	private int wait = 0;
	private Status myStatus;

	public WoutWanderingAI(RobotController myRC, MapLocation origin,
			Direction dir, MessageStack msgStack, Status myStatus) {
		this.myRC = myRC;
		this.origin = origin;
		this.dir = dir;
		this.msgStack = msgStack;
		this.myMap = new Map(myRC);
		this.myStatus = myStatus;
	}

	@Override
	public void proceed() throws GameActionException {
		
		if (myRC.getFlux() > 500){
			giveFluxToArchon1(myRC);
		}
//
//		boolean giving1 = false;
//		if (!giving1 && myRC.getFlux() > 1000) {
//			// System.out.println("look for archon");
//			Robot[] robots1 = myRC.senseNearbyAirRobots();
//			for (Robot robot : robots1) {
//				if (myRC.canSenseObject(robot)) {
//					try {
//						RobotInfo info = myRC.senseRobotInfo(robot);
//						// System.out.println("senseinfo");
//						giving1 = giveFluxToArchon(info);
//						if (giving1) {
//							// System.out.println("give to archon");
//							break;
//						}
//					} catch (GameActionException e) {
//
//					}
//				}
//			}
//		}
		if (myRC.hasActionSet()) {
			myRC.getAllMessages();
			myRC.yield();
		}

		checkHelp();

		// if already move then dont move
		while (myRC.getRoundsUntilMovementIdle() != 0
				|| Clock.getBytecodeNum() > 5000) {
			myRC.getAllMessages();
			myRC.yield();
			return;
		}

		myRC.setIndicatorString(0, "free");
		goForHelp();
		// myRC.setIndicatorString(0, "");
		myRC.getAllMessages();
		myRC.yield();
	}

	private boolean goForHelp() throws GameActionException {
		// see if it has enough blood or is their is any wout which has more
		// blood around
		double blood = myRC.getEventualEnergonLevel();
		int numOfArchon = 0;
		int distance = 200;
		MapLocation des = myRC.getLocation();
		int bestID = -1;
		boolean giving = false;
		if (blood < 7) {
			Robot[] robots1 = myRC.senseNearbyAirRobots();
			for (Robot robot : robots1) {
				if (myRC.canSenseObject(robot)) {
					try {
						RobotInfo info = myRC.senseRobotInfo(robot);
						// System.out.println("senseinfo");
						giving = goForArchon(info);
						if (giving) {
							// System.out.println("give to archon");
							break;
						}
					} catch (GameActionException e) {
						myRC.setIndicatorString(2, Clock.getBytecodeNum() + "");
						// myRC.breakpoint();
					}
				}
			}
			if (!giving) {
				Robot[] robots = myRC.senseNearbyGroundRobots();
				numOfArchon = robots.length;
				for (Robot robot : robots) {
					if (myRC.canSenseObject(robot)) {
						try {
							RobotInfo info = myRC.senseRobotInfo(robot);
							int newDis = info.location.distanceSquaredTo(myRC
									.getLocation());
							if (info.team.equals(myRC.getTeam()) // same team
									&& info.type.equals(RobotType.WOUT)
									&& newDis < distance
									&& info.location.distanceSquaredTo(origin) < des
											.distanceSquaredTo(origin)) {
								// priority = newEnergon;
								distance = newDis;
								bestID = info.id;
								des = info.location;
								if (distance == 1 || distance == 2) {
									break;
								}
							}
						} catch (GameActionException e) {
							myRC.getAllMessages();
							myRC.yield();
						}
					}
				}
			}
		}
		if ((distance <= 16 || (blood < 5 && numOfArchon > 0)) && bestID != -1) {
			decideForHelp(bestID, des);
			return true;
		} else {
			if (!giving && myRC.getFlux() > 500) {
				// System.out.println("look for archon");
				giveFluxToArchon1(myRC);
			}
			// System.out.println(origin);
			while (myRC.getRoundsUntilMovementIdle() != 0) {
				myRC.getAllMessages();
				myRC.yield();
			}
			if (Clock.getBytecodeNum() > 5000) {
				myRC.getAllMessages();
				myRC.yield();
			}
			myRC.setIndicatorString(0, "wandering");
			// myRC.setIndicatorString(1, Clock.getBytecodeNum() + "");
			Wandering.act(myRC, origin, dir, myMap, 0);
			// myRC.setIndicatorString(2, Clock.getBytecodeNum()+"");
			return false;
		}
	}

	private void decideForHelp(int bestID, MapLocation des)
			throws GameActionException {
		if (!myRC.hasBroadcastMessage()) {
			MessageEncoder encoder = new MessageEncoder(myRC, msgStack,
					MessageType.ENERGON_REQUEST, bestID);
			myRC.broadcast(encoder.encodeMessage());
			myRC.yield();
			myRC.yield();
			// myRC.yield();
			Message[] confirms = myRC.getAllMessages();
			for (Message confirm : confirms) {
				MessageDecoder decoder = new MessageDecoder(myRC, msgStack,
						confirm);
				if (decoder.isValid()
						&& decoder.getType().equals(
								MessageType.ENERGON_CONFIRMATION)
						&& decoder.getSourceID() == bestID) {
					// System.out.println("cool");
					myRC.setIndicatorString(0, "looking for blood: " + bestID);
					while (!des.isAdjacentTo(myRC.getLocation())) {
						if (myRC.canSenseSquare(des)
								&& myRC.senseGroundRobotAtLocation(des) == null) {
							break;
						}
						while (myRC.getRoundsUntilMovementIdle() != 0
								|| Clock.getBytecodeNum() > 5000) {
							myRC.getAllMessages();
							myRC.yield();
						}
						GoingTo.act(myRC, myMap, des);
						if (myRC.getEventualEnergonLevel() < 0.5) {
							MessageEncoder encoder1 = new MessageEncoder(myRC,
									msgStack, MessageType.BUILDING_DIED);
							myRC.broadcast(encoder1.encodeMessage());
							myRC.suicide();
							myRC.yield();
							return;
						}
					}
					if (myRC.senseGroundRobotAtLocation(des) == null) {
						System.out.println("no robot there");
					} else {
						myRC.setIndicatorString(0, "request done");
						myRC.transferFlux(myRC.getFlux(), des,
								RobotLevel.ON_GROUND);
					}
					myRC.getAllMessages();
					myRC.yield();
					return;
				}
			}
			myRC.getAllMessages();
			myRC.yield();
			return;
		}

	}

	private void checkHelp() throws GameActionException {

		// look at its own message and see if there is any wout need to help
		// -shinnyih
		Message[] msgs = myRC.getAllMessages();
		for (Message msg : msgs) {
			MessageDecoder decoder = new MessageDecoder(myRC, msgStack, msg);
			// System.out.println(decoder.isValid());
			if (decoder.isValid()
					&& decoder.getType().equals(MessageType.ENERGON_REQUEST)
					&& decoder.getID() == myRC.getRobot().getID()) {
				// System.out.println("work");
				if (!myRC.hasBroadcastMessage()) {
					MessageEncoder encoder = new MessageEncoder(myRC, msgStack,
							MessageType.ENERGON_CONFIRMATION, decoder
									.getSourceID());
					myRC.broadcast(encoder.encodeMessage());
					int id = decoder.getSourceID();
					Robot target = myRC.getRobot();
					Boolean found = false;
					// System.out.println(target.toString());
					while (target.equals(myRC.getRobot())) {
						Robot[] robots = myRC.senseNearbyGroundRobots();
						for (Robot robot : robots) {
							if (robot.getID() == id) {
								target = robot;
								found = true;
								break;
							}
						}
						if (found) {
							break;
						} else {
							if (wait == 6) {
								wait = 0;
								break;
							}
							wait++;
							myRC.getAllMessages();
							myRC.yield();
							giveFluxToNearArchon();
						}
					}
					if (!found) {
						break;
					}
					// System.out.println("found");
					while (true) {
						if (myRC.canSenseObject(target)) {
							RobotInfo info = myRC.senseRobotInfo(target);
							if (info.location.isAdjacentTo(myRC.getLocation())) {
								myRC.transferUnitEnergon(Math.min(myRC
										.getEnergonLevel() / 2,
										10 - info.energonReserve),
										info.location, RobotLevel.ON_GROUND);
								myRC.getAllMessages();
								myRC.setIndicatorString(0, "done");
								myRC.yield();
								// System.out.println("I did it");
								break;
							} else {
								Message[] msgs1 = myRC.getAllMessages();
								boolean heDied = false;
								for (Message msg1 : msgs1) {
									MessageDecoder decoder1 = new MessageDecoder(
											myRC, msgStack, msg1);
									if (decoder1.isValid()
											&& decoder1.getSourceID() == id
											&& decoder.getType().equals(
													MessageType.BUILDING_DIED)) {
										heDied = true;
										myRC.yield();
										break;
									}
								}
								if (heDied) {
									myRC.setIndicatorString(0, "done cuz die");
									myRC.yield();
									break;
								}
								if (wait == 30) {
									wait = 0;
									myRC.getAllMessages();
									myRC.yield();
									break;
								}
								wait++;
								myRC.setIndicatorString(0, "still waiting");
							}
						} else {
							Message[] msgs1 = myRC.getAllMessages();
							boolean heDied = false;
							for (Message msg1 : msgs1) {
								MessageDecoder decoder1 = new MessageDecoder(
										myRC, msgStack, msg1);
								if (decoder1.isValid()
										&& decoder1.getSourceID() == id
										&& decoder.getType().equals(
												MessageType.BUILDING_DIED)) {
									heDied = true;
									myRC.yield();
									break;
								}
							}
							if (heDied) {
								myRC.setIndicatorString(0, "done cuz die");
								myRC.yield();
								break;
							}
							if (wait == 30) {
								wait = 0;
								myRC.yield();
								break;
							}
							wait++;
							myRC.setIndicatorString(0, "still waiting");
							// break;
						}
						giveFluxToNearArchon();
						if (wait == 30) {
							wait = 0;
							myRC.yield();
							break;
						}
						wait++;
						myRC.yield();
					}
				}
			}

		}

	}

	private boolean giveFluxToArchon1(RobotController myRC)
			throws GameActionException {
		MapLocation des = findDefense();
		if (des == null) {
			return false;
		}
		while (!(des.isAdjacentTo(myRC.getLocation()) || des.equals(myRC
				.getLocation()))) {
			while (myRC.getRoundsUntilMovementIdle() != 0
					|| Clock.getBytecodeNum() > 5000) {
				myRC.getAllMessages();
				myRC.yield();
			}
			GoingTo.act(myRC, myMap, des);
			des = senseNextDesArchon(des);
		}
		myRC.getAllMessages();
		myRC.setIndicatorString(0, "wanna give");
		if (myRC.senseAirRobotAtLocation(des) != null) {
			myRC.transferFlux(myRC.getFlux(), des, RobotLevel.IN_AIR);
		}
		// System.out.println("I did it");
		return true;
	}

	private MapLocation senseNextDesArchon(MapLocation des) {
		MapLocation[] locs = myRC.senseAlliedArchons();
		for (MapLocation loc : locs) {
			if (loc.equals(des)){
				return des;
			}
			if (loc.isAdjacentTo(des)) {
				return loc;
			}
		}
		return des;
	}

	private boolean giveFluxToArchon(RobotInfo info) throws GameActionException {
		if (info.type.isAirborne() && info.team.equals(myRC.getTeam())) {
			// System.out.println("got one");
			while (!info.location.isAdjacentTo(myRC.getLocation())
					&& info.flux < 9000) {
				while (myRC.getRoundsUntilMovementIdle() != 0
						|| Clock.getBytecodeNum() > 5000) {
					myRC.getAllMessages();
					myRC.yield();
				}
				GoingTo.act(myRC, myMap, info.location);
			}
			myRC.getAllMessages();
			myRC.yield();
			myRC.transferFlux(myRC.getFlux(), info.location, RobotLevel.IN_AIR);
			// System.out.println("I did it");
			return true;
		}
		return false;
	}

	private boolean goForArchon(RobotInfo info) throws GameActionException {
		if (info.type.isAirborne() && info.team.equals(myRC.getTeam())) {
			// System.out.println("got one");
			while (!info.location.isAdjacentTo(myRC.getLocation())
					&& info.energonLevel > 10
					&& info.location.distanceSquaredTo(origin) < 16) {
				while (myRC.getRoundsUntilMovementIdle() != 0
						|| Clock.getBytecodeNum() > 5000) {
					myRC.getAllMessages();
					myRC.yield();
				}
				GoingTo.act(myRC, myMap, info.location);
			}
			myRC.getAllMessages();
			myRC.yield();
			myRC.transferFlux(myRC.getFlux(), info.location, RobotLevel.IN_AIR);
			// System.out.println("I did it");
			return true;
		}
		return false;
	}

	private void giveFluxToNearArchon() {
		Robot[] robots = myRC.senseNearbyAirRobots();
		for (Robot robot : robots) {
			try {
				RobotInfo info = myRC.senseRobotInfo(robot);
				if (info.location.isAdjacentTo(myRC.getLocation())
						&& info.team.equals(myRC.getTeam()) && info.flux < 9000) {
					myRC.transferFlux(myRC.getFlux(), info.location,
							RobotLevel.IN_AIR);
				}
			} catch (GameActionException e) {
				myRC.getAllMessages();
				myRC.yield();
			}
		}
	}

	private MapLocation findDefense() {
		MapLocation[] locs = myRC.senseAlliedArchons();
		if (locs.length < 6) {
			return null;
		}
		for (int i = 1; i < locs.length; i++) {
			for (int j = 0; j < i; j++) {
				if (locs[i].distanceSquaredTo(origin) < locs[j]
						.distanceSquaredTo(origin)) {
					MapLocation temp = locs[j];
					locs[j] = locs[i];
					locs[i] = temp;
				}
			}
		}
		if (locs[2].distanceSquaredTo(myRC.getLocation()) < locs[3]
				.distanceSquaredTo(myRC.getLocation())) {
			return locs[2];
		} else {
			return locs[3];
		}
	}

}
