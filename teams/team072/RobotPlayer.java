package team072;

import battlecode.common.*;
import team072.baseplayer.ArchonPlayer;
import team072.baseplayer.AuraPlayer;
import team072.baseplayer.BasePlayer;
import team072.baseplayer.ChainerPlayer;
import team072.baseplayer.CommPlayer;
import team072.baseplayer.SoldierPlayerX;
import team072.baseplayer.TeleporterPlayer;
import team072.baseplayer.TurretPlayer;
import team072.baseplayer.WoutPlayer;

/**@lalala
 * @author: Boon Teik Ooi, Chun-Kai Wang, Hao-Yu Wu, Yu-Chi Kuo
 *@LalalalalaGiong!Lambda
 */
public class RobotPlayer implements Runnable {

    private final RobotController myRC;
	private RobotType myType;
	private BasePlayer myPlayer;

    public RobotPlayer(RobotController rc) {
        myRC = rc;
		myType = rc.getRobotType();
		myPlayer = createPlayerFromType(myType);
    }
	
	private BasePlayer createPlayerFromType(RobotType type) {
		BasePlayer player = null;
		
		switch (type) {
			case ARCHON: player = new ArchonPlayer(myRC); break;
			case AURA: player = new AuraPlayer(myRC); break;
			case CHAINER: player = new ChainerPlayer(myRC); break;
			case COMM: player = new CommPlayer(myRC); break;
			case SOLDIER: player = new SoldierPlayerX(myRC); break;
			case TELEPORTER: player = new TeleporterPlayer(myRC); break;
			case TURRET: player = new TurretPlayer(myRC); break;
			case WOUT: player = new WoutPlayer(myRC); break;
		}

		return player;
	}
	
    public void run() {
        //System.out.println("STARTING");
        while (true) {
            try {
                /*** beginning of main loop ***/				
				myPlayer.proceed();
				//System.out.println("Run");
				//myRC.yield();
				
                /*** end of main loop ***/
            } catch (Exception e) {
                System.out.println("caught exception:");
                e.printStackTrace();
            }
        }
    }
}