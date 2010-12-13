package team072.baseplayer;

import team072.message.MessageDecoder;
import team072.message.MessageStack;
import team072.message.MessageEncoder.MessageType;
import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Message;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

/**
 * Move around and collect flux to building or archon
 * 
 * @author USER
 * 
 */
public class WoutPlayer extends BasePlayer {
	protected enum Status {
		NORMAL, OUTER, FLUX
	};

	protected MessageStack myMS;
	protected int myRound;

	private boolean waking; // whether the robot is just born -bunkie
	private WoutAI ai; // ai for the wout -shinnyih

	public WoutPlayer(RobotController rc) {
		super(rc);
		myType = RobotType.WOUT;
		waking = true;
		myMS = new MessageStack();
		myRound = Clock.getRoundNum();
	}

	/**
	 * @Override
	 */
	public void proceed() throws GameActionException {
		if (waking) {
			// check the messages and receive origin, wallX, wallY
			MessageDecoder msgDecoder;
			Message[] msgs = myRC.getAllMessages();

			if (msgs.length == 0) {
				// myRC.yield();
				// System.out.println("sleeping");
				myRC.yield();
				return;
			}
			// System.out.println("come on");
			for (Message m : msgs) {
				msgDecoder = new MessageDecoder(myRC, myMS, m);
				if (msgDecoder.isValid()
						&& (msgDecoder.getType() == MessageType.ORIGIN || msgDecoder
								.getType() == MessageType.ORIGINX || msgDecoder
								.getType() == MessageType.ORIGINY || msgDecoder
                                .getType() == MessageType.ORIGINZ)) {
					MapLocation origin = msgDecoder.getOrigin();
					Direction dir = msgDecoder.getDirection();
					if (msgDecoder.getType() == MessageType.ORIGINX) {
						ai = new WoutFluxAI(myRC, origin, dir, myMS, myRound);
						myRC.setIndicatorString(2, dir.toString());

					} else if (msgDecoder.getType() == MessageType.ORIGIN){
						ai = new WoutOuterAI(myRC, dir, origin, myMS);
					} else if (msgDecoder.getType() == MessageType.ORIGINZ) {
                        ai = new WoutBattleAI(myRC);
                    } else {
						ai = new WoutEatingAI(myRC, origin, dir, myMS, myRound);
						myRC.setIndicatorString(0, "I am eating");
					}
					waking = false;
					break;
				}
			}

			if (waking) {
				myRC.yield();
				return;
			}

			myRC.yield();

		} else {
			ai.proceed();
		}
	}
}
