package actions;

import bwapi.UpgradeType;
import bwta.BaseLocation;
import stateInformation.Player;
import ISMCTS.Entity;
import ISMCTS.Move;
import ISMCTS.Tech;
import bwapi.UnitType;

public class Action {
	
	public Player player;
	
	public Move move;
	
	
	
	public int baseLocation;
	
	public Action(){
	}
	
	public void print(){
		System.out.println("#Action");
	}
	
}
