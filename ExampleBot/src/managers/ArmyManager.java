package managers;

import bwapi.Unit;
import bwapi.UnitType;

import java.util.List;

import bwapi.Game;
import unitControl.*;
import bwta.BWTA;
import bwta.BaseLocation;

public class ArmyManager implements Manager {

	private Game game;
	
	private List<Group> groups;
	private List<UnitGroup> unitGroups;
	private ScoutGroup scout;
	private boolean isAttacking;
	
	public ArmyManager(Game game){
		this.game = game;
		isAttacking = false;
	}
	
	@Override
	public void onStart() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onFrame() {
		// TODO Auto-generated method stub
		
		// place army in defensive position
	}

	public void attack(){
		// attack enemy base
	}
	
	public int getArmySupply(){
		int result = 0;
		for(Group g : groups){
			result += g.getSupply();
		}
		return result;
	}
	
	@Override
	public void onUnitComplete(Unit unit) {
		if(unit.canMove() 
			&& unit.getType() != UnitType.Protoss_Probe  
			&& unit.getType() != UnitType.Protoss_Interceptor 
			&& unit.getType() != UnitType.Protoss_Scarab)
		{
			// if scout
			
			// add unit to unit group
			for(UnitGroup g : unitGroups){
				if(g.getType() == unit.getType()){
					g.addUnit(unit);
					return;
				}
			}
			UnitGroup g = new UnitGroup(game, unit.getType());
			g.addUnit(unit);
			unitGroups.add(g);
			groups.add(g);
		}
	}

	public boolean transferScout(){
		if(scout.size() > 0){
			return true;
		}
		UnitGroup fastestGroup = null;
		double fastestSpeed = 0.0;
		for(UnitGroup g : unitGroups){
			if(g.size() > 0 && g.getSpeed() > fastestSpeed){
				fastestGroup = g;
				fastestSpeed = g.getSpeed();
			}
		}
		if(fastestGroup != null){
			Unit unit = game.getUnit(fastestGroup.pop());
			scout.addUnit(unit);
			return true;
		}
		return false;
	}
	
	public void onUnitCreate(Unit unit) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onUnitDestroy(Unit unit) {
		for(Group g : groups){
			g.removeUnit(unit);
		}
	}

	@Override
	public void onUnitDiscover(Unit unit) {
		// TODO Auto-generated method stub
		scout.onUnitDiscover(unit);
	}

}
