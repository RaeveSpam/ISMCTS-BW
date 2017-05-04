package unitControl;

import bwapi.Game;
import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitType;
import java.util.*;


public class UnitGroup extends Group {
	private UnitType unitType;
	
	public UnitGroup(Game game, UnitType type) {
		super(game);
		unitType = type;
	}

	public UnitType getType(){
		return unitType;
	}

	@Override
	public void onFrame() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void addUnit(Unit unit){
		if(!isFull()){
			
			units.add(unit);
			//unit.attack(lastOrder);
		}
	}
	
	public boolean isFull(){
		return units.size() > 11;
	}
	
	public double getSpeed(){
		return unitType.topSpeed();
	}
	
	public int getSupply(){
		return unitType.supplyRequired()*units.size();
	}
	
	/**
	 * Removes and returns one unit from the group
	 * @return ID of a unit now removed from the group
	 */
	public Unit pop(){
		return units.remove(units.size()-1);
	}




}

